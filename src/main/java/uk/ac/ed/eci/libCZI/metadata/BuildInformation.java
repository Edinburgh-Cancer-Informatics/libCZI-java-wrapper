package uk.ac.ed.eci.libCZI.metadata;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import uk.ac.ed.eci.libCZI.IInterop;
import uk.ac.ed.eci.libCZI.LibCziFFM;

/**
 * <b>LibCZIBuildInformationInterop</b>
 * 
 * This struct gives information about the build of the libCZIApi-library. 
 * Note that all strings must be freed by the caller (using libCZI_Free).
 * @see https://zeiss.github.io/libczi/api/struct_lib_c_z_i_build_information_interop.html
 */
public class BuildInformation implements IInterop {
    private String compilerIdentification;
    private String repositoryUrl;
    private String repositoryBranch;
    private String repositoryTag;

    public BuildInformation(String compilerIdentification, String repositoryUrl, String repositoryBranch, String repositoryTag) {
        this.compilerIdentification = compilerIdentification;
        this.repositoryUrl = repositoryUrl;
        this.repositoryBranch = repositoryBranch;
        this.repositoryTag = repositoryTag;
    }
    public String getCompilerIdentification() {
        return compilerIdentification;
    }
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    public String getRepositoryBranch() {
        return repositoryBranch;
    }
    public String getRepositoryTag() {
        return repositoryTag;
    }
    
    public static MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ADDRESS.withName("compilerIdentification"),
        ADDRESS.withName("repositoryUrl"),
        ADDRESS.withName("repositoryBranch"),
        ADDRESS.withName("repositoryTag")
    );

    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toMemorySegment'");
    }

    public static BuildInformation fromMemorySegment(MemorySegment segment) {
        MemorySegment compilerIdentificationSegment = segment.get(ADDRESS, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("compilerIdentification")));
        String compilerId = compilerIdentificationSegment.reinterpret(Long.MAX_VALUE).getString(0);
        LibCziFFM.free(compilerIdentificationSegment);

        MemorySegment repoUrlSegment = segment.get(ADDRESS, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("repositoryUrl")));
        String repoUrl = repoUrlSegment.reinterpret(Long.MAX_VALUE).getString(0);
        LibCziFFM.free(repoUrlSegment);
        
        MemorySegment repoBranchSegment = segment.get(ADDRESS, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("repositoryBranch")));
        String repoBranch = repoBranchSegment.reinterpret(Long.MAX_VALUE).getString(0);
        LibCziFFM.free(repoBranchSegment);

        MemorySegment repoTagSegment = segment.get(ADDRESS, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("repositoryTag")));
        String repoTag = repoTagSegment.reinterpret(Long.MAX_VALUE).getString(0);
        LibCziFFM.free(repoTagSegment);

        return new BuildInformation(compilerId, repoUrl, repoBranch, repoTag);
    }
}
