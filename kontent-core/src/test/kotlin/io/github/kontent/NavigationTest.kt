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
                NavigationItem("About", Uri.of("/about"), true, level = 0),
                NavigationItem("Code of conduct", Uri.of("/code-of-conduct"), true, level = 0)
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
                NavigationItem("About", Uri.of("/about"), false, level = 0, children = listOf(
                    NavigationItem("Code of conduct", Uri.of("/about/code-of-conduct"), true, level = 1),
                    NavigationItem("Contact", Uri.of("/about/contact"), true, level = 1)
                )),
                NavigationItem("Docs", Uri.of("/docs"), false, level = 0, children = listOf(
                    NavigationItem("Bar", Uri.of("/docs/bar"), true, level = 1),
                    NavigationItem("Foo", Uri.of("/docs/foo"), true, level = 1)
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
                    NavigationItem("Go", Uri.of("/go"), false, level = 0,
                        children = listOf(
                            NavigationItem("Foo", Uri.of("/go/foo"), true, level = 1,
                                children = listOf(
                                    NavigationItem("B", Uri.of("/go/foo/b"), true, level = 2),
                                    NavigationItem("C", Uri.of("/go/foo/c"), true, level = 2)
                                )
                            ))),
                    NavigationItem("Stay", Uri.of("/stay"), false, level = 0,
                        children = listOf(
                            NavigationItem("Bar", Uri.of("/stay/bar"), false, level = 1,
                                children = listOf(
                                    NavigationItem("E", Uri.of("/stay/bar/e"), true, level = 2),
                                    NavigationItem("F", Uri.of("/stay/bar/f"), true, level = 2))
                            ),
                            NavigationItem("Foo", Uri.of("/stay/foo"), false, level = 1,
                                children = listOf(NavigationItem("D", Uri.of("/stay/foo/d"), true, level = 2))
                            )
                        )
                    ))))
        )

    }

    private fun page(path: String) = Uri.of(path)

}
