package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.kontent.NavigationGenerator.generateNavigation
import io.github.kontent.asset.Assets
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

internal class NavigationTest {

    @Test
    fun `single level`() {
        val pages = listOf(
                page("/"),
                page("/about"),
                page("/code-of-conduct")
        )
        val site = Site(pages, baseUri = Uri.of(""), assets = Assets(listOf()), navigation = pages.generateNavigation())

        assertThat(site.pages.generateNavigation(),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about"), page("/about")),
                NavigationItem("Code of conduct", Uri.of("/code-of-conduct"), page("/code-of-conduct"))
            )))
        )
    }

    @Test
    fun `multi level`() {
        val pages = listOf(
                page("/"),
                page("/docs/foo"),
                page("/docs/bar"),
                page("/about/contact"),
                page("/about/code-of-conduct")
        )
        val site = Site(pages, baseUri = Uri.of(""), assets = Assets(listOf()), navigation = pages.generateNavigation())

        assertThat(site.pages.generateNavigation(),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about"), null, listOf(
                    NavigationItem("Code of conduct", Uri.of("/about/code-of-conduct"), page("/about/code-of-conduct")),
                    NavigationItem("Contact", Uri.of("/about/contact"), page("/about/contact"))
                )),
                NavigationItem("Docs", Uri.of("/docs"), null, listOf(
                    NavigationItem("Bar", Uri.of("/docs/bar"), page("/docs/bar")),
                    NavigationItem("Foo", Uri.of("/docs/foo"), page("/docs/foo"))
                ))
            )))
        )
    }

    @Test
    fun `multi level with intermediate links`() {
        val pages = listOf(
                page("/go/foo"),
                page("/go/foo/b"),
                page("/go/foo/c"),
                page("/stay/foo/d"),
                page("/stay/bar/e"),
                page("/stay/bar/f")
        )
        val site = Site(pages, baseUri = Uri.of(""), assets = Assets(listOf()), navigation = pages.generateNavigation())
        assertThat(site.pages.generateNavigation(),
            equalTo(
                Navigation(listOf(
                    NavigationItem("Go", Uri.of("/go"), null,
                        children = listOf(
                            NavigationItem("Foo", Uri.of("/go/foo"), page("/go/foo"),
                                children = listOf(
                                    NavigationItem("B", Uri.of("/go/foo/b"), page("/go/foo/b")),
                                    NavigationItem("C", Uri.of("/go/foo/c"), page("/go/foo/c"))
                                )
                            ))),
                    NavigationItem("Stay", Uri.of("/stay"), null,
                        children = listOf(
                            NavigationItem("Bar", Uri.of("/stay/bar"), null,
                                children = listOf(
                                    NavigationItem("E", Uri.of("/stay/bar/e"), page("/stay/bar/e")),
                                    NavigationItem("F", Uri.of("/stay/bar/f"), page("/stay/bar/f")))
                            ),
                            NavigationItem("Foo", Uri.of("/stay/foo"), null,
                                children = listOf(NavigationItem("D", Uri.of("/stay/foo/d"), page("/stay/foo/d")))
                            )
                        )
                    ))))
        )

    }

    private fun page(path: String) = Page(Uri.of(path), html)

    companion object {
        val html = Html("<html></html>")
    }
}
