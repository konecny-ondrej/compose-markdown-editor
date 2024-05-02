# Markdown Editor for Compose Multiplatform

Wysiwyg editor component for Markdown text and components to build similar editors.

This repo contains 2 libraries and one app to demonstrate the libs' capabilities.

# Interactive Text

Located in the `interactive-text` are the basic tools, that will enable you to create WYSIWYG editors
for various **text-based** file formats.

## `InteractiveContainer`
This is the glue that makes everything work. It has a few responsibilities:
- Handles cursor navigation using keyboard and pointer.
- Sends text input events.
- Manages visual selection (text that the user sees as selected).
- Contains the all components that constitute the document.
  - You can use any layout you like.
  - You can use any components and mix them with text editable through `InteractiveText`.

## `InteractiveText`
This is the text, that is editable, if placed inside the `InteractiveContainer`.  

## `TextMapping`
Mapping of the visual representation of the document to its source code (and vice-versa).
This is the reason why only supports text-based formats are supported.

## `WysiwygEditor`
Builds on top of all the above to give you a simple framework for building your editor.
Most importantly it handles the source code editing. Just fill your `view` to render the document and start editing.

# Markdown Editor

Component `MarkdownEditor`, that builds on the above `WysiwygEditor`, provides capabilities to edit strings in Markdown
format using the fabulous [flexmark-java](https://github.com/vsch/flexmark-java) library.

## Features:
- Subset of Github-flavored Markdown - especially **tables**.
- Emoji via the `:emoji-name:` codes - e.g. :thumbsup: .
- Simple emoji autocompletion.
- Clickable links to anchors within the document.
- Web links.
- Toolbar with basic formatting tools.
- Embedded images, both local and from the web.

To render the document, `MarkdownEditor` uses the `MarkdownView` component, which you can also use standalone to display
documents that should not be editable.

# Demo App

Demo app is located in the root project. To try it out and play around with `MarkdownEditor`, just run:

```shell
./gradlew :run
```

