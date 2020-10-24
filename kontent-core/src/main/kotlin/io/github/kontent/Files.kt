package io.github.kontent

import org.http4k.core.Uri
import java.io.File

fun Site.exportFiles(targetDirectory: TargetDirectory) {
    assets.forEach {
        val destination = File(targetDirectory.value + "/" + it.uriWithFingerprint.path)
        File(it.mapsTo.path).copyTo(destination)
    }

    pages.forEach {
        val destination = File(targetDirectory.value + "/" + it.uri.path + "/index.html")
        destination.parentFile?.mkdirs()
        destination.createNewFile()
        destination.writeText(it.content.raw)
    }
}

data class TargetDirectory(val value: String) : ValidatedPath(value)

fun File.resolveAssetUri(assetsPath: AssetsPath) = Uri.of(relativePath(assetsPath))

fun File.relativePath(basePath: ValidatedPath) =
        "/" + this.path.replace(basePath.path, "").replace("^[/]*".toRegex(), "")
