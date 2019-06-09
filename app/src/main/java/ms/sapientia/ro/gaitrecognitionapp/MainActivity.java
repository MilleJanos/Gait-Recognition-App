package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.fragment.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.presenter.LoginFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.MainActivityPresenter;


public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View {

    // Consts:
    private static final String TAG = "MainActivity";
    private static final String TAG_NAME_FRAGMENT = "FragmentList";
    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    // MVP:
    MainActivityPresenter mPresenter;

    // Static members:
    public static MainActivity Instance;
    public static Context context;

    // Members:
    private ProgressBar mProgressBar;
    private boolean mDoubleBackToExitPressedOnce = false;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPresenter = new MainActivityPresenter(this);
        context = getApplicationContext();
        Instance = this;

        
        initProgressBar();

        // Init. Firebase
        mPresenter.InitFirebase();

        // Request internet permission
        RequestInternetPermission();


        // Get FragmentManager & FragmentTransaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //region OLD CODE
        // Create LoginFragment Instance
        /*
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,new LoginFragment())
                .addToBackStack(null)
                .commit();
         */
        //endregion

        fragmentTransaction.add(R.id.fragmentContainer, new LoginFragment(), "login_fragment");
        fragmentTransaction.commit();

        //printActivityFragmentList(fragmentManager);

        initProgressBar();
        hideProgressBar();
    }


    //region OLD CODE
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
    //endregion


    protected void bindViews() {

    }

    protected void bindClickListeners() {

    }


    //region OLD CODE
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
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }
    */
    //endregion


    private void RequestInternetPermission(){
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);
            Log.e(TAG, "No Internet Permission!");
            Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();
        }
    }



    public void doublePressExit(){
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    public void addFragmentToStack(Fragment fragment, FragmentManager fragmentManager, String fragment_tag){
        mPresenter.addFragmentToStack(fragment,fragmentManager,fragment_tag);
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if( fragment instanceof LoginFragment){
            // if Login fragment is displayed -> Exit app
             doublePressExit();
        
        }else{
            if( false /*TODO: IF(logged in && Main Fragment is displayed)*/){
                // if user is logged in and Main fragment is displayed --> Exit app
                doublePressExit();
        
            }else{
                // if are fragments on top of the Login fragmen -> Remove top fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
        
            }
        }
    }



    @Override
    public void initProgressBar() {
        mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        mProgressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels,250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.addContentView(mProgressBar, params);
        //showProgressBar();
    }



    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }



    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }



}
