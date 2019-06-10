package ms.sapientia.ro.gaitrecognitionapp.service;

import android.app.Activity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.ro.gaitrecognitionapp.MainActivity;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.GaitVerification;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitVerification;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.SerializationHelper;

public class Utils {

    private Utils() {
    }

    private static final String TAG = "Utils";

    public final static String INPUT_EXTRA_KEY = "inputextra";
    public final static String INPUT_CREATE_OR_VERIFY = "createorverify";
    public final static String CHANNEL_ID_01 = "channel_01";

    // File vars
    public static File rawdataUserFile;
    public static File featureUserFile;
    public static File modelUserFile;
    public static File featureNegativeDummyFile;
    public static File featureMergedFile;
    public static File featureUserFile_Copy;
    //region HELP
        // get full path:           file.getAbsolutePath()
        // get parent folder path:  file.getParentFile().getAbsolutePath()
        // get file name:           file.getName()
    //endregion

    public static boolean downloadingNegativeDummyFile = false;

    // Rawdata Default header
    public static final String RAWDATA_DEFAULT_HEADER = "timestamp,accx,accy,accz,stepnum";

    // Vars
    public static Date lastUsedDate = new Date();
    public static String deviceID;

    // stored internal files location
    public static File internalFilesRoot;

    //region HELP
    /**
     * This method saves the accArray<Accelerometer> list into file including header.
     *
     * @param array array that contains the data that will be writtem to the file
     * @param file descriptor of the file all the writing will be made into
     * @param headerStr inserts this string at the first line of the file, leave empty ("") if you want no header
     * @return 0 if there is no error
     * 1 if there occurred an error
     */
    //endregion
    public static short saveRawAccelerometerDataIntoCsvFile(ArrayDeque<Accelerometer> array, File file, String headerStr) {
        String TAG = "Util";
        Log.d(TAG, ">>>RUN>>>savingAccArrayIntoCSV()");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "IOException: file.createNewFile()");
                e.printStackTrace();
                return 1;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);

            // Header:
            if( ! headerStr.equals("") ){
                pw.println(headerStr);
            }

            for (Accelerometer a : array) {
                pw.println(a.toString());
            }
            pw.flush();
            pw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "******* File not found.");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        Log.d(TAG, "<<<FINISH<<<savingAccArrayIntoCSV()");
        return 0;
    }


    public static String getCurrentDateFormatted(){
        Date date = new Date();
        lastUsedDate = date;
        return DateFormat.format("yyyyMMdd_HHmmss", date.getTime()).toString();
    }

    public static String formatDate(Date date){
        return DateFormat.format("yyyyMMdd_HHmmss", date.getTime()).toString();
    }


    public static void createFileIfNotExists(File file){
        //File myInternalFilesRoot = new File(Utils.internalFilesRoot.getAbsolutePath() /*+ customDIR*/);
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


//    // Initial Files
//    public static void initInternalFiles(){
//
//        // Create folder if not exists:
//        File myInternalFilesRoot = new File(Utils.internalFilesRoot.getAbsolutePath() /*+ customDIR*/);
//        if (!myInternalFilesRoot.exists()) {
//            myInternalFilesRoot.mkdirs();
//            Log.i(TAG, "Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
//        }
//
//        // Creating user's raw data file path:
//        Utils.rawdata_user_path = Utils.internalFilesRoot.getAbsolutePath() + Utils.appCustomDIR + "/" + getCurrentDateFormatted() + "/rawdata" + /*"_" + getCurrentDateFormatted +*/ ".csv";
//        Utils.feature_user_path = Utils.internalFilesRoot.getAbsolutePath() + Utils.appCustomDIR + "/" + getCurrentDateFormatted() + "/feature" + /*"_" + getCurrentDateFormatted +*/ ".arff";
//        Utils.model_user_path =   Utils.internalFilesRoot.getAbsolutePath() + Utils.appCustomDIR + "/" + getCurrentDateFormatted() + "/model"   + /*"_" + getCurrentDateFormatted +*/ ".mdl";
//        Utils.feature_negative_dummy_path = Utils.internalFilesRoot.getAbsolutePath() + Utils.appCustomDIR + "/feature_negative.arff";
//        //region Print this 4 paths
//        Log.i(TAG, "PATH: Utils.feature_dummy_path = " + Utils.feature_negative_dummy_path);
//        Log.i(TAG, "PATH: Utils.rawdata_user_path  = " + Utils.rawdata_user_path);
//        Log.i(TAG, "PATH: Utils.feature_user_path  = " + Utils.feature_user_path);
//        Log.i(TAG, "PATH: Utils.model_user_path    = " + Utils.model_user_path);
//        //endregion                                                   //*//
//
//        // internal files as File type:
//        featureNegativeDummyFile = new File(Utils.feature_negative_dummy_path);
//        rawdataUserFile = new File(Utils.rawdata_user_path);
//        featureUserFile = new File(Utils.feature_user_path);
//        modelUserFile = new File(Utils.model_user_path);
//
//        //*//
//        // Creating user's raw data file (if not exists):
//        if (!Utils.rawdataUserFile.exists()) {
//            try {
//                Utils.rawdataUserFile.createNewFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "File can't be created: " + Utils.rawdata_user_path);
//            }
//        }
//        // Creating user's feature file (if not exists):
//        if (!Utils.featureUserFile.exists()) {
//            try {
//                Utils.featureUserFile.createNewFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "File can't be created: " + Utils.feature_user_path);
//            }
//        }
//        // Creating user's model file (if not exists):
//        if (!modelUserFile.exists()) {
//            try {
//                modelUserFile.createNewFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "File can't be created: " + Utils.model_user_path);
//            }
//        }
//        // Creating dummy's(negative data) feature file (if not exists):
//        if ( ! featureNegativeDummyFile.exists()) {
//            try {
//                featureNegativeDummyFile.createNewFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "File can't be created: " + Utils.feature_negative_dummy_path);
//            }
//        }
//    }


    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static ArrayDeque<Accelerometer> listToArrayDeque(List<Accelerometer> list){
        ArrayDeque<Accelerometer> ad = new ArrayDeque<>();

        for(Accelerometer a : list){
            ad.push(a);
        }

        return ad;
    }

    /**
     * Method that hides the keyboard in the given activity
     *
     * @param activity the activity sContext where the method will hide the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        // If keyboard is shown then hide:
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE); // TODO: MainActivity ?
        // Find the currently focused mView, so we can grab the correct window token from it.
        View activityOnFocusView = activity.getCurrentFocus();
        // If no mView currently has focus, create a new one, just so we can grab a window token from it
        if (activityOnFocusView == null) {
            activityOnFocusView = new View(activity);
        }
        imm.hideSoftInputFromWindow(activityOnFocusView.getWindowToken(), 0);
    }

    public static double checkUserInPercentage(/*Activity activity, String userRawDataFilePath, String userFeatureFilePath, String dummyFeatureFilePath, String userModelFilePath, String userId*/) {

        double percentage = -1;

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier;
        try {
            classifier = (RandomForest) SerializationHelper.read(new FileInputStream( Utils.modelUserFile.getAbsolutePath() )); //new RandomForest();

            //GaitHelperFunctions.createFeaturesFileFromRawFile(
            //        Utils.rawdataUserFile.getAbsolutePath(),
            //        Utils.featureUserFile.getAbsolutePath().substring(0, Utils.featureUserFile.getAbsolutePath().length() - (".arff").length()), "userId");


            // features_dummy + features_user
            GaitHelperFunctions.mergeEquallyArffFiles(
                    Utils.featureNegativeDummyFile.getAbsolutePath(),
                    Utils.featureUserFile_Copy.getAbsolutePath());

            ArrayList<Attribute> attributes = builder.getAttributes( Utils.featureUserFile_Copy.getAbsolutePath() ); ///feature (mar letezo)

            IGaitVerification verifier = new GaitVerification();
            //percentage = verifier.verifyUser(classifier, attributes, FRESH_RAWDATA_WAITING_TO_TEST ); // 3. param - user raw data
            percentage = verifier.verifyUser(classifier, attributes, Utils.rawdataUserFile.getAbsolutePath() );

            // percentage = Integer.parseInt( ((percentage * 100) + "").substring(0, 2) );

        } catch (FileNotFoundException e) {
            Log.e(TAG, "*********File not found!");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "*********Error!");
            e.printStackTrace();
        }

        return percentage;
    }


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



}
