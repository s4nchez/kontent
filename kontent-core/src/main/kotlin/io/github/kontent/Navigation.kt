package io.github.kontent

import org.http4k.core.Uri

data class Navigation(val items: List<NavigationItem>)

data class NavigationItem(
    val name: String,
    val uri: Uri,
    val page: Page? = null,
    val children: List<NavigationItem> = listOf()
) : Comparable<NavigationItem> {
    override fun compareTo(other: NavigationItem): Int = uri.path.compareTo(other.uri.path)
}

object NavigationResolving {
    fun Site.generateNavigation(): Navigation = Navigation(
        pages.exceptRoot()
            .map { it.toNavigationItem() }
            .addIntermediateNavigationItems()
            .sorted()
            .aggregateChildren()
            .toList()
    )

    private fun List<Page>.exceptRoot() = filterNot { it.uri.path.replace("/", "").isBlank() }

    private fun Page.toNavigationItem() = NavigationItem(uri.name(), uri, this)

    private fun List<NavigationItem>.addIntermediateNavigationItems() =
        fold(this) { acc, next -> acc + acc.createMissingNavFor(next.parents()) }

    private fun NavigationItem.parents() = uri.parents().filterNot { it == Uri.of("/") }

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

    private fun Uri.isParentOf(candidate: Uri) = candidate != this && candidate.path.startsWith(path) &&
        !candidate.path.replace(path, "").removePrefix("/").contains("/")

    private fun NavigationItem.findChildren(candidates: List<NavigationItem>): List<NavigationItem> =
        candidates.filter { uri.isParentOf(it.uri) }
            .map { it.copy(children = it.findChildren(candidates)) }

    private fun List<NavigationItem>.aggregateChildren(): List<NavigationItem> =
        if (containsOnlyTopLevel())
            this
        else
            fold(listOf()) { acc, next ->
                if (next.isTopLevel()) {
                    acc + next.copy(children = next.findChildren(this))
                } else acc
            }

    private fun NavigationItem.isTopLevel(): Boolean = segments().size == 1

    private fun List<NavigationItem>.containsOnlyTopLevel(): Boolean = map { it.isTopLevel() }.reduce { acc, next ->
        acc && next
    }
}
