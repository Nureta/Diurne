package org.nocturne.webserver

import kotlinx.serialization.Serializable

@Serializable
data class GenericStringResult(val id: String, val result: String)
