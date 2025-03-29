import torch
from diffusers import StableDiffusionPipeline

# Define model name (Stable Diffusion v1.5)
MODEL_NAME = "runwayml/stable-diffusion-v1-5"

UPSCALER_NAME="stabilityai/stable-diffusion-x4-upscaler"
# Load model (this downloads it if not already cached)
print("Downloading model... This may take some time.")
pipeline = StableDiffusionPipeline.from_pretrained(MODEL_NAME, torch_dtype=torch.float16)
pipeline.to("cuda" if torch.cuda.is_available() else "cpu")

# Save model locally
pipeline.save_pretrained("./stable_diffusion_model")
print("Model downloaded and saved locally.")
