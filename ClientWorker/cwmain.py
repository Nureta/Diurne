import socket
import ssl
import time
import dotenv
from typing import List

SERVER_IP = "127.0.0.1"
SERVER_PORT = 15656
CERT_FILE = "server-certificate.pem"  # Ensure this is the correct certificate
AUTH_SECRET = ""  # Must match the Kotlin server

dotenv.load_dotenv()
env_vals = dotenv.dotenv_values(".env")
cert_pass = env_vals["CERT_PASS"]
auth_pass = env_vals["AUTH_PASS"] 
assert auth_pass is not None

COMMAND_PREFIX = "@cmd"
REPLY_PREFIX = "@reply[9271d6]"
REPLY_SUFFIX = "[493f4a]"

CMD_AUTH = "REQUEST_AUTH"
CMD_ECHO = "REQUEST_ECHO"
# Connects to server & auto restarts on Error
# Loop reads for commands.
def connect_to_server():
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    context.verify_mode = ssl.CERT_REQUIRED
    context.load_verify_locations(CERT_FILE)
    context.load_cert_chain("./client-certificate.pem", "./client-key.pem", cert_pass)
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
        time.sleep(3)


def read_for_command(socket: ssl.SSLSocket):
    cmd_req = socket.recv(1024).decode()
    if (not cmd_req.startswith(COMMAND_PREFIX)):
        raise IOError(f"Command Error {cmd_req}")
    cmd_name = get_cmd_name(cmd_req)
    cmd_params = get_cmd_params(cmd_req)
    handle_command(socket, cmd_name, cmd_params)
    return

def handle_command(socket: ssl.SSLSocket, cmd: str, params: List[str]):
    print(f"Receieved Command: {cmd}")
    if cmd == CMD_AUTH:
        do_reply(socket, auth_pass or "")
    elif cmd == CMD_ECHO:
        reply = ""
        if (len(params) > 0):
            reply = params[0]
        do_reply(socket, reply)
    else:
        do_reply(socket, "")

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

connect_to_server()



