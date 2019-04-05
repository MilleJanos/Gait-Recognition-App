package ms.sapientia.ro.gaitrecognitionapp.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.feature_extractor.Settings;
import ms.sapientia.ro.feature_extractor.Util;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import weka.classifiers.Classifier;

public class Recorder {

    private static final String TAG = "Recorder";

    // Constants
    private final long MAX_ACCELEROMETER_ARRAY = 30*128;
    private final long INTERVAL_BETWEEN_TESTS = 30*128; // after analyzing data how m
    private final long FILES_COUNT_BEFORE_MODEL_GENERATING = 1;
    private final int PREPROCESSING_INTERVAL = 128;

    // Sensor
    private boolean mIsRecording = false;
    private Sensor mAccelerometerSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mAccelerometerEventListener;

    // Vars
    private Context mContext;
    private ArrayDeque<Accelerometer> mAccelerometerArray = new ArrayDeque<>();
    private ArrayDeque<String> mUploadableFilesPath = new ArrayDeque<>();
    private int mStepCount = 0;
    private long mRecordCount = 0;
    private long mIntervalBetweenTests = 0;
    private long mFileCount = 0;

    // Sound
    MediaPlayer mp_bing;
    MediaPlayer mp_feature;
    MediaPlayer mp_model;


    public Recorder(Context context) {
        mContext = context;
        mIsRecording = false;

        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelerometerSensor == null) {
            Log.d(TAG, "Recorder: Accelerometer not found!");
            Toast.makeText(mContext, "The device has no Accelerometer !", Toast.LENGTH_SHORT).show();
        }

        mAccelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                updateRecordedList(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        initSound();
    }

    private void initSound() {
        mp_bing = MediaPlayer.create(mContext, R.raw.relentless);
        mp_feature = MediaPlayer.create(mContext, R.raw.feature);
        mp_model = MediaPlayer.create(mContext, R.raw.model);
    }


