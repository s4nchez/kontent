package io.github.kontent

import org.http4k.core.Uri

data class Navigation(val items: List<NavigationItem>)

data class NavigationItem(
    val name: String,
    val uri: Uri,
    val page: Page? = null,
    val children: List<NavigationItem> = listOf()
)

object NavigationResolving {
    fun Site.generateNavigation(): Navigation = pages.toNavigation()

    private fun List<Page>.toNavigation() =
        Navigation(filterNot { it.uri.path.replace("/", "").isBlank() }
            .sortedBy { it.uri.path }
            .map { NavigationItem(it.uri.name(), it.uri, it) }
            .addIntermediateNavigationItems()
            .aggregateChildren()
            .toList()
        )

    private fun List<NavigationItem>.addIntermediateNavigationItems(): List<NavigationItem> =
        fold(this) { acc, next -> acc + acc.createMissingNavFor(next.parents()) }

    private fun NavigationItem.parents(): List<Uri> = uri.parents().filterNot { it == Uri.of("/") }

    private fun List<NavigationItem>.createMissingNavFor(parents: List<Uri>): List<NavigationItem> =
        parents.fold(emptyList()) { acc, next ->
            find { it.uri.path == next.path }?.let { acc }
                ?: acc + NavigationItem(next.name(), next, null)
        }

    private fun NavigationItem.segments() = uri.path.split("/").filterNot(String::isBlank)

    private fun Uri.name() = path.split("/").filterNot(String::isBlank).last().capitalize().replace("-", " ")

    private fun Uri.parent(): Uri? =
        if (path == "/") null
        else
            Uri.of("/" + path.split("/").filterNot(String::isBlank).dropLast(1).joinToString("/"))

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
}
