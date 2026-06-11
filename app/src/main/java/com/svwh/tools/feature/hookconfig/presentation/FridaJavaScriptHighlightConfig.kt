package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

internal data class FridaJavaScriptHighlightStyle(
    val keyword: SpanStyle,
    val fridaApi: SpanStyle,
    val string: SpanStyle,
    val number: SpanStyle,
    val comment: SpanStyle,
    val functionName: SpanStyle,
    val punctuation: SpanStyle,
)

internal object FridaJavaScriptHighlightConfig {
    val lightStyle = FridaJavaScriptHighlightStyle(
        keyword = SpanStyle(color = Color(0xFF0033CC), fontWeight = FontWeight.SemiBold),
        fridaApi = SpanStyle(color = Color(0xFF008A2E), fontWeight = FontWeight.SemiBold),
        string = SpanStyle(color = Color(0xFF008A2E)),
        number = SpanStyle(color = Color(0xFF1F2937)),
        comment = SpanStyle(color = Color(0xFF9AA0A6)),
        functionName = SpanStyle(color = Color(0xFF111827)),
        punctuation = SpanStyle(color = Color(0xFF4B5563)),
    )

    val javaScriptKeywords = setOf(
        "async",
        "await",
        "break",
        "case",
        "catch",
        "class",
        "const",
        "continue",
        "debugger",
        "default",
        "delete",
        "do",
        "else",
        "export",
        "extends",
        "false",
        "finally",
        "for",
        "function",
        "if",
        "import",
        "in",
        "instanceof",
        "let",
        "new",
        "null",
        "return",
        "switch",
        "this",
        "throw",
        "true",
        "try",
        "typeof",
        "undefined",
        "var",
        "void",
        "while",
        "yield",
    )

    val fridaApiNames = setOf(
        "Java",
        "Interceptor",
        "Module",
        "Memory",
        "NativeFunction",
        "NativeCallback",
        "Process",
        "Thread",
        "ObjC",
        "DebugSymbol",
        "ptr",
        "send",
        "recv",
        "console",
        "log",
        "perform",
        "use",
        "attach",
        "implementation",
        "findExportByName",
        "readUtf8String",
        "writeUtf8String",
        "allocUtf8String",
        "findBaseAddress",
        "enumerateModules",
    )

    val commentRegex = Regex("""//[^\n]*|/\*[\s\S]*?\*/""")
    val stringRegex = Regex("""(?:"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|`(?:\\.|[^`\\])*`)""")
    val numberRegex = Regex("""\b\d+(?:\.\d+)?\b""")
    val functionDeclarationRegex = Regex("""\bfunction\s+([A-Za-z_$][\w$]*)""")
    val functionCallRegex = Regex("""\b([A-Za-z_$][\w$]*)\s*(?=\()""")
    val keywordRegex = Regex("""\b(${javaScriptKeywords.joinToString("|")})\b""")
    val fridaApiRegex = Regex("""\b(${fridaApiNames.joinToString("|")})\b""")
    val punctuationRegex = Regex("""[{}()\[\].,;:]""")
}
