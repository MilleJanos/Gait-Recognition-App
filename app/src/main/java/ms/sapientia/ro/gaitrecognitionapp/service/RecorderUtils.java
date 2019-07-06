package ms.sapientia.ro.gaitrecognitionapp.service;


import android.text.format.DateFormat;
import android.util.Log;


import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.List;

import ms.sapientia.ro.commonclasses.Accelerometer;

/**
 * This class contains utility methods for the Recorder class.
 *
 * @author MilleJanos
 */
public class RecorderUtils {

    private RecorderUtils() {
    }

    // Tag member:
    private static final String TAG = "RecorderUtils";
    // Constant members:
    public final static String INPUT_EXTRA_KEY = "inputextra";
    public final static String INPUT_CREATE_OR_VERIFY = "createorverify";
    public final static String CHANNEL_ID_01 = "channel_01";
    // File  members:
    public static File featureMergedFile;
    public static File featureUserFile_Copy;
    //region file-HELP
        // get full path:           file.getAbsolutePath()
        // get parent folder path:  file.getParentFile().getAbsolutePath()
        // get file name:           file.getName()
    //endregion
    // Shows that the downloading of negative feature file is ongoing or not:
    public static boolean downloadingNegativeFeatureFile = false;
    // Rawdata Default header:
    public static final String RAWDATA_DEFAULT_HEADER = "timestamp,accx,accy,accz,stepnum";
    // Members:
    public static Date lastUsedDate = new Date();
    public static String deviceID;
    // Methods:

    /**
     * This method saves the accArray<Accelerometer> list into file including header.
     *
     * @param array array that contains the data that will be writtem to the file
     * @param file descriptor of the file all the writing will be made into
     * @param headerStr inserts this string at the first line of the file, leave empty ("") if you want no header
     * @return 0 if there is no error
     * 1 if there occurred an error
     */
    public static short saveRawAccelerometerDataIntoCsvFile(ArrayDeque<Accelerometer> array, File file, String headerStr) {
        String TAG = "saveRawAcc.IntoCsvFile";
        Log.d(TAG, ">>>RUN>>>savingAccArrayIntoCSV()");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "IOException: file.createNewFile()");
                e.printStackTrace();
                return 1;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);

            // Header:
            if( ! headerStr.equals("") ){
                pw.println(headerStr);
            }

            for (Accelerometer a : array) {
                pw.println(a.toString());
            }
            pw.flush();
            pw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "******* File not found.");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        Log.d(TAG, "<<<FINISH<<<savingAccArrayIntoCSV()");
        return 0;
    }

    /**
     * This method returns the current date in yyyyMMdd_HHmmss format.
     * @return formatted date
     */
    public static String getCurrentDateFormatted(){
        Date date = new Date();
        lastUsedDate = date;
        return formatDate( date );
    }

    /**
     * This method converts the date to yyyyMMdd_HHmmss format.
     * @return formatted date
     */
    public static String formatDate(Date date){
        return DateFormat.format("yyyyMMdd_HHmmss", date.getTime()).toString();
    }

    /**
     * This method returns the current time in timestamp.
     * @return current time in timestamp
     */
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * This method converts List into ArrayDeque.
     * @param list
     * @return
     */
    public static ArrayDeque<Accelerometer> listToArrayDeque(List<Accelerometer> list){
        ArrayDeque<Accelerometer> ad = new ArrayDeque<>();

        for(Accelerometer a : list){
            ad.push(a);
        }

        return ad;
    }

}
