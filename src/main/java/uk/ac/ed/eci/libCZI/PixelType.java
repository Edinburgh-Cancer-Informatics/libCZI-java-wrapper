package uk.ac.ed.eci.libCZI;

public enum PixelType {
    Invalid(0xFF),
    Gray8(0),
    Gray16(1),
    Gray32Float(2),
    Bgr24(3),
    Bgr48(4),
    Bgr96Float(8),
    Bgra32(9), // Currently not supported in libCZI.
    Gray64ComplexFloat(10), // Currently not supported in libCZI.
    Bgr192ComplexFloat(11), // Currently not supported in libCZI.
    Gray32(12), // Currently not supported in libCZI.
    Gray64Float(13); // Currently not supported in libCZI.

    private final int value;

    PixelType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Optional: A static method to get an enum by its value, similar to C++ casting
    public static PixelType fromValue(int value) {
        for (PixelType type : PixelType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        // You might want to throw an IllegalArgumentException here or return null
        // depending on how you want to handle unknown values.
        throw new IllegalArgumentException("Unknown PixelType value: " + value);
    }

}
