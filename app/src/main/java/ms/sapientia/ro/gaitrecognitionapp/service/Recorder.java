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
import java.io.IOException;
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
    private final long INTERVAL_BETWEEN_TESTS = 100; //30*128; // after analyzing data how m
    private final int PREPROCESSING_INTERVAL = 128;
    private final long FILES_COUNT_BEFORE_MODEL_GENERATING = 3;

    // Sensor
    private boolean mIsRecording = false;
    private Sensor mAccelerometerSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mAccelerometerEventListener;

    // Vars
    private Context mContext;
    private ArrayDeque<Accelerometer> mAccelerometerArray = new ArrayDeque<>();
    private ArrayDeque<String> mUploadableFilesPath = new ArrayDeque<>();
    private ArrayList<String> mUserFeatureFilesPath = new ArrayList<>();
    private int mStepCount = 0;
    private long mRecordCount = 0;
    private long mIntervalBetweenTests = 0;
    private long mFileCount = 0;
    private boolean mCreateModel; // true= Create Model; false= Verify last created model

    // Sound
    MediaPlayer mp_bing;
    MediaPlayer mp_feature;
    MediaPlayer mp_model;


    public Recorder(Context context, boolean create_model_or_verify) {
        mContext = context;
        mIsRecording = false;
        mCreateModel = create_model_or_verify;

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

        if (Utils.downloadingNegativeDummyFile == false) {  /*NO ACTIONS UNTIL Feature negative dummy is not downloaded*/

            if (mIsRecording) {

                if(mIntervalBetweenTests == INTERVAL_BETWEEN_TESTS) {


                    mp_bing.start();
//DEBUG//
//DEBUG//
//DEBUG//                    // Init Internal Files
//DEBUG//                    //Utils.initInternalFiles();
//DEBUG//                    Utils.rawdataUserFile = new File(Utils.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + Utils.getCurrentDateFormatted() + "/" + "rawdata.csv");
//DEBUG//                    Utils.createFileIfNotExists(Utils.rawdataUserFile);
//DEBUG//
//DEBUG//                    // Preprocessing raw data
//DEBUG//                    List<Accelerometer> list = new ArrayList(Arrays.asList(mAccelerometerArray.toArray()));
//DEBUG//                    Util featureUtil = new Util();
//DEBUG//                    Settings.setUseDynamicPreprocessingThreshold(true);
//DEBUG//                    Settings.setPreprocessingInterval(PREPROCESSING_INTERVAL);
//DEBUG//                    List<Accelerometer> preprocessedList = featureUtil.preprocess(list);
//DEBUG//
//DEBUG//                    // Save raw data
//DEBUG//                    //String rawdataStr = mAccelerometerArrayToString(mAccelerometerArray);
//DEBUG//                    ArrayDeque copy = Utils.listToArrayDeque(list);
//DEBUG//                    Utils.saveRawAccelerometerDataIntoCsvFile(copy, Utils.rawdataUserFile, Utils.RAWDATA_DEFAULT_HEADER);
//DEBUG//
//DEBUG//
//DEBUG//                    if(mCreateModel){
//DEBUG//
//DEBUG//                        // mFileCount = 0; // TODO, Ez vajon szukseges ide ? (test)
//DEBUG//
//DEBUG//                        Log.d(TAG, "mFileCount = " + mFileCount);
//DEBUG//
//DEBUG//
//DEBUG//                        // If we collected enought data to being
//DEBUG//                        if (mFileCount >= FILES_COUNT_BEFORE_MODEL_GENERATING - 1) {
//DEBUG//
//DEBUG//                            // Download Dummy
//DEBUG//                            if (Utils.rawdataUserFile.length() > 0) { // if the file is not empty
//DEBUG//
//DEBUG//                                Utils.featureNegativeDummyFile = new File(Utils.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff");
//DEBUG//                                Utils.createFileIfNotExists(Utils.featureNegativeDummyFile);
//DEBUG//
//DEBUG//                                StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
//DEBUG//                                        FirebaseUtils.STORAGE_FEATURES_KEY
//DEBUG//                                                + "/" + FirebaseUtils.firebaseDummyFileName
//DEBUG//                                );
//DEBUG//                                if (true /*Utils.downloadingNegativeDummyFile IS EMPTY*/) {        // If it's not downloaded
//DEBUG//
//DEBUG//                                    //if (Utils.downloadingNegativeDummyFile == false) {              // and if it's not downloading at the moment, then: download
//DEBUG//
//DEBUG//                                    Utils.downloadingNegativeDummyFile = true;
//DEBUG//
//DEBUG//                                    FirebaseUtils.downloadFileFromFirebaseStorage(reference, Utils.featureNegativeDummyFile, new FinishedCallback() {
//DEBUG//                                        @Override
//DEBUG//                                        public void onCallback(int errorCode) {                 // after onSuccess or onFailure
//DEBUG//
//DEBUG//                                            if (errorCode == 0) { // onSuccess
//DEBUG//
//DEBUG//                                                Toast.makeText(mContext, "Negative Data downloaded", Toast.LENGTH_SHORT).show();
//DEBUG//
//DEBUG//
//DEBUG//
//DEBUG//                                                createFeature();
//DEBUG//
//DEBUG//                                                mergeArffFiles();
//DEBUG//                                                mUserFeatureFilesPath.clear();
//DEBUG//
//DEBUG//                                                createModel();
//DEBUG//
//DEBUG//                                                createFeatureUserFileCopy();
//DEBUG//
//DEBUG//                                                // updateGait();   // uses last created feature file
//DEBUG//
//DEBUG//
//DEBUG//                                            } else { // onFailure or other error
//DEBUG//                                                Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
//DEBUG//                                            }
//DEBUG//
//DEBUG//                                            Utils.downloadingNegativeDummyFile = false;
//DEBUG//
//DEBUG//                                    Toast.makeText(mContext, "Negative Data already downloaded", Toast.LENGTH_SHORT).show();
//DEBUG//
//DEBUG//                                    createFeature();
//DEBUG//
//DEBUG//                                    mergeArffFiles();
//DEBUG//                                    mUserFeatureFilesPath.clear();
//DEBUG//
//DEBUG//                                    createModel();
//DEBUG//
//DEBUG//                                    createFeatureUserFileCopy();
//DEBUG//
//DEBUG//                                    //updateGait();   // uses last created feature file
//DEBUG//
//DEBUG//
//DEBUG//                                }
//DEBUG//                            } else {
//DEBUG//                                Log.e(TAG, "updateRecordedList: Rawdata path is empty!");
//DEBUG//                            }
//DEBUG//
//DEBUG//                            mFileCount = 0;
//DEBUG//
//DEBUG//                        } else {  // If it's not enought data
//DEBUG//
//DEBUG//                            createFeature();
//DEBUG//
//DEBUG//                            //mergeArffFiles();
//DEBUG//                            //mUserFeatureFilesPath.clear();
//DEBUG//
//DEBUG//                            //createModel();
//DEBUG//
//DEBUG//                            //createFeatureUserFileCopy();
//DEBUG//
//DEBUG//                            //updateGait();
//DEBUG//
//DEBUG//                            ++mFileCount;
//DEBUG//
//DEBUG//                        }
//DEBUG//
//DEBUG//                    }else{
//DEBUG//
//DEBUG//                        updateGait();   // uses last created feature file
//DEBUG//
//DEBUG//                    }
//DEBUG//
//DEBUG//
//DEBUG//                    // Reset counter
                    mIntervalBetweenTests = 0;
//DEBUG//
//DEBUG//
                }
//DEBUG//                if(mAccelerometerArray.size() == MAX_ACCELEROMETER_ARRAY - 1 ){     // if the list is full, then remove first then
//DEBUG//                    mAccelerometerArray.removeFirst();
//DEBUG//                }
//DEBUG//                mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z,mStepCount));     // add to last
                ++mRecordCount;
                ++mIntervalBetweenTests;
                // printAccelerometerList(mAccelerometerArray); // print every iteration
            }

        }  /*NO ACTIONS UNTIL Feature negative dummy is not downloaded*/


    }

    private void createFeature() {
        Log.d(TAG, "createFeature: IN");


        // Create feature file
        Utils.featureUserFile = new File ( Utils.internalFilesRoot + "/" + "generated" + "/" + Utils.formatDate(Utils.lastUsedDate) + "/" + "feature_user.arff" );
        Utils.createFileIfNotExists( Utils.featureUserFile );

        // Collect Feature files path until mergeing them all
        mUserFeatureFilesPath.add( Utils.featureUserFile.getAbsolutePath() );

        GaitHelperFunctions.createFeaturesFileFromRawFile(
                Utils.rawdataUserFile.getAbsolutePath(),      // in
                Utils.featureUserFile.getAbsolutePath().substring(0, Utils.featureUserFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
                Utils.deviceID                              // in
        );

        mp_feature.start();

        //GaitHelperFunctions.mergeEquallyArffFiles(
        //        Utils.featureNegativeDummyFile.getAbsolutePath(),  // in
        //        Utils.featureUserFile.getAbsolutePath()            // in and out
        //);

        Log.d(TAG, "createFeature: OUT");
    }

    private void createModel() {
        Log.d(TAG, "createModel: IN");

        try {
            IGaitModelBuilder builder = new GaitModelBuilder();

            Classifier classifier = builder.createModel(
                    //Utils.featureUserFile.getAbsolutePath()     // in
                    Utils.featureMergedFile.getAbsolutePath()     // in
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

    private void mergeArffFiles(){
        long num = FILES_COUNT_BEFORE_MODEL_GENERATING;
        Utils.featureMergedFile = new File(Utils.internalFilesRoot.getAbsolutePath() + "/"+ "generated" + "/"+ Utils.formatDate(Utils.lastUsedDate) + "/"  + "feature_merged_last_"+num+".arff");
        Utils.createFileIfNotExists(Utils.featureMergedFile);

        try {

            GaitHelperFunctions.mergeFeatureFiles(
                    mUserFeatureFilesPath,
                    Utils.featureMergedFile.getAbsolutePath()
            );

        } catch (IOException e) {
            Log.e(TAG, "mergeArffFiles: mergeRawFiles: IOException !" );
            e.printStackTrace();
        }
    }

    private void updateGait() {

        double d = Utils.checkUserInPercentage();

        Toast.makeText(mContext,"%%% "+ d +" %%%",Toast.LENGTH_LONG).show();

        Log.i(TAG, "==============================");
        Log.i(TAG, "============="+ d +"============");
        Log.i(TAG, "==============================");

    }

    private void createFeatureUserFileCopy(){
        Utils.featureUserFile_Copy = new File(Utils.internalFilesRoot + "/" + "generated" + "/" + Utils.formatDate(Utils.lastUsedDate) + "/" + "aux_feature_user.arff");
        Utils.createFileIfNotExists(Utils.featureUserFile_Copy);

        try {

            Utils.copy(Utils.featureUserFile, Utils.featureUserFile_Copy);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createFeatureUserFileCopy: File not found ! ( "+ Utils.featureUserFile +" or "+ Utils.featureUserFile_Copy +")");
        }
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
