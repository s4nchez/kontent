package io.github.kontent

import org.http4k.core.Uri
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail

@ExtendWith(ApprovalTest::class)
class ThemeTest {

    val site = Kontent(
            configuration.copy(
                    sourcePath = ContentSourcePath("src/test/resources/test-theme/pages"),
                    themePath = ThemePath("src/test/resources/test-theme/theme")
            )
    )

    @Test
    fun `renders basic content`(approver: Approver) {
        val homePage = site.build().pages.find { it.uri== Uri.of("/")  } ?: fail("page not found")
        approver.assertApproved(homePage.content.raw)
    }
}