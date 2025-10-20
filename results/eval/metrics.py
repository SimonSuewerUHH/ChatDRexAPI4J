import pandas as pd

llms = ['gpt-oss', 'qwen3-coder']

################################## TRANSLATION #########################################
res_dict = {}

for llm in llms:
    res_dict[llm] = pd.read_csv(f'{llm}/kg_cypher_result.csv')

def compute_precision(row):
    if row['lengthAI'] != 0:
        return row['hits']/row['lengthAI']
    else:
        return 0

def compute_recall(row):
    if row['lengthGold'] != 0:
        return row['hits']/row['lengthGold']
    else:
        return 0

def compute_f1(row):
    p = compute_precision(row)
    r = compute_recall(row)
    if p + r == 0:
        return 0
    else:
        return 2*p*r/(p+r)


for llm in llms:
    res_dict[llm]['P'] = res_dict[llm].apply(compute_precision, axis = 1)
    res_dict[llm]['R'] = res_dict[llm].apply(compute_recall, axis = 1)
    res_dict[llm]['F1'] = res_dict[llm].apply(compute_f1, axis = 1)

print('\n------- Metrics (avg) --------\n')
for llm in llms:
    print(f'---------{llm}-------------\n')
    print(f'PRECISION: {res_dict[llm]["P"].mean()}\n')
    print(f'RECALL: {res_dict[llm]["R"].mean()}\n')
    print(f'F1: {res_dict[llm]["F1"].mean()}\n')


################################## NL GENERATION #########################################
judge_res = {}
for llm in llms:
    judge_res[llm] = pd.read_csv(f'{llm}/kg_cypher_result_ai_judge.csv')
