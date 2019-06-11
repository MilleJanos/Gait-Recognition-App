package ms.sapientia.ro.gaitrecognitionapp.common;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    /**
     * Method that hides the keyboard in the given activity
     *
     * @param activity the activity context where the method will hide the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        // If keyboard is shown then hide:
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View activityOnFocusView = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (activityOnFocusView == null) {
            activityOnFocusView = new View(activity);
        }
        imm.hideSoftInputFromWindow(activityOnFocusView.getWindowToken(), 0);
    }


}
