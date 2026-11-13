package com.neonull.protocol.syntax

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neonull.protocol.ui.theme.CodeTextStyle
import com.neonull.protocol.ui.theme.NeoColors

/**
 * Custom Python lexical syntax highlighter tailored for the NEO-NULL EXAPUNKS Terminal Deck.
 * - Keywords (`def`, `if`, `else`, `return`, `for`, `class`, `import`, `try`, `except`) -> Cyber Cyan / Matrix Green
 * - Strings (`"..."`, `'...'`, `f"..."`) -> Yellow
 * - Comments (`# ...`) -> Gray
 * - Numbers -> Ice Cyan
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
        "OVERLORD", "SYNTHEX_ALERT", "SYNTAX_ERROR", "BREACH_FAIL",
        "FileNotFoundError", "SyntaxError", "Exception"
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

            // Decorator (@...)
            if (ch == '@') {
                var j = i + 1
                while (j < n && (line[j].isLetterOrDigit() || line[j] == '_' || line[j] == '.')) {
                    j++
                }
                builder.addStyle(
                    style = SpanStyle(color = NeoColors.SyntaxControl, fontWeight = FontWeight.Bold),
                    start = baseOffset + i,
                    end = baseOffset + j
                )
                i = j
                continue
            }

            // f-string / raw string prefix + literal
            val hasStringPrefix = (ch == 'f' || ch == 'r' || ch == 'F' || ch == 'R') &&
                i + 1 < n && (line[i + 1] == '"' || line[i + 1] == '\'')
            if (ch == '"' || ch == '\'' || hasStringPrefix) {
                val startIdx = i
                val quotePos = if (hasStringPrefix) i + 1 else i
                val quote = line[quotePos]
                var j = quotePos + 1
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
                    start = baseOffset + startIdx,
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
                    word.startsWith("__") && word.endsWith("__") -> SpanStyle(
                        color = NeoColors.SyntaxKeyword
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
 * Read-only syntax-highlighted Python code block with EXAPUNKS gutter & optional Crimson error markers.
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
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .width(38.dp)
                .fillMaxHeight()
                .background(NeoColors.GutterSurface)
                .padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            lines.forEachIndexed { idx, _ ->
                val lineNo = idx + 1
                val isErr = lineNo in errorLines
                Text(
                    text = lineNo.toString().padStart(2, '0'),
                    style = CodeTextStyle.copy(
                        color = if (isErr) NeoColors.CrimsonRed else NeoColors.TextMuted,
                        fontWeight = if (isErr) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(NeoColors.BorderSubtle)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScroll)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = highlighted,
                style = CodeTextStyle
            )
        }
    }
}

/**
 * EXAPUNKS-style Split-Screen Code Editor with line number gutter, grid backdrop,
 * live Python syntax highlighting, internal vertical scroll, and Crimson Red error markings.
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
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val hasErrors = errorLines.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(
                width = 1.2.dp,
                color = if (hasErrors) NeoColors.CrimsonRed else accentBorderColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Subtle EXAPUNKS terminal grid lines
        Canvas(modifier = Modifier.matchParentSize()) {
            val gridStep = 21.dp.toPx()
            var y = gridStep
            while (y < size.height) {
                drawLine(
                    color = NeoColors.DeckGridLine.copy(alpha = 0.22f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.8.dp.toPx()
                )
                y += gridStep
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line-number gutter
            Column(
                modifier = Modifier
                    .width(42.dp)
                    .background(NeoColors.GutterSurface.copy(alpha = 0.92f))
                    .padding(vertical = 10.dp, horizontal = 6.dp)
            ) {
                lines.forEachIndexed { idx, _ ->
                    val lineNo = idx + 1
                    val isErr = lineNo in errorLines
                    Text(
                        text = if (isErr) "!${lineNo}" else lineNo.toString().padStart(2, '0'),
                        style = CodeTextStyle.copy(
                            color = if (isErr) NeoColors.CrimsonRed else NeoColors.TextMuted,
                            fontWeight = if (isErr) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(if (hasErrors) NeoColors.CrimsonRed.copy(alpha = 0.5f) else NeoColors.BorderSubtle)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    textStyle = CodeTextStyle,
                    cursorBrush = SolidColor(
                        if (hasErrors) NeoColors.CrimsonRed else NeoColors.MatrixGreen
                    ),
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
