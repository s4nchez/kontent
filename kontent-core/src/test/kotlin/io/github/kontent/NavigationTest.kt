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
                NavigationItem("About", null, listOf(
                    NavigationItem("Code of conduct", Uri.of("/about/code-of-conduct")),
                    NavigationItem("Contact", Uri.of("/about/contact"))
                )),
                NavigationItem("Docs", null, listOf(
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
            .map { PagePath(it.uri.path, it.uri) to NavigationItem(it.uri.path.name(), it.uri) }
            .toMap()
            .aggregateChildren()
            .map(Map.Entry<PagePath, NavigationItem>::value)
            .toList()
    return Navigation(rootItems)
}

data class PagePath(val path: String, val target: Uri? = null)

private fun Site.generateNavigation(): Navigation = pages.toNavigation()
private fun PagePath.segments() = path.split("/").filterNot(String::isBlank)
private fun String.name() = split("/").filterNot(String::isBlank).last().capitalize().replace("-", " ")
private fun PagePath.parent() = "/" + path.split("/").filterNot(String::isBlank).dropLast(1).joinToString("/")

private fun Map<PagePath, NavigationItem>.aggregateChildren(): Map<PagePath, NavigationItem> =
    if (keys.map { it.segments().size }.max() == 1)
        this
    else
        entries.fold(mapOf<PagePath, NavigationItem>()) { acc, d: Map.Entry<PagePath, NavigationItem> ->
            val (path, nextItem) = d
            if (path.segments().size == 1)
                acc + (path to nextItem)
            else {
                val parent = path.parent()
                acc + (PagePath(parent) to acc.findOrCreateParent(parent).addChild(nextItem))
            }
        }.aggregateChildren()

private fun Map<PagePath, NavigationItem>.findOrCreateParent(parent: String): NavigationItem =
    keys.find { it.path == parent }?.let { get(it) } ?: NavigationItem(parent.name(), null)

private fun NavigationItem.addChild(navigationItem: NavigationItem) =
    copy(children = children + navigationItem)
