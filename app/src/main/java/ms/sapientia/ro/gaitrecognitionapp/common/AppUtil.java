package ms.sapientia.ro.gaitrecognitionapp.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.regex.Pattern;

import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;
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
    public static File featureNegativeFile;
    public static File mergedFeatureFile;   // merged feature files
    public static File mergedModelFile;     // model create from merged feature files
    // Mode:
    //public static Recorder.Mode sMode = Recorder.Mode.MODE_TRAIN;   // Default: Train
    public static boolean sTrainNewOne = true;                      // Default: Train new one
    // Other
    public static CharSequence recordDateAndTimeFormatted = "";
    // Logged in user informations:
    public static MyFirebaseUser sUser = null;

    
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
        FileUtil.createFoldersIfNotExists(internalFilesRoot);

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
        //mergedFeatureFile    = new File(trn);
        //
        //// Create files if they does not exist
        //createFileIfNotExists(rawdataUserFile);
        //createFileIfNotExists(featureUserFile);
        //createFileIfNotExists(modelUserFile);
        //createFileIfNotExists(featureNegativeFile);
        //createFileIfNotExists(mergedFeatureFile);
        //
        ////region Print this 4 paths
        //Log.i(TAG, "PATH: rawdataUserFile.getAbsolutePath()     = "    + rawdataUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: featureUserFile.getAbsolutePath()     = "    + featureUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: modelUserFile.getAbsolutePath()       = "    + modelUserFile.getAbsolutePath());
        //Log.i(TAG, "PATH: featureNegativeFile.getAbsolutePath() = "    + featureNegativeFile.getAbsolutePath());
        //Log.i(TAG, "PATH: mergedFeatureFile.getAbsolutePath()    = "    + mergedFeatureFile.getAbsolutePath());
        ////endregion
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

    /**
     * This method returns the string name of the mode.
     * @param mode input
     * @return string name of the mode
     */
    public static String modeToStr(Recorder.Mode mode){
        if(mode == Recorder.Mode.MODE_TRAIN){
            return "train";
        }else{
            if(mode == Recorder.Mode.MODE_AUTHENTICATE){
                return "auth";
            }else{
                return "collect" ;
            }
        }
    }

    /**
     * This method returns the mode of the string name.
     * @param mode_str input
     * @return mode or null if can't cconvert
     */
    public static Recorder.Mode modeStrToMode(String mode_str){
        if(mode_str.trim().equals("train")){
            return Recorder.Mode.MODE_TRAIN;
        }else{
            if(mode_str.trim().equals("auth")){
                return Recorder.Mode.MODE_AUTHENTICATE;
            }else{
                if(mode_str.trim().equals("collect")) {
                    return Recorder.Mode.MODE_COLLECT_DATA;
                }else{
                    return null;
                }
            }
        }
    }

    public static void requestPasswordReset(String email) {

        if(email.length() == 0){
            throw new InvalidParameterException("Email can't be null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.sInstance);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to send password reset request?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            AppUtil.sAuth.sendPasswordResetEmail( email )
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Reset mPassword request has been sent.");
                            Toast.makeText(MainActivity.sContext, "Reset mPassword request has been sent!", Toast.LENGTH_LONG).show();
                        }
                    });
        });
        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void sendEmail(String to, String subject, String body){
        // Intent intent = new Intent(Intent.ACTION_SEN);
        // intent.setType("text/plain");
        // intent.putExtra(Intent.EXTRA_EMAIL, to);
        // intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //
        // intent.putExtra(Intent.EXTRA_TEXT, body);
        // MainActivity.sInstance.startActivity(Intent.createChooser(intent, "Send Email"));
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",to, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        MainActivity.sInstance.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }



    /**
     * Check the email if is valid format.
     * Returns the error code:
     *  0 if email is Correct
     *  1 if email is empty
     *  2 if email contains extra spaces
     *  3 if email format is wrong
     * @param email to verify
     * @return error code
     */
    public static int isEmailFormatWrong(String email){
        // Length:
        if( email.length() == 0 ){
            return 1;
        }
        // Extra space
        if( ! email.trim().equals(email) ){
            return 2;
        }
        // Content
        Pattern email_address_pattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        );
        if( ! email_address_pattern.matcher(email).matches() ){
            return 3;
        }
        return 0;
    }

    /**
     * Check the email if is valid format.
     * Returns the error code:
     *  0 if password his Correct
     *  1 if password is empty
     *  2 if password is shorter then 6
     *  3 if password contains space
     *  4 if password format is wrong
     * @param password to verify
     * @return error code
     */
    public static int isPasswordFormatWrong(String password){
        // Empty
        if( password.length() == 0 ){
            return 1;
        }
        // Length
        if( password.length() < 6 ){
            return 2;
        }
        // Extra space
        if( ! password.trim().equals(password) ){
            return 3;
        }
        // Content
        Pattern password_address_pattern = Pattern.compile(
                "[a-zA-Z0-9_]+"
        );
        if( ! password_address_pattern.matcher(password).matches() ){
            return 4;
        }
        return 0;
    }

    public static void saveUriIntoFIle(Uri sourceUri, String destinationFilePath)
    {
        String sourceFilename= sourceUri.getPath();
        //String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath()+File.separatorChar+"abc.mp3";

        File file = new File(destinationFilePath);
        if( ! file.exists() ){
            FileUtil.createFileIfNotExists( file );
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilePath, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
