package io.github.kontent

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

internal class NavigationTest {

    @Test
    fun `single level`() {
        val site = Site(listOf(
            page("/"),
            page("/about"),
            page("/code-of-conduct")
        ), baseUri = Uri.of(""))

        assertThat(site.generateNavigation(),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about")),
                NavigationItem("Code of conduct", Uri.of("/code-of-conduct"))
            )))
        )
    }

    @Test
    fun `multi level`() {
        val site = Site(listOf(
            page("/"),
            page("/docs/foo"),
            page("/docs/bar"),
            page("/about/contact"),
            page("/about/code-of-conduct")
        ), baseUri = Uri.of(""))

        assertThat(site.generateNavigation(),
            equalTo(Navigation(listOf(
                NavigationItem("About", Uri.of("/about"), listOf(
                    NavigationItem("Code of conduct", Uri.of("/about/code-of-conduct")),
                    NavigationItem("Contact", Uri.of("/about/contact"))
                )),
                NavigationItem("Docs", Uri.of("/docs"), listOf(
                    NavigationItem("Bar", Uri.of("/docs/bar")),
                    NavigationItem("Foo", Uri.of("/docs/foo"))
                ))
            )))
        )
    }

    private fun page(path: String) = Page(Uri.of(path), html)

    companion object {
        val html = Html("<html></html>")
    }
}



data class Navigation(val items: List<NavigationItem>)

data class NavigationItem(val name: String, val uri: Uri?, val children: List<NavigationItem> = listOf())

private fun List<Page>.toNavigation(): Navigation {
    val rootItems =
        filterNot { it.uri.path.replace("/", "").isBlank() }
            .sortedBy { it.uri.path }
            .map { it to NavigationItem(it.uri.path.name(), it.uri) }
            .toMap()
            .aggregateChildren()
            .map(Map.Entry<Page, NavigationItem>::value)
            .toList()
    return Navigation(rootItems)
}

private fun Site.generateNavigation(): Navigation = pages.toNavigation()
private fun Page.segments() = uri.path.split("/").filterNot(String::isBlank)
private fun String.name() = split("/").filterNot(String::isBlank).last().capitalize().replace("-", " ")
private fun Page.parent() = "/" + uri.path.split("/").filterNot(String::isBlank).dropLast(1).joinToString("/")

private fun Map<Page, NavigationItem>.aggregateChildren(): Map<Page, NavigationItem> =
    if (keys.map { it.segments().size }.max() == 1)
        this
    else
        entries.fold(mapOf<Page, NavigationItem>()) { acc, d: Map.Entry<Page, NavigationItem> ->
            val (path, nextItem) = d
            if (path.segments().size == 1)
                acc + (path to nextItem)
            else {
                val parent = path.parent()
                acc + (Page(Uri.of(parent), Html("dummy")) to acc.findOrCreateParent(parent).addChild(nextItem))
            }
        }.aggregateChildren()

private fun Map<Page, NavigationItem>.findOrCreateParent(parent: String): NavigationItem =
    keys.find { it.uri.path == parent }?.let { get(it) } ?: NavigationItem(parent.name(), Uri.of(parent))

private fun NavigationItem.addChild(navigationItem: NavigationItem) =
    copy(children = children + navigationItem)
