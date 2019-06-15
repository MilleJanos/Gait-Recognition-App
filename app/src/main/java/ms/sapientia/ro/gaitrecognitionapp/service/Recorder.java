package ms.sapientia.ro.gaitrecognitionapp.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.ro.feature_extractor.Settings;
import ms.sapientia.ro.feature_extractor.Util;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ModeFragment;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import weka.classifiers.Classifier;

public class Recorder {

    // Tag:
    private static final String TAG = "Recorder";
    // Default members:
    private static final long DEFAULT_MAX_ACCELEROMETER_ARRAY = 30*128;
    private static final long DEFAULT_INTERVAL_BETWEEN_TESTS = 20 * 128;//30*128; // after analyzing data how m
    private static final int DEFAULT_PREPROCESSING_INTERVAL = 128;
    private static final long DEFAULT_FILES_COUNT_BEFORE_MODEL_GENERATING = 3;
    // Current used:
    private static long sMaxAccelerometerArray;
    private static long sIntervalBetweenTests;
    private static int  sPreprocessingInterval;
    private static long sFilesCountBetweenModelGenerating;
    // Settings class:
    public static class RecorderSettings{
        public static void setMaxAccelerometerArray(long value){
            sMaxAccelerometerArray = value;
        }
        public static void setIntervalBetweenTests(long value){
            sIntervalBetweenTests = value;
        }
        public static void setPreprocessingInterval(int value){
            sPreprocessingInterval = value;
        }
        public static void setFilesCountBetweenModelGenerating(long value){
            sFilesCountBetweenModelGenerating = value;
        }
        public static long getMaxAccelerometerArray(){
            return sMaxAccelerometerArray;
        }
        public static long getIntervalBetweenTests(){
            return sIntervalBetweenTests;
        }
        public static int getPreprocessingInterval(){
            return sPreprocessingInterval;
        }
        public static long getFilesCountBetweenModelGenerating(){
            return sFilesCountBetweenModelGenerating;
        }
    }
    // Sensor members:
    private boolean mIsRecording = false;
    private Sensor mAccelerometerSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mAccelerometerEventListener;
    // Members:
    private FirebaseAuth mSavedAuth = null;
    private Context mContext;
    private ArrayDeque<Accelerometer> mAccelerometerArray = new ArrayDeque<>();
    private ArrayDeque<String> mUploadableFilesPath = new ArrayDeque<>();
    private ArrayList<String> mUserFeatureFilesPath = new ArrayList<>();
    private int mCurretStepCount = 0;
    private long mCurrentRecordCount = 0;
    private long mCurrentIntervalBetweenTests = 0;
    private long mCurrentFileCount = 0;
    public enum Mode{ MODE_TRAIN, MODE_AUTHENTICATE, MODE_COLLECT_DATA }
    private Mode mCreateModel = Mode.MODE_TRAIN;
    // Sound Members:
    private MediaPlayer mp_bing;
    private MediaPlayer mp_feature;
    private MediaPlayer mp_model;

