# Memory-Refresher [WORK IN PROGRESS]

The goal of this tool is to help organize notes. It's a command-line utility that doesn't have any additional
dependencies (such as a running container or other command line tools), except for a Java runtime.

This program is a work in progress. It's being developed on a machine running Linux (Fedora 34), but it's designed with
the possibility to run in on every platform running JVM in mind.

## How it works

At its core, memory-refresher makes use of [Apache Lucene](https://lucene.apache.org) to create and maintain an index.
The index contains documents made of canonical paths and metadata for full-text search. It does **not** contain a copy
of the file. This allows memory-refresher to keep the index as small as possible.

The key limitation that has to be understood is that index is never automatically refreshed. A change in the filesystem
is not visible to memory-refresher until the selected file is either removed, re-added or refreshed using one of the
available commands.

## Commands

* `add [filename]`, for instance `add myfile.txt` - resolves the canonical path of a file, loads the file into memory
  and adds it to the Lucene index. Subsequent invocations of this command with the same input file will refresh index
  content.
* `remove [filename]`, for instance `remove myfile.txt` - resolves the canonical path of a file and removes it from the
  index. This command is idempotent.
* `refresh` - refreshes the index by sequentially loading all registered files and re-indexing them. Files that have
  been removed from the filesystem are removed from the index in the process.
* `list` - lists canonical paths of all indexed files.

## Future plans

* Add all files from a directory. Currently, it can be achieved with a short bash script, but that's inefficient due to
  JVM startup, JIT and having to open and close index file multiple times.
* Add all files from a directory if their names match a pattern.
* Remove all files in a directory.
* Tags management. Currently, files are only identified by their content. In the future, it should be possible to
  associate file with a tag, mark tags as aliases of each other and filter files by tags.
* Daemon-like mode to allow automatic index refresh in the background