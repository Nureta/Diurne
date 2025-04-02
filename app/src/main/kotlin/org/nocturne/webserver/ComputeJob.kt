package org.nocturne.webserver

import kotlinx.serialization.json.*
import java.util.UUID

class ComputeJob(val command: String, val params: List<String>) {
    companion object {
        val CMD_REQUEST_AUTH = "REQUEST_AUTH"
        val CMD_REQUEST_ECHO = "REQUEST_ECHO"
        val CMD_REQUEST_TOXIC_CHECK = "REQUEST_TOXIC_CHECK"
        val CMD_REQUEST_QUOTE_GEN = "REQUEST_QUOTE_GEN"
        val CMD_REQUEST_PFP_QUOTE_GEN = "REQUEST_PFP_QUOTE_GEN"
        val TIMEOUT = 100_000L
    }

    var id: UUID = UUID.randomUUID()
    var expirationTime = 0L
    var resultLock = CommandResultLock()

    fun toJson(): JsonObject {
        val json = buildJsonObject {
            put("id", id.toString())
            put("command", command)
            putJsonArray("params") {
                for (p in params) add(p)
            }
        }
        return json
    }

    class Builder(var computeCommand: String) {
        private var bParams = ArrayList<String>()
        private var timeout = TIMEOUT

        fun addParam(param: String): Builder {
            bParams.add(param)
            return this
        }

        fun addParams(param: ArrayList<String>): Builder {
            bParams.addAll(param)
            return this
        }

        fun setTimeout(time: Long): Builder {
            this.timeout = time
            return this
        }

        fun build(): ComputeJob {
            val compute = ComputeJob(computeCommand, bParams)
            compute.expirationTime = System.currentTimeMillis() + this.timeout
            return compute
        }
    }
}