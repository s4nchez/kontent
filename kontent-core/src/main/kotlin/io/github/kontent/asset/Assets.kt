package io.github.kontent.asset

import io.github.kontent.Asset
import java.io.File

class Assets(private val assets: List<Asset>) : Iterable<Asset> by assets {

    val size: Int = assets.size

    private val fingerprint = Fingerprint()

    private val uriWithFingerprintsToAsset = assets.map {
        fingerprint.generateFingerprintedUri(File(it.mapsTo.value).inputStream(), it.uri).path to it
    }.toMap()


    private val assetToFingerprint = uriWithFingerprintsToAsset.entries.map { (k, v) -> v.uri.path to k }.toMap()

    fun withFingerprint(context: String): String? = assetToFingerprint[context]

    fun assetsWithFingerprint() = uriWithFingerprintsToAsset
}