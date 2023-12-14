package me.okonecny.interactivetext

import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import co.touchlab.kermit.Logger

fun Modifier.textInput(
    onInput: (TextInputCommand) -> Unit
): Modifier = composed {
    var textInputSession by remember(onInput) { mutableStateOf<TextInputSession?>(null) }
    val textInputService = LocalTextInputService.current

    if (textInputSession == null && textInputService != null) {
        Logger.d("Start text input.")
        textInputSession = textInputService.startInput(value = TextFieldValue(""),
            imeOptions = ImeOptions.Default,
            onEditCommand = { editCommands ->
                editCommands.forEach { command: EditCommand ->
                    when (command) {
                        is CommitTextCommand -> onInput(Type(command.text))
                        is BackspaceCommand -> onInput(Delete(Delete.Direction.BEFORE_CURSOR, Delete.Size.LETTER))
                        // Is any other command relevant? See subclasses of EditCommand.
                    }
                }
            },
            onImeActionPerformed = { _ -> })
    }
    DisposableEffect(Unit) {
        onDispose {
            val session = textInputSession
            if (session != null && textInputService != null) {
                Logger.d("Stop text input.")
                textInputService.stopInput(session)
                textInputSession = null
            }
        }
    }

    return@composed onKeyEvent { keyEvent: KeyEvent ->
        if (keyEvent.isTypedEvent) {
            onInput(Type(StringBuilder().appendCodePoint(keyEvent.utf16CodePoint).toString()))
        } else if (keyEvent.type == KeyEventType.KeyDown) {
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
}

sealed interface TextInputCommand {
    val needsValidCursor: Boolean
}

data class Type(val text: String) : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data class Delete(val direction: Direction, val size: Size) : TextInputCommand {
    enum class Direction {
        BEFORE_CURSOR, AFTER_CURSOR
    }

    enum class Size {
        LETTER, WORD
    }

    override val needsValidCursor: Boolean = true
}

data class ReplaceRange(
    val sourceRange: TextRange, val newSource: String, val sourceCursorOffset: Int = 0
) : TextInputCommand {
    override val needsValidCursor: Boolean = sourceCursorOffset != 0
}

data object NewLine : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data object Copy : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data object Cut : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data object Paste : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

data object Undo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}

data object Redo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}