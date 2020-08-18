import io.github.kontent.AssetSourcePath
import io.github.kontent.ContentSourcePath
import io.github.kontent.Kontent
import io.github.kontent.PageSource
import io.github.kontent.PrintRawOperationalEvents
import io.github.kontent.SiteConfiguration
import io.github.kontent.TargetDirectory
import io.github.kontent.ThemePath
import io.github.kontent.asHttpHandler
import io.github.kontent.exportFiles
import org.http4k.core.Uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.io.File


private val site = Kontent(
    SiteConfiguration(
        sourcePath = ContentSourcePath("kontent-website/www"),
        themePath = ThemePath("kontent-website/www/theme"),
        assertSourcePath = AssetSourcePath("kontent-website/www/assets"),
        standalonePages = setOf(PageSource(Uri.of("/"), "README.md"))
    ), events = PrintRawOperationalEvents)

class Build {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            site.build().exportFiles(TargetDirectory("kontent-website/build/website").ensureExists())
        }

        private fun TargetDirectory.ensureExists(): TargetDirectory = apply {
            File(path).apply { if (exists()) deleteRecursively() else mkdirs() }
        }
    }
}

class Serve {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            site.asHttpHandler().asServer(SunHttp(8000)).start()
        }
    }
}
