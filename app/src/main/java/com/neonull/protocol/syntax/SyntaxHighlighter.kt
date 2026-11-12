package com.neonull.protocol.syntax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.neonull.protocol.ui.theme.CodeTextStyle
import com.neonull.protocol.ui.theme.NeoColors

/**
 * Lightweight lexical Python syntax highlighter for NEO-NULL.
 * - Keywords (`def`, `if`, `else`, `return`, `for`, `class`, etc.) -> Cyber Cyan / Matrix Green
 * - Strings (`"..."`, `'...'`) -> Yellow
 * - Comments (`# ...`) -> Gray
 * - Numbers -> Soft Cyan
 * - Error tokens / flagged identifiers / lines in errorLines -> Crimson Red (#DC143C)
 */
object PythonSyntaxHighlighter {

    private val primaryKeywords = setOf(
        "def", "class", "return", "if", "elif", "else", "for", "while",
        "in", "try", "except", "finally", "with", "as", "import", "from",
        "pass", "break", "continue", "raise", "lambda", "yield", "global"
    )

    private val logicAndBoolKeywords = setOf(
        "and", "or", "not", "is", "True", "False", "None", "self"
    )

    private val builtins = setOf(
        "print", "len", "range", "open", "int", "str", "float", "bool",
        "list", "dict", "set", "tuple", "sum", "min", "max", "enumerate",
        "sorted", "type", "isinstance", "super", "input"
    )

    private val crimsonAlertIdentifiers = setOf(
        "OVERLORD", "SYNTHEX_ALERT", "SYNTAX_ERROR", "BREACH_FAIL"
    )

    fun highlight(code: String, errorLines: Set<Int> = emptySet()): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            val lines = code.split("\n")
            var lineStartOffset = 0

            lines.forEachIndexed { lineIdx, line ->
                val lineNumber = lineIdx + 1
                val lineEndOffset = lineStartOffset + line.length

                if (lineNumber in errorLines && line.isNotEmpty()) {
                    addStyle(
                        style = SpanStyle(
                            background = NeoColors.CrimsonRedGlow,
                            color = NeoColors.CrimsonRed,
                            fontWeight = FontWeight.SemiBold
                        ),
                        start = lineStartOffset,
                        end = lineEndOffset
                    )
                }

                tokenizeLine(line, lineStartOffset, this)
                lineStartOffset = lineEndOffset + 1 // +1 for '\n'
            }
        }
    }

    private fun tokenizeLine(
        line: String,
        baseOffset: Int,
        builder: AnnotatedString.Builder
    ) {
        var i = 0
        val n = line.length
        while (i < n) {
            val ch = line[i]

            // Comment (# ...)
            if (ch == '#') {
                builder.addStyle(
                    style = SpanStyle(color = NeoColors.SyntaxComment),
                    start = baseOffset + i,
                    end = baseOffset + n
                )
                return
            }

            // String literal ("..." or '...')
            if (ch == '"' || ch == '\'') {
                val quote = ch
                var j = i + 1
                while (j < n) {
                    if (line[j] == '\\' && j + 1 < n) {
                        j += 2
                        continue
                    }
                    if (line[j] == quote) {
                        j++
                        break
                    }
                    j++
                }
                builder.addStyle(
                    style = SpanStyle(color = NeoColors.SyntaxString),
                    start = baseOffset + i,
                    end = baseOffset + j
                )
                i = j
                continue
            }

            // Identifiers & keywords
            if (ch.isLetter() || ch == '_') {
                var j = i + 1
                while (j < n && (line[j].isLetterOrDigit() || line[j] == '_')) {
                    j++
                }
                val word = line.substring(i, j)
                val spanStyle = when {
                    word in crimsonAlertIdentifiers -> SpanStyle(
                        color = NeoColors.SyntaxAlert,
                        fontWeight = FontWeight.Bold
                    )
                    word in primaryKeywords -> SpanStyle(
                        color = NeoColors.SyntaxKeyword,
                        fontWeight = FontWeight.Bold
                    )
                    word in logicAndBoolKeywords -> SpanStyle(
                        color = NeoColors.SyntaxControl,
                        fontWeight = FontWeight.SemiBold
                    )
                    word in builtins -> SpanStyle(
                        color = NeoColors.SyntaxControl
                    )
                    else -> null
                }
                if (spanStyle != null) {
                    builder.addStyle(spanStyle, baseOffset + i, baseOffset + j)
                }
                i = j
                continue
            }

            // Numeric literals
            if (ch.isDigit()) {
                var j = i + 1
                while (j < n && (line[j].isDigit() || line[j] == '.')) {
                    j++
                }
                builder.addStyle(
                    style = SpanStyle(color = NeoColors.SyntaxNumber),
                    start = baseOffset + i,
                    end = baseOffset + j
                )
                i = j
                continue
            }

            i++
        }
    }
}

class PythonSyntaxVisualTransformation(
    private val errorLines: Set<Int> = emptySet()
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = PythonSyntaxHighlighter.highlight(text.text, errorLines)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}

/**
 * Read-only syntax-highlighted Python code block with line numbers and optional error line markers.
 */
@Composable
fun SyntaxHighlightedCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    errorLines: Set<Int> = emptySet(),
    borderColor: Color = NeoColors.BorderSubtle
) {
    val lines = remember(code) { code.split("\n") }
    val highlighted = remember(code, errorLines) {
        PythonSyntaxHighlighter.highlight(code, errorLines)
    }
    val horizontalScroll = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.width(28.dp)) {
            lines.forEachIndexed { idx, _ ->
                val lineNo = idx + 1
                val isErr = lineNo in errorLines
                Text(
                    text = lineNo.toString(),
                    style = CodeTextStyle.copy(
                        color = if (isErr) NeoColors.CrimsonRed else NeoColors.TextMuted,
                        fontWeight = if (isErr) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            Text(
                text = highlighted,
                style = CodeTextStyle
            )
        }
    }
}

/**
 * Interactive Python code editor with line numbers, live syntax highlighting, and Crimson Red error markings.
 */
@Composable
fun SyntaxHighlightedCodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorLines: Set<Int> = emptySet(),
    accentBorderColor: Color = NeoColors.CyberCyan
) {
    val lines = remember(code) { code.split("\n").ifEmpty { listOf("") } }
    val visualTransformation = remember(errorLines) {
        PythonSyntaxVisualTransformation(errorLines)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(
                width = 1.dp,
                color = if (errorLines.isNotEmpty()) NeoColors.CrimsonRed else accentBorderColor.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.width(30.dp)) {
            lines.forEachIndexed { idx, _ ->
                val lineNo = idx + 1
                val isErr = lineNo in errorLines
                Text(
                    text = if (isErr) "!$lineNo" else "$lineNo",
                    style = CodeTextStyle.copy(
                        color = if (isErr) NeoColors.CrimsonRed else NeoColors.TextMuted,
                        fontWeight = if (isErr) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            textStyle = CodeTextStyle,
            cursorBrush = SolidColor(
                if (errorLines.isNotEmpty()) NeoColors.CrimsonRed else NeoColors.MatrixGreen
            ),
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
