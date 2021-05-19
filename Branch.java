package git;
import java.io.Serializable;

/**
 * Branch
 */
public class Branch implements Serializable {
    String filename;
    public Branch() {}
    public Branch(String sha1) {
        filename = sha1;
    }
    @Override
    public String toString() {
        String str=" === Branch Object\n";
        str += "filename = " + filename + "\n" ;
        str += "======";
        return str;

    }
}
