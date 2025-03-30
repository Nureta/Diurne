package org.nocturne.listeners

object GenericReplyListener {
    private var hasInit = false
    fun init() {
        if (hasInit) return
        hasInit = true
    }

    private fun registerToGlobalListeners() {
    }
}