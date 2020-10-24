package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.kontent.markdown.resolvePageUri
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import java.io.File

class UriTest {
    @Test
    fun `resolve basic page Uri`() {
        assertThat(source("/a-page.md").resolvePageUri(configuration.sourcePath),
            equalTo(Uri.of("/a-page")))
    }

    @Test
    fun `resolve nested page Uri`() {
        assertThat(source("/some/other-page.md").resolvePageUri(configuration.sourcePath),
            equalTo(Uri.of("/some/other-page")))
    }

    @Test
    fun `resolve basic asset Uri`() {
        assertThat(asset("/a-file.jpg").resolveAssetUri(configuration.assetsPath),
            equalTo(Uri.of("/a-file.jpg")))
    }

    @Test
    fun `resolve nested asset Uri`() {
        assertThat(asset("/some/other-file.jpg").resolveAssetUri(configuration.assetsPath),
            equalTo(Uri.of("/some/other-file.jpg")))
    }

    private fun source(path: String) = File(configuration.sourcePath.value + path)

    private fun asset(path: String) = File(configuration.assetsPath.value + path)
}