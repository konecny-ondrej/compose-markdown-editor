package me.okonecny.interactivetext

import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
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
        textInputSession = textInputService.startInput(
            value = TextFieldValue(""),
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
            onImeActionPerformed = { _ -> }
        )
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
                @OptIn(ExperimentalComposeUiApi::class)
                Key.Backspace -> onInput(
                    Delete(
                        Delete.Direction.BEFORE_CURSOR,
                        if (keyEvent.isCtrlPressed) {
                            Delete.Size.WORD
                        } else {
                            Delete.Size.LETTER
                        }
                    )
                )

                @OptIn(ExperimentalComposeUiApi::class)
                Key.Delete -> onInput(
                    Delete(
                        Delete.Direction.AFTER_CURSOR,
                        if (keyEvent.isCtrlPressed) {
                            Delete.Size.WORD
                        } else {
                            Delete.Size.LETTER
                        }
                    )
                )

                @OptIn(ExperimentalComposeUiApi::class)
                Key.Enter,
                @OptIn(ExperimentalComposeUiApi::class)
                Key.NumPadEnter -> onInput(NewLine)

                @OptIn(ExperimentalComposeUiApi::class)
                Key.C -> if (keyEvent.isCtrlPressed) onInput(Copy)

                @OptIn(ExperimentalComposeUiApi::class)
                Key.V -> if (keyEvent.isCtrlPressed) onInput(Paste)

                @OptIn(ExperimentalComposeUiApi::class)
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
    val sourceRange: TextRange,
    val newSource: String
) : TextInputCommand {
    override val needsValidCursor: Boolean = false
}

object NewLine : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

object Copy : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

object Paste : TextInputCommand {
    override val needsValidCursor: Boolean = true
}

object Undo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}

object Redo : TextInputCommand {
    override val needsValidCursor: Boolean = false
}