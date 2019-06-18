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
import com.google.firebase.storage.StorageReference;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.ro.feature_extractor.Settings;
import ms.sapientia.ro.feature_extractor.Util;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.common.FileUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.logic.MyFirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IFileCallback;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ModeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ProfileFragment;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.GaitVerification;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitVerification;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.SerializationHelper;

public class Recorder {

    // Tag:
    private static final String TAG = "Recorder";
    // Default members:
    private static final long DEFAULT_MAX_ACCELEROMETER_ARRAY = 33 * 128;
    private static final long DEFAULT_INTERVAL_BETWEEN_TESTS = 32 * 128;    //30*128; // after analyzing data how m
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
    private boolean mCanRecord = false; // used to finisd downloads before record
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
    private Mode mMode = Mode.MODE_TRAIN;
    private boolean mTrainNewOne = true;
    // Sound Members:
    private MediaPlayer mp_bing;
    private MediaPlayer mp_feature;
    private MediaPlayer mp_model;

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
                    //sensorChanged(timeStamp, x, y, z);
                    sensorChanged(timeStamp, x, y, z);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        initDefaultValues();

        initSound();

        if( mMode == Mode.MODE_TRAIN ) {
            mCanRecord = false;     // initTrainFiles will modofy this value
            initTrainFiles(mTrainNewOne);
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
        mp_feature = MediaPlayer.create(mContext, R.raw.feature);
        mp_model = MediaPlayer.create(mContext, R.raw.model);
    }

