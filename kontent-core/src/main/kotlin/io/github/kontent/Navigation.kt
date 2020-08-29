package io.github.kontent

import org.http4k.core.Uri

data class Navigation(val items: List<NavigationItem>)

data class NavigationItem(
    val name: String,
    val uri: Uri,
    val page: Page? = null,
    val children: List<NavigationItem> = listOf()
)

object NavigationGenerator {
    fun Site.generateNavigation(): Navigation = Navigation(
        pages.exceptRoot()
            .map { it.toNavigationItem() }
            .addIntermediateNavigationItems()
            .sortedBy { it.uri.path }
            .aggregateChildren()
            .toList()
    )

    private fun List<Page>.exceptRoot() = filterNot { it.uri.isRoot() }

    private fun Page.toNavigationItem() = NavigationItem(uri.name(), uri, this)

    private fun List<NavigationItem>.addIntermediateNavigationItems() =
        fold(this) { acc, next -> acc + acc.createMissingNavFor(next.parents()) }

    private fun NavigationItem.parents() = uri.parents().filterNot { it.isRoot() }

    private fun List<NavigationItem>.createMissingNavFor(parents: List<Uri>): List<NavigationItem> =
        parents.map { NavigationItem(it.name(), it, null) }
            .filterNot { candidate -> candidate.uri in map { it.uri } }

    private fun NavigationItem.segments() = uri.path.split("/").filterNot(String::isBlank)

    private fun Uri.name() = path.split("/").filterNot(String::isBlank).last().capitalize().replace("-", " ")

    private fun Uri.parent(): Uri? =
        if (isRoot()) null
        else
            Uri.of("/${path.split("/").filterNot(String::isBlank).dropLast(1).joinToString("/")}")

    private fun Uri.parents(): List<Uri> = parent()?.let { listOf(it) + it.parents() } ?: listOf()

    private fun List<NavigationItem>.aggregateChildren(): List<NavigationItem> =
        filter { it.isTopLevel() }.map { it.copy(children = it.findChildren(this)) }

    private fun NavigationItem.isTopLevel(): Boolean = segments().size == 1

    private fun NavigationItem.findChildren(candidates: List<NavigationItem>): List<NavigationItem> =
        candidates.filter { uri.isParentOf(it.uri) }
            .map { it.copy(children = it.findChildren(candidates)) }

    private fun Uri.isParentOf(candidate: Uri) = candidate != this && candidate.path.startsWith(path) &&
        !candidate.path.replace(path, "").removePrefix("/").contains("/")

    private fun Uri.isRoot() = this == Uri.of("/")
}
