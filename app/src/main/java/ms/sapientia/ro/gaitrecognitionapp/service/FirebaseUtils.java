package ms.sapientia.ro.gaitrecognitionapp.service;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.internal.Util;

import java.io.File;

public class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";

    private FirebaseUtils(){
    }

    //
    // Firebase Keys
    //
    // FireStore (Cloud Firestore)
    public static final String USER_COLLECTION_KEY = "user";
        /* <user_id> */



    //public static final String USER_RECORDS_OLD_KEY = "user_records";
    //public static final String USER_RECORDS_NEW_KEY = "user_records_2";
    //public static final String USER_RECORDS_DEBUG_KEY = "user_records_debug";
    //    /* <user_id> */
    //        /* <device_id> */
    //            /* <random_id> */
    //                public static final String DATE_KEY = "date";                   // they will be used more
    //                public static final String FILE_ID_KEY = "fileId";              // often in UserRecordObject
    //                public static final String DOWNLOAD_URL_KEY = "downloadUrl";    // class
    //                public static final String USER_DATA_KEY = "user_data";
    ///* <user_id> */
    //public static final String USER_DATE_KEY = "date";                   // they will be used more
    //public static final String USER_FILE_ID_KEY = "fileId";              // often in UserRecordObject
    //public static final String USER_DOWNLOAD_URL_KEY = "downloadUrl";    // class
    // Storage (Files)
    public static final String STORAGE_DATA_KEY = "data";
        /* <user_id> */
            public static final String STORAGE_RAW_KEY = "raw";
            public static final String STORAGE_FEATURE_KEY = "feature";
            public static final String STORAGE_MODEL_KEY = "model";
            public static final String STORAGE_OTHER_KEY = "other";
            public static final String STORAGE_TRAIN_KEY = "train";

    //public static final String STORAGE_FEATURES_KEY = "features";
    //public static final String STORAGE_FILES_KEY = "files";
    //public static final String STORAGE_FILES_METADATA_KEY = "files_metadata";
    //public static final String STORAGE_MODELS_KEY = "models";
    //public static final String STORAGE_FEATURES_DEBUG_KEY = "features_debug";
    //public static final String STORAGE_FILES_DEBUG_KEY = "files_debug";
    //public static final String STORAGE_MODELS_DEBUG_KEY = "models_debug";
    /**
     * A constant that contains the name of the Firebase/Firestore collection where user statistics
     * are stored
     *
     * @author Krisztian-Miklos Nemeth
     */
    public static final String FIRESTORE_STATS_NODE = "user_stats";

    //
    // Static variables
    //

    public static FirebaseStorage firebaseStorage;
    //public static StorageReference storageReference;

    // Negative Dummy Name (in STORAGE_FEATURES_KEY)
    public static final String negativeFeatureFileName = "features_negative.arff";

    //
    // Methods:
    //

    public static void Init(Context context){
        FirebaseApp.initializeApp(context);
        firebaseStorage = FirebaseStorage.getInstance();
        //storageReference = FirebaseStorage.getInstance().getReference();
    }

    /**
     * This method uploads the file to FireBase Storage where the refrence is set.
     *
     * @param activity the activity sContext where the method will display progress messages
     * @param file     the File that will be uploaded
     * @param ref      the StorageReference where the file will be uploaded
     * @author Mille Janos
     */
    public static void uploadFileToFirebaseStorage(Activity activity, File file, StorageReference ref) {
        Log.d(TAG, ">>>RUN>>>uploadFileToFirebaseStorage()");

        final Activity context = activity;

        //fileUploadFunctionFinished = false;

        Uri path = Uri.fromFile(file);
        StorageTask task = null;

        if (path != null) {
            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            /*
             *
             *  Generate
             *
             */

            task = ref.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            //AppUtil.progressDialog.dismiss();
                            Toast.makeText(context, "File uploaded.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "<<<FINISH(async)<<<uploadFileToFirebaseStorage - onSuccess");
                            //fileUploadFunctionFinished = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //AppUtil.progressDialog.dismiss();
                            Toast.makeText(context, "File upload Failed!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "<<<FINISH(async)<<<uploadFileToFirebaseStorage - onFailure");
                            //fileUploadFunctionFinished = true;
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                        }
                    });

        } else {
            Log.e(TAG, "ERROR: path = null");
            //fileUploadFunctionFinished = true;
        }
        Log.d(TAG, "(<<<FINISH<<<)uploadFileToFirebaseStorage() - running task in background");
        /*
        if( task.isSuccessful() ){
            Log.d(TAG,"SUCCESS");
        }else {
            Log.d(TAG,"FAILURE");
        }
        */


    }

    /**
     * This method downloads a file from Firebase FireStore.
     *
     * @param activity        the activity sContext where the method will display progress messaged
     * @param downloadFromRef the StorageReference where the file will be downloaded from
     * @param saveToThisFile  the file that will contain the downloaded data
     * @author Mille Janos
     */
    public static void downloadFileFromFirebaseStorage(StorageReference downloadFromRef, File saveToThisFile, final FinishedCallback callback) {
        Log.d(TAG, ">>>RUN>>>downloadFileFromFirebaseStorage()");

        final File file = saveToThisFile;

        //AppUtil.mRef = AppUtil.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + AppUtil.negativeFeatureFileName );

        try {
            downloadFromRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.d(TAG, "<<<FINISHED<<<(async)downloadFileFromFirebaseStorage() - onSuccess");
                    Log.i(TAG, "File feature found and downloaded to: Local PATH: " + file.getAbsolutePath());
                    callback.onCallback(0);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "<<<FINISHED<<<(async)downloadFileFromFirebaseStorage() - onFailure");
                    Log.i(TAG, "File not found or internet problems; -> return;");
                    e.printStackTrace();
                    callback.onCallback(1);

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error downloading file!");
            e.printStackTrace();
            callback.onCallback(2);
            return;
        }
        Log.d(TAG, "(<<<FINISHED<<<)downloadFileFromFirebaseStorage()");
    }





}
