package io.github.kontent.asset

import io.github.kontent.Asset

class Assets(private val assets: List<Asset>) : Iterable<Asset> by assets {

    val size: Int = assets.size

    private val uriWithFingerprintsToAsset = assets.map { it.uriWithFingerprint.path to it }.toMap()

    fun findByPath(path: String): Asset? = assets.find { it.uri.path == path }

    fun assetsWithFingerprint() = uriWithFingerprintsToAsset
}