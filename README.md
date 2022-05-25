# alfred-snippets

A Windows Alfred-snippets-like interactive command-line tools based on alfred configuration files synced from macOS.

### Dependencies

* [Ditto](https://ditto-cp.sourceforge.io/) A clipboard manager on Windows, similar to Alfred's clipboard manager.
* JDK & Maven

### Configuration, Build and Startup

Step 1: Override the default configuration in [application.properties](src/main/resources/application.properties) with your own.

```properties
# Ditto Sqlite DB filepath
alfred-snippets.ditto-db-filepath=D:/opt/ditto/Ditto.db
# Alfred snippets configuration dir, which sync from macOS by tools like Dropbox
alfred-snippets.alfred-snippets-configuration-dir=c:/Users/${USERNAME}/Dropbox/alfred/Alfred.alfredpreferences/snippets
```

Step 2: Build.

```bash
# build with Maven
./build.sh
```

Step 3: Startup.

```bash
# startup interactive command-line with Java
./startup.sh
```

### Usage

Usage 1: Search using the specified PREFIX and CMD.

```
shell:> snippet PREFIX CMD
```

Usage 2: Fuzzy search using CMD.

```
shell:> snippet ? CMD
```

Tips: Press TAB while typing PREFIX and CMD to give suggestions.