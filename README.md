# libCZI-java-wrapper

This repository contains a Java wrapper for the libCZI C library, enabling Java applications to read and process CZI (Carl Zeiss Image) files.

## Introduction

This library is far from complete and only currently implements the most basic functionality.  The library is currently being developed on Linux and makes certain expections in the build that docker compatible environment is available.  It will build the C library in a container if the library is not available and then download a test image from [OpenSlide](https://openslide.cs.cmu.edu/download/openslide-testdata/Zeiss/)

The native library is expected to be called `CZIAPI` mapped through the Java `mapLibraryName` method.
## Features (Planned)

*   **Read CZI Files:** Access image data, metadata, and associated information from CZI files.
*   **Multi-dimensional Data Access:** Navigate and extract data from complex multi-dimensional CZI datasets (e.g., time series, Z-stacks, channels).
*   **Metadata Extraction:** Retrieve essential metadata such as image dimensions, pixel type, acquisition parameters, and more.
*   **Native Library Integration:** Seamlessly integrates with the underlying C API using Foreign Function Interface (FFI).


## Prerequisites

*   Java Development Kit (JDK) 22 or higher
*   Maven (for building)
*   libCZI C library (pre-built binaries or build from source)

## Building

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/libCZI-java-wrapper.git
    cd libCZI-java-wrapper
    ```

2.  **Build the Java wrapper:**
    ```bash
    mvn clean install
    ```
    This will compile the Java code and generate a JAR file in the `target/` directory.

3.  **Ensure libCZI native library is accessible:**
    The `libCZI` native library (e.g., `libczi.so` on Linux, `libczi.dylib` on macOS, `czi.dll` on Windows) must be available on your system's library path or specified via the `java.library.path` system property when running your Java application.

    *   **Linux/macOS:** Place `libczi.so` or `libczi.dylib` in a standard library path (e.g., `/usr/local/lib`) or add its directory to `LD_LIBRARY_PATH` (Linux) or `DYLD_LIBRARY_PATH` (macOS).
    *   **Windows:** Place `czi.dll` in a directory included in your system's `PATH` environment variable.

## Usage

Add the generated JAR file as a dependency to your Java project.

Because this library interacts directly with native C code using features that require explicit permission, you must run your Java application with the --enable-native-access flag.

For example:
```bash
java --enable-native-access=ALL-UNNAMED -jar your-application.jar
```
