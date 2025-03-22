package org.nocturne.sockets

object WorkerProtocol {
    /**
     * Commands sent like: @cmd[CMD_REQUEST_AUTH]@param[p1, p2, p3]
     * Or, if needing special params/data, then make an exception idk
     */
    val COMMAND_PREFIX = "@cmd"

    /**
     * Reply received from client:
     * @reply[REPLY_PREFIX] -- CONTENT CONTENT CONTENT -- [REPLY_SUFFIX]
     * Basically just wrap the reply in prefix/suffix
     * */
    val REPLY_PREFIX = "@reply[9271d6]"
    val REPLY_SUFFIX = "[493f4a]"

    val CMD_REQUEST_AUTH = "REQUEST_AUTH"

}