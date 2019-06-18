package ms.sapientia.ro.gaitrecognitionapp.logic;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.IFileCallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;

public class FirebaseController {

    private static final String TAG = "FirebaseController";

    // Download:

    /**
     * This method downloads the object of a given user id.
     * <p>
     * Usage:
     * <p>
     * new FirebaseController().getUserObjectById( "USERID", new ICallback(){
     *      <p>
     *      // implemented methods
     *      </p>
     * });
     * </p>
     * </p>
     * @param user_id downloadable object's user id
     * @param callback used to write custom commands afrer: onSuccess, onFailure and onError. It can be null
     */
    public void getUserObjectById(String user_id , ICallback callback ){

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document(user_id);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData() );
                    MyFirebaseUser mfu = convertObjectToMyFirebaseUser( document.getData() );
                    if( callback != null )
                        callback.Success( mfu );
                } else {
                    Log.d(TAG, "No such document");
                    if( callback != null )
                        callback.Failure();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                if( callback != null )
                    callback.Error(1);
            }
        });



    }

    /**
     * This method downloads a file from firebase.
     * @param ref to download from
     * @param file_downloaded downloaded file
     * @param callback calls the methods of the callback if is not null
     */
    public void downloadFile(StorageReference ref, File file_downloaded, IFileCallback callback){

        try{

            ref.getFile(file_downloaded)
                    .addOnSuccessListener(taskSnapshot -> {
                        if(callback != null)
                            callback.Success(file_downloaded);
                    })
                    .addOnFailureListener(e -> {
                        if(callback != null)
                            callback.Failure();
                    });

        }catch (Exception e) {
            Log.e(TAG, "Error downloading file! ref= " + ref);
            e.printStackTrace();
            if(callback != null)
                callback.Error(1);
            return;
        }

    }

    // Upload / Update:

    /**
     * This method updates user's object in firebase by giving the uploadable object.
     * @param user uploadable user
     */
    public static void setUserObject(MyFirebaseUser user){

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document( AppUtil.sAuth.getUid() );

        Map<String, Object> data = new HashMap<>();
        data.put(MyFirebaseUser.ID_KEY, user.id);
        data.put(MyFirebaseUser.CURRENT_TRAIN_ID_KEY, user.current_train_id);
        data.put(MyFirebaseUser.AUTHENTICATION_AVG_KEY, user.authenticaiton_avg);
        data.put(MyFirebaseUser.SELECTED_MODE_KEY , AppUtil.modeToStr(user.selected_mode));
        data.put(MyFirebaseUser.PROFILE_PICTURE_IDX_KEY, user.profile_picture_idx);
        data.put(MyFirebaseUser.FIRST_NAME_KEY,user.first_name);
        data.put(MyFirebaseUser.LAST_NAME_KEY,user.last_name);
        data.put(MyFirebaseUser.RAW_COUNT_KEY,user.raw_count);
        data.put(MyFirebaseUser.FEATURE_COUNT_KEY,user.feature_count);
        data.put(MyFirebaseUser.MODEL_COUNT_KEY,user.model_count);
        //F//data.put(MyFirebaseUser.RAW_FILES_KEY, user.raw_files);
        //F//data.put(MyFirebaseUser.FEATURE_FILES_KEY, user.feature_files);
        //F//data.put(MyFirebaseUser.MODEL_FILES_KEY, user.model_files);
        data.put(MyFirebaseUser.MERGED_FEATURE_COUNT_KEY, user.train_feature_count);
        data.put(MyFirebaseUser.MERGED_MODEL_COUNT_KEY, user.train_model_count);

        ref.set(data);

    }


    /**
     * This method uploads a file into a given firebase storage reference.
     * @param ref upload here
     * @param file uploadable file
     */
    public static void uploadFile(StorageReference ref, File file){

        Uri path = Uri.fromFile(file);
        StorageTask task = null;

        if( path != null ){

            task = ref.putFile(path)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "uploadFile: File uploaded!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "uploadFile: File upload failed!");
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    });

        }
        Log.e(TAG, "uploadFile: Error: path = null");
    }

    // Converters:

    /**
     * This method converts Map into MyFirebaseUser.
     * @param map convertable object
     * @return converted MyFirebaseUser object
     */
    private MyFirebaseUser convertObjectToMyFirebaseUser(Map<String, Object> map){
        MyFirebaseUser user = new MyFirebaseUser();

        user.id = map.get(MyFirebaseUser.ID_KEY).toString();
        user.first_name = map.get(MyFirebaseUser.FIRST_NAME_KEY).toString();
        user.last_name = map.get(MyFirebaseUser.LAST_NAME_KEY).toString();
        user.authenticaiton_avg = Double.parseDouble( map.get(MyFirebaseUser.AUTHENTICATION_AVG_KEY).toString() );

        user.selected_mode = AppUtil.modeStrToMode( map.get(MyFirebaseUser.SELECTED_MODE_KEY).toString() );
        user.current_train_id = Integer.parseInt( map.get(MyFirebaseUser.CURRENT_TRAIN_ID_KEY).toString() );
        user.profile_picture_idx = Integer.parseInt( map.get(MyFirebaseUser.PROFILE_PICTURE_IDX_KEY).toString() );

        user.raw_count = Integer.parseInt( map.get(MyFirebaseUser.RAW_COUNT_KEY).toString() );
        user.feature_count = Integer.parseInt( map.get(MyFirebaseUser.FEATURE_COUNT_KEY).toString() );
        user.model_count = Integer.parseInt( map.get(MyFirebaseUser.MODEL_COUNT_KEY).toString() );

        //F//user.raw_files = (ArrayList<String>) map.get(MyFirebaseUser.RAW_FILES_KEY);
        //F//user.feature_files = (ArrayList<String>) map.get(MyFirebaseUser.FEATURE_FILES_KEY);
        //F//user.model_files = (ArrayList<String>) map.get(MyFirebaseUser.MODEL_FILES_KEY);

        user.train_feature_count = Integer.parseInt( map.get(MyFirebaseUser.MERGED_FEATURE_COUNT_KEY).toString() );
        user.train_model_count = Integer.parseInt( map.get(MyFirebaseUser.MERGED_MODEL_COUNT_KEY).toString() );

        return user;
    }
}
