import asyncio
import json
from typing import List

import dotenv
import ssl
import websockets
import dotenv
import requests
from websockets import ClientConnection

dotenv.load_dotenv()
env_vals = dotenv.dotenv_values(".env")
SOCKET_URL = env_vals["SOCKET_URL"]
SERVER_URL = env_vals["SERVER_URL"]

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

def getGenericJsonResult(id: str, result: str):
    jsonData = { "id": id, "result": result }
    return jsonData

asyncio.run(listen())
