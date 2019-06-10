package ms.sapientia.ro.gaitrecognitionapp.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ms.sapientia.ro.gaitrecognitionapp.MainActivity;

public class Util {

    private static final String TAG = "Util";

    //
    // Variables:
    //

    // Firebase:
    public static FirebaseAuth sAuth;
    public static StorageReference sStorageRef;
    public static FirebaseStorage sStorage;


    //
    // Static Methods
    //

    /**
     * Checks if internet connection is available
     * @return true if there is internet connection and false if not.
     */
    public static boolean requireInternetConnection() {
        Log.d(TAG, ">>>RUN>>>requireInternetConnection()");
        ConnectivityManager cm = (ConnectivityManager) MainActivity.sInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }


}
