package io.github.kontent

import io.github.kontent.CodeFetcher.Companion.NoOp
import io.github.kontent.code.Pygments
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.commonmark.parser.PostProcessor
import org.commonmark.renderer.html.HtmlRenderer
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.jsoup.Jsoup

class MarkdownConversion(fetcher: CodeFetcher = NoOp) {
    private val parser: Parser = Parser.builder().postProcessor(CodeFetchingPostProcessor(fetcher)).build()
    fun convert(markdown: Markdown): Html {
        val document: Node = parser.parse(markdown.raw)
        val renderer = HtmlRenderer.builder().build()
        return Html(renderer.render(document))
    }
}

data class Markdown(val raw: String)

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

class CodeFetchingPostProcessor(private val fetcher: CodeFetcher) : PostProcessor {
    override fun process(node: Node): Node {
        node.accept(object : AbstractVisitor() {
            override fun visit(node: Paragraph) {
                val firstChild = node.firstChild
                if (firstChild != null && firstChild is HtmlInline) {
                    firstChild.fetchParameters()?.apply {
                        val newNode = HtmlInline().apply {
                            literal = fetcher.fetch(uri)?.let { Pygments().highlight(it, language) }
                        }
                        node.appendChild(firstChild)
                        node.insertAfter(newNode)
                        node.unlink()
                    }
                }
            }
        })
        return node
    }
}

private fun HtmlInline.fetchParameters(): FetchParameters? {
    val html = Jsoup.parse(literal)
    val link = html.selectFirst("a")
    if (link.attr("data-kontent-fetch") != "github") return null
    val fetchLang = link.attr("data-kontent-lang") ?: ""
    val sanitisedUrl = link.attr("href")
        .replace("github.com", "raw.githubusercontent.com")
        .replace("/blob/", "/")
    return FetchParameters(Uri.of(sanitisedUrl), fetchLang)
}

data class FetchParameters(val uri: Uri, val language: String)
