package io.github.kontent

import org.python.util.PythonInterpreter

class Pygments {
    private val interpreter by lazy { PythonInterpreter() }

    fun highlight(code: String, language: String = "kotlin"): String {
        interpreter.set("code", code)
        interpreter.set("language", language)

        interpreter.exec("""
    from pygments import highlight
    from pygments.lexers import get_lexer_by_name, TextLexer
    from pygments.formatters import HtmlFormatter
    from pygments.util import ClassNotFound
    
    try:
        lexer = get_lexer_by_name(language, stripall=True)
    except ClassNotFound:
        lexer = TextLexer()
        
    result = highlight(code, lexer, HtmlFormatter())
    """.trimIndent())

        return interpreter.get("result", String::class.java)
    }
}
