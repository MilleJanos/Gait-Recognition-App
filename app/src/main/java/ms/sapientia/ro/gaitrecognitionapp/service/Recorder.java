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
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import weka.classifiers.Classifier;

public class Recorder {

    private static final String TAG = "Recorder";

    // Constants
    private static final long DEFAULT_MAX_ACCELEROMETER_ARRAY = 30*128;
    private static final long DEFAULT_INTERVAL_BETWEEN_TESTS = 30*128; // after analyzing data how m
    private static final int DEFAULT_PREPROCESSING_INTERVAL = 128;
    private static final long DEFAULT_FILES_COUNT_BEFORE_MODEL_GENERATING = 3;

    private long sMaxAccelerometerArray;
    private long sIntervalBetweenTests;
    private int  sPreprocessingInterval;
    private long sFilesCountBetweenModelGenerating;

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
    private boolean mCreateModel = true; // true= Create Model; false= Verify last created model

    // Sound
    MediaPlayer mp_bing;
    MediaPlayer mp_feature;
    MediaPlayer mp_model;


    public Recorder(Context context, boolean create_model_or_verify) {

        initDefaultValues();

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
                sensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        initSound();
    }

    private void initDefaultValues(){
        sMaxAccelerometerArray = DEFAULT_MAX_ACCELEROMETER_ARRAY;
        sIntervalBetweenTests = DEFAULT_INTERVAL_BETWEEN_TESTS;
        sPreprocessingInterval = DEFAULT_PREPROCESSING_INTERVAL;
        sFilesCountBetweenModelGenerating = DEFAULT_FILES_COUNT_BEFORE_MODEL_GENERATING;
    }

    private void initSound() {
        mp_bing = MediaPlayer.create(mContext, R.raw.relentless);
        mp_feature = MediaPlayer.create(mContext, R.raw.feature);
        mp_model = MediaPlayer.create(mContext, R.raw.model);
    }

