package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    val port = applicationEnvironment().config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"
    println("Listening on port $port")

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureFrameworks()
    configureRouting()
}

