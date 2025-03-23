import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# tokenizer = RobertaTokenizer.from_pretrained('./s-nlp/roberta_toxicity_classifier')
# model = RobertaForSequenceClassification.from_pretrained('s-nlp/roberta_toxicity_classifier')

tokenizer = AutoTokenizer.from_pretrained('./hatemodel')
model = AutoModelForSequenceClassification.from_pretrained('./hatemodel')


prompt = ""
while (prompt != "exit"):
    prompt = input("> ")
    batch = tokenizer.encode(prompt, return_tensors="pt")
    output = model(batch)
    print(output)
    # neutral = output[0][0][0]
    # toxic = output[0][0][1]
    # print(f"Neutral: {neutral}\nToxic: {toxic}\n")
    
