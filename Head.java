package git;
import java.io.Serializable;

/**
 * Head
 */
public class Head implements Serializable {
    /** Contains a string of the commit id */
    String filename;
    String currentBranch;

    public Head() {
    }

    public Head(String sha1) {
        filename = sha1;
    }

    @Override
    public String toString() {
        String str = " === Head Object\n";
        str += "filename = " + filename + "\n";
        str += "currentBranch = " + currentBranch + "\n";
        str += "======";
        return str;
    }
}
