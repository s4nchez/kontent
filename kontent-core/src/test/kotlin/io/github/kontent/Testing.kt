package io.github.kontent

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
