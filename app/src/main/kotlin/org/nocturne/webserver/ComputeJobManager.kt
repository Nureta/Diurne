package org.nocturne.webserver

import io.ktor.util.collections.*
import java.util.*

object ComputeJobManager {
    private val commandQueue = Collections.synchronizedMap(LinkedHashMap<UUID, ComputeJob>())
    private val pendingCommandMap = ConcurrentMap<UUID, ComputeJob>()
    private val cmdLock = Object()
    private val pendLock = Object()

    fun addComputeJob(job: ComputeJob): CommandResultLock {
        synchronized(cmdLock) {
            commandQueue[job.id] = job
        }
        WebServer.notifyRandomSocket()
        return job.resultLock
    }

    fun requestEcho(echo: String): CommandResultLock {
        val job = ComputeJob.Builder(ComputeJob.CMD_REQUEST_ECHO)
            .addParam(echo).build()
        return addComputeJob(job)
    }

    fun requestToxicCheck(msg: String): CommandResultLock {
        val job = ComputeJob.Builder(ComputeJob.CMD_REQUEST_TOXIC_CHECK)
            .addParam(msg).build()
        return addComputeJob(job)
    }

    fun generateQuoteAI(quote: String, author: String): CommandResultLock {
        val job = ComputeJob.Builder(ComputeJob.CMD_REQUEST_QUOTE_GEN)
            .addParam(quote).addParam(author).build()
        return addComputeJob(job)
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

    fun numberOfJobs(): Int {
        synchronized(cmdLock) {
            return commandQueue.size
        }
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