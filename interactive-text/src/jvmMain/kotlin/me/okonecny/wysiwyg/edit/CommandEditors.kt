package me.okonecny.wysiwyg.edit

import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.*
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers
import kotlin.reflect.KClass

data class CommandEditors<D : Any>(
    val unknownCommandEditor: CommandEditor<TextInputCommand, D> = noopCommandEditor(),
    val commandEditors: Map<KClass<*>, CommandEditor<*, D>> = emptyMap(),
    val ignoredCommands: Set<KClass<*>> = emptySet()
) {

    inline fun <reified T : TextInputCommand> withCommandEditor(editor: CommandEditor<T, D>): CommandEditors<D> =
        copy(
            commandEditors = commandEditors + (T::class to editor)
        )

    inline fun <reified T> withIgnoredCommandType(): CommandEditors<D> = copy(
        ignoredCommands = ignoredCommands + T::class
    )

    fun withUnknownCommandEditor(editor: CommandEditor<TextInputCommand, D>): CommandEditors<D> =
        copy(
            unknownCommandEditor = editor
        )

    fun <T : TextInputCommand> forCommand(command: T): CommandEditor<T, D> {
        val commandType = command::class
        return if (ignoredCommands.contains(commandType)) {
            noopCommandEditor()
        } else {
            commandEditors[commandType] as? CommandEditor<T, D> ?: unknownCommandEditor
        }
    }

    companion object {
        private fun <T : TextInputCommand, D : Any> noopCommandEditor(): CommandEditor<T, D> =
            object : CommandEditor<T, D> {
                override fun edit(editorState: WysiwygEditorState<D>, command: T): WysiwygEditorState<D>? {
                    TODO()
                }
            }

        fun <D : Any> basic(
            clipboard: Clipboard,
            clipboardSerializers: VisualNodeSerializers<D, AnnotatedString>
        ): CommandEditors<D> {
            val copyEditor = CopyEditor(clipboard, VisualNodeSerializationContext(clipboardSerializers))
            return CommandEditors<D>()
                .withCommandEditor<Type>(TypeEditor<D>().withUndo())
                .withCommandEditor<NewLine>(TypeNewLineEditor<D>().withUndo())
                .withCommandEditor<Delete>(DeleteEditor<D>().withUndo())
                .withCommandEditor<Copy>(copyEditor)
                .withCommandEditor<Cut>(CutEditor(copyEditor).withUndo())
                .withCommandEditor<Paste>(PlaintextPasteEditor<D>(clipboard).withUndo())
                .withCommandEditor<Undo>(UndoEditor())
                .withCommandEditor<Redo>(RedoEditor())
        }
    }
}
