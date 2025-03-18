package com.example

import Env
import EnvKey
import io.ktor.server.application.*

fun main(args: Array<String>) {
    val env = Env.getList(EnvKey.GEMINI_API_KEYS)
    println(env.first())
//    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureFrameworks()
    configureRouting()
}
