package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test


class MarkdownToHtmlTest {
    @Test
    fun `can convert file`() {
        val document: Html = MarkdownConversion().convert(Markdown("This is *Sparta*"))
        assertThat(document, equalTo(Html("<p>This is <em>Sparta</em></p>\n")) )
    }
}

