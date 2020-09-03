package io.github.kontent.markdown

import io.github.kontent.SiteConfiguration
import io.github.kontent.resolvePageUri
import org.http4k.core.Uri
import java.io.File

interface MarkdownSource {
    fun listAllSources(): Sequence<MarkdownSourceFile>
    fun read(sourceFile: MarkdownSourceFile): Markdown
}

class FileSystemMarkdownSource(private val config: SiteConfiguration) : MarkdownSource {

    override fun listAllSources(): Sequence<MarkdownSourceFile> = File(config.sourcePath.value).walkTopDown()
        .filter { it.name.endsWith(".md") }
        .map { MarkdownSourceFile(Uri.of(it.path), it.resolvePageUri(config)) }
        .plus(config.standalonePages.map { MarkdownSourceFile(Uri.of(it.sourcePath), it.uri) })

    override fun read(sourceFile: MarkdownSourceFile): Markdown = Markdown(File(sourceFile.location.path).readText())
}
