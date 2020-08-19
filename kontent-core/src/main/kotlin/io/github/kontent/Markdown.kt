package io.github.kontent

import io.github.kontent.code.CodeFetcher
import io.github.kontent.code.CodeFetcher.Companion.NoOp
import io.github.kontent.code.CodeLink
import io.github.kontent.code.Pygments
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.commonmark.parser.PostProcessor
import org.commonmark.renderer.html.HtmlRenderer

data class Markdown(val raw: String)

class MarkdownConversion(fetcher: CodeFetcher = NoOp) {
    private val parser: Parser = Parser.builder().postProcessor(CodeFetchingPostProcessor(fetcher)).build()
    fun convert(markdown: Markdown): Html {
        val document: Node = parser.parse(markdown.raw)
        val renderer = HtmlRenderer.builder().build()
        return Html(renderer.render(document))
    }
}

class CodeFetchingPostProcessor(private val fetcher: CodeFetcher) : PostProcessor {
    override fun process(node: Node): Node {
        node.accept(object : AbstractVisitor() {
            override fun visit(node: Paragraph) {
                val firstChild = node.firstChild
                if (firstChild != null && firstChild is HtmlInline) {
                    CodeLink().resolve(firstChild.literal)?.apply {
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
