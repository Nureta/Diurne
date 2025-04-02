# Diurne
The Nocturne's Server personal discord bot!
<sub>discord.gg/noctis<\sub>

### Generate Client/Server Certificates
*Generate Certs & Keys*

`openssl req -newkey rsa:2048 -nodes -keyout client-key.pem -x509 -days 365 -out client-certificate.pem`

`openssl req -newkey rsa:2048 -nodes -keyout server-key.pem -x509 -days 365 -out server-certificate.pem`

*Generate Keystore p12*

```openssl pkcs12 -inkey client-key.pem -in client-certificate.pem -export -out client-certificate.p12```

`openssl pkcs12 -inkey server-key.pem -in server-certificate.pem -export -out server-certificate.p12`


### To get AI Models:

1. Download "uv" https://github.com/astral-sh/uv (or use virutal env of choice)
2. In Diurne/ClientWorker: `uv venv env --python 3.12`
3. Activate virtual environment.. ./env/bin/activate.sh
4. Then do `uv pip install -r requirements.txt`
5. Go to ai/toxic and do python get_model.py (to test you can do python hatemodel.py to input text into it)
6. Done! You can run client in Diurne/ClientWorker `python cwmain.py`

