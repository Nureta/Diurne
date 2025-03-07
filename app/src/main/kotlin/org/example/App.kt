package org.example

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val dotenv = dotenv {
        directory = "./private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
    println("TOKEN: $token")
    println(App().greeting)
}
