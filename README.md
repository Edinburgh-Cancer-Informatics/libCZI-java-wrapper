# libCZI-java-wrapper

This repository contains a Java wrapper for the libCZI C library, enabling Java applications to read and process CZI (Carl Zeiss Image) files.

## Introduction

This library is far from complete and only currently implements the most basic functionality.  The library now uses pre-packaged by the [QuPath](https://qupath.github.io) team. It is possible to use your own binaries in the system path too.The tests download a test image from [OpenSlide](https://openslide.cs.cmu.edu/download/openslide-testdata/Zeiss/)

## Features (Planned)

*   **Read CZI Files:** Access image data, metadata, and associated information from CZI files.
*   **Multi-dimensional Data Access:** Navigate and extract data from complex multi-dimensional CZI datasets (e.g., time series, Z-stacks, channels).
*   **Metadata Extraction:** Retrieve essential metadata such as image dimensions, pixel type, acquisition parameters, and more.
*   **Native Library Integration:** Seamlessly integrates with the underlying C API using Foreign Function Interface (FFI).


## Prerequisites

*   Java Development Kit (JDK) 22 or higher
*   Maven (for building)

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
    The libCZI is now provided in it own jar from the 

## Usage

Add the generated JAR file as a dependency to your Java project.

Because this library interacts directly with native C code using features that require explicit permission, you must run your Java application with the --enable-native-access flag.

At present, the package only downloadable from our GitHub package repository.  You will need to add ```https://maven.pkg.github.com/Edinburgh-Cancer-Informatics/libCZI-java-wrapper``` to your package repositories.


For example:
```bash
java --enable-native-access=ALL-UNNAMED -jar your-application.jar
```
