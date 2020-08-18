# Kontent

Kontent is an opinionated website generator written in Kotlin.

Some principles are:
* Self-contained: it should not require any other tools (npm, for instance) to get a website from source to production.
* User friendly: all you need to get started is your source plus a single command.
* Clear split between content and layout/theme.
* Flexible runtime: generate static files for basics, or run it as a server for added features.
* GitHub as CMS: content can be edited directly in GH interface, and a website can refer to existing markdown/code.  

Technologies of choice:
* Language/runtime: Kotlin
* Templating: Handlebars
* Content creation: Markdown