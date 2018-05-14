#!/bin/sh

# arguments are passed directly to the java program.
# usage:
# Day15 [-t] [URL]
# -t: no terminal mode. If the program does not support
#     a non-blocking terminal, use this to fall-back
#     on a regular scanner.
#     otherwise we have an involved input system with ANSI codes.
# URL: optional URL to read words from. Otherwise, we use the wordlist
#     on the assignment page.

javac -cp jline.jar:. Day15.java
java -cp jline.jar:. Day15 "$@"
