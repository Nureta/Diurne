import dotenv
import socket
import ssl
from ssl import SSLSocket
import time
import torch
from transformers import RobertaTokenizer, RobertaForSequenceClassification, AutoTokenizer, AutoModelForSequenceClassification

from typing import List, Tuple

from CommandManager import CommandManager

SERVER_IP = "127.0.0.1"
SERVER_PORT = 15656
SERVER_CERT_FILE = "../cert/server-certificate.pem"
CLIENT_CERT_FILE = "../cert/client-certificate.pem"
CLIENT_KEY_FILE = "../cert/client-key.pem"

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
    context.load_verify_locations(SERVER_CERT_FILE)
    context.load_cert_chain(CLIENT_CERT_FILE, CLIENT_KEY_FILE, cert_pass)
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
    cmdManager.tryCommandInput(socket, cmd_req)

cmdManager = CommandManager(auth_pass)
connect_to_server()
