package me.okonecny.interactivetext

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.*
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.PlatformTextInputModifierNode
import androidx.compose.ui.platform.establishTextInputSession
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.compositeTextInput(
    onInput: (TextInputCommand) -> Unit
): Modifier = then(object : ModifierNodeElement<MarkdownEditorTextInputModifierNode>() {
    override fun create(): MarkdownEditorTextInputModifierNode = MarkdownEditorTextInputModifierNode(onInput)

    override fun update(node: MarkdownEditorTextInputModifierNode) {
        node.launchInput(onInput)
    }

    override fun hashCode(): Int = onInput.hashCode()

    override fun equals(other: Any?): Boolean =
        if (other is MarkdownEditorTextInputModifierNode) other.onInput == onInput else false
})

data class MarkdownEditorTextInputModifierNode(
    val onInput: (TextInputCommand) -> Unit
) : PlatformTextInputModifierNode, Modifier.Node() {
    private var inputSessionJob: Job? = null

    fun launchInput(handleInput: (TextInputCommand) -> Unit) {
        // TODO: only launch the input session if the editor is focused.
        inputSessionJob = coroutineScope.launch {
            establishTextInputSession {
                startInputMethod(object : PlatformTextInputMethodRequest {
                    @ExperimentalComposeUiApi
                    override val value: () -> TextFieldValue = { TextFieldValue() }

                    @ExperimentalComposeUiApi
                    override val state: TextEditorState = object : TextEditorState {
                        override val selection: TextRange = TextRange.Zero
                        override val composition: TextRange? = null
                        override val length: Int = 0

                        override fun get(index: Int): Char {
                            throw IllegalStateException()
                        }

                        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                            throw IllegalStateException()
                        }
                    }

                    @ExperimentalComposeUiApi
                    override val imeOptions: ImeOptions = ImeOptions.Default

                    @ExperimentalComposeUiApi
                    override val onEditCommand: (List<EditCommand>) -> Unit = { editCommands ->
                        editCommands.forEach { command: EditCommand ->
                            when (command) {
                                is CommitTextCommand -> handleInput(Type(command.text))
                                is BackspaceCommand -> handleInput(
                                    Delete(
                                        Delete.Direction.BEFORE_CURSOR,
                                        Delete.Size.LETTER
                                    )
                                )
                                // Is any other command relevant? See subclasses of EditCommand.
                            }
                        }
                    }

                    @ExperimentalComposeUiApi
                    override val onImeAction: ((ImeAction) -> Unit)? = null

                    @ExperimentalComposeUiApi
                    override val textLayoutResult: () -> TextLayoutResult? = { null }

                    @ExperimentalComposeUiApi
                    override val focusedRectInRoot: () -> Rect? = { null }

                    @ExperimentalComposeUiApi
                    override val textFieldRectInRoot: () -> Rect? = { null }

                    @ExperimentalComposeUiApi
                    override val textClippingRectInRoot: () -> Rect? = { null }

                    @ExperimentalComposeUiApi
                    override val editText: (block: TextEditingScope.() -> Unit) -> Unit = { block ->
                        object : TextEditingScope {
                            // FIXME: emulate the methods properly so the system thinks this is an actual input field.
                            override fun deleteSurroundingTextInCodePoints(
                                lengthBeforeCursor: Int,
                                lengthAfterCursor: Int
                            ) {
                                TODO("Not yet implemented")
                            }

                            override fun commitText(text: CharSequence, newCursorPosition: Int) {
                                if (text.isNotEmpty()) handleInput(Type(text.toString()))
                            }

                            override fun setComposingText(text: CharSequence, newCursorPosition: Int) {
                                TODO("Not yet implemented")
                            }

                            override fun finishComposingText() {
                                TODO("Not yet implemented")
                            }
                        }.block()
//                        textState = TextFieldValue(
//                            text = mutable.text,
//                            selection = mutable.selection,
//                            composition = mutable.composition
//                        )
                    }
                })
            }
        }
    }

}

fun Modifier.textInput(
    onInput: (TextInputCommand) -> Unit
): Modifier = this
    .compositeTextInput(onInput)
    .onKeyEvent { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            val typedChar = keyEvent.utf16CodePoint.toChar()
            if (typedChar > Char.MIN_VALUE && typedChar < Char.MAX_VALUE && !typedChar.isISOControl()) {
                onInput(Type(typedChar.toString()))
                return@onKeyEvent false
            }
            when (keyEvent.key) {
                Key.Backspace -> onInput(
                    Delete(
                        Delete.Direction.BEFORE_CURSOR, if (keyEvent.isCtrlPressed) {
                            Delete.Size.WORD
                        } else {
                            Delete.Size.LETTER
                        }
                    )
                )

                Key.Delete -> onInput(
                    Delete(
                        Delete.Direction.AFTER_CURSOR, if (keyEvent.isCtrlPressed) {
                            Delete.Size.WORD
                        } else {
                            Delete.Size.LETTER
                        }
                    )
                )

                Key.Enter, Key.NumPadEnter -> onInput(NewLine)

                Key.C -> if (keyEvent.isCtrlPressed) onInput(Copy)
                Key.X -> if (keyEvent.isCtrlPressed) onInput(Cut)
                Key.V -> if (keyEvent.isCtrlPressed) onInput(Paste)
                Key.Z -> if (keyEvent.isCtrlPressed) {
                    if (keyEvent.isShiftPressed) {
                        onInput(Redo)
                    } else {
                        onInput(Undo)
                    }
                }
            }
        }
        return@onKeyEvent false
    }

sealed interface TextInputCommand {
    val needsValidCursor: Boolean
}

sealed interface TextEditCommand : TextInputCommand

data class Type(val text: String) : TextInputCommand, TextEditCommand {
    override val needsValidCursor: Boolean = true
}

data class Delete(
    val direction: Direction, val size: Size
) : TextInputCommand, TextEditCommand {
    enum class Direction {
        BEFORE_CURSOR, AFTER_CURSOR
    }

    enum class Size {
        LETTER, WORD
    }

    override val needsValidCursor: Boolean = true
}

data object NewLine : TextInputCommand, TextEditCommand {
    override val needsValidCursor: Boolean = true
}

data object Copy : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data object Cut : TextInputCommand, TextEditCommand {
    override val needsValidCursor: Boolean = true
}

data object Paste : TextInputCommand, TextEditCommand {
    override val needsValidCursor: Boolean = true
}

data object Undo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}

data object Redo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}
