package io.github.kontent

import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

class KontentTest {

    @Test
    fun `minimal site with single page`() {
        val site = Kontent(configuration).build()

        assertThat(site.pages.first(), matchesPageUri(Uri.of("/my-page")))
    }

    @Test
    fun `include static content`() {
        val site = Kontent(configuration).build()

        assertThat(site.assets.first(), matchesAssetUri(Uri.of("/css/main.css")))
    }

    @Test
    fun `include reference to standalone pages outside content directory`() {
        val source = PageSource(Uri.of("/random"), "src/test/resources/mvp/standalone-page/standalone.md")

        val site = Kontent(configuration.copy(standalonePages = setOf(source))).build()

        assertThat(site.pages, anyElement(matchesPageUri(Uri.of("/random"))))
    }

    @Test
    fun `redefine a page URI`() {
        val mappings = mapOf(Uri.of("/my-page") to Uri.of("/"))
        val site = Kontent(configuration.copy(urlMappings = mappings)).build()

        assertThat(site.pages.first(), matchesPageUri(Uri.of("/")))
    }
}
