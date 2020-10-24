package io.github.kontent

import io.github.kontent.NavigationGenerator.generateNavigation
import io.github.kontent.OperationalEvents.Companion.NoOp
import io.github.kontent.asset.Assets
import io.github.kontent.asset.FileSystemAssetsSource
import io.github.kontent.markdown.FileSystemMarkdownSource
import io.github.kontent.models.Sitemap
import io.github.kontent.models.Url
import io.github.kontent.templating.HandlebarsTemplating
import org.http4k.core.Uri
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringWriter


class Kontent(private val configuration: SiteConfiguration, private val events: OperationalEvents = NoOp) {

    fun build(): Site {
        val assets = FileSystemAssetsSource(configuration.assetsPath).retrieve()
        val markdownSource = FileSystemMarkdownSource(configuration.sourcePath)

        val pageSources = markdownSource.listAllSources(configuration.urlMappings, configuration.standalonePages)
        val navigation = pageSources.map { it.targetUri }.toList().generateNavigation()

         val templating = HandlebarsTemplating(configuration.themePath, assets)

        val pages = pageSources
            .map {
                templating.renderPage(it.targetUri, markdownSource.read(it), navigation)
            }

        val allPages = pages.toList()

        return Site(allPages, configuration.baseUri, assets)
            .also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
    }
}

fun Site.sitemap(): XmlDocument {
    val serializer: Serializer = Persister()
    val example = Sitemap(urls = pages.map { Url(baseUri.path(it.uri.path).toString()) })
    val result = StringWriter()

    serializer.write(example, result)
    return XmlDocument(result.toString())
}

data class PageModel(val content: String, val nav: Navigation, val title: String?)

data class Site(val pages: List<Page>, val baseUri: Uri, val assets: Assets)

data class XmlDocument(val raw: String)

data class MarkdownResult(val html: Html, val metadata: PageMetadata)

data class Page(val uri: Uri, val content: Html)

data class PageMetadata(val title:String? = null)

data class Html(val raw: String)

data class Asset(val uri: Uri, val mapsTo: AssetPath, val uriWithFingerprint: Uri)

data class AssetPath(val value: String) : ValidatedPath(value)
