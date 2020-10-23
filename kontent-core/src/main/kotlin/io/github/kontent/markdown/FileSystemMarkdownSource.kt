package io.github.kontent.markdown

import io.github.kontent.SiteConfiguration
import io.github.kontent.resolvePageUri
import org.http4k.core.Uri
import java.io.File

interface MarkdownSource {
    fun listAllSources(urlMappings: Map<Uri, Uri>): Sequence<MarkdownSourceFile>
    fun read(sourceFile: MarkdownSourceFile): Markdown
}

class FileSystemMarkdownSource(private val config: SiteConfiguration) : MarkdownSource {

    override fun listAllSources(urlMappings: Map<Uri, Uri>): Sequence<MarkdownSourceFile> = File(config.sourcePath.value).walkTopDown()
        .filter { it.name.endsWith(".md") }
        .map { MarkdownSourceFile(Uri.of(it.path), it.resolvePageUri(config)) }
        .plus(config.standalonePages.map { MarkdownSourceFile(Uri.of(it.sourcePath), it.uri) })
            .map { it.copy(targetUri = urlMappings[it.targetUri] ?: it.targetUri) }

    override fun read(sourceFile: MarkdownSourceFile): Markdown = Markdown(File(sourceFile.location.path).readText())
}
