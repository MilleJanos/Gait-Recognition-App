package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.google.firebase.FirebaseApp;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.LoginFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.RegisterFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.ActivityBase;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;

public class MainActivity extends ActivityBase {

    private static final String TAG = "MainActivity";
    private static final String TAG_NAME_FRAGMENT = "FragmentList";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init. Firebase
        FirebaseApp.initializeApp(this);
        FirebaseUtils.Init( MainActivity.this );

        // Check internet permission
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);

            Log.e(TAG, "No Internet Permission!");
            Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();
        }

        // MVP test
        //mLoginPresenter = new LoginPresenter(this);
        //EVENT:  button-onClick: mLoginPresenter.onLogin(_,_);

        // Get FragmentManager & FragmentTransaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Create LoginFragmentPresenter instance
        /*
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,new LoginFragmentPresenter())
                .addToBackStack(null)
                .commit();
         */
         fragmentTransaction.add(R.id.fragmentContainer, new LoginFragmentPresenter(), "login_fragment");
         fragmentTransaction.commit();

        //printActivityFragmentList(fragmentManager);
    }

/*
    // Print fragment manager managed fragment in debug log.
    public static void printActivityFragmentList(FragmentManager fragmentManager)
    {
        // Get all Fragment list.
        List<Fragment> fragmentList = fragmentManager.getFragments();

        if(fragmentList!=null)
        {
            int size = fragmentList.size();
            for(int i=0;i<size;i++)
            {
                Fragment fragment = fragmentList.get(i);

                if(fragment!=null) {
                    String fragmentTag = fragment.getTag();
                    Log.d(TAG_NAME_FRAGMENT, fragmentTag);
                }
            }

            Log.d(TAG_NAME_FRAGMENT, "***********************************");
        }
    }
*/


    @Override
    protected void bindViews() {

    }

    @Override
    protected void bindClickListeners() {

    }



    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

/*
    private void onTabSelected(int position){
        // Pop off everythink up to and including the current tab
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Add the new tab fragment
        /////fragmentManager.beginTransaction()
        /////        .replace(R.id.fragmentContainer, )
    }


    private void addFragmentOnTop(){
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragmentPresenter())
                .addToBackStack(null)
                .commit();
    }
*/
    @Override
    public void onBackPressed() {
        //TODO: ON BACK PRESSED !
        //Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        //if(fragment != null){
        //    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //    fragmentTransaction.remove(fragment);
        //    fragmentTransaction.commit();
        //}else{
        //    super.onBackPressed();
        //}
    }
}
