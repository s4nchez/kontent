import org.github.s4nchez.ContentSourcePath
import org.github.s4nchez.Kontent
import org.github.s4nchez.PrintRawOperationalEvents
import org.github.s4nchez.ThemePath
import org.github.s4nchez.asHttpHandler
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent(PrintRawOperationalEvents).build(
            sourcePath = ContentSourcePath("kontent-website/src/main/resources/www/pages"),
            themePath = ThemePath("kontent-theme-default/theme")
    ).asHttpHandler().asServer(SunHttp(8000)).start()
}