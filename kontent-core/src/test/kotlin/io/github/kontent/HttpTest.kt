package io.github.kontent

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.ContentType
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasContentType
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
class HttpTest {

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
