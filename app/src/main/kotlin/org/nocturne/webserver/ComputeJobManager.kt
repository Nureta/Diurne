package org.nocturne.webserver

import io.ktor.util.collections.*
import org.nocturne.sockets.CommandResultLock
import java.util.*

object ComputeJobManager {
    val commandQueue = Collections.synchronizedMap(LinkedHashMap<UUID, ComputeJob>())
    val pendingCommandMap = ConcurrentMap<UUID, ComputeJob>()
    val cmdLock = Object()
    val pendLock = Object()

    fun requestEcho(echo: String): CommandResultLock {
        val job = ComputeJob.Builder(ComputeJob.CMD_REQUEST_ECHO)
            .addParam(echo).build()
        synchronized(cmdLock) {
            commandQueue[job.id] = job
        }
        return job.resultLock
    }

    fun generateQuoteAI(quote: String, author: String) {

    }

    /**
     * Removes a compute job from the queue and returns it.
     */
    fun getComputeJob(): ComputeJob? {
        var entry: Map.Entry<UUID, ComputeJob>? = null
        synchronized(cmdLock) {
            val itr = commandQueue.iterator()
            val currTime = System.currentTimeMillis()
            while (itr.hasNext()) {
                entry = itr.next()
                itr.remove()
                if (entry!!.value.expirationTime > currTime) continue
            }
        }
        if (entry != null) {
            synchronized(pendLock) {
                pendingCommandMap[entry!!.key] = entry!!.value
            }
        }
        return entry?.value
    }

    fun genericStringResult(id: UUID, result: String) {
        var pendingCmd: ComputeJob? = null
        synchronized(pendingCommandMap) {
            pendingCmd = pendingCommandMap.remove(id)
        }
        if (pendingCmd == null) {
            logger.warn("Missing Pending Cmd $id -> $result")
            return
        }
        pendingCmd!!.resultLock.__complete(result)
    }

}