    // Constructor:
    public Recorder(Context context, FirebaseAuth auth, Mode create_model_or_verify) {

        initDefaultValues();

        mSavedAuth = auth;

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
                if (mIsRecording) {
                    // Get accelerometer coordinates:
                    long timeStamp = event.timestamp;
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    // Record:
                    //sensorChanged(timeStamp, x, y, z);
                    sensorChanged_NEW(timeStamp, x, y, z);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        initSound();
    }


    // Methods:


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

    private void sensorChanged_NEW(long timeStamp, float x, float y, float z) {

        if(mCurrentIntervalBetweenTests == sIntervalBetweenTests) {
            // Limit reached.

            // Init Internal Files:
            String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + RecorderUtils.getCurrentDateFormatted() + "/" + "rawdata.csv";
            AppUtil.rawdataUserFile = new File(path);
            RecorderUtils.createFileIfNotExists(AppUtil.rawdataUserFile);

            // Preprocessing raw data:
            List<Accelerometer> list = new ArrayList(Arrays.asList(mAccelerometerArray.toArray()));
            Util featureUtil = new Util();
            Settings.setUseDynamicPreprocessingThreshold(true);
            //Settings.setUseDynamicPreprocessingThreshold(false);
            //Settings.setPreprocessingThreshold(10.5);
            Settings.usingPreprocessing(true);
            Settings.setPreprocessingInterval(sPreprocessingInterval);
            List<Accelerometer> preprocessedList = featureUtil.preprocess(list);

            Log.i(TAG, "Info: Settings.getPreprocessingInterval() = " + Settings.getPreprocessingInterval() );
            Log.i(TAG, "Info: Settings.getPreprocessingThreshold() = " + Settings.getPreprocessingThreshold() );
            Log.i(TAG, "Info: Settings.isUsingPreprocessing() = " + Settings.isUsingPreprocessing() );
            Log.i(TAG, "Info: Settings.isUsingDynamicPreprocessingThreshold() = " + Settings.isUsingDynamicPreprocessingThreshold() );


            // Save raw data:
            ArrayDeque preProcAD = RecorderUtils.listToArrayDeque(preprocessedList);
            RecorderUtils.saveRawAccelerometerDataIntoCsvFile(preProcAD, AppUtil.rawdataUserFile, RecorderUtils.RAWDATA_DEFAULT_HEADER);

            // DEBUG -----------------------------------------------
            //Print_UnPreprocessed_and_Preprocessed_array(list, preprocessedList);
            // \DEBUG ----------------------------------------------

            switch (mCreateModel){

                case MODE_TRAIN:{
                    // Create feature from raw
                    // Merge feature with train,
                    // update train in Firebase
                    mode_train_NEW();
                    break;
                }

                case MODE_AUTHENTICATE:{
                    mode_authenticate();
                    break;
                }

                case MODE_COLLECT_DATA:{
                    // upload raw files
                    mode_collect_data();
                    break;
                }
            }

            // Reset counter
            mCurrentIntervalBetweenTests = 0;
        }

        // if the list is full, then remove first then (Circle effect):
        if(mAccelerometerArray.size() == sMaxAccelerometerArray - 1 ){
            mAccelerometerArray.removeFirst();
        }
        // add to last:
        mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z, mCurretStepCount));
        // iteration end: increase counters:
        ++mCurrentRecordCount;
        ++mCurrentIntervalBetweenTests;

    }


    private void sensorChanged(long timeStamp, float x, float y, float z){

        // No action until Negative feature is not downloaded.
        if ( ! RecorderUtils.downloadingNegativeFeatureFile) {
            // Negative data is downloaded.

            if(mCurrentIntervalBetweenTests == sIntervalBetweenTests) {
                // Reached the limit.

                // PlaySound
                mp_bing.start(); // TODO: remove sound

                // Init Internal Files
                //RecorderUtils.initInternalFiles();
                String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + RecorderUtils.getCurrentDateFormatted() + "/" + "rawdata.csv";
                AppUtil.rawdataUserFile = new File(path);
                RecorderUtils.createFileIfNotExists(AppUtil.rawdataUserFile);

                // Preprocessing raw data
                List<Accelerometer> list = new ArrayList(Arrays.asList(mAccelerometerArray.toArray()));
                Util featureUtil = new Util();
                Settings.setUseDynamicPreprocessingThreshold(true);
                Settings.setPreprocessingInterval(sPreprocessingInterval);
                List<Accelerometer> preprocessedList = featureUtil.preprocess(list);

                // Save raw data
                //String rawdataStr = mAccelerometerArrayToString(mAccelerometerArray);
                ArrayDeque copy = RecorderUtils.listToArrayDeque(list);
                RecorderUtils.saveRawAccelerometerDataIntoCsvFile(copy, AppUtil.rawdataUserFile, RecorderUtils.RAWDATA_DEFAULT_HEADER);

                switch (mCreateModel){

                    case MODE_TRAIN:{
                        // Merge collected Arff Files,
                        // then upload to Firebase
                        mode_train();
                        break;
                    }

                    case MODE_AUTHENTICATE:{
                        mode_authenticate();
                        break;
                    }

                    case MODE_COLLECT_DATA:{
                        // upload raw files
                        mode_collect_data();
                        break;
                    }
                }

                // Reset counter
                mCurrentIntervalBetweenTests = 0;


            }
            // if the list is full, then remove first then (Circle effect):
            if(mAccelerometerArray.size() == sMaxAccelerometerArray - 1 ){
                mAccelerometerArray.removeFirst();
            }
            // add to last:
            mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z, mCurretStepCount));
            // iteration end: increase counters:
            ++mCurrentRecordCount;
            ++mCurrentIntervalBetweenTests;
            // printAccelerometerList(mAccelerometerArray); // print every iteration


        }  /*NO ACTIONS UNTIL Feature negative dummy is not downloaded*/


    }

    private void downloadNegativeFeature(){
        // Download Dummy
        if ( AppUtil.rawdataUserFile.length() > 0) { // if the file is not empty

            // Create new file with new date:
            AppUtil.featureNegativeFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff");
            RecorderUtils.createFileIfNotExists(AppUtil.featureNegativeFile);

            StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                    FirebaseUtils.STORAGE_DATA_KEY
                    + "/" + FirebaseUtils.negativeFeatureFileName
            );
            if (true /*RecorderUtils.downloadingNegativeFeatureFile IS EMPTY*/) {        // If it's not downloaded

                //if (RecorderUtils.downloadingNegativeFeatureFile == false) {              // and if it's not downloading at the moment, then: download

                RecorderUtils.downloadingNegativeFeatureFile = true;

                FirebaseUtils.downloadFileFromFirebaseStorage(reference, AppUtil.featureNegativeFile, new FinishedCallback() {
                    @Override
                    public void onCallback(int errorCode) {                 // after onSuccess or onFailure

                        if (errorCode == 0) { // onSuccess

                            Toast.makeText(mContext, "Negative Data downloaded", Toast.LENGTH_SHORT).show();

                            generateStuff();



                            // updateGait();   // uses last created feature file


                        } else { // onFailure or other error
                            Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                        }

                        RecorderUtils.downloadingNegativeFeatureFile = false;
                    }
                });
                //}
            }else{

                Toast.makeText(mContext, "Negative Data already downloaded", Toast.LENGTH_SHORT).show();

                generateStuff();

                //updateGait();   // uses last created feature file


            }
        } else {
            Log.e(TAG, "sensorChanged: Rawdata path is empty!");
        }
    }

    private void mode_train_NEW(){
        //*//if (mCurrentFileCount >= sFilesCountBetweenModelGenerating - 1) {
            // Collected enought arff files to being.

            // Raw --> Feature

            createFeature();

            // Merge: Feature & Train --> Train
            train();

            // Upload train:
            StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                    FirebaseUtils.STORAGE_DATA_KEY
                    + "/" + AppUtil.sAuth.getUid()
                    + "/" + FirebaseUtils.STORAGE_TRAIN_KEY
                    + "/" + AppUtil.trainFeatureFile.getName()
            );
            FirebaseUtils.uploadFileToFirebaseStorage(MainActivity.sInstance, AppUtil.trainFeatureFile, ref);

            mCurrentFileCount = 0;

        //*//} else {
        //*//    // No enought data, continue collecting:
        //*//    ++mCurrentFileCount;
        //*//}
    }

    private void updateTrainFeature(){

    }

    private void mode_train(){
        // mCurrentFileCount = 0; // TODO, Ez vajon szukseges ide ? (test)
        // Log.d(TAG, "mCurrentFileCount = " + mCurrentFileCount);


        // If we collected enought arff files to being
        if (mCurrentFileCount >= sFilesCountBetweenModelGenerating - 1) {
            // Collected enought arff files to being.

            downloadNegativeFeature();

            // Reset required file counter;
            mCurrentFileCount = 0;

        } else {
            // No enought data, continue collecting:

            createFeature();

            /*
            mergeArffFiles();
            mUserFeatureFilesPath.clear();

            createModel();

            createFeatureUserFileCopy();

            updateGait();
            */

            // Increase required file count:
            ++mCurrentFileCount;
        }
    }

    private void mode_authenticate(){
        updateGait();
    }

    private void mode_collect_data(){
        StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                + "/" + AppUtil.sAuth.getUid()
                + "/" + FirebaseUtils.STORAGE_RAW_KEY
                + "/" + AppUtil.rawdataUserFile.getName()
        );
        FirebaseUtils.uploadFileToFirebaseStorage(MainActivity.sInstance, AppUtil.rawdataUserFile, ref);
    }


    private void createFeature() {
        Log.d(TAG, "createFeature: IN");


        // Create feature file
        String path = AppUtil.internalFilesRoot + "/generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/feature_user.arff";
        AppUtil.featureUserFile = new File(path);
        RecorderUtils.createFileIfNotExists( AppUtil.featureUserFile );

        // Collect Feature files path, until mergeing them all
        mUserFeatureFilesPath.add( AppUtil.featureUserFile.getAbsolutePath() );

        GaitHelperFunctions.createFeaturesFileFromRawFile(
                AppUtil.rawdataUserFile.getAbsolutePath(),      // in
                AppUtil.featureUserFile.getAbsolutePath().substring(0, AppUtil.featureUserFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
                RecorderUtils.deviceID                              // in
        );

        mp_feature.start(); // TODO: Remove sound

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

            AppUtil.modelUserFile = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "model.mdl");
            RecorderUtils.createFileIfNotExists(AppUtil.modelUserFile);

            ((GaitModelBuilder) builder).saveModel(
                    classifier,                             // in
                    AppUtil.modelUserFile.getAbsolutePath()   // in
            );

        } catch (Exception e) {
            Toast.makeText(mContext, "Model Generating failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Model Generating failed!");
            e.printStackTrace();
        }

        Log.d(TAG, "createModel: OUT");
    }


    private void train(){

        // Create file
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + AppUtil.customDIR + "/trainFeature_" + AppUtil.sAuth.getUid() + ".arff";
        AppUtil.trainFeatureFile = new File(path);
        RecorderUtils.createFileIfNotExists(AppUtil.trainFeatureFile);

        // Merge these two:
        ArrayList<String> toMerge = new ArrayList<>();
        toMerge.add( AppUtil.featureUserFile.getAbsolutePath() );
        toMerge.add( AppUtil.trainFeatureFile.getAbsolutePath() );

        String auxPath = AppUtil.internalFilesRoot + "aux_train.arff";
        File auxFile = new File(auxPath);
        AppUtil.createFileIfNotExists(auxFile);


        try {
            // Merge feature & train into aux-train file:
            GaitHelperFunctions.mergeFeatureFiles(
                    toMerge,
                    auxFile.getAbsolutePath()
            );

        } catch (IOException e) {
            Log.e(TAG, "mergeLastArffFileIntoTrain: mergeRawFiles: IOException !" );
            e.printStackTrace();
        }

        // aux-train --copy--> train:
        try {
            RecorderUtils.copy(auxFile, AppUtil.trainFeatureFile);
        } catch (IOException e) {
            Log.e(TAG, "mergeLastArffFileIntoTrain: Copy: IOException !" );
            e.printStackTrace();
        }

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

        Toast.makeText(mContext,"--> "+ d +" <--",Toast.LENGTH_LONG).show();

        Log.i(TAG, "==============================");
        Log.i(TAG, "============="+ d +"============");
        Log.i(TAG, "==============================");

    }

    private void generateStuff(){
        createFeature();

        mergeArffFiles();
        mUserFeatureFilesPath.clear();

        createModel();

        createFeatureUserFileCopy();
    }

    private void createFeatureUserFileCopy(){
        RecorderUtils.featureUserFile_Copy = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "aux_feature_user.arff");
        RecorderUtils.createFileIfNotExists(RecorderUtils.featureUserFile_Copy);

        try {

            RecorderUtils.copy(AppUtil.featureUserFile, RecorderUtils.featureUserFile_Copy);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createFeatureUserFileCopy: File not found ! ( "+ AppUtil.featureUserFile +" or "+ RecorderUtils.featureUserFile_Copy +")");
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
            mCurrentRecordCount = 0;
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
                        .append(mCurretStepCount)
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
                        .append(mCurretStepCount);
            }
        }
        return sb.toString();
    }

    private void Print_UnPreprocessed_and_Preprocessed_array(List<Accelerometer> list, List<Accelerometer> preprocessedList){
        GraphView graph1 = ModeFragment.sInstance.getView().findViewById(R.id.graph_view_1);
        graph1.setVisibility(View.VISIBLE);
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>();


        int counter = 1;
        for(Accelerometer a : getAcceleromerList()){
            series1.appendData(
                    new DataPoint(counter, Math.sqrt( a.getX()*a.getX()* + a.getY()*a.getY() + a.getZ()*a.getZ() ) ),
                    false,
                    30*128
            );
            ++counter;
            if(counter == sIntervalBetweenTests) break;
        }
        graph1.clearAnimation();
        graph1.removeAllSeries();
        graph1.addSeries( series1 );

        GraphView graph2 = ModeFragment.sInstance.getView().findViewById(R.id.graph_view_2);
        graph2.setVisibility(View.VISIBLE);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();

        int counter2 = 1;
        for(Accelerometer a : preprocessedList){
            series2.appendData(
                    new DataPoint(counter2, Math.sqrt( a.getX()*a.getX()* + a.getY()*a.getY() + a.getZ()*a.getZ() ) ),
                    false,
                    30*128
            );
            ++counter2;
            if(counter2 == sIntervalBetweenTests) break;
        }
        graph2.clearAnimation();
        graph2.removeAllSeries();
        graph2.addSeries( series2 );
    }
    
}
