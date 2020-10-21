package io.github.kontent.code

import org.http4k.core.Uri
import org.jsoup.Jsoup

class CodeLink {
    fun resolve(literal: String): FetchParameters? {
        val html = Jsoup.parse(literal)
        val link = html.selectFirst("a")
        if (link == null || link.attr("data-kontent-fetch") != "github") return null
        val fetchLang = link.attr("data-kontent-lang") ?: ""
        val sanitisedUrl = link.attr("href")
            .replace("github.com", "raw.githubusercontent.com")
            .replace("/blob/", "/")
        return FetchParameters(Uri.of(sanitisedUrl), fetchLang)
    }
}

data class FetchParameters(val uri: Uri, val language: String)
