package ms.sapientia.ro.gaitrecognitionapp.logic;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;

public class FirebaseController {

    private static final String TAG = "FirebaseController";

    // Download:

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
                    callback.Success( mfu );
                } else {
                    Log.d(TAG, "No such document");
                    MyFirebaseUser mfu = convertObjectToMyFirebaseUser( document.getData() );
                    callback.Failure();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                callback.Error(1);
            }
        });



    }

    // Upload / Update

    public static void setUserObject(MyFirebaseUser user){

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document( AppUtil.sAuth.getUid() );

        Map<String, Object> data = new HashMap<>();
        data.put(MyFirebaseUser.ID_KEY, user.id);
        data.put(MyFirebaseUser.CURRENT_TRAIN_ID_KEY, user.current_train_id);
        data.put(MyFirebaseUser.SELECTED_MODE_KEY , AppUtil.modeToStr(user.selected_mode));
        data.put(MyFirebaseUser.PROFILE_PICTURE_IDX_KEY, user.profile_picture_idx);
        data.put(MyFirebaseUser.FIRST_NAME_KEY,user.first_name);
        data.put(MyFirebaseUser.LAST_NAME_KEY,user.last_name);
        data.put(MyFirebaseUser.RAW_COUNT_KEY,user.raw_count);
        data.put(MyFirebaseUser.FEATURE_COUNT_KEY,user.feature_count);
        data.put(MyFirebaseUser.MODEL_COUNT_KEY,user.model_count);
        data.put(MyFirebaseUser.RAW_FILES_KEY, user.raw_files);
        data.put(MyFirebaseUser.FEATURE_FILES_KEY, user.feature_files);
        data.put(MyFirebaseUser.MODEL_FILES_KEY, user.model_files);

        ref.set(data);

    }

    public void createUserObjectByIdIfNotExists(String user_id, ICallback callback ){
        // TODO

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document(user_id);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "User object already created");
                    Map<String, Object> obj = document.getData();
                    callback.Failure();
                } else {
                    Log.d(TAG, "No such document --> Create one");
                    MyFirebaseUser firebaseUser = new MyFirebaseUser(user_id);
                    setUserObject(firebaseUser);
                    callback.Success(firebaseUser);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                callback.Error(1);
            }
        });
    }

    public static void setUserObjectMode(String user_id, Recorder.Mode mode){

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document( user_id );

        Map<String, Object> data = new HashMap<>();

        data.put("selected_mode" , AppUtil.modeToStr(mode) );

        ref.set(data);
    }

    private MyFirebaseUser convertObjectToMyFirebaseUser(Map<String, Object> map){
        MyFirebaseUser user = new MyFirebaseUser();

        user.first_name = map.get(MyFirebaseUser.FIRST_NAME_KEY).toString();
        user.last_name = map.get(MyFirebaseUser.LAST_NAME_KEY).toString();
        user.selected_mode = AppUtil.modeStrToMode( map.get(MyFirebaseUser.SELECTED_MODE_KEY).toString() );
        user.current_train_id = Integer.parseInt( map.get(MyFirebaseUser.CURRENT_TRAIN_ID_KEY).toString() );
        user.profile_picture_idx = Integer.parseInt( map.get(MyFirebaseUser.PROFILE_PICTURE_IDX_KEY).toString() );

        user.raw_count = Integer.parseInt( map.get(MyFirebaseUser.RAW_COUNT_KEY).toString() );
        user.feature_count = Integer.parseInt( map.get(MyFirebaseUser.FEATURE_COUNT_KEY).toString() );
        user.model_count = Integer.parseInt( map.get(MyFirebaseUser.MODEL_COUNT_KEY).toString() );

        user.raw_files = (ArrayList<String>) map.get(MyFirebaseUser.RAW_FILES_KEY);
        user.feature_files = (ArrayList<String>) map.get(MyFirebaseUser.FEATURE_FILES_KEY);
        user.model_files = (ArrayList<String>) map.get(MyFirebaseUser.MODEL_FILES_KEY);

        return user;
    }

}
