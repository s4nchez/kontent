package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(ApprovalTest::class)
class MarkdownTest {
    @Test
    fun `can convert file`() {
        val document: Html = MarkdownConversion().convert(Markdown("This is *Sparta*"))
        assertThat(document, equalTo(Html("<p>This is <em>Sparta</em></p>\n")))
    }

    @Test
    fun `can render normal code`() {
        val document: Html = MarkdownConversion().convert(Markdown("""
        ```kotlin
        val a = "bob"
        ```
        """.trimIndent()))

        assertThat(document, equalTo(Html("<pre><code class=\"language-kotlin\">val a = &quot;bob&quot;\n" +
            "</code></pre>\n")))
    }

    @Test
    fun `can render code from another source`(approver: Approver) {
        val github = { request: Request ->
            if (request.uri == Uri.of("https://raw.githubusercontent.com/http4k/http4k/master/src/docs/quickstart/example.kt"))
                Response(Status.OK).body("val a = \"bob\"") else error("fail")
        }

        val document: Html = MarkdownConversion(HttpCodeFetcher(github)).convert(Markdown("""
        see the code:
            
        <a href="https://github.com/http4k/http4k/blob/master/src/docs/quickstart/example.kt" data-kontent-fetch="github" data-kontent-lang="kotlin">this code</a>
        
        thank you
        """.trimIndent()))

        approver.assertApproved(document.raw)
    }
}




