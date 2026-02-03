import os
import json
from tqdm import tqdm
from dotenv import load_dotenv

from src.inputs import Neo4jConnection
from src.bootstrapping import generate_synthetic_data

load_dotenv()



if __name__ == "__main__":

    # connect to NeDRex
    uri = os.getenv("NEO4J_HOST", "bolt://neo4j.nedrex.net:6689")
    user = os.getenv("NEO4J_USER")
    password = os.getenv("NEO4J_PASSWORD")
    NeDRex = Neo4jConnection(uri, user, password)

    # retrieve schema
    schema = NeDRex.retrieve_schema()
    formatted_schema = NeDRex.format_schema(schema)

    # parameters
    successful_queries = []
    num_questions_per_batch = 5
    generated = {}
    successful = {}
    directory_name = 'GS_candidates_2'

    ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..'))
    os.chdir(ROOT_DIR)
    # loop in nodes
    for node in tqdm(list(schema['node_types'].keys())):
        try:

            # Start the main loop with batching
            print(f'\n------------------------------------------Processing node: {node}--------------------------------------------------------------------------\n')
            generated[node], successful[node] = generate_synthetic_data(NeDRex, formatted_schema, node, num_questions_per_batch=num_questions_per_batch, N = 10, dir_name =  directory_name)
            print(f'\n------------------------------------------Finish Processing node: {node}-------------------------------------------------------------------\n')

            # save statistics in a json file
            try:
                os.mkdir(os.path.join('successful_queries_2', directory_name))
            except FileExistsError:
                pass
            with open(os.path.join('successful_queries_2', directory_name, f'generated_{node}.json'), 'w') as f:
                json.dump(generated[node], f, indent = 2)
            print(f"Stored running data for node type {node}.")
            with open(os.path.join('successful_queries_2', directory_name, f'successful_{node}.json'), 'w') as f:
                json.dump(successful[node], f,  indent = 2)
            print(f"\nStored running data for node type {node}.\n")

        except Exception as e:
            print(f'Error while generating synthetic data:  {e}')
    NeDRex.close()
