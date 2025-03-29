import base64
import ssl
from typing import List, Tuple
import Util
import ModelManager

COMMAND_PREFIX = "@cmd"
REPLY_PREFIX = "@reply[9271d6]"
REPLY_SUFFIX = "[493f4a]"

CMD_AUTH = "REQUEST_AUTH"
CMD_ECHO = "REQUEST_ECHO"
CMD_TOXIC = "REQUEST_TOXIC_CHECK"
CMD_QUOTE_GEN = "REQUEST_QUOTE_GEN"

class CommandManager():
    __auth_pass = ""
    def __init__(self, auth_pass: str):
        self.__auth_pass = auth_pass
        pass

    def tryCommandInput(self, socket: ssl.SSLSocket, cmd_req: str):
        if (not cmd_req.startswith(COMMAND_PREFIX)):
            raise IOError(f"Command Error {cmd_req}")
        cmd_name = self.__get_cmd_name(cmd_req)
        cmd_params = self.__get_cmd_params(cmd_req)
        self.__handle_command(socket, cmd_name, cmd_params)
        return

    def do_reply(self, socket: ssl.SSLSocket, reply: str):
        reply_str = f"{REPLY_PREFIX}{reply}{REPLY_SUFFIX}\n"
        socket.write(reply_str.encode())


    def __get_cmd_name(self, cmd_req: str):
        start = cmd_req.find('[') + 1
        end = cmd_req.find(']')
        result = cmd_req[start:end]
        return result

    def __get_cmd_params(self, cmd_req: str) -> List[str]:
        start_param = cmd_req.find("@param[") + len("@param[")
        end_param = cmd_req.find("]", start_param)
        params = cmd_req[start_param:end_param].split(",")
        return params
    """
        -- Handle Commands --
    """
    def __handle_command(self, socket: ssl.SSLSocket, cmd: str, params: List[str]):
        print(f"Received Command: {cmd}")
        if cmd == CMD_AUTH:
            self.do_reply(socket, self.__auth_pass or "")
        elif cmd == CMD_ECHO:
            reply = ""
            if (len(params) > 0):
                reply = params[0]
            self.do_reply(socket, reply)
        elif cmd == CMD_TOXIC:
            self.__handle_cmd_toxic(socket, params)
        elif cmd == CMD_QUOTE_GEN:
            self.__handle_cmd_quotegen(socket, params)
        else:
            self.do_reply(socket, "")

    def __handle_cmd_toxic(self, socket: ssl.SSLSocket, params: List[str]):
        if len(params) == 0:
            self.do_reply(socket, "0.0,0.0")
            return
        prompt = str(params[0])
        neutral, toxic = ModelManager.check_toxic(prompt)
        self.do_reply(socket, f"{neutral},{toxic}")

    def __handle_cmd_quotegen(self, socket:ssl.SSLSocket, params: List[str]):
        if len(params) != 2:
            self.do_reply(socket, "FAILURE")
            return
        quote = str(params[0])
        author = str(params[1])
        result_path = ModelManager.createQuoteImg(quote, author)
        filename = result_path.split("/")[-1]
        bytes = base64.standard_b64encode(Util.load_img_bytes(result_path))
        result = f"!{filename},{len(bytes)}!{bytes}"
        self.do_reply(socket, result)

    def do_quotegen(self, quote: str, author: str) -> Tuple[str,bytes]:
        result_path = ModelManager.createQuoteImg(quote, author)
        filename = result_path.split("/")[-1]
        img_base = base64.b64encode(Util.load_img_bytes(result_path))
        return filename, img_base
