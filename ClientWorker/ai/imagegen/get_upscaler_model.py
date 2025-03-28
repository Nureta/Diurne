import torch
from diffusers import StableDiffusionUpscalePipeline


UPSCALER_NAME="stabilityai/stable-diffusion-x4-upscaler"
# Load model (this downloads it if not already cached)
print("Downloading model... This may take some time.")
pipeline = StableDiffusionUpscalePipeline.from_pretrained(UPSCALER_NAME, torch_dtype=torch.float16)
pipeline.to("cuda" if torch.cuda.is_available() else "cpu")

# Save model locally
pipeline.save_pretrained("./stable_diffusion_model")
print("Model downloaded and saved locally.")
