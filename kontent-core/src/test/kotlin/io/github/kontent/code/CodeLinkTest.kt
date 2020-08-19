package io.github.kontent.code

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

internal class CodeLinkTest {
    private val link = CodeLink()

    @Test
    fun `simple link`() {
        assertThat(
            link.resolve("""<a href="some-uri" data-kontent-lang="php" data-kontent-fetch="github">"""),
            equalTo(FetchParameters(Uri.of("some-uri"), "php"))
        )
    }

    @Test
    fun `link without fetch`() {
        assertThat(
            link.resolve("""<a href="some-uri" data-kontent-lang="php">"""),
            absent()
        )
    }

    @Test
    fun `link without language`() {
        assertThat(
            link.resolve("""<a href="some-uri" data-kontent-fetch="github">"""),
            equalTo(FetchParameters(Uri.of("some-uri"), ""))
        )
    }

    @Test
    fun `github code url rewrite`() {
        assertThat(
            link.resolve("""<a href="https://github.com/repo/master/blob/some-code.php" data-kontent-fetch="github" data-kontent-lang="php">"""),
            equalTo(FetchParameters(Uri.of("https://raw.githubusercontent.com/repo/master/some-code.php"), "php"))
        )
    }
}