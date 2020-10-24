package io.github.kontent.templating

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import io.github.kontent.Html
import io.github.kontent.Navigation
import io.github.kontent.Page
import io.github.kontent.PageModel
import io.github.kontent.ThemePath
import io.github.kontent.asset.Assets
import io.github.kontent.code.HttpCodeFetcher
import io.github.kontent.markdown.Markdown
import io.github.kontent.markdown.MarkdownConversion
import org.http4k.client.JavaHttpClient
import org.http4k.core.Uri

class HandlebarsTemplating(themePath: ThemePath, assets: Assets, private val navigation: Navigation) {

    private val markdownConversion = MarkdownConversion(HttpCodeFetcher(JavaHttpClient()))

    private val handlebars = Handlebars(FileTemplateLoader(themePath.value)).apply {
        registerHelper("asset", Helper<String> { path, _ -> assets.findByPath(path)?.uriWithFingerprint })
        infiniteLoops(true)
    }

    private val template: Template = handlebars.compile("index")

    fun renderPage(targetUri: Uri, markdown: Markdown): Page {
        val result = markdownConversion.convert(markdown)
        val pageModel = PageModel(
            content = result.html.raw,
            nav = navigation,
            title = result.metadata.title
        )
        val compiledPage = template.apply(pageModel)
        return Page(targetUri, Html(compiledPage))
    }
}