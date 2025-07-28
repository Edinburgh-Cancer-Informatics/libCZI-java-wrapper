#!/bin/bash
set -e  # Exit immediately if a command exits with a non-zero status

# Define the output directory relative to the script's location
OUTPUT_DIR="$(dirname "$0")/src/main/resources/native"
LIBRARY_FILE="$OUTPUT_DIR/libCZIAPI.so"

# Check if the library already exists
if [ -f "$LIBRARY_FILE" ]; then
  echo "Native library '$LIBRARY_FILE' already exists. Skipping build."
  exit 0
fi

LIBRARY_FILE="$OUTPUT_DIR/libCZIAPI.so"

# Check if the library already exists
if [ -f "$LIBRARY_FILE" ]; then
  echo "Native library '$LIBRARY_FILE' already exists. Skipping build."
  exit 0
fi

INCLUDE_DIR="$(dirname "$0")/src/main/include"

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"
mkdir -p "$INCLUDE_DIR"

# Get the container ID after building and running it
CONTAINER_ID=$(docker build -t libczi-builder -f Dockerfile.native . && docker run -d libczi-builder)

# Check if the container ID is empty, indicating a build or run failure
if [ -z "$CONTAINER_ID" ]; then
  echo "Error: Docker build or run failed!"
  exit 1
fi

# Copy the library from the container. Adjust paths according to your Dockerfile.
docker cp "$CONTAINER_ID:/build/Src/libCZIAPI/liblibCZIAPI.so" "$OUTPUT_DIR/"

# Copy header files (adjust paths as needed)
docker cp "$CONTAINER_ID:/build/libczi/Src/libCZIAPI/inc/" "$INCLUDE_DIR/"
# You might need to copy other headers as well, depending on your needs.

# Stop and remove the container
docker stop "$CONTAINER_ID"
docker rm "$CONTAINER_ID"

# Fix library name
mv "$OUTPUT_DIR/liblibCZIAPI.so" "$OUTPUT_DIR/libCZIAPI.so"

echo "Successfully built and packaged native library in $OUTPUT_DIR"
