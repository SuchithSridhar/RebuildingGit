package git;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * FileManager
 */
public class FileManager {

    // Cache
    private ArrayList<String> filenames;
    private ArrayList<Object> objects;

    public FileManager() {
        filenames = new ArrayList<String>();
        objects = new ArrayList<Object>();
    }

    /**
     * Write plain text data to a file.
     * @param filename the name of the file.
     * @param data String input of the data to write to the file.
     */
    public void writeTextFile(String filename, String data) {
        File file = new File(filename);
        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(data);
            fw.close();
        } catch(IOException e) {
            //System.out.println("Some IO error occured");
        }
    }

    public Object readFile(String sha1Filename) {
        if (!filenames.contains(sha1Filename)) {
            filenames.add(sha1Filename);
            objects.add(readBinFile(sha1Filename));
        }

        return objects.get(filenames.indexOf(sha1Filename));

    }

    public void writeFile(String sha1Filename, Object obj) {

        writeBinFile(sha1Filename, obj); // write it

        filenames.add(sha1Filename); // store it to cache
        objects.add(obj);
    }

    private Object readBinFile(String file) {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            Object obj = objectIn.readObject();
            objectIn.close();
            return obj;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeBinFile(String file, Object obj) {
        // The object has to be a serializable obj
        File f = new File(file);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(obj);
            objOut.close();
        } catch (Exception e) {
            //System.out.println("File unable to be written: " + file);
            //e.printStackTrace();
            return ;
        }
    }
}
