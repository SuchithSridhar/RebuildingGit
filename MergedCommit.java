package git;

public class MergedCommit extends Commit {
    public String mergedParent;

    public MergedCommit(String A, String B, Commit ac, Commit bc) {
        super("Merged "+ bc.branch + " into "+ ac.branch +".");
        this.isMerged = true;
    }
}
