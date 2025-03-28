package org.nocturne.webserver

import org.nocturne.sockets.CommandResultLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ComputeJob(val command: String, val params: List<String>) {
    @OptIn(ExperimentalUuidApi::class)
    var id: Uuid = Uuid.random()
    var timeout: Long = 30000L
    var resultLock = CommandResultLock()

    class Builder(var computeCommand: String) {
        var bParams = ArrayList<String>()
        var timeout = 30000L

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
            compute.timeout = this.timeout
            return compute
        }
    }
}