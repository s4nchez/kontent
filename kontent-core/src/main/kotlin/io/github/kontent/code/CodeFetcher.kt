package io.github.kontent.code

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri

interface CodeFetcher {
    fun fetch(codeUrl: Uri): String?

    companion object {
        val NoOp = object : CodeFetcher {
            override fun fetch(codeUrl: Uri): String? = null
        }
    }
}

class HttpCodeFetcher(val client: HttpHandler) : CodeFetcher {
    override fun fetch(codeUrl: Uri) = client(Request(Method.GET, codeUrl)).let {
        if (it.status.successful) it.bodyString() else null
    }
}