# Introduction

## What is Markdown?

Markdown is a plain text format for writing structured documents,
based on conventions used for indicating formatting in email and
usenet posts. It was developed in 2004 by John Gruber, who wrote
the first Markdown-to-HTML converter in perl, and it soon became
widely used in websites. By 2014 there were dozens of
implementations in many languages. Some of them extended basic
Markdown syntax with conventions for footnotes, definition lists,
tables, and other constructs, and some allowed output not just in
HTML but in LaTeX and many other formats.

## Why is a spec needed?

John Gruber's [canonical description of Markdown's
syntax](http://daringfireball.net/projects/markdown/syntax)
does not specify the syntax unambiguously. Here are some examples of
questions it does not answer:

1. How much indentation is needed for a sublist? The spec says that
   continuation paragraphs need to be indented four spaces, but is
   not fully explicit about sublists. It is natural to think that
   they, too, must be indented four spaces, but `Markdown.pl` does
   not require that. This is hardly a "corner case," and divergences
   between implementations on this issue often lead to surprises for
   users in real documents. (See [this comment by John
   Gruber](http://article.gmane.org/gmane.text.markdown.general/1997).)

2. Is a blank line needed before a block quote or header?
   Most implementations do not require the blank line. However,
   this can lead to unexpected results in hard-wrapped text, and
   also to ambiguities in parsing (note that some implementations
   put the header inside the blockquote, while others do not).
   (John Gruber has also spoken [in favor of requiring the blank
   lines](http://article.gmane.org/gmane.text.markdown.general/2146).)

3. Is a blank line needed before an indented code block?
   (`Markdown.pl` requires it, but this is not mentioned in the
   documentation, and some implementations do not require it.)

   ``` markdown
   paragraph
       code?
   ```

4. What is the exact rule for determining when list items get
   wrapped in `<p>` tags? Can a list be partially "loose" and partially
   "tight"? What should we do with a list like this?

   ``` markdown
   1. one

   2. two
   3. three
   ```

   Or this?

   ``` markdown
   1.  one
       - a

       - b
   2.  two
   ```

   (There are some relevant comments by John Gruber
   [here](http://article.gmane.org/gmane.text.markdown.general/2554).)

5. Can list markers be indented? Can ordered list markers be right-aligned?

   ``` markdown
    8. item 1
    9. item 2
   10. item 2a
   ```

6. Is this one list with a horizontal rule in its second item,
   or two lists separated by a horizontal rule?

   ``` markdown
   * a
   * * * * *
   * b
   ```

7. When list markers change from numbers to bullets, do we have
   two lists or one?  (The Markdown syntax description suggests two,
   but the perl scripts and many other implementations produce one.)

   ``` markdown
   1. fee
   2. fie
   -  foe
   -  fum
   ```

8. What are the precedence rules for the markers of inline structure?
   For example, is the following a valid link, or does the code span
   take precedence ?

   ``` markdown
   [a backtick (`)](/url) and [another backtick (`)](/url).
   ```

9. What are the precedence rules for markers of emphasis and strong
   emphasis? For example, how should the following be parsed?

   ``` markdown
   *foo *bar* baz*
   ```

10. What are the precedence rules between block-level and inline-level
    structure? For example, how should the following be parsed?

    ``` markdown
    - `a long code span can contain a hyphen like this
      - and it can screw things up`
    ```

11. Can list items include section headers?  (`Markdown.pl` does not
    allow this, but does allow blockquotes to include headers.)

    ``` markdown
    - # Heading
    ```

12. Can list items be empty?

    ``` markdown
    * a
    *
    * b
    ```

13. Can link references be defined inside block quotes or list items?

    ``` markdown
    > Blockquote [foo].
    >
    > [foo]: /url
    ```

14. If there are multiple definitions for the same reference, which takes
    precedence?

    ``` markdown
    [foo]: /url1
    [foo]: /url2

    [foo][]
    ```

In the absence of a spec, early implementers consulted `Markdown.pl`
to resolve these ambiguities. But `Markdown.pl` was quite buggy, and
gave manifestly bad results in many cases, so it was not a
satisfactory replacement for a spec.

Because there is no unambiguous spec, implementations have diverged
considerably. As a result, users are often surprised to find that
a document that renders one way on one system (say, a github wiki)
renders differently on another (say, converting to docbook using
pandoc). To make matters worse, because nothing in Markdown counts
as a "syntax error," the divergence often isn't discovered right away.

## About this document

This document attempts to specify Markdown syntax unambiguously.
It contains many examples with side-by-side Markdown and
HTML. These are intended to double as conformance tests. An
accompanying script `spec_tests.py` can be used to run the tests
against any Markdown program:

    python test/spec_tests.py --spec spec.txt --program PROGRAM

Since this document describes how Markdown is to be parsed into
an abstract syntax tree, it would have made sense to use an abstract
representation of the syntax tree instead of HTML. But HTML is capable
of representing the structural distinctions we need to make, and the
choice of HTML for the tests makes it possible to run the tests against
an implementation without writing an abstract syntax tree renderer.

This document is generated from a text file, `spec.txt`, written
in Markdown with a small extension for the side-by-side tests.
The script `tools/makespec.py` can be used to convert `spec.txt` into
HTML or CommonMark (which can then be converted into other formats).

In the examples, the `→` character is used to represent tabs.