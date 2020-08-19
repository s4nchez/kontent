package io.github.kontent

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import io.github.kontent.OperationalEvents.Companion.NoOp
import io.github.kontent.models.Sitemap
import io.github.kontent.models.Url
import org.http4k.client.JavaHttpClient
import org.http4k.core.Uri
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.File
import java.io.StringWriter


class Kontent(private val configuration: SiteConfiguration, private val events: OperationalEvents = NoOp) {
    private val markdownConversion = MarkdownConversion(HttpCodeFetcher(JavaHttpClient()))

    fun build(): Site {
        val handlebars = Handlebars(FileTemplateLoader(configuration.themePath.value))
        val template: Template = handlebars.compile("index")

        val pages = File(configuration.sourcePath.value).walkTopDown()
            .filter { it.name.endsWith(".md") }
            .map { generatePage(it, template, it.resolvePageUri(configuration), configuration.urlMappings) }

        val standalonePages = configuration.standalonePages
            .map { generatePage(File(it.sourcePath), template, it.uri, configuration.urlMappings) }

        val assets = File(configuration.assertSourcePath.value).walkTopDown()
            .filterNot { it.isDirectory || it.name.endsWith(".md") }
            .map { Asset(it.resolveAssetUri(configuration), AssetPath(it.absolutePath)) }

        val allPages = (pages + standalonePages).toList()

        return Site(allPages, configuration.baseUri, assets.toList())
            .also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
    }

    private fun generatePage(it: File, template: Template, targetUri: Uri, urlMappings: Map<Uri, Uri>): Page {
        val markdown = Markdown(it.readText())
        val contentHtml = markdownConversion.convert(markdown)
        val compiledPage = template.apply(mapOf("content" to contentHtml.raw))
        val finalUrl = urlMappings[targetUri] ?: targetUri
        return Page(finalUrl, Html(compiledPage))
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
    val assertSourcePath: AssetSourcePath,
    val baseUri: Uri = Uri.of(""),
    val standalonePages: Set<PageSource> = setOf(),
    val urlMappings: Map<Uri, Uri> = mapOf()
)

data class ContentSourcePath(val value: String) : ValidatedPath(value)

data class PageSource(val uri: Uri, val sourcePath: String) : ValidatedPath(sourcePath)

data class AssetSourcePath(val value: String):ValidatedPath(value)

data class ThemePath(val value: String) : ValidatedPath(value)

data class Site(val pages: List<Page>, val baseUri: Uri, val assets: List<Asset> = listOf())

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

fun File.resolveAssetUri(config: SiteConfiguration) = Uri.of(relativePath(config.assertSourcePath))

fun File.resolvePageUri(config: SiteConfiguration) = Uri.of(relativePath(config.sourcePath).removeSuffix(".md"))

private fun File.relativePath(basePath: ValidatedPath) =
    "/" + this.path.replace(basePath.path, "").replace("^[/]*".toRegex(), "")
