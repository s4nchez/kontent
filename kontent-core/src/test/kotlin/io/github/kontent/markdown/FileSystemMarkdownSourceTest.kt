package io.github.kontent.markdown

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.kontent.ContentSourcePath
import io.github.kontent.configuration
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import java.io.File

class FileSystemMarkdownSourceTest {

    private val testFiles = listOf(
        "/readme.md",
        "/irrelevant/bob.md",
        "/www/index.md",
        "/www/sub/page.md"
    )

    private val testDir = createTempDir("kontent-test-source")

    init {
        testFiles.forEach {
            val filename = it.split("/").last()
            val relativePath = it.removeSuffix(filename)
            File(testDir.absolutePath + relativePath).mkdirs()
            File(testDir.absolutePath + it).createNewFile()
        }
    }

    private val source = FileSystemMarkdownSource(configuration.copy(sourcePath = ContentSourcePath(testDir.absolutePath + "/www")))

    @Test
    fun `list all markdown files`() {
        val files = source.listAllSources()

        assertThat(files.toSet(), equalTo(setOf(
            MarkdownSourceFile(sourceUriFor("/www/index.md"), Uri.of("/index")),
            MarkdownSourceFile(sourceUriFor("/www/sub/page.md"), Uri.of("/sub/page"))
        )))
    }

    private fun sourceUriFor(relativePath: String) = Uri.of(testDir.absolutePath + relativePath)
}