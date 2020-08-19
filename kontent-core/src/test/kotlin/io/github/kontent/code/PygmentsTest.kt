package io.github.kontent.code

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

internal class PygmentsTest {

    @Test
    fun `can highlight a supported languages`() {
        val snippet = "println(\"hello world\");"
        val highlighted = Pygments().highlight(snippet, "kotlin")

        assertThat(Jsoup.parse(highlighted).text(), equalTo(snippet))
    }

    @Test
    fun `handles unsupported language`() {
        val snippet = "println(\"hello world\");"
        val highlighted = Pygments().highlight(snippet, "jibber-jabber")

        assertThat(Jsoup.parse(highlighted).text(), equalTo(snippet))
    }
}