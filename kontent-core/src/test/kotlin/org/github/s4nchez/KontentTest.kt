package org.github.s4nchez

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.ContentType
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasContentType
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
class KontentTest {

    private val sourcePath = ContentSourcePath("src/main/resources/mvp")

    @Test
    fun `minimal site is a single page`() {
        val site = Kontent().build(sourcePath = sourcePath, themePath = ThemePath("kontent-theme-default/theme"))
        val first: Page = site.pages.first()
        assertThat(first, matchesPage(Page(Uri.of("/my-page"), Html("""<body>My site<h1>My Page</h1><p>My <strong>content</strong></p></body>"""))))
    }

    @Test
    fun `site can be served`() {
        val app = Kontent().build(sourcePath = sourcePath, themePath = ThemePath("kontent-theme-default/theme")).asHttpHandler()

        assertThat(app(Request(GET, "/my-page")), hasStatus(OK) and hasBody(containsSubstring("<h1>My Page</h1>")))
    }

    @Test
    fun `generates sitemap`(approver: Approver){
        val site = Kontent().build(sourcePath, ThemePath("kontent-theme-default/theme"), Uri.of("https://example.org")).asHttpHandler()
        val sitemapResponse = site(Request(GET, "/sitemap.xml"))

        assertThat(sitemapResponse, hasStatus(OK) and hasContentType(ContentType.APPLICATION_XML))
        approver.assertApproved(sitemapResponse)
    }
}


fun matchesPage(expected: Page) = allOf(has(Page::uri, equalTo(expected.uri)), has(Page::content, matchesHtml(expected.content)))

fun matchesHtml(expected: Html): Matcher<Html> =
        object : Matcher<Html> {
            override val description: String = "matches HTML of ${describe(expected)}"
            override fun invoke(actual: Html): MatchResult = if (expected.raw.replace("\n", "") == actual.raw.replace("\n", "")) MatchResult.Match else MatchResult.Mismatch("was: ${describe(actual)}")
        }