    private void initTrainFiles(boolean train_new_one){
        String path;

        if( train_new_one ){
            // Train new file:

            int tf_count = AppUtil.sUser.train_feature_count;
            int tm_count = AppUtil.sUser.train_model_count;

            path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "train" + "/" + "trainModel_" + AppUtil.sUser.id + "_" + tf_count + ".mdl";
            AppUtil.mergedModelFile = new File(path);
            FileUtil.createFileIfNotExists(AppUtil.mergedModelFile);

            path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "train" + "/" + "trainFeature_" + AppUtil.sUser.id + "_" + tm_count + ".arff";
            AppUtil.mergedFeatureFile = new File(path);
            FileUtil.createFileIfNotExists(AppUtil.mergedFeatureFile);

            // Increase number of feature and model count:
            AppUtil.sUser.train_feature_count++;
            AppUtil.sUser.train_model_count++;

            // Save numbers to firebase:
            FirebaseController.setUserObject(AppUtil.sUser);

            mCanRecord = true;

        }else {
            // Train last trained file:

            downloadLastMergedFeature(new IFileCallback(){

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
            // After training - create model
            if(mMode == Mode.MODE_TRAIN){
                finishTrainedData();
            }
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

    private void sensorChanged(long timeStamp, float x, float y, float z) {

        if(mCurrentIntervalBetweenTests == sIntervalBetweenTests) {
            // Limit reached.

            // Init Internal Raw File:
            String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + RecorderUtils.getCurrentDateFormatted() + "/" + "rawdata.csv";
            AppUtil.rawdataUserFile = new File(path);
            FileUtil.createFileIfNotExists(AppUtil.rawdataUserFile);

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
            Log.i(TAG, "Info: Settings.isUsingPreprocessing() = " + Settings.isUsingDynamicPreprocessingThreshold() );

            // Save raw data:
            ArrayDeque preProcAD = RecorderUtils.listToArrayDeque(preprocessedList);
            RecorderUtils.saveRawAccelerometerDataIntoCsvFile(preProcAD, AppUtil.rawdataUserFile, RecorderUtils.RAWDATA_DEFAULT_HEADER);

            // region OLD CODE
            /*
            // Download user object from database:
            class Callback implements ICallback{

                @Override
                public void Success(MyFirebaseUser obj) {
                    Log.d(TAG, "Success: ");
                }

                @Override
                public void Failure(MyFirebaseUser obj) {
                    
                }

                @Override
                public void Error(int error_code) {

                }
            }
            FirebaseController.getUserObjectById( mSavedAuth.getUid(), new Callback());
            */
            //endregion

            // DEBUG -----------------------------------------------
            //Print_UnPreprocessed_and_Preprocessed_array(list, preprocessedList);
            // \DEBUG ----------------------------------------------

            switch (mMode){

                case MODE_TRAIN:{

                    // Create feature from raw
                    // Create (trained) model from feature
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

    }

    // Modes:

    private void mode_train(){
        //*//if (mCurrentFileCount >= sFilesCountBetweenModelGenerating - 1) {
            // Collected enought arff files to being.

            // Raw --> Feature
        
            train();

            // Upload trained feature file:
            StorageReference featureRef = FirebaseUtils.firebaseStorage.getReference().child(
                    FirebaseUtils.STORAGE_DATA_KEY
                    + "/" + AppUtil.sAuth.getUid()
                    + "/" + FirebaseUtils.STORAGE_MERGED_KEY
                    + "/" + FirebaseUtils.STORAGE_FEATURE_KEY
                    + "/" + AppUtil.mergedFeatureFile.getName()
            );
            FirebaseController.uploadFile(featureRef, AppUtil.mergedFeatureFile);

            mCurrentFileCount = 0;

        //*//} else {
        //*//    // No enought data, continue collecting:
        //*//    ++mCurrentFileCount;
        //*//}
    }

    private void mode_authenticate(){
        //updateGait();

        // DOWNLOAD NEGATIVE FILE:

        StorageReference refNeg = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                + "/" + FirebaseUtils.negativeFeatureFileName
        );

        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "negativeFeature.arff";
        AppUtil.featureNegativeFile = new File(path);
        FileUtil.createFileIfNotExists(AppUtil.featureNegativeFile);

        new FirebaseController().downloadFile(refNeg, AppUtil.featureNegativeFile, new IFileCallback() {
            @Override
            public void Success(File file) {

                // DOWNLOAD LAST TRAINED MODEL FILE:

                downloadLastMergedModel(new IFileCallback() {
                    @Override
                    public void Success(File file) {

                        // File downloadad.

                        // VERIFY GAIT:
                        validateGait();

                    }

                    @Override
                    public void Failure() {
                        try {
                            // File already downloaded.

                            // VERIFY GAIT:
                            if( AppUtil.mergedModelFile != null )
                                validateGait();

                        }catch(Exception e){
                            Toast.makeText(MainActivity.sContext,"Error (3)",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void Error(int error_code) {
                        Toast.makeText(MainActivity.sContext,"Error (4)",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext,"Error (1)",Toast.LENGTH_LONG).show();
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext,"Error (2)",Toast.LENGTH_LONG).show();
            }
        });



    }

    private void validateGait(){

        File lastTrainedModelFile = AppUtil.mergedModelFile;

        // Copy feature file:
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "rawdata" + "/" + "feature_" + AppUtil.sUser.id + "_" + "copy" + ".arff";
        File newFeatureFileCopy = new File(path);
        FileUtil.createFileIfNotExists(newFeatureFileCopy);

        // TODO: ----------------- createFeatureFromRaw(); // AppUtil.rawdataUserFile ==> AppUtil.featureUserFile

        try {

            FileUtil.copy( AppUtil.featureUserFile , newFeatureFileCopy );

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate similarities (%):
        double score = calculateAuthenticationValue(
                lastTrainedModelFile,
                AppUtil.featureNegativeFile,
                newFeatureFileCopy
        );

        score = (score<0)?0:score;

        Toast.makeText(MainActivity.sContext, "---> " + score + "% <---", Toast.LENGTH_LONG).show();

        // Refresh average auth. score in firebase:
        recalculateScore( score );
    }

    private void mode_collect_data(){
        StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                + "/" + AppUtil.sAuth.getUid()
                + "/" + FirebaseUtils.STORAGE_RAW_KEY
                + "/" + AppUtil.rawdataUserFile.getName()
        );
        FirebaseController.uploadFile(ref, AppUtil.rawdataUserFile);
    }

    // Other methods:

    /**
     * This method is responsible for training.
     * Train steps:
     *  1. Download negative feature from Firebase. (If can't download: return)
     *  2. Create user's feature from user's raw data.
     *  3. Merge negative feature and user's feature.
     *  4. Upload merged feature file to Firebase.
     */
    private void train(){

        // 1. Download negative feature from Firebase. (If can't download: return)

        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff";
        AppUtil.featureNegativeFile = new File( path );

        downloadNegativeFeature(AppUtil.featureNegativeFile, new IFileCallback() {
            @Override
            public void Success(File file) {

                // Feature negative downloaded.

                // 2. Create user's feature from user's raw data.

                String path = AppUtil.internalFilesRoot + "/generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/feature_user.arff";
                AppUtil.featureUserFile = new File(path);

                createFeatureFromRaw(
                        AppUtil.rawdataUserFile,
                        AppUtil.featureUserFile,
                        AppUtil.sUser.id
                );


                // 3. Merge negative feature and user's feature into merged feature file.

                mergeArffFiles(
                        AppUtil.featureNegativeFile,
                        AppUtil.featureUserFile,
                        AppUtil.mergedFeatureFile
                );

                // 4. Upload merged feature file to Firebase.

                MyFirebaseController.uploadMergedFeatureFileIntoStorage(
                        AppUtil.mergedFeatureFile,
                        AppUtil.sUser.id
                );

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
     * This method will generate the final trained model from merged features.
     */
    public void finishTrainedData(){
        // Show progress bar:
        MainActivity.sInstance.showProgressBar();

        // Create merged model file from merged feature file:
        createModelFromFeature(
                AppUtil.mergedFeatureFile,  // TODO: Timi: change it later !
                AppUtil.mergedModelFile
        );

        // Upload model to Firebase:
        MyFirebaseController.uploadMergedModelFileIntoStorage(
                AppUtil.mergedModelFile,
                AppUtil.sAuth.getUid()
        );

        // Hide progress bar:
        MainActivity.sInstance.hideProgressBar();
    }

    /**
     * This method creates a feature file from a given raw file.
     * Output file and upper folders will be automatically created.
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
                outputFeatureFile.getAbsolutePath().substring(0, outputFeatureFile.getAbsolutePath().length() - (".arff").length()),   // out   - without ".arff" at the end
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
     * This methods downloads the negative feature from Firebase.
     * After download will call the respective IFileCallback method.
     * Output file will be automatically created.
     *
     * @param intoFile output downloaded file.
     * @param callback callback with 3 implemented methods (use null in case you don't need it).
     */
    private void downloadNegativeFeature(File intoFile, IFileCallback callback){

        // Create output file:
        FileUtil.createFileIfNotExists(intoFile);

        // Create output file:

        FileUtil.createFileIfNotExists(intoFile);

        // Set storage refrence:
        StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                + "/" + FirebaseUtils.negativeFeatureFileName);

        // Download then call callback methods:
        new FirebaseController().downloadFile(reference, AppUtil.featureNegativeFile, callback);

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
            // Copy back auxFile into inputFile2:
            FileUtil.copy(auxFile, inputFile2);
            
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
    private void mergeArffFilesIntoOne(ArrayList<String> toMerge, File outputMergedFile) {
        
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

    private double calculateAuthenticationValue( File model, File negative, File feature_copy ){
        double percentage = -1;

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier;
        try {
            classifier = (RandomForest) SerializationHelper.read(new FileInputStream( model.getAbsolutePath() )); //new RandomForest();

            // feature_negative + features_user
            GaitHelperFunctions.mergeEquallyArffFiles(
                    negative.getAbsolutePath(),
                    feature_copy.getAbsolutePath());

            ArrayList<Attribute> attributes = builder.getAttributes( feature_copy.getAbsolutePath() ); ///feature (mar letezo)

            IGaitVerification verifier = new GaitVerification();
            //percentage = verifier.verifyUser(classifier, attributes, FRESH_RAWDATA_WAITING_TO_TEST ); // 3. param - user raw data
            percentage = verifier.verifyUser(classifier, attributes, negative.getAbsolutePath() );

            // percentage = Integer.parseInt( ((percentage * 100) + "").substring(0, 2) );

        } catch (FileNotFoundException e) {
            Log.e(TAG, "*********File not found!");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "*********Error!");
            e.printStackTrace();
        }

        return percentage;
    }

    private void recalculateScore( double score ){

        // Add new score to old:
        double oldScore = AppUtil.sUser.authenticaiton_avg;

        if ( oldScore == 0){
            // If is the first First time:
            AppUtil.sUser.authenticaiton_avg = score;
        }else{
            AppUtil.sUser.authenticaiton_avg = ( oldScore + score ) / 2;
        }

        // Save to firebase:
        FirebaseController.setUserObject( AppUtil.sUser );

        // Refresh if Profile page is open:
        FragmentManager fragmentManager = MainActivity.sInstance.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if( fragment instanceof ProfileFragment){
            ProfileFragment.refreshScores();
        }

    }

    private void updateGait() {

        double d = RecorderUtils.checkUserInPercentage();

        Toast.makeText(mContext,"--> "+ d +" <--",Toast.LENGTH_LONG).show();

        Log.i(TAG, "==============================");
        Log.i(TAG, "============="+ d +"============");
        Log.i(TAG, "==============================");

    }

    private void createFeatureUserFileCopy(){
        RecorderUtils.featureUserFile_Copy = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "aux_feature_user.arff");
        FileUtil.createFileIfNotExists(RecorderUtils.featureUserFile_Copy);

        try {

            FileUtil.copy(AppUtil.featureUserFile, RecorderUtils.featureUserFile_Copy);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createFeatureUserFileCopy: File not found ! ( "+ AppUtil.featureUserFile +" or "+ RecorderUtils.featureUserFile_Copy +")");
        }
    }

    public ArrayDeque<Accelerometer> getAcceleromerList(){
        return mAccelerometerArray;
    }

    // Download methods:

    private void downloadLastMergedFeature(IFileCallback callback){

        // Download last trained file:
        int lastTrainedArffId = AppUtil.sUser.train_feature_count - 1;      // last file index = count - 1
        String fileName = "trainFeature_" + AppUtil.sUser.id + "_" + lastTrainedArffId + ".arff";
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "train" + "/" + fileName;
        File file = new File( path );
        FileUtil.createFileIfNotExists( file );

        // show progress bar:
        showProgressBar();

        // Download feature file matching the file parameter name:
        MyFirebaseController.downloadFeatureFile(file.getName(), file, AppUtil.sUser.id, new IFileCallback() {
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

    private void downloadLastMergedModel(IFileCallback callback){
        // Download last trained file:
        int lastTrainedModelId = AppUtil.sUser.train_model_count - 1; // last file index = count - 1
        String fileName = "trainModel_" + AppUtil.sUser.id + "_" + lastTrainedModelId + ".mdl";

        StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                        + "/" + AppUtil.sUser.id
                        + "/" + FirebaseUtils.STORAGE_MERGED_KEY
                        + "/" + FirebaseUtils.STORAGE_MODEL_KEY
                        + "/" + fileName
        );
        String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "train" + "/" + fileName;
        AppUtil.mergedModelFile = new File( path );
        FileUtil.createFileIfNotExists( AppUtil.mergedModelFile);

        // show progress bar:
        showProgressBar();


        new FirebaseController().downloadFile(ref, AppUtil.mergedModelFile, new IFileCallback() {
            @Override
            public void Success(File nullFile) {
                if(callback != null)
                    callback.Success( new File( path ) );
            }

            @Override
            public void Failure() {
                if(callback != null)
                    callback.Failure();
            }

            @Override
            public void Error(int error_code) {
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





    // Old methods:

    private void sensorChanged_OLD(long timeStamp, float x, float y, float z){

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
                FileUtil.createFileIfNotExists(AppUtil.rawdataUserFile);

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

                switch (mMode){

                    case MODE_TRAIN:{
                        // Merge collected Arff Files,
                        // then upload to Firebase
                        mode_train_OLD();
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

    /**
     * Converts array of Accelerometers to string.
     *
     * ArrayList<Accelerometer> mAccelerometerArray ==> String str
     *
     * output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
     *
     * @return the converted string.
     *
     * @author Mille Janos
     */
    public String mAccelerometerArrayToString_OLD(ArrayDeque<Accelerometer> arrayDeque) {
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

    private void downloadNegativeFeature_OLD(){
        // Download Dummy
        if ( AppUtil.rawdataUserFile != null && AppUtil.rawdataUserFile.length() > 0) { // if the file is not empty

            // Create new file with new date:
            AppUtil.featureNegativeFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "feature_negative_dummy.arff");
            FileUtil.createFileIfNotExists(AppUtil.featureNegativeFile);

            StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                    FirebaseUtils.STORAGE_DATA_KEY
                            + "/" + FirebaseUtils.negativeFeatureFileName
            );
            if (true /*RecorderUtils.downloadingNegativeFeatureFile IS EMPTY*/) {        // If it's not downloaded

                //if (RecorderUtils.downloadingNegativeFeatureFile == false) {              // and if it's not downloading at the moment, then: download

                RecorderUtils.downloadingNegativeFeatureFile = true;

                new FirebaseController().downloadFile(reference, AppUtil.featureNegativeFile, new IFileCallback(){

                    @Override
                    public void Success(File file) {
                        Toast.makeText(mContext, "Negative Data downloaded", Toast.LENGTH_SHORT).show();

                        generateStuff_OLD();

                        // updateGait();   // uses last created feature file
                        RecorderUtils.downloadingNegativeFeatureFile = false;
                    }

                    @Override
                    public void Failure() {
                        Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                        RecorderUtils.downloadingNegativeFeatureFile = false;
                    }

                    @Override
                    public void Error(int error_code) {
                        Toast.makeText(mContext, "Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                        RecorderUtils.downloadingNegativeFeatureFile = false;
                    }
                });
                //}
            }else{

                Toast.makeText(mContext, "Negative Data already downloaded", Toast.LENGTH_SHORT).show();

                generateStuff_OLD();

                //updateGait();   // uses last created feature file


            }
        } else {
            Log.e(TAG, "sensorChanged: Rawdata path is empty!");
        }
    }

    private void generateStuff_OLD(){
        // TODO --------------- createFeatureFromRaw();

        mergeArffFiles_OLD();
        mUserFeatureFilesPath.clear();

        createModel_OLD();

        createFeatureUserFileCopy();
    }

    private void createModel_OLD() {
        Log.d(TAG, "createModel: IN");

        try {
            IGaitModelBuilder builder = new GaitModelBuilder();

            Classifier classifier = builder.createModel(
                    //RecorderUtils.featureUserFile.getAbsolutePath()     // in
                    RecorderUtils.featureMergedFile.getAbsolutePath()     // in
            );

            mp_model.start();

            AppUtil.modelUserFile = new File(AppUtil.internalFilesRoot + "/" + "generated" + "/" + RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/" + "model.mdl");
            FileUtil.createFileIfNotExists(AppUtil.modelUserFile);

            ((GaitModelBuilder) builder).saveModel(
                    classifier,                               // in
                    AppUtil.modelUserFile.getAbsolutePath()   // in
            );

        } catch (Exception e) {
            Toast.makeText(mContext, "Model Generating failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Model Generating failed!");
            e.printStackTrace();
        }

        Log.d(TAG, "createModel: OUT");
    }

    private void mode_train_OLD(){
        // mCurrentFileCount = 0; // TODO, Ez vajon szukseges ide ? (test)
        // Log.d(TAG, "mCurrentFileCount = " + mCurrentFileCount);


        // If we collected enought arff files to being
        if (mCurrentFileCount >= sFilesCountBetweenModelGenerating - 1) {
            // Collected enought arff files to being.

            downloadNegativeFeature(AppUtil.featureNegativeFile, new IFileCallback() {
                @Override
                public void Success(File file) {

                }

                @Override
                public void Failure() {

                }

                @Override
                public void Error(int error_code) {

                }
            });

            // Reset required file counter;
            mCurrentFileCount = 0;

        } else {
            // No enought data, continue collecting:

            // TODO ------------- createFeatureFromRaw();

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

    private void mergeArffFiles_OLD(){
        long num = sFilesCountBetweenModelGenerating;
        RecorderUtils.featureMergedFile = new File(AppUtil.internalFilesRoot.getAbsolutePath() + "/"+ "generated" + "/"+ RecorderUtils.formatDate(RecorderUtils.lastUsedDate) + "/"  + "feature_merged_last_"+num+".arff");
        FileUtil.createFileIfNotExists(RecorderUtils.featureMergedFile);

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

    private void arrayLeftShift_OLD(){
        long c = 0;
        for(Accelerometer a : mAccelerometerArray){
            if(c == 0){
                ++c;
            }else{
                //mAccelerometerArray.
            }

        }
    }





}
