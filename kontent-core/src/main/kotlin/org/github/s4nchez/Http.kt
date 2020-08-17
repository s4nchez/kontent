package org.github.s4nchez

import org.http4k.core.ContentType.Companion.APPLICATION_XML
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.MimeTypes
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header.CONTENT_TYPE
import java.io.File


fun Kontent.asHttpHandler(): HttpHandler = { request -> build().asHttpHandler()(request) }

fun Site.asHttpHandler(): HttpHandler {
    val allPages = pages.map { it.uri.path to it }.toMap()
    val staticContent = assets.map { it.uri.path to it }.toMap()

    return { request ->
        when (request.uri.path) {
            "/sitemap.xml" -> Response(OK).with(CONTENT_TYPE of APPLICATION_XML).body(sitemap().raw)
            in staticContent -> {
                val file = File((staticContent[request.uri.path]
                        ?: error("content not found: ${request.uri.path}")).mapsTo.value)
                Response(OK)
                        .with(CONTENT_TYPE of MimeTypes().forFile(request.uri.path))
                        .body(file.readBytes().inputStream())
            }
            in allPages -> {
                val content = (allPages[request.uri.path] ?: error("page not found: ${request.uri.path}")).content.raw
                Response(OK)
                        .with(CONTENT_TYPE of TEXT_HTML)
                        .body(content)
            }
            else -> Response(NOT_FOUND)
        }
    }
}
