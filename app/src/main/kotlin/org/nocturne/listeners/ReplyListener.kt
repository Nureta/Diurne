package org.nocturne.listeners

object ReplyListener {
    private var hasInit = false
    fun init() {
        if (hasInit) return
        hasInit = true
    }

    private fun registerToGlobalListeners() {
    }
}