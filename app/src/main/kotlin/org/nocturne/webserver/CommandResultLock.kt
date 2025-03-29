package org.nocturne.webserver

import java.util.concurrent.atomic.AtomicBoolean

class CommandResultLock {
    private var result: Map<String, String> = HashMap<String,String>()
    private var isCompleted = AtomicBoolean(false)
    var SLEEP_WAIT = 200L

    fun __complete(res: Map<String, String>) {
        this.result = res
        this.isCompleted.set(true)
    }

    /**
     * Wait for result until timeout is reached.
     */
    fun waitBlocking(timeout: Long): Map<String, String>? {
        var waited = 0L
        Thread.sleep(SLEEP_WAIT/5)
        while (isCompleted.get() == false && waited < timeout) {
            Thread.sleep(SLEEP_WAIT)
            waited += SLEEP_WAIT
        }
        if (isCompleted.get() == true) {
            return result
        }
        return null
    }

    /**
     * Poll for result nonblocking in a thread
     * Once result is populated or timeout is reached, call the callback
     */
    fun waitCallback(timeout: Long, callback: (Map<String, String>?) -> Unit) {
        Thread {
            var waited = 0L
            Thread.sleep(SLEEP_WAIT/5)
            while (isCompleted.get() == false && waited < timeout) {
                Thread.sleep(SLEEP_WAIT)
                waited += SLEEP_WAIT
            }
            if (isCompleted.get() == true) {
                callback(result)
            } else {
                callback(null)
            }
        }.start()
    }
}