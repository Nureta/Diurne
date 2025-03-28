package org.nocturne.webserver

import java.util.concurrent.ConcurrentLinkedQueue

object ComputeJobManager {
    val commandQueue = ConcurrentLinkedQueue<ComputeJob>()

    fun requestEcho(echo: String) {

    }

    fun generateQuoteAI(quote: String, author: String) {

    }


}