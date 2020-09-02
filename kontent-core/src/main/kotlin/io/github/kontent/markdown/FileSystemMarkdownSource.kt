package io.github.kontent.markdown

import io.github.kontent.SiteConfiguration
import io.github.kontent.resolvePageUri
import org.http4k.core.Uri
import java.io.File

class FileSystemMarkdownSource(private val config: SiteConfiguration) {

    fun listAllSources(): Sequence<MarkdownSourceFile> = File(config.sourcePath.value).walkTopDown()
        .filter { it.name.endsWith(".md") }
        .map { MarkdownSourceFile(Uri.of(it.path), it.resolvePageUri(config)) }
        .plus(config.standalonePages.map { MarkdownSourceFile(Uri.of(it.sourcePath), it.uri) })

    fun read(sourceFile: MarkdownSourceFile): Markdown = Markdown(File(sourceFile.location.path).readText())
}
