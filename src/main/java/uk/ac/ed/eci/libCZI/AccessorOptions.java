package uk.ac.ed.eci.libCZI;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;

/**
 * A class representing options for accessing CZI files.
   For more info, see https://zeiss.github.io/libczi/api/struct_accessor_options_interop.html
   Defaults are white for the background (1,1,1)
   Sort by M-index false
   Use visibility check optimization true
 */

public class AccessorOptions implements IInterop {
    // Fields reflect snake_case given in API documentation from Zeiss
    private final float backGroundColorR;
    private final float backGroundColorG;
    private final float backGroundColorB;
    private final boolean sortByM;
    private final boolean useVisibilityCheckOptimization;
    private final String additionalParameters;

    private AccessorOptions(float backGroundColorR, float backGroundColorG, float backGroundColorB, boolean sortByM, boolean useVisibilityCheckOptimization, String additionalParameters) {
        this.backGroundColorR = backGroundColorR;
        this.backGroundColorG = backGroundColorG;
        this.backGroundColorB = backGroundColorB;
        this.sortByM = sortByM;
        this.useVisibilityCheckOptimization = useVisibilityCheckOptimization;
        this.additionalParameters = additionalParameters;
    }

    private AccessorOptions(Builder builder) {
        this.backGroundColorR = builder.backGroundColorR;
        this.backGroundColorG = builder.backGroundColorG;
        this.backGroundColorB = builder.backGroundColorB;
        this.sortByM = builder.sortByM;
        this.useVisibilityCheckOptimization = builder.useVisibilityCheckOptimization;
        this.additionalParameters = builder.additionalParameters;
    }

    public static class Builder {
        private float backGroundColorR = 1;
        private float backGroundColorG = 1;
        private float backGroundColorB = 1;
        private boolean sortByM = false;
        private boolean useVisibilityCheckOptimization = true;
        private String additionalParameters = "";

        public AccessorOptions build() {
            return new AccessorOptions(this);
        }
        
        public Builder background(float f) {
            this.backGroundColorR = f;
            this.backGroundColorG = f;
            this.backGroundColorB = f;

            return this;
        }
        public Builder backgroundR(float f) {
            this.backGroundColorR = f;

            return this;
        }
        public Builder backgroundG(float f) {
            this.backGroundColorG = f;

            return this;
        }
        public Builder backgroundB(float f) {
            this.backGroundColorB = f;

            return this;
        }
        public Builder sortByM(boolean val) {
            this.sortByM = val;

            return this;
        }
        public Builder useVisibilityCheckOptimization(boolean val) {
            this.useVisibilityCheckOptimization = val;

            return this;
        }
        public Builder additionalParameters(String params) {
            this.additionalParameters = params;

            return this;
        }
    }

    public Builder builder() {
        return new Builder();
    }

    public static MemoryLayout layout() {
        return MemoryLayout.structLayout(
            JAVA_FLOAT.withName("back_ground_color_r"),
            JAVA_FLOAT.withName("back_ground_color_g"),
            JAVA_FLOAT.withName("back_ground_color_b"),
            JAVA_BOOLEAN.withName("sort_by_m"),
            JAVA_BOOLEAN.withName("use_visibility_check_optimization"),
            MemoryLayout.paddingLayout(2),
            ADDRESS.withName("additional_parameters")
        );
    }

    public static AccessorOptions createFromMemorySegment(MemorySegment segment) {
        MemorySegment cString = segment.get(ADDRESS, layout().byteOffset(PathElement.groupElement("additional_parameters")));
        String additonalParams = cString.reinterpret(Long.MAX_VALUE).getString(0);
        LibCziFFM.free(cString);
        return new AccessorOptions(
            segment.get(JAVA_FLOAT, layout().byteOffset(PathElement.groupElement("back_ground_color_r"))),
            segment.get(JAVA_FLOAT, layout().byteOffset(PathElement.groupElement("back_ground_color_g"))),
            segment.get(JAVA_FLOAT, layout().byteOffset(PathElement.groupElement("back_ground_color_b"))),
            segment.get(JAVA_BOOLEAN, layout().byteOffset(PathElement.groupElement("sort_by_m"))),
            segment.get(JAVA_BOOLEAN, layout().byteOffset(PathElement.groupElement("use_visibility_check_optimization"))),
            additonalParams
        );   
    }

    public MemorySegment toMemorySegment(Arena arena) {
        MemorySegment segment = arena.allocate(layout());
        MemorySegment cString;
        if (additionalParameters == null) {
            cString = MemorySegment.NULL;
        } else {
            cString = arena.allocate(MemoryLayout.sequenceLayout(additionalParameters.length(), JAVA_BYTE));
        }
        
        segment.set(JAVA_FLOAT, 0, backGroundColorR);
        segment.set(JAVA_FLOAT, 4, backGroundColorG);
        segment.set(JAVA_FLOAT, 8, backGroundColorB);
        segment.set(JAVA_BOOLEAN, 12, sortByM); 
        segment.set(JAVA_BOOLEAN, 13, useVisibilityCheckOptimization);
        segment.set(ADDRESS, 16, cString);
        return segment;
    }

    public float backGroundColorR() {
        return backGroundColorR;
    }

    public float backGroundColorG() {
        return backGroundColorG;
    }

    public float backGroundColorB() {   
        return backGroundColorB;
    }

    public boolean sortByM() {
        return sortByM;
    }

    public boolean useVisibilityCheckOptimization() {
        return useVisibilityCheckOptimization;
    }

    public String additionalParameters() {
        return additionalParameters;
    }
}
