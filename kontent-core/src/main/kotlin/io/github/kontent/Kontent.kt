package io.github.kontent

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import io.github.kontent.OperationalEvents.Companion.NoOp
import io.github.kontent.asset.Assets
import io.github.kontent.asset.Fingerprint
import io.github.kontent.code.HttpCodeFetcher
import io.github.kontent.markdown.FileSystemMarkdownSource
import io.github.kontent.markdown.MarkdownConversion
import io.github.kontent.markdown.MarkdownSourceFile
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
        val fingerprint = Fingerprint()

        val assets = Assets(File(configuration.assetsPath.value).walkTopDown()
            .filterNot { it.isDirectory }
            .map {
                val assetUri = it.resolveAssetUri(configuration)
                Asset(assetUri, AssetPath(it.absolutePath),
                    fingerprint.generateFingerprintedUri(File(it.absolutePath).inputStream(), assetUri)) }
            .toList()
        )

        val handlebars = Handlebars(FileTemplateLoader(configuration.themePath.value))

        handlebars.registerHelper("asset", Helper<String> { context, _ -> assets.withFingerprint(context) })

        val template: Template = handlebars.compile("index")

        val markdownSource = FileSystemMarkdownSource(configuration)
        val pages = markdownSource.listAllSources()
            .map { generatePage(markdownSource, it, template, configuration.urlMappings) }

        val allPages = pages.toList()

        return Site(allPages, configuration.baseUri, assets)
            .also { events.emit(BuildSucceeded(it.pages.size, it.assets.size)) }
    }

    private fun generatePage(source: FileSystemMarkdownSource, sourceFile: MarkdownSourceFile, template: Template, urlMappings: Map<Uri, Uri>): Page {
        val markdown = source.read(sourceFile)
        val contentHtml = markdownConversion.convert(markdown)
        val compiledPage = template.apply(mapOf("content" to contentHtml.raw))
        val finalUrl = urlMappings[sourceFile.targetUri] ?: sourceFile.targetUri
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

data class Site(val pages: List<Page>, val baseUri: Uri, val assets: Assets)

data class XmlDocument(val raw: String)

data class Page(val uri: Uri, val content: Html)

data class Html(val raw: String)

data class Asset(val uri: Uri, val mapsTo: AssetPath, val uriWithFingerprint: Uri)

data class AssetPath(val value: String) : ValidatedPath(value)

fun File.resolveAssetUri(config: SiteConfiguration) = Uri.of(relativePath(config.assetsPath))

fun File.resolvePageUri(config: SiteConfiguration) = Uri.of(relativePath(config.sourcePath).removeSuffix(".md").removeSuffix("/index"))

private fun File.relativePath(basePath: ValidatedPath) =
    "/" + this.path.replace(basePath.path, "").replace("^[/]*".toRegex(), "")

