package ms.sapientia.ro.gaitrecognitionapp.model;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.service.Recorder;

public class FirebaseUser extends User {

    public String id = "";
    public int current_train_id = -1;
    public int raw_count = 0;
    public int feature_count = 0;
    public int model_count = 0;
    public ArrayList<String> raw_files = new ArrayList<>();
    public ArrayList<String> feature_files = new ArrayList<>();
    public ArrayList<String> model_files = new ArrayList<>();
    public Recorder.Mode selected_mode = Recorder.Mode.MODE_TRAIN;


    public FirebaseUser(String email, String password) {
        super(email, password);
    }

    public FirebaseUser(String email, String firstName, String lastName, String password) {
        super(email, firstName, lastName, password);
    }
}
