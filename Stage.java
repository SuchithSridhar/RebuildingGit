package git;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Stage
 */
public class Stage implements Serializable {
    ArrayList<String> filenames;
    ArrayList<String> blobs;
    ArrayList<String> removed;
    ArrayList<String> removedBlobs;

    public Stage() {
        filenames = new ArrayList<String>();
        blobs = new ArrayList<String>();
        removed = new ArrayList<String>();
        removedBlobs = new ArrayList<String>();
    }

    @Override
    public String toString() {
        String str=" === Stage Object\n";
        str += "filenames = " + filenames + "\n" ;
        str += "blobs = " + blobs + "\n" ;
        str += "removed = " + removed + "\n" ;
        str += "======";
        return str;
    }
}
