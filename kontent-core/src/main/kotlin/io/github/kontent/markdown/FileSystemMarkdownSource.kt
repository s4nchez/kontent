package io.github.kontent.markdown

import io.github.kontent.MarkdownSourceFile
import io.github.kontent.SiteConfiguration
import io.github.kontent.resolvePageUri
import java.io.File

class FileSystemMarkdownSource(private val config: SiteConfiguration) {

    fun listAllSources(): Sequence<MarkdownSourceFile> = File(config.sourcePath.value).walkTopDown()
        .filter { it.name.endsWith(".md") }
        .map { MarkdownSourceFile(it, it.resolvePageUri(config)) }
        .plus(config.standalonePages.map { MarkdownSourceFile(File(it.sourcePath), it.uri) })
}
