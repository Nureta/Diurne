"""
Manages AI Models or intensive compute tasks
"""

import torch
from transformers import RobertaTokenizer, RobertaForSequenceClassification, AutoTokenizer, AutoModelForSequenceClassification

# AI STUFF
TOXIC_MODEL_DIR = "../ai/toxic/model/"
HATE_MODEL_DIR = "../ai/toxic/hatemodel/"

# toxic_tokenizer = RobertaTokenizer.from_pretrained(TOXIC_MODEL_DIR)
# toxic_model = RobertaForSequenceClassification.from_pretrained(TOXIC_MODEL_DIR)
toxic_model = AutoModelForSequenceClassification.from_pretrained(HATE_MODEL_DIR)
toxic_tokenizer = AutoTokenizer.from_pretrained(HATE_MODEL_DIR)

def check_toxic(prompt: str) -> tuple[float, float]:
    batch = toxic_tokenizer.encode(prompt, return_tensors="pt")
    output = toxic_model(batch)
    neutral = round(float(output[0][0].tolist()[0]), 2)
    toxic = round(float(output[0][0].tolist()[1]), 2)
    return (neutral, toxic)