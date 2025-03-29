package org.nocturne.webserver

import io.ktor.util.collections.*
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

    fun generateQuoteAI(quote: String, author: String): CommandResultLock {
        val job = ComputeJob.Builder(ComputeJob.CMD_REQUEST_QUOTE_GEN)
            .addParam(quote).addParam(author).build()
        synchronized(cmdLock) {
            commandQueue[job.id] = job
        }
        return job.resultLock
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
        val pendingCmd = removePendingJob(id) ?: return
        pendingCmd.resultLock.__complete(hashMapOf("result" to result))
    }

    fun genericMapResult(id: UUID, result: Map<String, String>) {
        val pendingCmd = removePendingJob(id) ?: return
        pendingCmd.resultLock.__complete(result)
    }

    private fun removePendingJob(id: UUID): ComputeJob? {
        var pendingCmd: ComputeJob? = null
        synchronized(pendingCommandMap) {
            pendingCmd = pendingCommandMap.remove(id)
        }
        if (pendingCmd == null) {
            logger.warn("Missing Pending Cmd $id")
            return null
        }
        return pendingCmd
    }

}