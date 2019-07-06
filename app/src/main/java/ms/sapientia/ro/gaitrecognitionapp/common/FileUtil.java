package ms.sapientia.ro.gaitrecognitionapp.common;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class contains static methods, to support the entire application with
 * common functions refered to file operations.
 *
 * @author MilleJanos
 */
public class FileUtil {

    private static final String TAG = "FileUtils";

    /**
     * This method creates the file if not exists.
     * @param file file to create
     */
    public static void createFileIfNotExists(File file){
        //File myInternalFilesRoot = new File(RecorderUtils.internalFilesRoot.getAbsolutePath() /*+ customDIR*/);
        //if (!myInternalFilesRoot.exists()) {
        //    myInternalFilesRoot.mkdirs();
        //    Log.i(TAG, "Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
        //}

        String path = file.getParentFile().getAbsolutePath();
        String name = file.getName();

        File folders = new File( path );

        if ( ! folders.exists() ) {
            try {
                //retFile.createNewFile();
                folders.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + folders.getAbsolutePath() );
            }
        }

        if ( ! file.exists() ) {
            try {
                file.createNewFile();
                //retFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + file.getAbsolutePath() );
            }
        }
    }

    /**
     * This method copies the content of src file into dst file
     * @param src source file
     * @param dst destination file
     * @throws IOException
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * This method creates the folders to the input file DIR.
     * @param file folders root to create
     */
    public static void createFoldersIfNotExists(File file) {
        if ( ! file.exists()) {
            file.mkdirs();
            Log.i(TAG, "Path not exists (" + file.getAbsolutePath() + ") --> .mkdirs()");
        }
    }


}
