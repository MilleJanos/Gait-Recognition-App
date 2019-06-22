package ms.sapientia.ro.gaitrecognitionapp.service;

public interface IRecorder {

    // Constant members:
    public static final long DEFAULT_MAX_ACCELEROMETER_ARRAY = 120*128;//=15,360        //33 * 128;
    public static final long DEFAULT_INTERVAL_BETWEEN_TESTS = 8000;    //100*128;//=12,800       //32 * 128;    //30*128; // after analyzing data how m
    public static final int DEFAULT_PREPROCESSING_INTERVAL = 128;
    public static final long DEFAULT_FILES_COUNT_BEFORE_MODEL_GENERATING = 3;
    public static final long DATA_COLLECTION_MAX_ACCELEROMETER_ARRAY = Integer.MAX_VALUE;
    public static final long MIN_TRAIN_LENGTH = 10000;

}
