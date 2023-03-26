package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    /** Stores CONTENTS of the file. */
    private String contents;

    public Blob(File toSaveContents) {
        contents = Utils.readContentsAsString(toSaveContents);
    }

    public String getContents() {
        return contents;
    }
}
