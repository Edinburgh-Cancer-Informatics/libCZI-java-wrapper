/**
 * Defines the module for the libczi-wrapper library.
 */
module uk.ac.ed.eci.libczi.wrapper {
    // This makes the public classes in the uk.ac.ed.eci.libCZI package
    // accessible to any other module that 'requires' this one.
    exports uk.ac.ed.eci.libCZI;
    exports uk.ac.ed.eci.libCZI.bitmaps;

    // Required for JSON processing with Jackson.
    // 'transitive' is used because some exported classes are annotated for Jackson,
    // and consumers may want to perform their own JSON operations.
    // This also transitively requires com.fasterxml.jackson.annotation and com.fasterxml.jackson.core.
    requires transitive com.fasterxml.jackson.databind;

    // The 'opens' directive allows the Jackson databind library to use reflection
    // on the classes within the uk.ac.ed.eci.libCZI package at runtime.
    // This is necessary for Jackson to serialize/deserialize the POJOs.
    opens uk.ac.ed.eci.libCZI to com.fasterxml.jackson.databind;
}
