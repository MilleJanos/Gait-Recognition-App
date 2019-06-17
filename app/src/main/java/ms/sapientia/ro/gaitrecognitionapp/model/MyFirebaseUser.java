package ms.sapientia.ro.gaitrecognitionapp.model;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;

public class MyFirebaseUser extends User {

    public static final String ID_KEY = "id";
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String CURRENT_TRAIN_ID_KEY = "current_train_id";
    public static final String AUTHENTICATION_AVG_KEY = "auth_avg";
    public static final String RAW_COUNT_KEY = "raw_count";
    public static final String FEATURE_COUNT_KEY = "feature_count";
    public static final String MODEL_COUNT_KEY = "model_count";
    public static final String RAW_FILES_KEY = "raw_files";
    public static final String FEATURE_FILES_KEY = "feature_files";
    public static final String MODEL_FILES_KEY = "model_files";
    public static final String SELECTED_MODE_KEY = "selected_mode";
    public static final String PROFILE_PICTURE_IDX_KEY = "profile_picture_idx";
    public static final String TRAIN_FEATURE_COUNT_KEY = "train_feature_count";
    public static final String TRAIN_MODEL_COUNT_KEY = "train_model_count";


    public int current_train_id = -1;
    public int raw_count = 0;
    public int profile_picture_idx = -1;
    public double authenticaiton_avg = 0;
    public int feature_count = 0;
    public int model_count = 0;
    public ArrayList<String> raw_files = new ArrayList<>();
    public ArrayList<String> feature_files = new ArrayList<>();
    public ArrayList<String> model_files = new ArrayList<>();
    public Recorder.Mode selected_mode = Recorder.Mode.MODE_TRAIN;
    public int train_feature_count = 0;
    public int train_model_count = 0;

    public MyFirebaseUser() {
        super();
    }

    public MyFirebaseUser(String user_id) {
        super(user_id);
    }

    public MyFirebaseUser(String first_name, String last_name) {
        super(first_name, last_name);
    }
}
