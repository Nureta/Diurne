import asyncio
import base64
import json
import threading
import urllib.parse
from typing import List

import dotenv
import ssl
import websockets
import dotenv
import requests
from websockets import ClientConnection

from src.CommandManager import CommandManager

dotenv.load_dotenv()
env_vals = dotenv.dotenv_values(".env")
SOCKET_URL = env_vals["SOCKET_URL"]
SERVER_URL = env_vals["SERVER_URL"]
AUTH_PASS = env_vals["AUTH_PASS"]
assert AUTH_PASS is not None

async def listen():
    retry_delay = 3
    while True:
        try:
            if (SOCKET_URL.startswith("wss")):
                ssl_context = ssl.create_default_context()
                async with websockets.connect(SOCKET_URL, ssl=ssl_context) as websocket:
                    retry_delay = 3
                    await socket_handler(websocket)
            else:
                async with websockets.connect(SOCKET_URL) as websocket:
                    retry_delay = 3
                    await socket_handler(websocket)
        except Exception as e:
            print(f"Disconnected: {e}. Reconnecting in {retry_delay} seconds...")
            await asyncio.sleep(retry_delay)
            retry_delay = min(retry_delay * 2, 30)
            pass

async def socket_handler(websocket: ClientConnection):
    print("Connected...")
    while True:
        message = await websocket.recv()
        numJobs = int(message)
        if (numJobs == 0):
            continue
        print(f"Requests: {numJobs}")
        await requestHandleJob()

async def requestHandleJob():
    resp = requests.get(f"{SERVER_URL}/task")
    if (resp.status_code == 204):
        return
    elif (resp.status_code != 200):
        print(resp)
        return
    cmd_req = json.loads(resp.content.decode("utf-8"))
    await commandHandler(cmd_req['id'], cmd_req['command'], cmd_req['params'])

async def commandHandler(id: str, cmd: str, params: List[str]):
    if cmd == "REQUEST_ECHO":
        echoJson = getGenericJsonResult(id, params[0])
        requests.post(f"{SERVER_URL}/result/string", json=echoJson)
    elif cmd == "REQUEST_QUOTE_GEN":
        t = threading.Thread(target=post_quote_gen, args=(id, params[0], params[1]))
        t.start()

def post_quote_gen(id: str, quote: str, author: str):
    filename, result = commandManager.do_quotegen(quote, author)
    result = result.decode("ASCII")
    filename = urllib.parse.quote_plus(filename)
    requests.post(f"{SERVER_URL}/result/quote?id={id}&filename={filename}", data=result)

def getGenericJsonResult(id: str, result: str):
    jsonData = { "id": id, "result": result }
    return jsonData

commandManager = CommandManager(AUTH_PASS)
asyncio.run(listen())
