package mywebsite

import io.github.kontent.AssetsPath
import io.github.kontent.ContentSourcePath
import io.github.kontent.Kontent
import io.github.kontent.SiteConfiguration
import io.github.kontent.ThemePath
import io.github.kontent.asHttpHandler
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent(SiteConfiguration(
        sourcePath = ContentSourcePath("src/main/resources/mvp"),
        themePath = ThemePath("kontent-theme-default/theme"),
        assetsPath = AssetsPath("../kontent-theme-default/assets")
    )
    ).build().asHttpHandler().asServer(SunHttp(8000)).start()
}