package ms.sapientia.ro.gaitrecognitionapp.logic;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;

public class FirebaseController {

    private static final String TAG = "FirebaseController";

    public static void SetUserObject(MyFirebaseUser user ){

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document( AppUtil.sAuth.getUid() );

        Map<String, Object> data = new HashMap<>();
        data.put("current_train_id", user.current_train_id);
        data.put("selected_mode" , AppUtil.modeToStr(user.selected_mode));
        data.put("profile_picture_idx", user.profile_picture_idx);
        data.put("first_name",user.getFirst_name());
        data.put("last_name",user.getLast_name());
        data.put("raw_count",user.raw_count);
        data.put("feature_count",user.feature_count);
        data.put("model_count",user.model_count);
        data.put("raw_files", user.raw_files);
        data.put("feature_files", user.feature_files);
        data.put("model_files", user.model_files);

        ref.set(data);

    }

    public static void GetUserObjectById(String user_id , ICallback callback ){
        // TODO

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("user" + "/")
                .document(user_id);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    callback.Success(document.toObject(MyFirebaseUser.class));
                } else {
                    Log.d(TAG, "No such document");
                    callback.Failure(document.toObject(MyFirebaseUser.class));
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                callback.Error(1);
            }
        });
    }

    public static void CreateUserObjectByIdIfNotExists( String user_id ){
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
                } else {
                    Log.d(TAG, "No such document --> Create one");
                    SetUserObject(new MyFirebaseUser());
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }


}
