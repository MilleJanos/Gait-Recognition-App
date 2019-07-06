package ms.sapientia.ro.gaitrecognitionapp.service;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;

/**
 * This class
 */
public class FirebaseUtils {

    // Constant members:
    private static final String TAG = "FirebaseUtils";
    // FireStore (Cloud Firestore) members:
    public static final String USER_COLLECTION_KEY = "user";
    // Storage (Files) members:
    public static final String STORAGE_DATA_KEY = "data";
    public static final String STORAGE_RAW_KEY = "raw";
    public static final String STORAGE_FEATURE_KEY = "feature";
    public static final String STORAGE_MODEL_KEY = "model";
    public static final String STORAGE_OTHER_KEY = "other";
    public static final String STORAGE_MERGED_KEY = "merged";

    private FirebaseUtils(){
    }

    public static FirebaseStorage firebaseStorage;
    //public static StorageReference storageReference;

    // Negative Dummy Name (in STORAGE_FEATURES_KEY)
    public static final String negativeFeatureFileName = "features_negative.arff";

    /**
     * This method initiates the Firebase.
     * @param context
     */
    public static void Init(Context context){
        FirebaseApp.initializeApp(context);
        firebaseStorage = FirebaseStorage.getInstance();
        //storageReference = FirebaseStorage.getInstance().getReference();
    }

//    /**
//     * This method uploads the file to FireBase Storage where the refrence is set.
//     *
//     * @param activity the activity sContext where the method will display progress messages
//     * @param file     the File that will be uploaded
//     * @param ref      the StorageReference where the file will be uploaded
//     * @author Mille Janos
//     */
//    public static void uploadFileToFirebaseStorage(Activity activity, File file, StorageReference ref) {
//        Log.d(TAG, ">>>RUN>>>uploadFileToFirebaseStorage()");
//
//        final Activity context = activity;
//
//        //fileUploadFunctionFinished = false;
//
//        Uri path = Uri.fromFile(file);
//        StorageTask task = null;
//
//        if (path != null) {
//            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
//            //progressDialog.setTitle("Uploading...");
//            //progressDialog.show();
//            /*
//             *
//             *  Generate
//             *
//             */
//
//            task = ref.putFile(path)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            //progressDialog.dismiss();
//                            //AppUtil.progressDialog.dismiss();
//                            Toast.makeText(context, "File uploaded.", Toast.LENGTH_LONG).show();
//                            Log.d(TAG, "<<<FINISH(async)<<<uploadFileToFirebaseStorage - onSuccess");
//                            //fileUploadFunctionFinished = true;
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            //AppUtil.progressDialog.dismiss();
//                            Toast.makeText(context, "File upload Failed!", Toast.LENGTH_LONG).show();
//                            Log.d(TAG, "<<<FINISH(async)<<<uploadFileToFirebaseStorage - onFailure");
//                            //fileUploadFunctionFinished = true;
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
//                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
//                        }
//                    });
//
//        } else {
//            Log.e(TAG, "ERROR: path = null");
//            //fileUploadFunctionFinished = true;
//        }
//        Log.d(TAG, "(<<<FINISH<<<)uploadFileToFirebaseStorage() - running task in background");
//        /*
//        if( task.isSuccessful() ){
//            Log.d(TAG,"SUCCESS");
//        }else {
//            Log.d(TAG,"FAILURE");
//        }
//        */
//
//
//    }
//
//    /**
//     * This method downloads a file from Firebase FireStore.
//     *
//     * @param activity        the activity sContext where the method will display progress messaged
//     * @param downloadFromRef the StorageReference where the file will be downloaded from
//     * @param saveToThisFile  the file that will contain the downloaded data
//     * @author Mille Janos
//     */
//    public static void downloadFileFromFirebaseStorage(StorageReference downloadFromRef, File saveToThisFile, final FinishedCallback callback) {
//        Log.d(TAG, ">>>RUN>>>downloadFileFromFirebaseStorage()");
//
//        final File file = saveToThisFile;
//
//        //AppUtil.mRef = AppUtil.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + AppUtil.negativeFeatureFileName );
//
//        try {
//            downloadFromRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                    Log.d(TAG, "<<<FINISHED<<<(async)downloadFileFromFirebaseStorage() - onSuccess");
//                    Log.i(TAG, "File feature found and downloaded to: Local PATH: " + file.getAbsolutePath());
//                    callback.onCallback(0);
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                    Log.d(TAG, "<<<FINISHED<<<(async)downloadFileFromFirebaseStorage() - onFailure");
//                    Log.i(TAG, "File not found or internet problems; -> return;");
//                    e.printStackTrace();
//                    callback.onCallback(1);
//
//                }
//            });
//        } catch (Exception e) {
//            Log.e(TAG, "Error downloading file!");
//            e.printStackTrace();
//            callback.onCallback(2);
//            return;
//        }
//        Log.d(TAG, "(<<<FINISHED<<<)downloadFileFromFirebaseStorage()");
//    }





}
