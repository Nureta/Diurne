import dotenv
import socket
import ssl
from ssl import SSLSocket
import time
import torch
from transformers import RobertaTokenizer, RobertaForSequenceClassification, AutoTokenizer, AutoModelForSequenceClassification

from typing import List, Tuple

SERVER_IP = "127.0.0.1"
SERVER_PORT = 15656
CERT_FILE = "./cert/server-certificate.pem"  # Ensure this is the correct certificate

dotenv.load_dotenv()
env_vals = dotenv.dotenv_values(".env")
cert_pass = env_vals["CERT_PASS"]
auth_pass = env_vals["AUTH_PASS"] 
assert auth_pass is not None

# Connects to server & auto restarts on Error
# Loop reads for commands.
def connect_to_server():
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    context.verify_mode = ssl.CERT_REQUIRED
    context.load_verify_locations(CERT_FILE)
    context.load_cert_chain("./cert/client-certificate.pem", "./cert/client-key.pem", cert_pass)
    context.check_hostname = False
    while True:
        try:
            with socket.create_connection((SERVER_IP, SERVER_PORT)) as sock:
                with context.wrap_socket(sock, server_hostname=SERVER_IP) as ssl_sock:
                    while True:
                        try:
                            print("-- Waiting for command... -- ")
                            read_for_command(ssl_sock)
                        except Exception as e:
                            print(f"Socket Communication Error: {e}")
                            break
        except Exception as e:
            print(f"Error: {e}")
        time.sleep(10)


def read_for_command(socket: ssl.SSLSocket):
    cmd_req = socket.recv(1024).decode()
    if (not cmd_req.startswith(COMMAND_PREFIX)):
        raise IOError(f"Command Error {cmd_req}")
    cmd_name = get_cmd_name(cmd_req)
    cmd_params = get_cmd_params(cmd_req)
    handle_command(socket, cmd_name, cmd_params)
    return

def do_reply(socket: ssl.SSLSocket, reply: str):
    reply_str = f"{REPLY_PREFIX}{reply}{REPLY_SUFFIX}\n"
    socket.write(reply_str.encode())


def get_cmd_name(cmd_req: str):
    start = cmd_req.find('[') + 1
    end = cmd_req.find(']')
    result = cmd_req[start:end]
    return result

def get_cmd_params(cmd_req: str) -> List[str]:
    start_param = cmd_req.find("@param[") + len("@param[")
    end_param = cmd_req.find("]", start_param)
    params = cmd_req[start_param:end_param].split(",")
    return params

COMMAND_PREFIX = "@cmd"
REPLY_PREFIX = "@reply[9271d6]"
REPLY_SUFFIX = "[493f4a]"

CMD_AUTH = "REQUEST_AUTH"
CMD_ECHO = "REQUEST_ECHO"
CMD_TOXIC = "REQUEST_TOXIC_CHECK"

def handle_command(socket: ssl.SSLSocket, cmd: str, params: List[str]):
    print(f"Receieved Command: {cmd}")
    if cmd == CMD_AUTH:
        do_reply(socket, auth_pass or "")
    elif cmd == CMD_ECHO:
        reply = ""
        if (len(params) > 0):
            reply = params[0]
        do_reply(socket, reply)
    elif cmd == CMD_TOXIC:
        handle_cmd_toxic(socket, params)
    else:
        do_reply(socket, "")

def handle_cmd_toxic(socket: SSLSocket, params: List[str]):
    if len(params) == 0:
        do_reply(socket, "0.0,0.0")
        return
    prompt = str(params[0])
    neutral, toxic = check_toxic(prompt)
    do_reply(socket, f"{neutral},{toxic}")


# AI STUFF
TOXIC_MODEL_DIR = "./ai/toxic/model/"
HATE_MODEL_DIR = "./ai/toxic/hatemodel/"

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


# END AI STUFF

check_toxic("hate!")
connect_to_server()



