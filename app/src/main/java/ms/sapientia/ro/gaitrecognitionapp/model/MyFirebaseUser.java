package ms.sapientia.ro.gaitrecognitionapp.model;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;

public class MyFirebaseUser extends User implements IFirebaseUser {



    public int raw_count = 0;
    public int profile_picture_idx = -1;
    public double authenticaiton_avg = 0;
    public ArrayList<Double> authenticaiton_values = new ArrayList<>();
    public int feature_count = 0;
    public int model_count = 0;
    //F//public ArrayList<String> raw_files = new ArrayList<>();
    //F//public ArrayList<String> feature_files = new ArrayList<>();
    //F//public ArrayList<String> model_files = new ArrayList<>();
    public Recorder.Mode selected_mode = Recorder.Mode.MODE_TRAIN;
    public int merged_feature_count = 0;
    public int merged_model_count = 0;


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
