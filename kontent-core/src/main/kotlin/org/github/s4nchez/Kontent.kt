package org.github.s4nchez

import org.github.s4nchez.models.HandlebarsViewModel
import org.http4k.client.JavaHttpClient
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.filter.DebuggingFilters.PrintResponse
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.viewModel

val app = routes(
    "/ping" bind GET to {
        Response(OK).body("pong")
    },

    "/templates/handlebars" bind GET to {
        val renderer = HandlebarsTemplates().CachingClasspath()
        val view = Body.viewModel(renderer, TEXT_HTML).toLens()
        val viewModel = HandlebarsViewModel("Hello there!")
        Response(OK).with(view of viewModel)
    }
)

fun main() {

    val server = DebuggingFilters.PrintRequestAndResponse()
        .then(app)
        .asServer(SunHttp(9000)).start()

    val client = PrintResponse()
        .then(JavaHttpClient())

    val response = client(Request(GET, "http://localhost:9000/ping"))

    println(response.bodyString())


    println("Server started on " + server.port())
}