package io.github.kontent.markdown

import org.http4k.core.Uri

data class MarkdownSourceFile(val location: Uri, val targetUri: Uri)