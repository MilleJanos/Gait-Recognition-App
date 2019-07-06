package ms.sapientia.ro.gaitrecognitionapp.presenter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class MainActivityPresenter {

    // Constant members:
    private static final String TAG = "MainActivityPresenter";
    // Member members:
    public View mView;
    private boolean mDoubleBackToExitPressedOnce = false;
    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }


    public MainActivityPresenter(View view) {
        this.mView = view;
    }

    /**
     * This method initiates Firebase.
     */
    public void InitFirebase(){
        FirebaseApp.initializeApp( MainActivity.sInstance );
        FirebaseUtils.Init( MainActivity.sInstance);

        AppUtil.sAuth = FirebaseAuth.getInstance();
        // sFirestore = FirebaseStorage.getInstance();
        // sStorageReference = sFirestore.getReference();
    }

    /**
     * This method adds a new fragment on top of the stack.
     * @param fragment new fragment
     * @param fragmentManager application's fragment manager
     * @param fragment_tag string tag
     */
    public void addFragment(Fragment fragment, FragmentManager fragmentManager, String fragment_tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, fragment, fragment_tag);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * Removes the fragment from fragment stack.
     * @param fragment fragment which should be removed
     */
    public void removeFragment(Fragment fragment, FragmentManager fragmentManager){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    /**
     * This method replaces fragment on top of the stack.
     * @param fragment new fragment
     * @param fragmentManager application's fragment manager
     * @param fragment_tag string tag
     */
    public void replaceFragment(Fragment fragment, FragmentManager fragmentManager, String fragment_tag){
        Fragment topFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if( topFragment == null ){
            // if there is nothing to replace, then add a new one:
            addFragment(fragment, fragmentManager, fragment_tag);
        }else{
            // if there is fragment to replace, then replace it:
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, fragment, fragment_tag);

            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    /**
     * This method solves the double press mechanic.
     * If this method is called twice then
     */
    public void doublePressExit(){
        if (mDoubleBackToExitPressedOnce) {
            //MainActivity.sInstance.onBackPressed();
            MainActivity.sInstance.finish();
            return;
        }

        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(MainActivity.sInstance, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * This method checks the permission for internet connectivity.
     * If permission is denied then ask the user for it.
     */
    public void RequestInternetPermission(){
        if (MainActivity.sInstance.checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.sInstance, new String[]{Manifest.permission.INTERNET}, 212);
            Log.e(TAG, "No Internet Permission!");
            Toast.makeText(MainActivity.sContext,"No Internet Permission!", Toast.LENGTH_LONG).show();
        }
    }
}
