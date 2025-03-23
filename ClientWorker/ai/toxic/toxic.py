import torch
from transformers import RobertaTokenizer, RobertaForSequenceClassification

# tokenizer = RobertaTokenizer.from_pretrained('./s-nlp/roberta_toxicity_classifier')
# model = RobertaForSequenceClassification.from_pretrained('s-nlp/roberta_toxicity_classifier')

tokenizer = RobertaTokenizer.from_pretrained('./model')
model = RobertaForSequenceClassification.from_pretrained('./model')


prompt = ""
while (prompt != "exit"):
    prompt = input("> ")
    batch = tokenizer.encode(prompt, return_tensors="pt")
    output = model(batch)
    neutral = output[0][0][0]
    toxic = output[0][0][1]
    print(f"Neutral: {neutral}\nToxic: {toxic}\n")
    
