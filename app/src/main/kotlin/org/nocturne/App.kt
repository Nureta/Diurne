package org.nocturne

import io.github.cdimascio.dotenv.dotenv


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val dotenv = dotenv {
        directory = "private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
}
