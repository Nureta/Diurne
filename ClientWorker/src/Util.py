def load_img_bytes(img_path: str) -> bytes:
    """Reads a file and returns its content as bytes."""
    try:
        file_content_bytes = ""
        with open(img_path, 'rb') as file:
            file_content_bytes = file.read()
        return file_content_bytes
    except FileNotFoundError:
        return None