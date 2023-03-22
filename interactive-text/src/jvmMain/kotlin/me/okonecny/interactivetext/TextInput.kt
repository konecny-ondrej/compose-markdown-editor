package me.okonecny.interactivetext

import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.*
import co.touchlab.kermit.Logger

internal fun Modifier.textInput(
    enabled: Boolean,
    onInput: (TextInputCommand) -> Unit
): Modifier = composed {
    var textInputSession by remember { mutableStateOf<TextInputSession?>(null) }
    val textInputService = LocalTextInputService.current

    if (textInputSession == null && textInputService != null && enabled) {
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
    val session = textInputSession
    if (session != null && !enabled && textInputService != null) {
        Logger.d("Stop text input.")
        textInputService.stopInput(session)
        textInputSession = null
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
            }
        }
        return@onKeyEvent false
    }
}

sealed interface TextInputCommand
data class Type(val text: String) : TextInputCommand
data class Delete(val direction: Direction, val size: Size) : TextInputCommand {
    enum class Direction {
        BEFORE_CURSOR, AFTER_CURSOR
    }

    enum class Size {
        LETTER, WORD
    }

}

object NewLine : TextInputCommand
object Copy : TextInputCommand
object Paste : TextInputCommand