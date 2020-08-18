package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.junit.jupiter.api.Test


class MarkdownConversionTest {
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
    fun `can render code from another source`() {
        val github = { request: Request ->
            println(request.uri)
            if (request.uri == Uri.of("https://raw.githubusercontent.com/http4k/http4k/master/src/docs/quickstart/example.kt"))
                Response(Status.OK).body("val a = \"bob\"") else error("fail")
        }

        val document: Html = MarkdownConversion(HttpCodeFetcher(github)).convert(Markdown("""
        see the code:
            
        <a href="https://github.com/http4k/http4k/blob/master/src/docs/quickstart/example.kt" data-kontent-fetch="github" data-kontent-lang="kotlin">this code</a>
        
        thank you
        """.trimIndent()))

        assertThat(document, equalTo(Html("""<p>see the code:</p>
<pre><code class="language-kotlin">val a = &quot;bob&quot;</code></pre>
<p>thank you</p>
""")))
    }
}



