from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageEnhance
import io
import textwrap

def __create_post(image_file, quote_text: str, quote_font, author_font, output_path, file_name, logo_file , author_text: str | None = None):
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
              stroke_fill=(0,0,0,255), stroke_width=5,
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
        draw.text(author_position, text=author_text, font=author_font, fill=(255, 255, 255, 255), anchor='mm', align='center', stroke_fill=(0,0,0,255), stroke_width=5)

    if logo_file is not None:
        # Open logo file
        img_logo = Image.open(logo_file)

        # Reduce the alpha of the overlay image by 30%
        alpha = 0.7
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


def extend_image_with_gradient_and_text(image_path, output_path, text, author_text = None, font_path="./assets/lato.ttf", font_size=75):
    if not (text.startswith("\"") or text.endswith("\"")):
        text = f"\"{text}\""

    gradient_magnitude = 2.
    # Open the original image
    img = Image.open(image_path)
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    w, h = img.size

    gradient = Image.new('L', (w, 1), color=0xFF)
    for x in range(w):
        gradient.putpixel((x, 0), int(255 * (1 - gradient_magnitude * float(x)/w)))

    alpha = gradient.resize(img.size).rotate(180)
    black_im = Image.new('RGBA', (w, h), color=0) # i.e. black
    black_im.putalpha(alpha)
    gradient_im = Image.alpha_composite(img, black_im)

    # Extend Image to the right
    nw = int(gradient_im.width * 1.7)
    ext_img = Image.new("RGB", (nw, h), "black")
    ext_img.paste(gradient_im, (0,0))


    # Add text
    draw = ImageDraw.Draw(ext_img)
    num_chars = len(text)
    text_wrap = 15
    try:
        if num_chars > 500:
            text_wrap = 50
            font_size = 12
        elif num_chars > 300:
            text_wrap = 40
            font_size = 24
        elif num_chars > 120:
            text_wrap = 30
            font_size = 25
        font = ImageFont.truetype(font_path, font_size)  # Load custom font
    except:
        font = ImageFont.load_default()  # Fallback if font not found

    new_text = textwrap.fill(text=text, width=text_wrap)

    x_text = (ext_img.size[0] * 0.8)
    y_text = img.size[1] * 0.35
    quote_position = (x_text, y_text)

    # Add main text to the image
    draw.text(quote_position, text=new_text, font=font, fill=(255, 255, 255, 255), anchor='mm',
              stroke_fill=(0, 0, 0, 255), stroke_width=5,
              align='center')

    if author_text is not None:
        # Add author text
        author_font = ImageFont.truetype(font=f'{font_path}', size=45)
        num_of_lines = new_text.count("\n") + 1
        author_position = (quote_position[0], int(ext_img.size[1] * 0.8))
        draw.text(author_position, text=author_text, font=author_font, fill=(255, 255, 255, 255), anchor='mm',
                  align='center', stroke_fill=(0, 0, 0, 255), stroke_width=5)
    # Save output
    ext_img.save(output_path)


def create_quote(img_file: str, quote_text: str, author: str | None, output_path: str, output_file: str):
    __create_post(img_file, quote_text, './assets/lato.ttf',
                './assets/lato.ttf', output_path,
                output_file, None, author)

