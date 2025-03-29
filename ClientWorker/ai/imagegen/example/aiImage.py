import torch
from diffusers import StableDiffusionPipeline

# Load model (1st time will download)
pipe = StableDiffusionPipeline.from_pretrained("../stable_diffusion_model", torch_dtype=torch.float16)
pipe.to("cuda")  # Use "cpu" if no GPU

positive_prompt = ""
with open("positive_prompt.txt", "r", encoding="utf-8") as f:
    positive_prompt = f.read().strip()  # Remove unnecessary spaces or newlines

negative_prompt = "blurry, low-resolution, distorted, bad anatomy, dark, overexposed, worst quality, low quality, normal quality"
# Generate image
prompt = input("Prompt: ")
prompt = f"((({prompt}))), {positive_prompt}"
print(f"Prompt: {prompt}")
image = pipe(prompt, num_inference_steps=50, guidance_scale=50).images[0]

# Save image
image.save("generated_image.png")
print("Image saved!")
