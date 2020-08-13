package org.github.s4nchez

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import org.http4k.core.Uri
import java.io.File

class Kontent {
    private val markdownConversion = MarkdownConversion()
    fun build(): Site {
        val handlebars = Handlebars(ClassPathTemplateLoader("/mvp/theme"))
        val template: Template = handlebars.compile("index")

        val pageSources = File("src/main/resources/mvp").walkTopDown().filter { it.name.endsWith(".md") }
                .map {
                    val markdown = Markdown(it.readText())
                    val contentHtml = markdownConversion.convert(markdown)
                    val compiledPage = template.apply(mapOf("content" to contentHtml.raw))
                    Page(Uri.of("/${it.name.removeSuffix(".md")}"), Html(compiledPage))
                }

        return Site(pageSources.toSet())
    }
}


data class Site(val pages: Set<Page>) {
    constructor(vararg pages: Page) : this(pages.toSet())
}

data class Page(val uri: Uri, val content: Html)
data class Html(val raw: String)