package ms.sapientia.ro.gaitrecognitionapp.service;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayDeque;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.model.Accelerometer;
import ms.sapientia.ro.model_builder.GaitHelperFunctions;
import ms.sapientia.ro.model_builder.GaitModelBuilder;
import ms.sapientia.ro.model_builder.IGaitModelBuilder;
import weka.classifiers.Classifier;

public class Recorder {

    private static final String TAG = "Recorder";

    // Constants
    private final int MAX_ACCELEROMETER_ARRAY = 5000;
    private final int INTERVAL_BETWEEN_TESTS = 5000; // after analyzing data how m

    // Sensor
    private boolean mIsRecording = false;
    private Sensor mAccelerometerSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mAccelerometerEventListener;

    // Vars
    private Context mContext;
    ArrayDeque<Accelerometer> mAccelerometerArray = new ArrayDeque<>();
    private int mStepCount = 0;
    private long mRecordCount = 0;
    private long mIntervalBetweenTests = 0;


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

    }

    public void Run(){



    }

    private void updateRecordedList(SensorEvent event){
        long timeStamp = event.timestamp;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (mIsRecording) {
            if(mIntervalBetweenTests == INTERVAL_BETWEEN_TESTS){
                MediaPlayer mp = MediaPlayer.create(mContext, R.raw.relentless);
                mp.start();
                // Init Internal Files
                Utils.initInternalFiles();
                // Save raw data
                //String rawdataStr = mAccelerometerArrayToString(mAccelerometerArray);
                Utils.saveAccArrayIntoCsvFile(mAccelerometerArray, Utils.rawdataUserFile, Utils.RAWDATA_DEFAULT_HEADER);

                // Download Dummy
                if( Utils.rawdataUserFile.length() > 0 ){ // if the file is not empty
                    StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                              FirebaseUtils.STORAGE_FEATURES_KEY
                            + "/"
                            + FirebaseUtils.firebaseDummyFileName
                    );

                    FirebaseUtils.downloadFileFromFirebaseStorage( (Activity) mContext, reference, Utils.featureNegativeDummyFile, new FinishedCallback() {
                        @Override
                        public void onCallback(int errorCode) {

                            if(errorCode == 0){

                                // Generate Feature

                                GaitHelperFunctions.createFeaturesFileFromRawFile(
                                        Utils.rawdata_user_path,      // in
                                        Utils.feature_user_path.substring(0,Utils.feature_user_path.length()-(".arff").length()),   // out
                                        "noUserId"          // in
                                );

                                GaitHelperFunctions.mergeEquallyArffFiles(
                                        Utils.feature_negative_dummy_path,  // in
                                        Utils.feature_user_path             // in and out
                                );

                                // Generate Model

                                try{
                                    IGaitModelBuilder builder = new GaitModelBuilder();

                                    Classifier classifier = builder.createModel(
                                            Utils.feature_user_path     // in
                                    );

                                    ((GaitModelBuilder) builder).saveModel(
                                            classifier,             // in
                                            Utils.model_user_path   // in
                                    );

                                }catch (Exception e){
                                    Toast.makeText(mContext,"Model Generating failed!",Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Model Generating failed!");
                                    e.printStackTrace();
                                }

                            }
                            else
                            {
                                Toast.makeText(mContext,"Error downloading Negative Data!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Reset counter
                mIntervalBetweenTests= 0;
            }
            if(mAccelerometerArray.size() == MAX_ACCELEROMETER_ARRAY-1 ){
                mAccelerometerArray.removeFirst();
            }
            mAccelerometerArray.addLast(new Accelerometer(timeStamp,x,y,z,mStepCount));
            ++mRecordCount;
            ++mIntervalBetweenTests;
            // printAccelerometerList(mAccelerometerArray); // print every iteration
        }
    }

    public void startRecording(){
        if(mIsRecording == false){
            resetRecording();
            mIsRecording = true;
            mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Run();
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

    public boolean ismIsRecording(){
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
