package io.github.kontent.asset

import io.github.kontent.Asset
import io.github.kontent.AssetPath
import io.github.kontent.AssetsPath
import io.github.kontent.resolveAssetUri
import java.io.File

class AssetsSource(private val assetsPath: AssetsPath) {
    private val fingerprint = Fingerprint()

    fun retrieve() = Assets(File(assetsPath.value).walkTopDown()
            .filterNot { it.isDirectory }
            .map {
                val assetUri = it.resolveAssetUri(assetsPath)
                Asset(assetUri, AssetPath(it.absolutePath),
                        fingerprint.generateFingerprintedUri(File(it.absolutePath).inputStream(), assetUri))
            }
            .toList()
    )
}