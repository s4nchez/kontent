package io.github.kontent

import org.python.util.PythonInterpreter

class Pygments {
    fun highlight(code: String): String {
        val interpreter = PythonInterpreter()

        interpreter.set("code", code)

        interpreter.exec("""
    from pygments import highlight
    from pygments.lexers import KotlinLexer
    from pygments.formatters import HtmlFormatter
    
    result = highlight(code, KotlinLexer(), HtmlFormatter())
    """.trimIndent())

        return interpreter.get("result", String::class.java)
    }

}

fun main() {
    Pygments().highlight("val a = \"foo\";")
}