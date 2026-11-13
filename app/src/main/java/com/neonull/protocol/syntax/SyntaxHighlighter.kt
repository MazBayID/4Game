package com.neonull.protocol.syntax

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextRange
import androidx.compose.ui.unit.sp
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
    val focusRequester = remember { FocusRequester() }

    var editorValue by remember {
        mutableStateOf(TextFieldValue(text = code, selection = TextRange(code.length)))
    }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }

    LaunchedEffect(code) {
        if (editorValue.text != code) {
            val cursor = editorValue.selection.end.coerceIn(0, code.length)
            editorValue = TextFieldValue(text = code, selection = TextRange(cursor))
        }
    }

    fun updateText(nextText: String, cursor: Int) {
        val safeCursor = cursor.coerceIn(0, nextText.length)
        editorValue = TextFieldValue(nextText, TextRange(safeCursor))
        onCodeChange(nextText)
    }

    fun setCursor(target: Int) {
        val safeTarget = target.coerceIn(0, editorValue.text.length)
        val selection = if (shiftActive) {
            TextRange(editorValue.selection.start, safeTarget)
        } else {
            TextRange(safeTarget)
        }
        editorValue = editorValue.copy(selection = selection)
    }

    fun moveHorizontal(direction: Int) {
        val text = editorValue.text
        var target = editorValue.selection.end.coerceIn(0, text.length)
        when {
            altActive -> {
                target = if (direction < 0) {
                    text.lastIndexOf('\n', (target - 1).coerceAtLeast(0)) + 1
                } else {
                    val lineEnd = text.indexOf('\n', target)
                    if (lineEnd < 0) text.length else lineEnd
                }
            }
            ctrlActive && direction < 0 -> {
                while (target > 0 && text[target - 1].isWhitespace()) target--
                while (target > 0 && !text[target - 1].isWhitespace()) target--
            }
            ctrlActive && direction > 0 -> {
                while (target < text.length && !text[target].isWhitespace()) target++
                while (target < text.length && text[target].isWhitespace()) target++
            }
            else -> target += direction
        }
        setCursor(target)
    }

    fun moveVertical(direction: Int) {
        val text = editorValue.text
        val cursor = editorValue.selection.end.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)) + 1
        val column = cursor - lineStart
        val target = if (direction < 0) {
            if (lineStart == 0) return
            val previousEnd = lineStart - 1
            val previousStart = text.lastIndexOf('\n', (previousEnd - 1).coerceAtLeast(0)) + 1
            (previousStart + column).coerceAtMost(previousEnd)
        } else {
            val currentEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
            if (currentEnd == text.length) return
            val nextStart = currentEnd + 1
            val nextEnd = text.indexOf('\n', nextStart).let { if (it < 0) text.length else it }
            (nextStart + column).coerceAtMost(nextEnd)
        }
        setCursor(target)
    }

    fun insertText(insert: String) {
        val selection = editorValue.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)
        val nextText = editorValue.text.replaceRange(start, end, insert)
        updateText(nextText, start + insert.length)
    }

    fun deleteBackward() {
        val selection = editorValue.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)
        if (start != end) {
            updateText(editorValue.text.removeRange(start, end), start)
        } else if (start > 0) {
            updateText(editorValue.text.removeRange(start - 1, start), start - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(
                width = 1.2.dp,
                color = if (hasErrors) NeoColors.CrimsonRed else accentBorderColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(bottom = 4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val editorMinHeight = (maxHeight - 18.dp).coerceAtLeast(120.dp)

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
                        .background(
                            if (hasErrors) NeoColors.CrimsonRed.copy(alpha = 0.5f)
                            else NeoColors.BorderSubtle
                        )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = editorValue,
                        onValueChange = { next ->
                            editorValue = next
                            onCodeChange(next.text)
                        },
                        onTextLayout = { textLayoutResult = it },
                        textStyle = CodeTextStyle,
                        cursorBrush = SolidColor(
                            if (hasErrors) NeoColors.CrimsonRed else NeoColors.MatrixGreen
                        ),
                        visualTransformation = visualTransformation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = editorMinHeight)
                            .focusRequester(focusRequester)
                            .pointerInput(textLayoutResult, editorValue.text) {
                                detectTapGestures { tapPosition ->
                                    focusRequester.requestFocus()
                                    val offset = textLayoutResult
                                        ?.getOffsetForPosition(tapPosition)
                                        ?.coerceIn(0, editorValue.text.length)
                                    if (offset != null) {
                                        editorValue = editorValue.copy(selection = TextRange(offset))
                                    }
                                }
                            }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorKeyButton("TAB") { insertText("    ") }
            EditorKeyButton("CTRL", ctrlActive) { ctrlActive = !ctrlActive }
            EditorKeyButton("ALT", altActive) { altActive = !altActive }
            EditorKeyButton("SHIFT", shiftActive) { shiftActive = !shiftActive }

            IconButton(onClick = { moveHorizontal(-1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Move cursor left", tint = NeoColors.CyberCyan)
            }
            IconButton(onClick = { moveVertical(-1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move cursor up", tint = NeoColors.CyberCyan)
            }
            IconButton(onClick = { moveVertical(1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move cursor down", tint = NeoColors.CyberCyan)
            }
            IconButton(onClick = { moveHorizontal(1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Move cursor right", tint = NeoColors.CyberCyan)
            }

            EditorKeyButton("HOME") {
                val cursor = editorValue.selection.end
                setCursor(editorValue.text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)) + 1)
            }
            EditorKeyButton("END") {
                val cursor = editorValue.selection.end
                val lineEnd = editorValue.text.indexOf('\n', cursor)
                setCursor(if (lineEnd < 0) editorValue.text.length else lineEnd)
            }
            EditorKeyButton("BKSP") { deleteBackward() }
            EditorKeyButton("ENTER") { insertText("\n") }
            EditorKeyButton("A") {
                if (ctrlActive) {
                    editorValue = editorValue.copy(selection = TextRange(0, editorValue.text.length))
                    ctrlActive = false
                } else {
                    insertText("a")
                }
            }
        }
    }
}

@Composable
private fun EditorKeyButton(
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(5.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (active) NeoColors.ObsidianVoid else NeoColors.CyberCyan,
            containerColor = if (active) NeoColors.MatrixGreen else NeoColors.TerminalSurface
        )
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
