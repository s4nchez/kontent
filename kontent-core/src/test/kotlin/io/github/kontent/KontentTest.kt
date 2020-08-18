package io.github.kontent

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.ContentType
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.hamkrest.hasContentType
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
class KontentTest {

    private val sourcePath = ContentSourcePath("src/test/resources/mvp/pages")

    private val configuration = SiteConfiguration(
        sourcePath = sourcePath,
        themePath = ThemePath("../kontent-theme-default/theme"),
        baseUri = Uri.of("https://example.org")
    )

    @Test
    fun `minimal site is a single page`() {
        val site = Kontent(configuration).build()

        assertThat(site.pages.first(), matchesPageUri(Uri.of("/my-page")))
    }

    @Test
    fun `site can include static content`() {
        val site = Kontent(configuration).build()

        assertThat(site.assets.first(), matchesAssetUri(Uri.of("/css/main.css")))
    }

    @Test
    fun `site can include reference to standalone pages outside content directory`(){
        val source = PageSource(Uri.of("/random"), "src/test/resources/mvp/standalone-page/standalone.md")

        val site = Kontent(configuration.copy(standalonePages = setOf(source))).build()

        assertThat(site.pages, anyElement(matchesPageUri(Uri.of("/random"))))
    }

    @Test
    fun `site can redefine a page URI` (){
        val mappings = mapOf(Uri.of("/my-page") to Uri.of("/"))
        val site = Kontent(configuration.copy(urlMappings = mappings)).build()

        assertThat(site.pages.first(), matchesPageUri(Uri.of("/")))
    }

    @Test
    fun `site can be served`(approver: Approver) {
        val app = Kontent(configuration).build().asHttpHandler()

        approver.assertApproved(app(Request(GET, "/my-page"))
            .also {
                assertThat(it, hasStatus(OK) and hasContentType(TEXT_HTML))
            }
        )
    }

    @Test
    fun `static content can be served`() {
        val app = Kontent(configuration).build().asHttpHandler()

        val response = app(Request(GET, "/css/main.css"))

        assertThat(response, hasStatus(OK) and hasContentType(ContentType("text/css")))
    }

    @Test
    fun `generates sitemap`(approver: Approver) {
        val app = Kontent(configuration).build().asHttpHandler()

        approver.assertApproved(app(Request(GET, "/sitemap.xml")).also {
            assertThat(it, hasStatus(OK) and hasContentType(ContentType.APPLICATION_XML))
        })
    }
}

fun matchesPageUri(expected: Uri) = has(Page::uri, equalTo(expected))
fun matchesAssetUri(expected: Uri) = has(Asset::uri, equalTo(expected))
