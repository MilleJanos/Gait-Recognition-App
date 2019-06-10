package ms.sapientia.ro.gaitrecognitionapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
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
import ms.sapientia.ro.gaitrecognitionapp.presenter.MainActivityPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.LoginFragment;


public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View {

    // Constants:
    private final String TAG = "MainActivity";
    private final String TAG_NAME_FRAGMENT = "FragmentList";
    private final String BACK_STACK_ROOT_TAG = "root_fragment";

    // Static members:
    public static MainActivity sInstance;
    public static Context sContext;

    // Members:
    private MainActivityPresenter mPresenter;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lock screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set static variables
        sContext = getApplicationContext();
        sInstance = this;

        // Set presenter for MainActivity
        mPresenter = new MainActivityPresenter(this);

        // Init. Firebase
        mPresenter.InitFirebase();

        // Request internet permission
        RequestInternetPermission();

        // Get FragmentManager & FragmentTransaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //region OLD CODE
        // Create LoginFragment sInstance
        /*
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,new LoginFragment())
                .addToBackStack(null)
                .commit();
         */
        //endregion

        // Add Login fragment to FrameLayout
        // first stack item:
        fragmentTransaction.add(R.id.fragmentContainer, new LoginFragment(), "login_fragment");
        fragmentTransaction.commit();

        // Init progress bar
        initProgressBar();
        hideProgressBar();
    }

    /**
     * This method binds the view elements
     * using findViewById().
     */
    protected void bindViews() {

    }

    /**
     * This method binds view elements
     * to click listeners.
     */
    protected void bindClickListeners() {

    }

    /**
     * This method checks the permission for internet connectivity.
     * If permission is denied then ask the user for it.
     */
    private void RequestInternetPermission(){
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 212);
            Log.e(TAG, "No Internet Permission!");
            Toast.makeText(MainActivity.this,"No Internet Permission!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method calls the presenters method which adds a new fragment on top of the stack.
     * @param fragment new fragment
     * @param fragmentManager application's fragment manager
     * @param fragment_tag string tag
     */
    public void addFragmentToStack(Fragment fragment, String fragment_tag){

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPresenter.addFragmentToStack(fragment,fragmentManager,fragment_tag);

    }

    /**
     * This method runs when the device's back button is pressed.
     * If the user is on the login fragment or is logged in and is on the
     * main fragmentt then only exits the app on double press.
     * Any other cases removes the top fragment from stack.
     */
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if( fragment instanceof LoginFragment){
            // if Login fragment is displayed -> Exit app
             mPresenter.doublePressExit();
        
        }else{
            if( false /*TODO: IF(  is logged in && (fragment instanceof LoginFragment)  )*/){
                // if user is logged in and Main fragment is displayed --> Exit app
                mPresenter.doublePressExit();
        
            }else{
                // if are fragments on top of the Login fragmen -> Remove top fragment
                removeFragment(fragment);
            }
        }
    }

    /**
     * Removes the fragment from fragment stack.
     * @param fragment fragment which should be removed
     */
    public void removeFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    /**
     * This method initiates the progress bar
     * used by any fragment.
     */
    @Override
    public void initProgressBar() {
        mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        mProgressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels,250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.addContentView(mProgressBar, params);
        //showProgressBar();
    }

    /**
     * This method makes the progress bar visible.
     */
    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * This method makes the progress bar invisible.
     */
    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
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

}
