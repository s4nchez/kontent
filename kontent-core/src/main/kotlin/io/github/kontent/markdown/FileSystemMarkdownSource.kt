package io.github.kontent.markdown

import io.github.kontent.ContentSourcePath
import io.github.kontent.PageSource
import io.github.kontent.relativePath
import org.http4k.core.Uri
import java.io.File

interface MarkdownSource {
    fun listAllSources(urlMappings: Map<Uri, Uri>, standalonePages: Set<PageSource>): Sequence<MarkdownSourceFile>
    fun read(sourceFile: MarkdownSourceFile): Markdown
}

class FileSystemMarkdownSource(private val sourcePath: ContentSourcePath) : MarkdownSource {

    override fun listAllSources(urlMappings: Map<Uri, Uri>, standalonePages: Set<PageSource>): Sequence<MarkdownSourceFile> = File(sourcePath.value).walkTopDown()
            .filter { it.name.endsWith(".md") }
            .map { MarkdownSourceFile(Uri.of(it.path), it.resolvePageUri(sourcePath)) }
            .plus(standalonePages.map { MarkdownSourceFile(Uri.of(it.sourcePath), it.uri) })
            .map { it.copy(targetUri = urlMappings[it.targetUri] ?: it.targetUri) }

    override fun read(sourceFile: MarkdownSourceFile): Markdown = Markdown(File(sourceFile.location.path).readText())
}

fun File.resolvePageUri(sourcePath: ContentSourcePath) = Uri.of(relativePath(sourcePath).removeSuffix(".md").removeSuffix("/index").ensureFirstSlash())

private fun String.ensureFirstSlash() = if (startsWith("/")) this else "/" + this