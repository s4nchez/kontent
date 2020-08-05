package org.github.s4nchez

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownConversion {
    private val parser: Parser = Parser.builder().build()
    fun convert(markdown: Markdown): Html {
        val document: Node = parser.parse(markdown.raw)
        val renderer = HtmlRenderer.builder().build()
        return Html(renderer.render(document))
    }
}

data class Markdown(val raw: String)