package io.github.kontent

import org.http4k.core.Uri
import java.io.File


data class SiteConfiguration(
    val sourcePath: ContentSourcePath,
    val themePath: ThemePath,
    val assetsPath: AssetsPath,
    val baseUri: Uri = Uri.of(""),
    val standalonePages: Set<PageSource> = setOf(),
    val urlMappings: Map<Uri, Uri> = mapOf()
)

data class ContentSourcePath(val value: String) : ValidatedPath(value)

data class PageSource(val uri: Uri, val sourcePath: String) : ValidatedPath(sourcePath)

data class AssetsPath(val value: String) : ValidatedPath(value)

data class ThemePath(val value: String) : ValidatedPath(value)

open class ValidatedPath(val path: String) {
    init {
        if (!File(path).exists()) throw IllegalArgumentException("path ${File(path).absolutePath} does not exist")
    }
}
