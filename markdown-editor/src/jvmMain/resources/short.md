# Demo

Based on [this cheatsheet][cheatsheet]

This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this
is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.


---

## Emoji :100:

- By name: :ok: :grin: :+1: :boy|type_1_2:
- Unicode: 🇧🇭
- Emoji within link [this 🇧🇭 :ok: cheatsheet][cheatsheet]

---

## Headers
---

# Header 1

## Header 2

### Header 3

#### Header 4

##### Header 5

###### Header 6
---

## Emphasis

Emphasis, aka italics, with *asterisks* or _underscores_.

Strong emphasis, aka bold, with **asterisks** or __underscores__.

Combined emphasis with **asterisks and _underscores_**.

---

## Lists

1. First ordered list item
2. Another item
    * Unordered sub-list.
1. Actual numbers don't matter, just that it's a number
    1. Ordered sub-list
4. And another item.
5. [ ] Open task

   You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at
   least one, but we'll use three here to also align the raw Markdown).

   To have a line break without a paragraph, you will need to use two trailing spaces.
   Note that this line is separate, but within the same paragraph.
   (This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)

* Unordered list can use asterisks

- Or minuses

+ Or pluses
+ [x] Done task

---

## Links

[I'm an inline-style link](https://www.google.com)

[I'm a reference-style link][Arbitrary case-insensitive reference text]

[I'm a relative reference to a repository file](../blob/master/LICENSE)

[You can use numbers for reference-style link definitions][1]

Or leave it empty and use the [link text itself].

Test link to [Header 3](#header-3)

Link with [empty URL]().

Test reference link to [Header 2][h2link]

[h2link]: #header-2

Autolink option will detect text links like https://www.google.com and turn them into Markdown links automatically.

---

## Code

Inline `code` has `back-ticks around` it.

```javascript
var s = "JavaScript syntax highlighting";
alert(s);
```

```python
s = "Python syntax highlighting"
print s
```

```java
/**
 * Helper method to obtain a Parser with registered strike-through &amp; table extensions
 * &amp; task lists (added in 1.0.1)
 *
 * @return a Parser instance that is supported by this library
 * @since 1.0.0
 */
@NonNull
class ParserFactory {
    public static Parser createParser() {
        return new Parser.Builder()
                .extensions(Arrays.asList(
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TaskListExtension.create()
                ))
                .build();
    }
}
```

```xml

<ScrollView
        android:id="@+id/scroll_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="?android:attr/actionBarSize">

    <TextView
            android:id="@+id/text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dip"
            android:lineSpacingExtra="2dip"
            android:textSize="16sp"
            tools:text="yo\nman"/>

</ScrollView>
```

```
No language indicated, so no syntax highlighting.
But let's throw in a <b>tag</b>.
```

---

## Images

Inline-style:

![random image](https://picsum.photos/seed/picsum/400/400 "Text 1") ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1.5")
![local image](flower.jpg "Some flowers loaded from disk")
![bad image](https://example.com "Text 1.5")

Reference-style:

![random image][logo]

[logo]: https://picsum.photos/seed/picsum2/400/400 "Text 2"

---

## Tables

Colons can be used to align columns.

| Tables        |      Are      |       Cool |
|---------------|:-------------:|-----------:|
| col 3 is      | right-aligned | ${'$'}1600 |
| col 2 is      |   centered    |   ${'$'}12 |
| zebra stripes |   are neat    |    ${'$'}1 |

There must be at least 3 dashes separating each header cell.
The outer pipes (|) are optional, and you don't need to make the
raw Markdown line up prettily. You can also use inline Markdown.

 Markdown | Less      | Pretty                                                              
----------|-----------|---------------------------------------------------------------------
 *Still*  | `renders` | ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1") 
 1        | 2         | 3                                                                   

---

## Blockquotes

> Blockquotes are very handy in email to emulate reply text.
> This line is part of the same quote.

Quote break.

> This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this
> is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.

Nested quotes
> Hello!
>> And to you!

---

## Inline HTML

```html
<u><i>H<sup>T<sub>M</sub></sup><b><s>L</s></b></i></u>
```

<body><u><i>H<sup>T<sub>M</sub></sup><b><s>L</s></b></i></u></body>

---

## Horizontal Rule

Three or more...

---

Hyphens (`-`)

***

Asterisks (`*`)

___

Underscores (`_`)

## License

```
  Copyright 2019 Dimitry Ivanov (legal@noties.io)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```

[cheatsheet]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet

[arbitrary case-insensitive reference text]: https://www.mozilla.org

[1]: http://slashdot.org

[link text itself]: http://www.reddit.com