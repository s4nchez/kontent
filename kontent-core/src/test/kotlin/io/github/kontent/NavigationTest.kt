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
                NavigationItem("About", Uri.of("/about"), page("/about")),
                NavigationItem("Code of conduct", Uri.of("/code-of-conduct"), page("/code-of-conduct"))
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
        assertThat(Site(listOf(
            page("/go/foo"),
            page("/go/foo/b"),
            page("/go/foo/c"),
            page("/stay/foo/d"),
            page("/stay/bar/e"),
            page("/stay/bar/f")
        ), baseUri = Uri.of("")).generateNavigation(),
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

data class Navigation(val items: List<NavigationItem>)

data class NavigationItem(val name: String, val uri: Uri, val page: Page? = null, val children: List<NavigationItem> = listOf())

private fun List<Page>.toNavigation() =
    Navigation(filterNot { it.uri.path.replace("/", "").isBlank() }
        .sortedBy { it.uri.path }
        .map { NavigationItem(it.uri.path.name(), it.uri, it) }
        .addIntermediateNavigationItems()
        .aggregateChildren()
        .toList()
    )

private fun List<NavigationItem>.addIntermediateNavigationItems(): List<NavigationItem> =
    fold(this) { acc, next -> acc + acc.createMissingNavFor(next.parents()) }

private fun NavigationItem.parents(): List<String> = uri.parents().filterNot { it == Uri.of("/") }.map { it.path }

fun List<NavigationItem>.createMissingNavFor(parents: List<String>): List<NavigationItem> =
    parents.fold(emptyList()) { acc, next ->
        find { it.uri.path == next }?.let { acc }
            ?: acc + NavigationItem(next.name(), Uri.of(next), null)
    }

private fun Site.generateNavigation(): Navigation = pages.toNavigation()
private fun NavigationItem.segments() = uri.path.split("/").filterNot(String::isBlank)
private fun String.name() = split("/").filterNot(String::isBlank).last().capitalize().replace("-", " ")

private fun Uri.parent(): Uri? =
    if (path == "/") null else Uri.of("/" + path.split("/").filterNot(String::isBlank).dropLast(1).joinToString("/"))

private fun Uri.parents(): List<Uri> = parent()?.let { listOf(it) + it.parents() } ?: listOf()

private fun Uri.isParentOf(candidate: Uri): Boolean = candidate != this &&
    candidate.path.startsWith(path) &&
    !candidate.path.replace(path, "").removePrefix("/").contains("/")

private fun NavigationItem.findChildren(candidates: List<NavigationItem>): List<NavigationItem> =
    candidates.filter { uri.isParentOf(it.uri) }
        .map { it.copy(children = it.findChildren(candidates)) }

private fun List<NavigationItem>.aggregateChildren(): List<NavigationItem> =
    if (map { it.segments().size }.max() == 1)
        this
    else
        fold(listOf()) { acc, d: NavigationItem ->
            if (d.segments().size == 1) {
                acc + d.copy(children = d.findChildren(this))
            } else acc
        }
