import os
import logging
import getpass

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import PydanticOutputParser
from langchain_openai import ChatOpenAI
from langchain_google_genai import ChatGoogleGenerativeAI

from pydantic import BaseModel, Field
from typing import List, Dict
from dotenv import load_dotenv

from src.utils import read_markdown_file
from src.model import CosyChatOllama


load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Pydantic models for parsers
class QuestionModel(BaseModel):
    questions: List[str] = Field(description='list of generated natural language questions')

class PairsModel(BaseModel):
    questions: List[Dict] = Field(description='list of generated natural language questions - Cypher query pairs')

class CypherDecModel(BaseModel):
    cypher_tool: str = Field(description="decides which cypher prompt to use")


class CypherModel(BaseModel):
    cypher_query: str = Field(description="cypher query")

class AssessModel(BaseModel):
    output: str = Field(description="Binary result showing if the query makes biological sense")


# Generator chain
def create_question_generator_chain():

    # prompt
    chain_instructions = read_markdown_file(os.path.join('prompts', 'NL_chain_instructions.md'))
    chain_prompt = read_markdown_file(os.path.join('prompts', 'NL_chain_prompt.md'))
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", chain_instructions),
            ("human", chain_prompt)
        ]
    )

    # llm
    llm = CosyChatOllama(model = 'mistral:7b', temperature= 1.0, num_ctx = 2500)

    # parser
    parser = PydanticOutputParser(pydantic_object=QuestionModel)

    generator_chain = prompt | llm | parser
    return generator_chain


if not os.environ.get("OPENAI_API_KEY"):
    os.environ["OPENAI_API_KEY"] = getpass.getpass("Enter your OpenAI API key: ")

if "GOOGLE_API_KEY" not in os.environ:
    os.environ["GOOGLE_API_KEY"] = getpass.getpass("Enter your Google AI API key: ")


def create_advanced_question_generator_chain():
    # prompt
    chain_instructions = read_markdown_file(os.path.join('prompts', 'NL_chain_instructions.md'))
    chain_prompt = read_markdown_file(os.path.join('prompts', 'NL_chain_prompt.md'))
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", chain_instructions),
            ("human", chain_prompt)
        ]
    )

    # llm
    llm = ChatGoogleGenerativeAI(model="gemini-2.5-pro", temperature = 1.0)

    # parser
    parser = PydanticOutputParser(pydantic_object=QuestionModel)

    generator_chain = prompt | llm | parser
    return generator_chain




# Translator chain
def create_translator_chain(model_name= 'qwen2.5-coder:latest'):

    # prompt
    chain_instructions = read_markdown_file(os.path.join('prompts','translator_chain_instructions.md'))
    chain_prompt = read_markdown_file(os.path.join('prompts','translator_chain_prompt.md'))
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", chain_instructions),
            ("human", chain_prompt)
        ]
    )

    # llm (coder expert)
    coder_llm = CosyChatOllama(model= model_name, num_ctx = 25000)
    # parser
    parser = PydanticOutputParser(pydantic_object=PairsModel)


    translator_chain = prompt | coder_llm | parser
    return translator_chain


# Decider chain
def create_decider_chain():

    # prompt
    cypher_decider_instructions = read_markdown_file(os.path.join('prompts','cypher', 'cypher_decider_instructions.md'))
    cypher_decider_prompt = read_markdown_file(os.path.join('prompts','cypher', 'cypher_decider_prompt.md'))
    cypher_decider_template = ChatPromptTemplate.from_messages(
        [
            ("system", cypher_decider_instructions),
            ("human", cypher_decider_prompt),
        ]
    )

    # llm
    decider_llm = CosyChatOllama()

    # parser
    parser1  = PydanticOutputParser(pydantic_object=CypherDecModel)

    decider_chain = cypher_decider_template | decider_llm | parser1

    return decider_chain


# Few-shot chain
def create_few_shot_chain(prompt_scope, model_name):
    # prompt
    chain_instructions = read_markdown_file(os.path.join('prompts','translator_chain_instructions.md'))
    # Adapt the prompt
    if prompt_scope != 'None':
        chain_prompt = read_markdown_file(os.path.join('prompts', 'cypher', f'{prompt_scope}_based.md'))
    else:
        chain_prompt = None
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", chain_instructions),
            ("human", chain_prompt)
        ]
    )

    # llm
    coder_llm = CosyChatOllama(model= model_name, num_ctx = 25000)

    # parser
    parser = PydanticOutputParser(pydantic_object=CypherModel)


    translator_chain = prompt | coder_llm | parser
    return translator_chain



# Assessment chain
def create_assessment_chain():

    # prompt
    chain_instructions = read_markdown_file(os.path.join('prompts','assessment_chain_instructions.md'))
    # Adapt the prompt
    chain_prompt = read_markdown_file(os.path.join('prompts', 'assessment_chain_prompt.md'))
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", chain_instructions),
            ("human", chain_prompt)
        ]
    )

    # llm
    llm = CosyChatOllama(model= 'deepseek-r1:70b')

    # parser
    parser = PydanticOutputParser(pydantic_object=AssessModel)


    assess_chain = prompt | llm | parser
    return assess_chain