package io.github.kontent

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import io.github.kontent.OperationalEvents.Companion.NoOp
import io.github.kontent.models.Sitemap
import io.github.kontent.models.Url
import org.http4k.core.Uri
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.File
import java.io.StringWriter


class Kontent(private val configuration: SiteConfiguration, private val events: OperationalEvents = NoOp) {
    private val markdownConversion = MarkdownConversion()

    fun build(): Site {
        val handlebars = Handlebars(FileTemplateLoader(configuration.themePath.value))
        val template: Template = handlebars.compile("index")

        val pageSources = File(configuration.sourcePath.value).walkTopDown().filter { it.name.endsWith(".md") }
            .map {
                val markdown = Markdown(it.readText())
                val contentHtml = markdownConversion.convert(markdown)
                val compiledPage = template.apply(mapOf("content" to contentHtml.raw))
                Page(it.resolvePageUri(configuration), Html(compiledPage))
            }

        val assets = File(configuration.themePath.value).walkTopDown().filterNot { it.isDirectory || it.name.endsWith(".md") }
                .map {
                    Asset(Uri.of("/" + it.path.replace(configuration.themePath.value, "").replace("^[/]*".toRegex(), "")), AssetPath(it.absolutePath))
                }

        return Site(pageSources.toSet(), configuration.baseUri, assets.toSet()).also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
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

open class ValidatedPath(val path: String) {
    init {
        if (!File(path).exists()) throw IllegalArgumentException("path ${File(path).absolutePath} does not exist")
    }
}