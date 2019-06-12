package ms.sapientia.ro.gaitrecognitionapp.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Date;

import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class AppUtil {

    private static final String TAG = "AppUtil";
    // Firebase:
    public static FirebaseAuth sAuth;
    public static StorageReference sStorageRef;
    public static FirebaseStorage sStorage;
    // Local files location:
    public static File internalFilesRoot;
    public static String customDIR = "";
    // Local files:
    public static File rawdataUserFile;
    public static File featureUserFile;
    public static File modelUserFile;
    public static File featureNegativeFile;  // local stored dummy file from firebase
    public static File trainFeatureFile;
    // Other
    public static CharSequence recordDateAndTimeFormatted = "";

    
    // Static Methods

    /**
     * This method initiates all members of the internal storage, creates the folders and files.
     */
    public static void initInternalFiles(){

        // Internal files Path:
        Date date = new Date();
        recordDateAndTimeFormatted = DateFormat.format("yyyyMMdd_HHmmss", date.getTime());

        // Init internal file location
        internalFilesRoot = new File(MainActivity.sInstance.getFilesDir().toString());

        // Create folders to internal files location
        createFoldersIfNotExists(internalFilesRoot);

        //// internal files path:
        //String raw = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/rawdata_" + sAuth.getUid() + "_" + AppUtil.recordDateAndTimeFormatted + ".csv";
        //String fea = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/feature_" + sAuth.getUid() + "_" + AppUtil.recordDateAndTimeFormatted + ".arff";
        //String mdl = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/model_"   + sAuth.getUid() + "_" + AppUtil.recordDateAndTimeFormatted + ".mdl";
        //String neg = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/feature_negative" + ".arff";
        //String trn = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/trainFeature_" + sAuth.getUid() + ".arff";
        //
        //// internal files in File type:
        //rawdataUserFile     = new File(raw);
        //featureUserFile     = new File(fea);
        //modelUserFile       = new File(mdl);
        //featureNegativeFile = new File(neg);
        //trainFeatureFile    = new File(trn);
        //
        //// Create files if they does not exist
        //createFileIfNotExists(rawdataUserFile);
        //createFileIfNotExists(featureUserFile);
        //createFileIfNotExists(modelUserFile);
        //createFileIfNotExists(featureNegativeFile);
        //createFileIfNotExists(trainFeatureFile);
        //
        ////region Print this 4 paths
        //Log.i(TAG, "PATH: rawdataUserFile.getAbsolutePath()     = "    + rawdataUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: featureUserFile.getAbsolutePath()     = "    + featureUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: modelUserFile.getAbsolutePath()       = "    + modelUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: featureNegativeFile.getAbsolutePath() = "    + featureNegativeFile.getAbsolutePath());
        //Log.i(TAG, "PATH: trainFeatureFile.getAbsolutePath()    = "    + trainFeatureFile.getAbsolutePath());
        ////endregion
    }

    /**
     * This method creates the file if not exists.
     * @param file file to create
     */
    public static void createFileIfNotExists(File file) {
        if ( ! file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + file.getAbsolutePath());
            }
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

    /**
     * Checks if internet connection is available
     * @return true if there is internet connection and false if not.
     */
    public static boolean requireInternetConnection() {
        Log.d(TAG, ">>>RUN>>>requireInternetConnection()");
        ConnectivityManager cm = (ConnectivityManager) MainActivity.sInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    /**
     * Method that hides the keyboard in the given activity
     *
     * @param activity the activity context where the method will hide the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        // If keyboard is shown then hide:
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View activityOnFocusView = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (activityOnFocusView == null) {
            activityOnFocusView = new View(activity);
        }
        imm.hideSoftInputFromWindow(activityOnFocusView.getWindowToken(), 0);
    }

    /**
     * Class to get device screen resolution.
     */
    public static class DeviceScreenResolution{

        private static boolean ready = false;
        private static int width;
        private static int height;

        private DeviceScreenResolution(){
            Display display = MainActivity.sInstance.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
            ready = true;
        }

        public static int GetWidth(){
            if( ! ready ){
                new DeviceScreenResolution();
            }
            return width;
        }

        public static int GetHeight(){
            if( ! ready ){
                new DeviceScreenResolution();
            }
            return height;
        }

    }


}
