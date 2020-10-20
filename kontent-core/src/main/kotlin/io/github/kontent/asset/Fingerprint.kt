package io.github.kontent.asset

import org.http4k.core.Uri
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*

class Fingerprint {

    fun generateFingerprintedUri(asset: InputStream, uri: Uri): Uri =
        uri.path(replaceLastFragment(calculateFingerprint(asset), uri.path))

    private fun calculateFingerprint(asset: InputStream): String {
        val md = MessageDigest.getInstance("MD5")
        asset.use { inputStream -> DigestInputStream(inputStream, md).use { } }
        val digest = md.digest()
        return String(Base64.getEncoder().encode(digest)).replace("[=]*$".toRegex(), "")
    }

    private fun replaceLastFragment(fingerprint: String, path: String): String {
        val lastFragment = path.split("/").last()
        val replacement = lastFragment.replace("""([^/\.]*)\.(.*)$""".toRegex()) { result ->
            result.groupValues[1] + "-" + fingerprint + "." + result.groupValues[2]
        }
        return if (path.contains("/")) path.replaceAfterLast("/", replacement) else replacement
    }
}
