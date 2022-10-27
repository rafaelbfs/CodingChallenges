package eu.rafael.roman

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            call.request.path().startsWith("/roman")
        }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
        }
    }
    routing {
        trace { application.log.trace(it.buildText()) }
        get("/roman/adde") {
            var n1 = call.parameters["n1"]?.uppercase() ?: ""
            var n2 = call.parameters["n2"]?.uppercase() ?: ""
            if (n1.isEmpty() || n2.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest, "Paenitemus, ordinem " +
                            "vestrum implere non possumus."
                )
            } else {
                val x1 = RNumber.parse(n1)
                val x2 = RNumber.parse(n2)

                call.respondText("Resultatus est: ${x1 + x2}")
            }
        }
    }
}
