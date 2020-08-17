package mywebsite

import org.github.s4nchez.ContentSourcePath
import org.github.s4nchez.Kontent
import org.github.s4nchez.SiteConfiguration
import org.github.s4nchez.ThemePath
import org.github.s4nchez.asHttpHandler
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Kontent(SiteConfiguration(
            sourcePath = ContentSourcePath("src/main/resources/mvp"),
            themePath = ThemePath("kontent-theme-default/theme"))
    ).build().asHttpHandler().asServer(SunHttp(8000)).start()
}