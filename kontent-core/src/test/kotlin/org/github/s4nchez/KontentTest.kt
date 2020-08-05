package org.github.s4nchez

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KontentTest {

    @Test
    fun `Ping test`() {
        assertEquals(app(Request(GET, "/ping")), Response(OK).body("pong"))
    }

    @Test
    fun `minimal site is a single page`() {
        val site = Kontent().build()
        val first: Page = site.pages.first()
        assertThat(first, allOf(matchesUri(Uri.of("/my-page")), matchesPage(Html("""<body>My site<h1>My Page</h1><p>My <strong>content</strong></p></body>"""))))
    }
}


//fun matchesPage(expected: Page) = allOf(has(Page::uri, equalTo(expected.uri)), matchesHtml(expected.content))
fun matchesUri(expected:Uri) = has(Page::uri, equalTo(expected))
fun matchesPage(expected: Html): Matcher<Page> = has(Page::content, matchesHtml(expected))

fun matchesHtml(expected: Html): Matcher<Html> =
        object : Matcher<Html> {
            override val description: String = "matches HTML of ${describe(expected)}"
            override fun invoke(actual: Html): MatchResult = if(expected.raw.replace("\n", "") == actual.raw.replace("\n", "")) MatchResult.Match else MatchResult.Mismatch("was: ${describe(actual)}")
        }