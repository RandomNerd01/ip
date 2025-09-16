# MeowTheCat — User Guide 🐱

**Chatbot name:** **MeowTheCat**

## Overview

MeowTheCat is a simple task chatbot for adding/listing/managing **todos**, **deadlines**, and **events**. This guide only explains how to *use* the bot.

## Quick usage

* **Date format:** `yyyy-MM-dd` (e.g. `2025-06-06`).

### Commands (case-insensitive)

* `todo <description>` — add a todo.
* `deadline <description> /by <yyyy-MM-dd>` — add a deadline.
* `event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>` — add an event.
* `list` — show all tasks (numbered).
* `mark <n>` — mark task number `n` done.
* `unmark <n>` — mark task number `n` not done.
* `delete <n>` — remove task number `n`.
* `find <keyword>` — show tasks containing `keyword`.
* `clear` — remove all tasks.
* `undo` — undo the last mutating action (single-level).
* `bye` — exit the app.

### Examples

* `todo buy milk`
* `deadline submit report /by 2025-09-30`
* `event meeting /from 2025-10-01 /to 2025-10-01`