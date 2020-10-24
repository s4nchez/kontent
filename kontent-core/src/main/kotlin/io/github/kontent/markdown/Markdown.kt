package io.github.kontent.markdown

import io.github.kontent.Html
import io.github.kontent.MarkdownResult
import io.github.kontent.PageMetadata
import io.github.kontent.code.CodeFetcher
import io.github.kontent.code.CodeFetcher.Companion.NoOp
import io.github.kontent.code.CodeLink
import io.github.kontent.code.Pygments
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.commonmark.parser.PostProcessor
import org.commonmark.renderer.html.HtmlRenderer

data class Markdown(val raw: String)

class MarkdownConversion(fetcher: CodeFetcher = NoOp) {
    private val parser: Parser = Parser.builder()
            .extensions(listOf(YamlFrontMatterExtension.create()))
            .postProcessor(CodeFetchingPostProcessor(fetcher))
            .build()

    fun convert(markdown: Markdown): MarkdownResult {
        val visitor = YamlFrontMatterVisitor()
        val document: Node = parser.parse(markdown.raw)
        document.accept(visitor)
        val renderer = HtmlRenderer.builder().build()
        return MarkdownResult(Html(renderer.render(document)), visitor.data.toPageMetadata())
    }
}

private fun Map<String, List<String>>.toPageMetadata() =
        PageMetadata(
                title = get("title")?.firstOrNull()
        )

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
