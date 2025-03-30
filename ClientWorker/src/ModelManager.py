"""
Manages AI Models or intensive compute tasks
"""
import datetime
import subprocess

import torch
from transformers import RobertaTokenizer, RobertaForSequenceClassification, AutoTokenizer, AutoModelForSequenceClassification
from diffusers import StableDiffusionPipeline

from QuoteGen import create_quote, extend_image_with_gradient_and_text

# AI STUFF

# TOXIC MODEL
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

# IMAGE GEN MODEL
STABLE_DIFFUSION_DIR = "../ai/imagegen/stable_diffusion_model"
POSITIVE_PROMPT_FILE = "../ai/imagegen/positive_prompt.txt"
negative_prompt = "human, face, person, blurry, low-resolution, distorted, bad anatomy, dark, overexposed, worst quality, low quality, normal quality"
CACHE_DIR = "../cache"
positive_prompt = ""

with open(POSITIVE_PROMPT_FILE, "r", encoding="utf-8") as f:
    positive_prompt = f.read().strip()  # Remove unnecessary spaces or newlines

# Load model (1st time will download)
pipe = StableDiffusionPipeline.from_pretrained(STABLE_DIFFUSION_DIR, torch_dtype=torch.float16)
pipe.to("cuda")  # Use "cpu" if no GPU

"""
Creates image using prompt and returns filepath where image is.
"""
def createStableDiffImg(prompt: str) -> str:
    prompt = f"((({prompt}))), {positive_prompt}"
    now = datetime.datetime.now()
    formatted_time = now.strftime("%m-%d_%H-%M-S")
    filename = f"{CACHE_DIR}/{formatted_time}.png"
    image = pipe(prompt, num_inference_steps=50, guidance_scale=10, negative_prompt=negative_prompt).images[0]
    image.save(filename)
    cmd = f'/usr/bin/realesrgan-ncnn-vulkan -i "{filename}" -o "{filename}" -s 2'
    result = subprocess.run(cmd, shell=True)
    print(result.stdout)
    return filename

def createQuoteImg(quote: str, author: str) -> str:
    img_path = createStableDiffImg(quote)
    extend_image_with_gradient_and_text(img_path, img_path, quote, f"- {author}")
    return img_path
    # fname = img_path.split("/")[-1]
    # create_quote(img_path, quote, author, CACHE_DIR, fname)
    # return f"{CACHE_DIR}/{fname}"

