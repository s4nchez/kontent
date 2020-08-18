package io.github.kontent

import java.io.File

fun Site.exportFiles(targetDirectory: TargetDirectory) {
    assets.forEach {
        val destination = File(targetDirectory.value + "/" + it.uri.path)
        File(it.mapsTo.path).copyTo(destination)
    }
}


data class TargetDirectory(val value: String) : ValidatedPath(value)