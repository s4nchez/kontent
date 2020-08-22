package io.github.kontent

import com.natpryce.hamkrest.ValueDescription
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.Uri

val configuration = SiteConfiguration(
    sourcePath = ContentSourcePath("src/test/resources/mvp/pages"),
    themePath = ThemePath("../kontent-theme-default/theme"),
    assertSourcePath = AssetSourcePath("../kontent-theme-default/assets"),
    baseUri = Uri.of("https://example.org")
)

fun matchesPageUri(expected: Uri) = has(Page::uri, equalTo(expected))

fun matchesAssetUri(expected: Uri) = has(Asset::uri, equalTo(expected))

class NavigationDescription : ValueDescription {
    override fun describe(v: Any?) = (v as? Navigation)?.betterToString()

    private fun Navigation.betterToString() = "\n" + items.joinToString("") { it.toString(0) }

    private fun NavigationItem.toString(level: Int): String {
        return """${(0..level * 4).map { " " }.joinToString("")}- '$name': ${uri.path.let { "'$it'" }} (page=${page != null})
            |${children.joinToString("") { it.toString(level + 1) }}
        """.trimMargin()
    }
}