package org.github.s4nchez

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.ContentType
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

    private val sourcePath = ContentSourcePath("src/test/resources/mvp")

    private val configuration = SiteConfiguration(sourcePath, ThemePath("../kontent-theme-default/theme"), Uri.of("https://example.org"))

    @Test
    fun `minimal site is a single page`() {
        val site = Kontent(configuration).build()
        val first: Page = site.pages.first()
        assertThat(first, matchesPageUri(Uri.of("/my-page")))
    }

    @Test
    fun `site can include static content`() {
        val site = Kontent(configuration).build()
        val first: Asset = site.assets.first()
        println(site.assets)
        assertThat(first, matchesAssetUri(Uri.of("/css/main.css")))
    }

    @Test
    fun `site can be served`(approver: Approver) {
        val app = Kontent(configuration).build().asHttpHandler()

        val response = app(Request(GET, "/my-page"))
        assertThat(response, hasStatus(OK))
        approver.assertApproved(response)
    }

    @Test
    fun `static content can be served`() {
        val app = Kontent(configuration).build().asHttpHandler()

        val response = app(Request(GET, "/css/main.css") )
        assertThat(response, hasStatus(OK) and hasContentType(ContentType("text/css")))
    }

    @Test
    fun `generates sitemap`(approver: Approver){
        val site = Kontent(configuration).build().asHttpHandler()
        val sitemapResponse = site(Request(GET, "/sitemap.xml"))

        assertThat(sitemapResponse, hasStatus(OK) and hasContentType(ContentType.APPLICATION_XML))
        approver.assertApproved(sitemapResponse)
    }
}

fun matchesPageUri(expected: Uri) = has(Page::uri, equalTo(expected))
fun matchesAssetUri(expected: Uri) = has(Asset::uri, equalTo(expected))
