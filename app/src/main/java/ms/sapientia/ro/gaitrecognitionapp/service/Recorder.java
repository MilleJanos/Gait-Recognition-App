package ms.sapientia.ro.gaitrecognitionapp.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.ro.feature_extractor.Settings;
import ms.sapientia.ro.feature_extractor.Util;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.common.FileUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.logic.MyFirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ModeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ProfileFragment;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.GaitVerification;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitVerification;
import weka.classifiers.Classifier;
import weka.core.Attribute;

public class Recorder implements IRecorder {

    // Tag:
    private static final String TAG = "Recorder";
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
    private boolean mCanRecord = false; // used to finisd downloads before record
    private Sensor mAccelerometerSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mAccelerometerEventListener;
    // Other members:
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
    private Mode mMode = Mode.MODE_TRAIN;
    private boolean mTrainNewOne = true;
    // Sound Members:
    private MediaPlayer mp_bing;
    private MediaPlayer mp_feature;
    private MediaPlayer mp_model;
    private MediaPlayer mp_bing2;

    // Constructor:

    public Recorder(Context context, FirebaseAuth auth, Mode mode, boolean  train_new_one) {

        mSavedAuth = auth;
        mContext = context;
        mIsRecording = false;
        mMode = mode;
        mTrainNewOne = train_new_one;

        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelerometerSensor == null) {
            Log.d(TAG, "Recorder: Accelerometer not found!");
            Toast.makeText(mContext, "The device has no Accelerometer !", Toast.LENGTH_SHORT).show();
        }

        mAccelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mIsRecording && mCanRecord) {
                    // Get accelerometer coordinates:
                    long timeStamp = event.timestamp;
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    // Record:
                    //sensorChanged_OLD(timeStamp, x, y, z);
                    //sensorChanged(timeStamp, x, y, z);
                    sensorChanged_2(timeStamp, x, y, z);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        initDefaultValues();

        initSound();

        if( mMode == Mode.MODE_TRAIN ) {
            mCanRecord = true;
            //mCanRecord = false;     // initTrainFiles will modofy this value
            //initTrainFiles_OLD(mTrainNewOne);
        }else{
            // Auth & Data collect
            mCanRecord = true;
        }

    }

    // Init. methods:

    private void initDefaultValues(){
        sMaxAccelerometerArray = DEFAULT_MAX_ACCELEROMETER_ARRAY;
        sIntervalBetweenTests = DEFAULT_INTERVAL_BETWEEN_TESTS;
        sPreprocessingInterval = DEFAULT_PREPROCESSING_INTERVAL;
        sFilesCountBetweenModelGenerating = DEFAULT_FILES_COUNT_BEFORE_MODEL_GENERATING;
    }

    private void initSound() {
        mp_bing = MediaPlayer.create(mContext, R.raw.relentless);
        mp_bing2 = MediaPlayer.create(mContext, R.raw.open_ended);
        mp_feature = MediaPlayer.create(mContext, R.raw.feature);
        mp_model = MediaPlayer.create(mContext, R.raw.model);
    }

    private void initTrainFiles_OLD(boolean train_new_one){
        String path;

        if( train_new_one ){
            // Train new file:

            // int tf_count = AppUtil.sUser.merged_feature_count;
            // int tm_count = AppUtil.sUser.merged_model_count;
            //
            // path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + "mergedModel_" + AppUtil.sUser.id + "_" + tf_count + ".mdl";
            // AppUtil.mergedModelFile = new File(path);
            // FileUtil.createFileIfNotExists(AppUtil.mergedModelFile);
            //
            // path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + "mergedFeature_" + AppUtil.sUser.id + "_" + tm_count + ".arff";
            // AppUtil.mergedFeatureFile = new File(path);
            // FileUtil.createFileIfNotExists(AppUtil.mergedFeatureFile);

            // // Increase number of feature and model count:
            // AppUtil.sUser.merged_feature_count++;
            // AppUtil.sUser.merged_model_count++;
            //
            // // Save numbers to firebase:
            // FirebaseController.setUserObject(AppUtil.sUser);

            mCanRecord = true;

        }else {
            // Train with last merged file:
            /*
            downloadLastMergedFeature(AppUtil.sUser.id, new IFileCallback(){

                @Override
                public void Success(File file) {
                    mCanRecord = true;
                    hideProgressBar();
                }

                @Override
                public void Failure() {
                    mCanRecord = false;
                    Toast.makeText(MainActivity.sContext,"Error: downloading last train!",Toast.LENGTH_LONG).show();
                    hideProgressBar();
                }

                @Override
                public void Error(int error_code) {
                    mCanRecord = false;
                    Toast.makeText(MainActivity.sContext,"Error: downloading last train!",Toast.LENGTH_LONG).show();
                    hideProgressBar();
                }
            });
            */

        }


    }

    // Start / Stop / Resume / Reset:

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

            // Only for for _2 method:
            if( mAccelerometerArray.size() < MIN_TRAIN_LENGTH ){
                Toast.makeText(MainActivity.sContext,"No enoght data! Please Try again.", Toast.LENGTH_LONG).show();
                return;
            }
            processRecordedData();

            // // After training - create model
            // if(mMode == Mode.MODE_TRAIN){
            //     finishTrainedData();
            // }
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

    // State info. methods:

    public boolean isRecording(){
        return mIsRecording;
    }

    // Sensor on changed method:

    private void sensorChanged_2(long timeStamp, float x, float y, float z) {

        if ( mMode == Mode.MODE_TRAIN ) {
            // Record until stops.

            // Note: Train mode will triggered processRecordedData() method in stopRecording().

            if( mAccelerometerArray.size() == MIN_TRAIN_LENGTH ){
                mp_bing2.start();
            }

            // if the list is full, then remove first then (Circle effect):
            if (mAccelerometerArray.size() == sMaxAccelerometerArray - 1) {
                mAccelerometerArray.removeFirst();
            }
        }

        if ( mMode == Mode.MODE_AUTHENTICATE ){
            // Validate user in every mCurrentIntervalBetweenTests.

            if(mCurrentIntervalBetweenTests == sIntervalBetweenTests){

                mCanRecord = false; // Stop recording while processing
                mp_bing.start();
                processRecordedData();

                mCurrentIntervalBetweenTests = 0;
            }
            ++mCurrentIntervalBetweenTests;

            // if the list is full, then remove first then (Circle effect):
            if (mAccelerometerArray.size() == sMaxAccelerometerArray - 1) {
                mAccelerometerArray.removeFirst();
            }
        }

        if ( mMode == Mode.MODE_COLLECT_DATA ){

            // TODO

        }

        // add to last:
        mAccelerometerArray.addLast(new Accelerometer(timeStamp, x, y, z, mCurretStepCount));


    }

    private void processRecordedData(){

        // show progress bar:
        showProgressBar();

        // Init Internal Raw File:
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + "rawdata_" + RecorderUtils.getCurrentDateFormatted() + ".csv";
        AppUtil.rawdataUserFile = new File(path);
        FileUtil.createFileIfNotExists(AppUtil.rawdataUserFile);

        // Preprocessing raw data:
        List<Accelerometer> list = new ArrayList(Arrays.asList(mAccelerometerArray.toArray()));
        Util featureUtil = new Util();
        // Settings.setUseDynamicPreprocessingThreshold(true);
        // //Settings.setUseDynamicPreprocessingThreshold(false);
        // //Settings.setPreprocessingThreshold(10.5);
        // Settings.usingPreprocessing(true);
        // Settings.setPreprocessingInterval(sPreprocessingInterval);
        Settings.useRecommendedSettingsWithFrames();
        Settings.setDefaultUserId( AppUtil.sUser.id );

        List<Accelerometer> preprocessedList = featureUtil.preprocess(list);

        Log.i(TAG, "Info: Settings.getPreprocessingInterval() = " + Settings.getPreprocessingInterval() );
        Log.i(TAG, "Info: Settings.getPreprocessingThreshold() = " + Settings.getPreprocessingThreshold() );
        Log.i(TAG, "Info: Settings.isUsingPreprocessing() = " + Settings.isUsingPreprocessing() );
        Log.i(TAG, "Info: Settings.isUsingPreprocessing() = " + Settings.isUsingDynamicPreprocessingThreshold() );

        // Save raw file into file:
        ArrayDeque preProcAD = RecorderUtils.listToArrayDeque(preprocessedList);
        RecorderUtils.saveRawAccelerometerDataIntoCsvFile(preProcAD, AppUtil.rawdataUserFile, RecorderUtils.RAWDATA_DEFAULT_HEADER);

        // hide progress bar
        hideProgressBar();

        switch (mMode){

            case MODE_TRAIN:{
                // Create feature from raw
                // Create (trained) model from feature
                mode_train();
                break;
            }

            case MODE_AUTHENTICATE:{
                // Download ..............
                mode_authenticate();
                break;
            }

            case MODE_COLLECT_DATA:{
                // upload raw files ..........
                mode_collect_data();
                break;
            }
        }

    }

    // Modes:

    /**
     * This method calls the train() method and after train() finishes his job
     * the files and user object fill be uploaded/updated in the database.
     */
    private void mode_train(){

                //*//if (mCurrentFileCount >= sFilesCountBetweenModelGenerating - 1) {
                // Collected enought arff files to being.

                // Show progress bar
                showProgressBar();

            // Train then upload files:

            train( new IAfter(){

                @Override
                public void Do() {

                    // Note:
                    // If the file upload suceed, then the user objectt will be updated too.

                    // Upload raw file:
                    MyFirebaseController.uploadRawFileIntoStorage(AppUtil.rawdataUserFile, AppUtil.sUser.id, new ICallback() {
                        @Override
                        public void Success(Object user) {

                            // Update uer object:
                            AppUtil.sUser.raw_count++;
                            FirebaseController.setUserObject( AppUtil.sUser );
                        }

                        @Override
                        public void Failure() {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 10: Can't upload raw.");
                        }

                        @Override
                        public void Error(int error_code) {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 11: Can't upload raw.");
                        }
                    });

                    // Upload merged feature file:
                    MyFirebaseController.uploadMergedFeatureFileIntoStorage(AppUtil.mergedFeatureFile, AppUtil.sUser.id, new ICallback()
                    {
                        @Override
                        public void Success(Object user) {

                            // Update uer object:
                            AppUtil.sUser.merged_feature_count++;
                            AppUtil.sUser.authenticaiton_avg = 0;
                            AppUtil.sUser.authenticaiton_values.clear();
                            FirebaseController.setUserObject( AppUtil.sUser );

                        }

                        @Override
                        public void Failure() {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 12: Can't upload merged feature.");
                        }

                        @Override
                        public void Error(int error_code) {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 13: Can't upload merged feature.");
                        }
                    });

                    // Upload merged model to Firebase:
                    MyFirebaseController.uploadMergedModelFileIntoStorage(AppUtil.mergedModelFile, AppUtil.sAuth.getUid(), new ICallback() {
                        @Override
                        public void Success(Object user) {
                            // Update uer object:
                            AppUtil.sUser.merged_model_count++;
                            FirebaseController.setUserObject( AppUtil.sUser );
                        }

                        @Override
                        public void Failure() {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 14: Can't upload merged feature.");
                        }

                        @Override
                        public void Error(int error_code) {
                            Toast.makeText(MainActivity.sContext,"Upload error",Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ERROR 15: Can't upload merged feature.");
                        }
                    });

                    // Show progress bar
                    hideProgressBar();

                }
            });





        //    mCurrentFileCount = 0;

        //*//} else {
        //*//    // No enought data, continue collecting:
        //*//    ++mCurrentFileCount;
        //*//}
    }

    private void mode_authenticate(){

        // Stop recording while processing:
        //mCanRecord = false;

        // Download merged feature:

        downloadLastMergedFeature(AppUtil.sUser.id, new ICallback<File>() {
            @Override
            public void Success(File file) {

                // DEBUG:
                // AppUtil.mergedFeatureFile = new File(AppUtil.internalFilesRoot.getAbsoluteFile() + "/" + "debug" + "/" + "merged_feature_zsombi.arff");
                // AppUtil.rawdataUserFile   = new File(AppUtil.internalFilesRoot.getAbsoluteFile() + "/" + "debug" + "/" + "rawdata_jancsi_0.csv");

                // Calculate similarities (%), then continue recording:
                double score = calculateAuthenticationValue(
                        AppUtil.mergedFeatureFile,
                        AppUtil.rawdataUserFile,
                        AppUtil.sUser.id,
                        new IAfter(){

                            @Override
                            public void Do() {
                                // After procedding continue recording:
                                mCanRecord = true;
                            }
                        }
                );

                score = (score<0)?0:score;

                Toast.makeText(MainActivity.sContext, "---> " + Math.floor(score * 100) + " % <---", Toast.LENGTH_LONG).show();

                mp_bing2.start(); // TODO: remove it

                // Update avg. score and list
                if( AppUtil.sUser.authenticaiton_avg != 0 ){
                    AppUtil.sUser.authenticaiton_avg = (score + AppUtil.sUser.authenticaiton_avg ) / 2;
                }else{
                    AppUtil.sUser.authenticaiton_avg = score;
                }
                AppUtil.sUser.authenticaiton_values.add( score );
                FirebaseController.setUserObject( AppUtil.sUser );

                // DEBUG PRINT:
                Log.i(TAG, "Success: Files used in Authentication:\n");
                Log.i(TAG, "Success: AppUtil.rawdataUserFile =" + AppUtil.rawdataUserFile);
                Log.i(TAG, "Success: AppUtil.mergedFeatureFile =" + AppUtil.mergedFeatureFile);
                Log.i(TAG, "Success: score =" + score);
            }

            @Override
            public void Failure() {
                // After failed process continue recording:
                mCanRecord = true;
                Toast.makeText(MainActivity.sContext, "ERROR 5: Download error.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR 5: Download error.");
            }

            @Override
            public void Error(int error_code) {
                // After failed process continue recording:
                mCanRecord = true;
                Toast.makeText(MainActivity.sContext, "ERROR 6: Download error.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR 6: Download error.");
            }
        });


    }

    private void mode_collect_data(){
        StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                + "/" + AppUtil.sAuth.getUid()
                + "/" + FirebaseUtils.STORAGE_RAW_KEY
                + "/" + AppUtil.rawdataUserFile.getName()
        );
        FirebaseController.uploadFile(
                ref,
                AppUtil.rawdataUserFile,
                null);
    }

    // Other methods:

    /**
     * This method is responsible for training.
     * Train steps:
     *  1. Download negative feature from Firebase. (If can't download: return).
     *  2. Create user's feature from user's raw data.
     *  3. Merge negative feature and user's feature.
     *  4. Create merged model file from merged feature file.
     *  5. Call doIt.Do() method method.
     *
     *  @param doIt method which will becalled in the end of creating model
     */
    private void train(IAfter afterIt){

        // 1. Download negative feature from Firebase. (If can't download: return):

        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative.arff";
        AppUtil.featureNegativeFile = new File( path );

        MyFirebaseController.downloadNegativeFeature(AppUtil.featureNegativeFile, new ICallback<File>() {
            @Override
            public void Success(File file) {

                // Feature negative downloaded:

                // 2. Create user's feature from user's raw data:

                String path = AppUtil.internalFilesRoot + "/" + "feature" + "/feature_" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + ".arff";
                AppUtil.featureUserFile = new File(path);
                FileUtil.createFileIfNotExists( AppUtil.featureUserFile );

                createFeatureFromRaw(
                        AppUtil.rawdataUserFile,
                        AppUtil.featureUserFile,
                        AppUtil.sUser.id
                );

                // TODO: IF no data after "@data" then break & show error !

                // 3. Merge negative feature and user's feature into merged feature file:

                path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + "mergedFeature_" + AppUtil.sUser.id + "_" + AppUtil.sUser.merged_feature_count + ".arff";
                AppUtil.mergedFeatureFile = new File(path);
                FileUtil.createFileIfNotExists(AppUtil.mergedFeatureFile);

                mergeArffFiles(
                        AppUtil.featureNegativeFile,
                        AppUtil.featureUserFile,
                        AppUtil.mergedFeatureFile
                );

                // Show progress bar:
                MainActivity.sInstance.showProgressBar();

                // 4. Create merged model file from merged feature file:

                path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + "mergedModel_" + AppUtil.sUser.id + "_" + AppUtil.sUser.merged_model_count + ".mdl";
                AppUtil.mergedModelFile = new File(path);
                FileUtil.createFileIfNotExists(AppUtil.mergedModelFile);

                createModelFromFeature(
                        AppUtil.mergedFeatureFile,  // TODO: Timi: change it later !
                        AppUtil.mergedModelFile
                );

                // Hide progress bar:
                MainActivity.sInstance.hideProgressBar();

                // 5. Call doIt.Do() method method:
                afterIt.Do();
                
                // DEBUG PRINT:
                Log.i(TAG, "Success: Files used in Train:\n");
                Log.i(TAG, "Success: AppUtil.rawdataUserFile =" + AppUtil.rawdataUserFile);
                Log.i(TAG, "Success: AppUtil.featureUserFile =" + AppUtil.featureUserFile);
                Log.i(TAG, "Success: AppUtil.mergedFeatureFile =" + AppUtil.mergedFeatureFile);
                Log.i(TAG, "Success: AppUtil.mergedModelFile =" + AppUtil.mergedModelFile);

            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext, "ERROR 1: Download error!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext, "ERROR 2: Download error!", Toast.LENGTH_LONG).show();
            }
        });

    }


    /**
     * This method creates a feature file from a given raw file.
     * Output file and upper folders will be automatically created.
     *
     * @param inputRawFile input raw file
     * @param outputFeatureFile output feature file
     * @param userId user id contained by feature file
     */
    private void createFeatureFromRaw(File inputRawFile, File outputFeatureFile, String userId) {

        // Handle inputs:
        if( inputRawFile == null ){
            throw new InvalidParameterException("Invalid input: inputRawFile");
        }
        if( inputRawFile == null ){
            throw new InvalidParameterException("Invalid input: outputFeatureFile");
        }
        if( userId.length() == 0){
            Log.w(TAG, "createFeatureFromRaw: Parameter userId is empty!");
        }

        // Create feature file:
        FileUtil.createFileIfNotExists( outputFeatureFile );

        // Collect Feature files path, until mergeing them all:
        GaitHelperFunctions.createFeaturesFileFromRawFile(
                inputRawFile.getAbsolutePath(),         // in
                outputFeatureFile.getAbsolutePath(),    // out      //outputFeatureFile.getAbsolutePath().substring(0, outputFeatureFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
                userId                                  // in
        );

        // For later ideas:
        mUserFeatureFilesPath.add( outputFeatureFile.getAbsolutePath() ); // TODO is this used ? or we need it ?

        // Play sound:
        mp_feature.start(); // TODO: Remove sound
    }

    /**
     * This method will create a model file from the given feature file.
     * Output file will be automatically generated.
     *
     * @param inputFeature input feature file.
     * @param outputModel output model file.
     */
    private void createModelFromFeature(File inputFeature, File outputModel){

        // Handle inputs:
        if( inputFeature == null ){
            throw new InvalidParameterException("Invalid input: inputFeature can't be null!");
        }

        // Generate model:
        try {

            // Init. builder:
            IGaitModelBuilder builder = new GaitModelBuilder();

            // Create classifier:
            Classifier classifier = builder.createModel(
                    inputFeature.getAbsolutePath()  // in
            );

            // Create output file:
            FileUtil.createFileIfNotExists( outputModel );

            // Save model into output file.
            ((GaitModelBuilder) builder).saveModel(
                    classifier,                     // in
                    outputModel.getAbsolutePath()   // in
            );

        } catch (Exception e) {
            Toast.makeText(mContext, "Model Generating failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Model Generating failed! (inputFeature: " + inputFeature.getAbsolutePath() + ")");
            e.printStackTrace();
        }

        // Play sound:
        mp_model.start(); // TODO remove it
    }


    /**
     * This method merges two files into a third one.
     * Output file will be created automatically.
     *
     * @param inputFile1 input file to be merged
     * @param inputFile2 input file to be merged
     * @param outputFile output merged file
     */
    private void mergeArffFiles(File inputFile1, File inputFile2, File outputFile) {

        // Handle inputs:
        if( inputFile1 == null ){
            throw new InvalidParameterException("Invalid input: inputFile1");
        }
        if( inputFile2 == null ){
            throw new InvalidParameterException("Invalid input: inputFile2");
        }
        if( outputFile == null ){
            throw new InvalidParameterException("Invalid input: outputFile");
        }
        
        // Create aux file
        String path = AppUtil.internalFilesRoot + "/aux.arff";
        File auxFile = new File( path );
        FileUtil.createFileIfNotExists(auxFile);

        // Create output file
        FileUtil.createFileIfNotExists(outputFile);

        try {
            // Copy inputFile2 into auxFile:
            FileUtil.copy(inputFile2, auxFile);
            
        } catch (IOException e) {
            Log.e(TAG, "mergeArffFiles: Error copying file " + inputFile2.getAbsolutePath() + " into " + auxFile.getAbsolutePath() );
            e.printStackTrace();
            auxFile.delete();
        }
        
        // Merge inputFile1 into auxFile:
        GaitHelperFunctions.mergeEquallyArffFiles(
                inputFile1.getAbsolutePath(),
                auxFile.getAbsolutePath()
        );
        
        try {
            // Copy back auxFile into outputFile:
            FileUtil.copy(auxFile, outputFile);
            
        } catch (IOException e) {
            Log.e(TAG, "mergeArffFiles: Error copying file " + auxFile.getAbsolutePath() + " into " + inputFile2.getAbsolutePath() );
            e.printStackTrace();
        } finally {
            auxFile.delete();
        }
    }

    /**
     * This method merges a list of .arff feature files into one.
     * Output file will be created automatically.
     * 
     * @param toMerge input list of files
     * @param outputMergedFile output file
     */
    private void mergeArffFileList(ArrayList<String> toMerge, File outputMergedFile) {
        
        // Handle inputs:
        if( toMerge == null ){
            throw new InvalidParameterException("Invalid input: toMerge");
        }
        int counter = 0;
        for(String str : toMerge){
            if( str.isEmpty() ){
                throw new InvalidParameterException("Invalid input: toMerge[" + counter + "] is empty!");
            }
            ++counter;
        }
        if( outputMergedFile == null ){
            throw new InvalidParameterException("Invalid input: outputMergedFile");
        }

        // Create output file
        FileUtil.createFileIfNotExists(outputMergedFile);
        
        try {

            GaitHelperFunctions.mergeFeatureFiles(
                    toMerge,
                    outputMergedFile.getAbsolutePath()
            );

        } catch (IOException e) {
            Log.e(TAG, "mergeLastArffFileIntoTrain: mergeRawFiles: IOException !" );
            e.printStackTrace();
        }
    }

    private double calculateAuthenticationValue( File mergedFeature, File toVerifyRaw, String userId, IAfter doIt){
        double percentage = -1;

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier;
        try {
            //old//classifier = (RandomForest) SerializationHelper.read(new FileInputStream( mergedFeature.getAbsolutePath() ));
            classifier = builder.createModel( mergedFeature.getAbsolutePath() );



            ArrayList<Attribute> attributes = builder.getAttributes( mergedFeature.getAbsolutePath() );

            IGaitVerification verifier = new GaitVerification();
            //percentage = verifier.verifyUser(classifier, attributes, FRESH_RAWDATA_WAITING_TO_TEST ); // 3. param - user raw data
            percentage = verifier.verifyUser(classifier, attributes, toVerifyRaw.getAbsolutePath(), userId);

            // percentage = Integer.parseInt( ((percentage * 100) + "").substring(0, 2) );

            //old//} catch (FileNotFoundException e) {
            //old//    Log.e(TAG, "*********File not found!");
            //old//    e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "*********Error!");
            e.printStackTrace();
        } finally {
            // Continue recording !
            doIt.Do();
        }

        return percentage;
    }

    public ArrayDeque<Accelerometer> getAcceleromerList(){
        return mAccelerometerArray;
    }

    // Download methods:

    /**
     * This method downloads the last merged feature file of a given user.
     *
     * @param userId used id
     * @param callback methods will be run after downloading the file.
     */
    private void downloadLastMergedFeature(String userId, ICallback callback){

        // Download last trained file:
        int lastTrainedArffId = AppUtil.sUser.merged_feature_count - 1;      // last file index = count - 1
        String fileName = "mergedFeature_" + userId + "_" + lastTrainedArffId + ".arff";
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + fileName;
        AppUtil.mergedFeatureFile = new File( path );
        FileUtil.createFileIfNotExists( AppUtil.mergedFeatureFile );

        // show progress bar:
        showProgressBar();

        // Download feature file matching the file parameter name:
        MyFirebaseController.downloadFeatureFile(AppUtil.mergedFeatureFile.getName(), AppUtil.mergedFeatureFile, userId, new ICallback<File>() {
                    @Override
                    public void Success(File file) {

                        // hide progress bar:
                        hideProgressBar();

                        // callback method:
                        if(callback != null)
                            callback.Success(file);
                    }

                    @Override
                    public void Failure() {

                        // hide progress bar:
                        hideProgressBar();

                        // callback method:
                        if(callback != null)
                            callback.Failure();
                    }

                    @Override
                    public void Error(int error_code) {

                        // hide progress bar:
                        hideProgressBar();

                        // callback method:
                        if(callback != null)
                            callback.Error(error_code);
                    }
                });


        // TODO: Download last trained .arff: ( !!! OVERRIDE CURRENT !!! )

    }

    /**
     * This method downloads the last merged feature file of a given user.
     *
     * @param userId used id
     * @param callback methods will be run after downloading the file.
     */
    private void downloadLastMergedModel(String userId, ICallback callback){
        // Download last trained file:
        int lastTrainedModelId = AppUtil.sUser.merged_model_count - 1; // last file index = count - 1
        String fileName = "mergedModel_" + userId + "_" + lastTrainedModelId + ".mdl";
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "merged" + "/" + fileName;
        AppUtil.mergedModelFile = new File( path );
        FileUtil.createFileIfNotExists( AppUtil.mergedModelFile);

        // show progress bar:
        showProgressBar();

        MyFirebaseController.downloadModelFile(AppUtil.mergedModelFile.getName(), AppUtil.mergedModelFile, userId, new ICallback<File>() {
            @Override
            public void Success(File nullFile) {

                // hide progress bar:
                hideProgressBar();

                // callback method:
                if(callback != null)
                    callback.Success( new File( path ) );
            }

            @Override
            public void Failure() {

                // hide progress bar:
                hideProgressBar();

                // callback method:
                if(callback != null)
                    callback.Failure();
            }

            @Override
            public void Error(int error_code) {
                // hide progress bar:
                hideProgressBar();

                // callback method:
                if(callback != null)
                    callback.Error(error_code);
            }
        });

        // TODO: Download last trained .arff: ( !!! OVERRIDE CURRENT !!! )

    }

    // Progress bar controller methods:

    private void showProgressBar(){
        MainActivity.sInstance.showProgressBar();
    }

    private void hideProgressBar(){
        MainActivity.sInstance.hideProgressBar();
    }

    // Debug print methods:

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
