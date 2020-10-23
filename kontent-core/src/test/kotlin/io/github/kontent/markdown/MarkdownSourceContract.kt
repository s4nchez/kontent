package io.github.kontent.markdown

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

abstract class MarkdownSourceContract {
    protected val testFiles = listOf(
        "/readme.md",
        "/irrelevant/bob.md",
        "/www/index.md",
        "/www/sub/page.md"
    )

    protected abstract val source: MarkdownSource

    protected abstract fun sourceUriFor(relativePath: String): Uri

    @Test
    fun `list all markdown files`() {
        val files = source.listAllSources()

        assertThat(files.toSet(), equalTo(setOf(
            MarkdownSourceFile(sourceUriFor("/www/index.md"), Uri.of("/")),
            MarkdownSourceFile(sourceUriFor("/www/sub/page.md"), Uri.of("/sub/page"))
        )))
    }
}

