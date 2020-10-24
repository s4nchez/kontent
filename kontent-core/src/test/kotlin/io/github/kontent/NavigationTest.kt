package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.kontent.NavigationGenerator.generateNavigation
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

        assertThat(generateNavigation(pages),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about"), true),
                NavigationItem("Code of conduct", Uri.of("/code-of-conduct"), true)
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

        assertThat(generateNavigation(pages),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about"), false, listOf(
                    NavigationItem("Code of conduct", Uri.of("/about/code-of-conduct"), true),
                    NavigationItem("Contact", Uri.of("/about/contact"), true)
                )),
                NavigationItem("Docs", Uri.of("/docs"), false, listOf(
                    NavigationItem("Bar", Uri.of("/docs/bar"), true),
                    NavigationItem("Foo", Uri.of("/docs/foo"), true)
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
        assertThat(generateNavigation(pages),
            equalTo(
                Navigation(listOf(
                    NavigationItem("Go", Uri.of("/go"), false,
                        children = listOf(
                            NavigationItem("Foo", Uri.of("/go/foo"), true,
                                children = listOf(
                                    NavigationItem("B", Uri.of("/go/foo/b"), true),
                                    NavigationItem("C", Uri.of("/go/foo/c"), true)
                                )
                            ))),
                    NavigationItem("Stay", Uri.of("/stay"), false,
                        children = listOf(
                            NavigationItem("Bar", Uri.of("/stay/bar"), false,
                                children = listOf(
                                    NavigationItem("E", Uri.of("/stay/bar/e"), true),
                                    NavigationItem("F", Uri.of("/stay/bar/f"), true))
                            ),
                            NavigationItem("Foo", Uri.of("/stay/foo"), false,
                                children = listOf(NavigationItem("D", Uri.of("/stay/foo/d"), true))
                            )
                        )
                    ))))
        )

    }

    private fun page(path: String) = Uri.of(path)

}
