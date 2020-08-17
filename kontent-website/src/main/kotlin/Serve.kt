import io.github.kontent.ContentSourcePath
import io.github.kontent.Kontent
import io.github.kontent.PrintRawOperationalEvents
import io.github.kontent.SiteConfiguration
import io.github.kontent.ThemePath
import io.github.kontent.asHttpHandler
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent(SiteConfiguration(
        sourcePath = ContentSourcePath("kontent-website/src/main/resources/www/pages"),
        themePath = ThemePath("kontent-theme-default/theme")),
        events = PrintRawOperationalEvents
    ).asHttpHandler().asServer(SunHttp(8000)).start()
}