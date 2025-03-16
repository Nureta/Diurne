import socket
import ssl
import time
import dotenv

SERVER_IP = "127.0.0.1"
SERVER_PORT = 15656
CERT_FILE = "server-certificate.pem"  # Ensure this is the correct certificate
AUTH_SECRET = ""  # Must match the Kotlin server

dotenv.load_dotenv()
cert_pass = dotenv.dotenv_values(".env")["CERT_PASS"]

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
                            response = input("> ")
                            response += "\n"
                            ssl_sock.send(response.encode())
                            time.sleep(1)
                            print("-- Socket Communication --")
                            print(ssl_sock.recv(1024).decode())
                            print(ssl_sock.recv(1024).decode())
                        except Exception as e:
                            print(f"Socket Communication Error: {e}")
                            break
        except Exception as e:
            print(f"Error: {e}")
        time.sleep(3)

connect_to_server()
