package org.nocturne.sockets

class WorkerCommand(val cmd: String, val params: List<String>) {
    companion object {
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
        val CMD_REQUEST_ECHO = "REQUEST_ECHO"
        val CMD_REQUEST_TOXIC_CHECK = "REQUEST_TOXIC_CHECK"
    }
    class Builder(val cmd: String) {
        val bParams = ArrayList<String>()

        fun addParam(params: List<String>): Builder {
            bParams.addAll(params)
            return this
        }

        fun addParam(param: String): Builder {
            bParams.add(param)
            return this
        }

        fun build(): WorkerCommand {
           return WorkerCommand(cmd, bParams)
        }
    }

    fun toTransmitString(): String {
        var sentCmd = "${COMMAND_PREFIX}[${cmd}]"
        if (params.isNotEmpty()) {
            sentCmd += "@param["
            for (p in params) {
                sentCmd += p + ","
            }
            sentCmd = sentCmd.removeSuffix(",")
            sentCmd += "]"
        }
        return sentCmd
    }






}
