package io.github.kontent.asset

import io.github.kontent.Asset
import io.github.kontent.AssetPath
import io.github.kontent.AssetsPath
import io.github.kontent.resolveAssetUri
import java.io.File

interface SystemAssetsSource {
    fun retrieve(): Assets
}

class FileSystemAssetsSource(private val assetsPath: AssetsPath) : SystemAssetsSource {
    private val fingerprint = Fingerprint()

    override fun retrieve() = Assets(File(assetsPath.value).walkTopDown()
            .filterNot { it.isDirectory }
            .map {
                val assetUri = it.resolveAssetUri(assetsPath)
                Asset(assetUri, AssetPath(it.absolutePath),
                        fingerprint.generateFingerprintedUri(File(it.absolutePath).inputStream(), assetUri))
            }
            .toList()
    )
}