    private void updateRecordedList(SensorEvent event){
        long timeStamp = event.timestamp;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (mIsRecording) {

            if(mIntervalBetweenTests == INTERVAL_BETWEEN_TESTS){


                mp_bing.start();

                // Init Internal Files
                //Utils.initInternalFiles();
                Utils.rawdataUserFile = new File( Utils.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + Utils.getCurrentDateFormatted() + "/" + "rawdata.csv");
                Utils.createFileIfNotExists( Utils.rawdataUserFile );

                // Preprocessing raw data
                List<Accelerometer> list = new ArrayList(Arrays.asList( mAccelerometerArray.toArray()));
                Util featureUtil = new Util();
                Settings.setUseDynamicPreprocessingThreshold(true);
                Settings.setPreprocessingInterval(PREPROCESSING_INTERVAL);
                List<Accelerometer> preprocessedList = featureUtil.preprocess(list);

                // Save raw data
                //String rawdataStr = mAccelerometerArrayToString(mAccelerometerArray);
                ArrayDeque copy = Utils.listToArrayDeque(list);
                Utils.saveRawAccelerometerDataIntoCsvFile(copy, Utils.rawdataUserFile, Utils.RAWDATA_DEFAULT_HEADER);

                Log.d(TAG, "mFileCount = " + mFileCount);

                // If we collected enought data to being
                if (mFileCount >= FILES_COUNT_BEFORE_MODEL_GENERATING - 1 ) {

                    // Download Dummy
                    if (Utils.rawdataUserFile.length() > 0) { // if the file is not empty

                        Utils.featureNegativeDummyFile = new File(Utils.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff");
                        if( ! Utils.featureNegativeDummyFile.exists()) {
                            Utils.createFileIfNotExists(Utils.featureNegativeDummyFile);
                        }

                        StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                                FirebaseUtils.STORAGE_FEATURES_KEY
                                        + "/" + FirebaseUtils.firebaseDummyFileName
                        );
                        if( true /*Utils.downloadingNegativeDummyFile IS EMPTY*/ ) {        // If it's not downloaded

                            if (Utils.downloadingNegativeDummyFile == false) {              // and if it's not downloading at the moment, then: download

                                Utils.downloadingNegativeDummyFile = true;

                                FirebaseUtils.downloadFileFromFirebaseStorage(reference, Utils.featureNegativeDummyFile, new FinishedCallback() {
                                    @Override
                                    public void onCallback(int errorCode) {                 // after onSuccess or onFailure

                                        if (errorCode == 0) {

                                            Toast.makeText(mContext, "Negative Data downloaded", Toast.LENGTH_SHORT).show();

                                            createFeature();

                                            createModel();
                                            
                                            updateGait();
                                            
                                        } else {
                                            Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                                        }

                                        Utils.downloadingNegativeDummyFile = false;
                                    }
                                });
                            }
                        }else{              // if it's downloaded
                            Toast.makeText(mContext, "Negative Data already downloaded", Toast.LENGTH_SHORT).show();

                            createFeature();

                            createModel();

                            updateGait();
                        }
                    }
                    mFileCount = 0
                    ;
                }else{

                    ++mFileCount;

                }

                // Reset counter
                mIntervalBetweenTests= 0;
            }
            if(mAccelerometerArray.size() == MAX_ACCELEROMETER_ARRAY - 1 ){     // if the list is full, then remove first then
                mAccelerometerArray.removeFirst();
            }
            mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z,mStepCount));     // add to last
            ++mRecordCount;
            ++mIntervalBetweenTests;
            // printAccelerometerList(mAccelerometerArray); // print every iteration
        }
    }


    private void createFeature() {
        Log.d(TAG, "createFeature: IN");


        // Create feature file
        Utils.featureUserFile = new File ( Utils.internalFilesRoot + "/" + "generated" + "/" + Utils.formatDate(Utils.lastUsedDate) + "/" + "feature_user.arff" );
        Utils.createFileIfNotExists( Utils.featureUserFile );

        GaitHelperFunctions.createFeaturesFileFromRawFile(
                Utils.rawdataUserFile.getAbsolutePath(),      // in
                Utils.featureUserFile.getAbsolutePath().substring(0, Utils.featureUserFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
                "noUserId"                          // in
        );

        mp_feature.start();

        GaitHelperFunctions.mergeEquallyArffFiles(
                Utils.featureNegativeDummyFile.getAbsolutePath(),  // in
                Utils.featureUserFile.getAbsolutePath()            // in and out
        );

        Log.d(TAG, "createFeature: OUT");
    }

    private void createModel() {
        Log.d(TAG, "createModel: IN");

        try {
            IGaitModelBuilder builder = new GaitModelBuilder();

            Classifier classifier = builder.createModel(
                    Utils.featureUserFile.getAbsolutePath()     // in
            );

            mp_model.start();

            Utils.modelUserFile = new File(Utils.internalFilesRoot + "/" + "generated" + "/" + Utils.formatDate(Utils.lastUsedDate) + "/" + "model.mdl");
            Utils.createFileIfNotExists(Utils.modelUserFile);

            ((GaitModelBuilder) builder).saveModel(
                    classifier,                             // in
                    Utils.modelUserFile.getAbsolutePath()   // in
            );

        } catch (Exception e) {
            Toast.makeText(mContext, "Model Generating failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Model Generating failed!");
            e.printStackTrace();
        }

        Log.d(TAG, "createModel: OUT");
    }

    private void updateGait() {

        double d = Utils.checkUserInPercentage();

        Toast.makeText(mContext,"%%% "+ d +" %%%",Toast.LENGTH_LONG).show();

        Log.i(TAG, "==============================");
        Log.i(TAG, "============="+ d +"============");
        Log.i(TAG, "==============================");

    }

    public void startRecording(){
        if(mIsRecording == false){
            resetRecording();
            mIsRecording = true;
            mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void stopRecording(){
        if(mIsRecording == true) {
            mSensorManager.unregisterListener(mAccelerometerEventListener);
            mIsRecording = false;
        }
    }

    public void resumeRecording(){
        if( !mIsRecording){
            mIsRecording = true;
        }
    }

    public void resetRecording(){
        if( !mIsRecording){
            mRecordCount = 0;
            mAccelerometerArray.clear();
        }
    }

    public ArrayDeque<Accelerometer> getAcceleromerList(){
        return mAccelerometerArray;
    }

    public boolean isRecording(){
        return mIsRecording;
    }

    /*private void arrayLeftShift(){
        long c = 0;
        for(Accelerometer a : mAccelerometerArray){
            if(c == 0){
                ++c;
            }else{
                mAccelerometerArray.
            }

        }
    }*/

    private void printAccelerometerList(ArrayDeque<Accelerometer> list){
        int idx = 0;
        for(Accelerometer a : list){
            Log.i(TAG, "AccelerometerList[" + idx +"]: "
                    + a.getTimeStamp() + "\t"
                    + a.getX() + "\t"
                    + a.getY() + "\t"
                    + a.getZ() + "\t"
                    + a.getStep() );
            ++idx;
        }
        Log.i(TAG, "AccelerometerList: end");
    }

    //region HELP
    /*
    ArrayList<Accelerometer> mAccelerometerArray ==> String str

    output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
    */
    //endregion
    /**
     * Converts array of Accelerometers to string.
     * @return the converted string.
     *
     * @author Mille Janos
     */
    public String mAccelerometerArrayToString(ArrayDeque<Accelerometer> arrayDeque) {
        Log.d(TAG, ">>>RUN>>>mAccelerometerArrayToString()");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(Accelerometer a : arrayDeque){
            if(i != arrayDeque.size()-1 ){
                sb.append(a.getTimeStamp())
                        .append(",")
                        .append(a.getX())
                        .append(",")
                        .append(a.getY())
                        .append(",")
                        .append(a.getZ())
                        .append(",")
                        .append(mStepCount)
                        .append(",");
            }else{
                sb.append(a.getTimeStamp())
                        .append(",")
                        .append(a.getX())
                        .append(",")
                        .append(a.getY())
                        .append(",")
                        .append(a.getZ())
                        .append(",")
                        .append(mStepCount);
            }
        }
        return sb.toString();
    }

    
}
