import io.github.kontent.AssetSourcePath
import io.github.kontent.ContentSourcePath
import io.github.kontent.Kontent
import io.github.kontent.PageSource
import io.github.kontent.PrintRawOperationalEvents
import io.github.kontent.SiteConfiguration
import io.github.kontent.ThemePath
import io.github.kontent.asHttpHandler
import org.http4k.core.Uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent(SiteConfiguration(
        sourcePath = ContentSourcePath("kontent-website/www"),
        themePath = ThemePath("kontent-website/www/theme"),
        assertSourcePath = AssetSourcePath("kontent-website/www/assets"),
        standalonePages = setOf(PageSource(Uri.of("/"), "README.md"))
    ),
        events = PrintRawOperationalEvents
    ).asHttpHandler().asServer(SunHttp(8000)).start()
}