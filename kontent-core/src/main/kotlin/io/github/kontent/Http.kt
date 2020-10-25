package io.github.kontent

import io.github.kontent.models.Sitemap
import io.github.kontent.models.Url
import org.http4k.core.ContentType.Companion.APPLICATION_XML
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.MimeTypes
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header.CONTENT_TYPE
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.File
import java.io.StringWriter


fun Site.asHttpHandler(): HttpHandler {
    val allPages = pages.map { it.uri.path to it }.toMap()
    val staticContent = assets.map { it.uriWithFingerprint.path to it }.toMap()

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

private fun Site.sitemap(): XmlDocument {
    val serializer: Serializer = Persister()
    val example = Sitemap(urls = pages.map { Url(baseUri.path(it.uri.path).toString()) })
    val result = StringWriter()

    serializer.write(example, result)
    return XmlDocument(result.toString())
}
