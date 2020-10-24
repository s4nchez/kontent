package io.github.kontent.markdown

import io.github.kontent.ContentSourcePath
import io.github.kontent.configuration
import org.http4k.core.Uri
import java.io.File

class FileSystemMarkdownSourceTest : MarkdownSourceContract() {

    private val testDir = createTempDir("kontent-test-source")

    init {
        testFiles.forEach {
            val filename = it.split("/").last()
            val relativePath = it.removeSuffix(filename)
            File(testDir.absolutePath + relativePath).mkdirs()
            File(testDir.absolutePath + it).createNewFile()
        }
    }

    override val source: MarkdownSource = FileSystemMarkdownSource(configuration.copy(sourcePath = ContentSourcePath(testDir.absolutePath + "/www")).sourcePath)

    override fun sourceUriFor(relativePath: String) = Uri.of(testDir.absolutePath + relativePath)
}