package org.github.s4nchez

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import org.github.s4nchez.OperationalEvents.Companion.NoOp
import org.github.s4nchez.models.Sitemap
import org.github.s4nchez.models.Url
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.MimeTypes
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.lens.Header.CONTENT_TYPE
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.File
import java.io.StringWriter


class Kontent(private val configuration: SiteConfiguration, private val events: OperationalEvents = NoOp) {
    private val markdownConversion = MarkdownConversion()

    fun build(): Site {
        val handlebars = Handlebars(FileTemplateLoader(configuration.themePath.value))
        val template: Template = handlebars.compile("index")

        val sourceDirectory = File(configuration.sourcePath.value)

        val pageSources = sourceDirectory.walkTopDown().filter { it.name.endsWith(".md") }
                .map {
                    val markdown = Markdown(it.readText())
                    val contentHtml = markdownConversion.convert(markdown)
                    val compiledPage = template.apply(mapOf("content" to contentHtml.raw))
                    Page(Uri.of("/${it.name.removeSuffix(".md")}"), Html(compiledPage))
                }

        val assets = File(configuration.themePath.value).walkTopDown().filterNot { it.isDirectory || it.name.endsWith(".md") }
                .map {
                    Asset(Uri.of("/" + it.path.replace(configuration.themePath.value, "").replace("^[/]*".toRegex(), "")), AssetPath(it.absolutePath))
                }

        return Site(pageSources.toSet(), configuration.baseUri, assets.toSet()).also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
    }
}

fun Kontent.asHttpHandler(): HttpHandler = { request -> build().asHttpHandler()(request) }

fun Site.asHttpHandler(): HttpHandler {
    val allPages = pages.map { it.uri.path to it }.toMap()
    val staticContent = assets.map { it.uri.path to it }.toMap()

    val extMap = MimeTypes()
    return { request ->
        when (request.uri.path) {
            "/sitemap.xml" -> Response(OK).with(CONTENT_TYPE of ContentType.APPLICATION_XML).body(sitemap().raw)
            in staticContent -> {
                val file = File((staticContent[request.uri.path]
                        ?: error("content not found: ${request.uri.path}")).mapsTo.value)
                Response(OK).with(CONTENT_TYPE of extMap.forFile(request.uri.path)).body(file.readBytes().inputStream())
            }
            in allPages -> {
                val content = (allPages[request.uri.path] ?: error("page not found: ${request.uri.path}")).content.raw
                Response(OK).with(CONTENT_TYPE of ContentType.TEXT_HTML).body(content)
            }
            else -> Response(NOT_FOUND)
        }
    }
}

fun Site.sitemap(): XmlDocument {
    val serializer: Serializer = Persister()
    val example = Sitemap(urls = pages.map { Url(baseUri.path(it.uri.path).toString()) })
    val result = StringWriter()

    serializer.write(example, result)
    return XmlDocument(result.toString())
}

data class SiteConfiguration(
        val sourcePath: ContentSourcePath,
        val themePath: ThemePath,
        val baseUri: Uri = Uri.of("")
)

data class ContentSourcePath(val value: String) : ValidatedPath(value)

data class ThemePath(val value: String) : ValidatedPath(value)

data class Site(val pages: Set<Page>, val baseUri: Uri, val assets: Set<Asset> = setOf())

data class XmlDocument(val raw: String)

data class Page(val uri: Uri, val content: Html)

data class Html(val raw: String)

data class Asset(val uri: Uri, val mapsTo: AssetPath)

data class AssetPath(val value: String) : ValidatedPath(value)

open class ValidatedPath(path: String) {
    init {
        if (!File(path).exists()) throw IllegalArgumentException("path ${File(path).absolutePath} does not exist")
    }
}