package uk.ac.ed.eci.libCZI.document;

import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Optional;

import uk.ac.ed.eci.libCZI.IInterop;

public class ScalingInfo implements IInterop {
    private final Optional<Double> scaleX;
    private final Optional<Double> scaleY;
    private final Optional<Double> scaleZ;

    public ScalingInfo(double scaleX, double scaleY, double scaleZ) {
        this.scaleX = Double.isNaN(scaleX) ? Optional.empty() : Optional.of(scaleX);
        this.scaleY = Double.isNaN(scaleY) ? Optional.empty() : Optional.of(scaleY);
        this.scaleZ = Double.isNaN(scaleZ) ? Optional.empty() : Optional.of(scaleZ);
    }

    public Optional<Double> scaleX() {
        return scaleX;
    }

    public Optional<Double> scaleY() {
        return scaleY;
    }

    public Optional<Double> scaleZ() {
        return scaleZ;
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        JAVA_DOUBLE.withName("scale_x"),
        JAVA_DOUBLE.withName("scale_y"),
        JAVA_DOUBLE.withName("scale_z")
    );

    public static ScalingInfo createFromMemorySegment(MemorySegment segment) {
        return new ScalingInfo(
            segment.get(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_x"))),
            segment.get(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_y"))),
            segment.get(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_z")))
        );
    }

    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        MemorySegment segment = arena.allocate(LAYOUT);
        segment.set(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_x")), scaleX.orElse(Double.NaN));
        segment.set(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_y")), scaleY.orElse(Double.NaN));
        segment.set(JAVA_DOUBLE, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("scale_z")), scaleZ.orElse(Double.NaN));
        return segment;
    }
    
}