    private void sensorChanged(SensorEvent event){
        long timeStamp = event.timestamp;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (RecorderUtils.downloadingNegativeDummyFile == false) {  /*NO ACTIONS UNTIL Feature negative dummy is not downloaded*/

            if (mIsRecording) {

                if(mIntervalBetweenTests == sIntervalBetweenTests) {

                    // PlaySound
                    mp_bing.start(); // TODO: remove sound

                    // Init Internal Files
                    //RecorderUtils.initInternalFiles();
                    RecorderUtils.rawdataUserFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + RecorderUtils.getCurrentDateFormatted() + "/" + "rawdata.csv");
                    RecorderUtils.createFileIfNotExists(RecorderUtils.rawdataUserFile);

                    // Preprocessing raw data
                    List<Accelerometer> list = new ArrayList(Arrays.asList(mAccelerometerArray.toArray()));
                    Util featureUtil = new Util();
                    Settings.setUseDynamicPreprocessingThreshold(true);
                    Settings.setPreprocessingInterval(sPreprocessingInterval);
                    List<Accelerometer> preprocessedList = featureUtil.preprocess(list);

                    // Save raw data
                    //String rawdataStr = mAccelerometerArrayToString(mAccelerometerArray);
                    ArrayDeque copy = RecorderUtils.listToArrayDeque(list);
                    RecorderUtils.saveRawAccelerometerDataIntoCsvFile(copy, RecorderUtils.rawdataUserFile, RecorderUtils.RAWDATA_DEFAULT_HEADER);


                    if(mCreateModel){

                        // mFileCount = 0; // TODO, Ez vajon szukseges ide ? (test)

                        Log.d(TAG, "mFileCount = " + mFileCount);


                        // If we collected enought data to being
                        if (mFileCount >= sFilesCountBetweenModelGenerating - 1) {

                            // Download Dummy
                            if (RecorderUtils.rawdataUserFile.length() > 0) { // if the file is not empty

                                RecorderUtils.featureNegativeDummyFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff");
                                RecorderUtils.createFileIfNotExists(RecorderUtils.featureNegativeDummyFile);

                                StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                                        FirebaseUtils.STORAGE_FEATURES_KEY
                                                + "/" + FirebaseUtils.firebaseDummyFileName
                                );
                                if (true /*RecorderUtils.downloadingNegativeDummyFile IS EMPTY*/) {        // If it's not downloaded

                                    //if (RecorderUtils.downloadingNegativeDummyFile == false) {              // and if it's not downloading at the moment, then: download

                                    RecorderUtils.downloadingNegativeDummyFile = true;

                                    FirebaseUtils.downloadFileFromFirebaseStorage(reference, RecorderUtils.featureNegativeDummyFile, new FinishedCallback() {
                                        @Override
                                        public void onCallback(int errorCode) {                 // after onSuccess or onFailure

                                            if (errorCode == 0) { // onSuccess

                                                Toast.makeText(mContext, "Negative Data downloaded", Toast.LENGTH_SHORT).show();


                                                createFeature();

                                                mergeArffFiles();
                                                mUserFeatureFilesPath.clear();

                                                createModel();

                                                createFeatureUserFileCopy();

                                                // updateGait();   // uses last created feature file


                                            } else { // onFailure or other error
                                                Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                                            }

                                            RecorderUtils.downloadingNegativeDummyFile = false;
                                        }
                                    });
                                    //}
                                }else{

                                    Toast.makeText(mContext, "Negative Data already downloaded", Toast.LENGTH_SHORT).show();

                                    createFeature();

                                    mergeArffFiles();
                                    mUserFeatureFilesPath.clear();

                                    createModel();

                                    createFeatureUserFileCopy();

                                    //updateGait();   // uses last created feature file


                                }
                            } else {
                                Log.e(TAG, "sensorChanged: Rawdata path is empty!");
                            }

                            mFileCount = 0;

                        } else {  // If it's not enought data

                            createFeature();

                            //mergeArffFiles();
                            //mUserFeatureFilesPath.clear();

                            //createModel();

                            //createFeatureUserFileCopy();

                            //updateGait();

                            ++mFileCount;

                        }

                    }else{

                        updateGait();   // uses last created feature file

                    }


                    // Reset counter
                    mIntervalBetweenTests = 0;


                }
                if(mAccelerometerArray.size() == sMaxAccelerometerArray - 1 ){     // if the list is full, then remove first then
                    mAccelerometerArray.removeFirst();
                }
                mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z,mStepCount));     // add to last
                ++mRecordCount;
                ++mIntervalBetweenTests;
                // printAccelerometerList(mAccelerometerArray); // print every iteration
            }

        }  /*NO ACTIONS UNTIL Feature negative dummy is not downloaded*/


    }

    private void createFeature() {
        Log.d(TAG, "createFeature: IN");


        // Create feature file
        RecorderUtils.featureUserFile = new File ( AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "feature_user.arff" );
        RecorderUtils.createFileIfNotExists( RecorderUtils.featureUserFile );

        // Collect Feature files path until mergeing them all
        mUserFeatureFilesPath.add( RecorderUtils.featureUserFile.getAbsolutePath() );

        GaitHelperFunctions.createFeaturesFileFromRawFile(
                RecorderUtils.rawdataUserFile.getAbsolutePath(),      // in
                RecorderUtils.featureUserFile.getAbsolutePath().substring(0, RecorderUtils.featureUserFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
                RecorderUtils.deviceID                              // in
        );

        mp_feature.start();

        //GaitHelperFunctions.mergeEquallyArffFiles(
        //        RecorderUtils.featureNegativeDummyFile.getAbsolutePath(),  // in
        //        RecorderUtils.featureUserFile.getAbsolutePath()            // in and out
        //);

        Log.d(TAG, "createFeature: OUT");
    }

    private void createModel() {
        Log.d(TAG, "createModel: IN");

        try {
            IGaitModelBuilder builder = new GaitModelBuilder();

            Classifier classifier = builder.createModel(
                    //RecorderUtils.featureUserFile.getAbsolutePath()     // in
                    RecorderUtils.featureMergedFile.getAbsolutePath()     // in
            );

            mp_model.start();

            RecorderUtils.modelUserFile = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "model.mdl");
            RecorderUtils.createFileIfNotExists(RecorderUtils.modelUserFile);

            ((GaitModelBuilder) builder).saveModel(
                    classifier,                             // in
                    RecorderUtils.modelUserFile.getAbsolutePath()   // in
            );

        } catch (Exception e) {
            Toast.makeText(mContext, "Model Generating failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Model Generating failed!");
            e.printStackTrace();
        }

        Log.d(TAG, "createModel: OUT");
    }

    private void mergeArffFiles(){
        long num = sFilesCountBetweenModelGenerating;
        RecorderUtils.featureMergedFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/"+ "generated" + "/"+ RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/"  + "feature_merged_last_"+num+".arff");
        RecorderUtils.createFileIfNotExists(RecorderUtils.featureMergedFile);

        try {

            GaitHelperFunctions.mergeFeatureFiles(
                    mUserFeatureFilesPath,
                    RecorderUtils.featureMergedFile.getAbsolutePath()
            );

        } catch (IOException e) {
            Log.e(TAG, "mergeArffFiles: mergeRawFiles: IOException !" );
            e.printStackTrace();
        }
    }

    private void updateGait() {

        double d = RecorderUtils.checkUserInPercentage();

        Toast.makeText(mContext,"%%% "+ d +" %%%",Toast.LENGTH_LONG).show();

        Log.i(TAG, "==============================");
        Log.i(TAG, "============="+ d +"============");
        Log.i(TAG, "==============================");

    }

    private void createFeatureUserFileCopy(){
        RecorderUtils.featureUserFile_Copy = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "aux_feature_user.arff");
        RecorderUtils.createFileIfNotExists(RecorderUtils.featureUserFile_Copy);

        try {

            RecorderUtils.copy(RecorderUtils.featureUserFile, RecorderUtils.featureUserFile_Copy);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createFeatureUserFileCopy: File not found ! ( "+ RecorderUtils.featureUserFile +" or "+ RecorderUtils.featureUserFile_Copy +")");
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
