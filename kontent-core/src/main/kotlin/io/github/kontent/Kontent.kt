package io.github.kontent

import io.github.kontent.OperationalEvents.Companion.NoOp
import io.github.kontent.asset.Assets
import io.github.kontent.asset.FileSystemAssetsSource
import io.github.kontent.markdown.FileSystemMarkdownSource
import io.github.kontent.templating.HandlebarsTemplating
import org.http4k.core.Uri


class Kontent(private val configuration: SiteConfiguration, private val events: OperationalEvents = NoOp) {

    fun build(): Site {
        val assets = FileSystemAssetsSource(configuration.assetsPath).retrieve()
        val markdownSource = FileSystemMarkdownSource(configuration.sourcePath)
        val markdowns = markdownSource.listAllSources(configuration.urlMappings, configuration.standalonePages)

        val navigation = markdowns.navigation()

        val templating = HandlebarsTemplating(configuration.themePath, assets)

        val pages = markdowns.map { templating.renderPage(it.targetUri, markdownSource.read(it), navigation)}

        val allPages = pages.toList()

        return Site(allPages, configuration.baseUri, assets)
            .also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
    }
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
