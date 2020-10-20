package io.github.kontent.asset

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

internal class FingerprintTest {

    private val fingerprint = Fingerprint()

    @Test
    fun `creates fingerprint uri for given asset`() {
        assertThat(
            fingerprint.generateFingerprintedUri("foo".byteInputStream(), Uri.of("/foo.txt")),
            equalTo(Uri.of("/foo-1B2M2Y8AsgTpgAmY7PhCfg.txt"))
        )
    }

    @Test
    fun `adds fingerprint to various kinds of URI`() {
        val asset = "bar".byteInputStream()
        val suffix = "1B2M2Y8AsgTpgAmY7PhCfg"

        mapOf(
            "/style.css" to "/style-$suffix.css",
            "/a/b/c/d.jpg" to "/a/b/c/d-$suffix.jpg",
            "cat.gif" to "cat-$suffix.gif",
            "/archive.tar.gz" to "/archive-$suffix.tar.gz",
            "/something.with/dots.txt" to "/something.with/dots-$suffix.txt",
            "/bob" to "/bob"
        )
            .entries.forEach { (key, value) ->
                assertThat(
                    fingerprint.generateFingerprintedUri(asset, Uri.of(key)),
                    equalTo(Uri.of(value))
                )
            }
    }
}