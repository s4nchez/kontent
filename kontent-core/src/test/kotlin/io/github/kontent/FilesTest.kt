package io.github.kontent

import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
class FilesTest {

    @Test
    fun `exports files correctly`(approver: Approver) {
        val site = Kontent(configuration).build()

        val target = createTempDir("kontent-export")

        site.exportFiles(TargetDirectory(target.path))

        val files = target.walkTopDown()
            .filterNot { it.isDirectory }
            .sorted()
            .map { it.path.replace(target.path, "") to it.readText() }

        val summary = files.map(Pair<String, String>::first).joinToString("\n")

        val details = files.joinToString("\n") {
            """===============
            |file:${it.first}
            |content:
            |${it.second}
            """.trimMargin("|")
        }

        approver.assertApproved(Response(OK).body(summary + "\n" + details))

    }

}