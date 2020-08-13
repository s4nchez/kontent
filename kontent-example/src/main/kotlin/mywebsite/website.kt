package mywebsite

import org.github.s4nchez.Kontent
import org.github.s4nchez.asHttpHandler
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent().build().asHttpHandler().asServer(SunHttp(8000)).start()
}