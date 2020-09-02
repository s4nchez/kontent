package io.github.kontent.markdown

import org.http4k.core.Uri
import java.io.File

data class MarkdownSourceFile(val location: File, val targetUri: Uri)