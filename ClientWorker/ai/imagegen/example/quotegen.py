import textwrap
import sys
import argparse
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageEnhance
# import helper

def create_post(image_file, quote_text: str, quote_font, author_font, output_path, file_name, logo_file , author_text: str | None = None):
    if not (quote_text.startswith("\"") or quote_text.endswith("\"")):
       quote_text = f"\"{quote_text}\""
    # Open specific image
    img = Image.open(image_file)

    # Load selected font
    quote_font = ImageFont.truetype(font=f'{quote_font}', size=75)

    # Create DrawText object
    draw = ImageDraw.Draw(im=img)

    # Define our text:
    # Calculate the average length of a single character of our font.
    # Note: this takes into account the specific font and font size.
    # avg_char_width = sum(font.getsize(char)[0] for char in ascii_letters) / len(ascii_letters)

    # Translate this average length into a character count
    # max_char_count = int(img.size[0] / avg_char_width)
    max_char_count = 25
    # Create a wrapped text object using scaled character count
    new_text = textwrap.fill(text=quote_text, width=max_char_count)
    # FIX FONT WITH SPACES
    new_text = new_text.replace(" ", "  ")
    # new_text = helper_images.split_string(text, max_char_count)
    # Define the positions of logo and text
    x_logo = 0
    y_logo = 1100
    x_text = img.size[0] / 2
    y_text = img.size[1] / 2
    position = (x_text, y_text)

    # Draw the shadow text
    shadow_color = (0, 0, 0, 128)
    shadow_position = (x_text+5, y_text+5)
    draw.text(shadow_position, new_text, font=quote_font, fill=shadow_color, anchor='mm', align='center')

    # Add main text to the image
    draw.text(position, text=new_text, font=quote_font, fill=(255, 255, 255, 255), anchor='mm',
              align='center')

    if author_text is not None:
        # Add author text
        # Count '\n' in the text to see how many lines there are
        author_font = ImageFont.truetype(font=f'{author_font}', size=45)
        num_of_lines = new_text.count("\n") + 1
        line_height = 55     # TODO CHECK REAL HEIGHT
        text_height = line_height * num_of_lines + 40
        # TODO CHANGE AUTHORS FONT
        author_position = (position[0], position[1] + text_height)
        draw.text(author_position, text=author_text, font=author_font, fill=(255, 255, 255, 255), anchor='mm', align='center')

    if logo_file is not None:
        # Open logo file
        img_logo = Image.open(logo_file)

        # Reduce the alpha of the overlay image by 30%
        alpha = 1
        enhancer = ImageEnhance.Brightness(img_logo)
        img_logo_darken = enhancer.enhance(alpha)

        # Create a new image object with the same size as the background image
        img_with_logo = Image.new('RGBA', img.size, (0, 0, 0, 0))

        # Draw the background image onto the new image
        img_with_logo.paste(img, (0, 0))

        # Draw the overlay image onto the new image
        img_with_logo.paste(img_logo_darken, (int(x_logo), int(y_logo)), mask=img_logo_darken)

        # Convert from RGBA to RGB
        img_with_logo_rgb = img_with_logo.convert("RGB")

        # file_name

        # Save the image
        img_with_logo_rgb.save(f"{output_path}/{file_name}")
        # combined.show()
        return f"{output_path}/{file_name}"

    # If logo was off
    # Save the image
    img.save(f"{output_path}/{file_name}")
    # combined.show()
    return f"{output_path}/{file_name}"

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Create a post with an image and quote.")
    parser.add_argument("image_file", type=str, help="Path to the image file")
    parser.add_argument("quote_text", type=str, help="The quote text")
    parser.add_argument("author_text", type=str, help="The author of the quote")
    args = parser.parse_args()

    create_post(args.image_file, args.quote_text, 'garamond.ttf', 'garamond.ttf', '.', 'out.jpg', None, args.author_text)
    pass