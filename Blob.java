package git;
import java.io.File;
import java.io.Serializable;
/**
 * Blob
 */
public class Blob implements Serializable {
    public String data;
    public String filename;
    public Blob() {
    }
    public Blob(String filename) {
        this.data = GitUtils.readContentsAsString(new File(filename));
        this.filename = filename;
    }

    @Override
    public String toString() {
        String str=" === Blob Object\n";
        str += "filename = " + filename + "\n";
        str += "data = " + data + "\n" ;
        str += "======";
        return str;
    }
}
