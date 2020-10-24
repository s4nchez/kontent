package io.github.kontent.asset

import io.github.kontent.Asset

class Assets(private val assets: List<Asset>) : Iterable<Asset> by assets {

    val size: Int = assets.size

    fun findByPath(path: String): Asset? = assets.find { it.uri.path == path }
}