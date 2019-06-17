package ms.sapientia.ro.gaitrecognitionapp.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.presenter.MainActivityPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.RegisterFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.HomeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ModeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ProfileFragment;

/*
EXAMPLE HOW TO REACH DRAWER MENU HEADER ITEMS

navigationMenuUserName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
navigationMenuEmail =    navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

OPEN NAVIGATION DRAWER

DrawerLayout mDrawerLayout = (DrawerLayout) getView().findViewById(R.id.nav_view);
mDrawerLayout.openDrawer(mDrawerLayout);

*/

public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View, NavigationView.OnNavigationItemSelectedListener{

    // Constants:
    private final String TAG = "MainActivity";
    private final String TAG_NAME_FRAGMENT = "FragmentList";
    private final String BACK_STACK_ROOT_TAG = "root_fragment";

    // Static members:
    public static MainActivity sInstance;
    public static Context sContext;

    // Members:
    private static MainActivityPresenter mPresenter;
    private ProgressBar mProgressBar;
    private TextView mProgressBarTextView;
    Toolbar mToolbar;
    DrawerLayout mDrawer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

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
        //addFragment(new LoginFragment(), "login_fragment");
        replaceFragment(new LoginFragment(), "login_fragment");

        // Init progress bar
        initProgressBar();
        hideProgressBar();

        // Init toolbar:
        initToolbar();

        // Init Navigation Menu Drawer:
        initNavigationMenuDrawer();

        // Lock navigation drawer
        lockNavigationDrawer();
    }

    private void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationMenuDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.getMenu().getItem(0).setChecked(true);

        // Default selected item (Home)
        navigationView.setCheckedItem(R.id.nav_home);

        // Start locked (unlock after login)
        lockNavigationDrawer();
    }

    public void lockNavigationDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        // hide toolbar
        hideToolbar();
    }

    public void unlockNavigationDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        // show toolbar
        showToolbar();
    }

    private void hideToolbar(){
        getSupportActionBar().hide();
    }

    private void showToolbar(){
        getSupportActionBar().show();
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
     * @param fragment_tag string tag
     */
    public void addFragment(Fragment fragment, String fragment_tag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPresenter.addFragment(fragment,fragmentManager,fragment_tag);
    }

    /**
     * This method calls the presenters method which replaces the fragment on top of the stack.
     * @param fragment new fragment
     * @param fragment_tag string tag
     */
    public void replaceFragment(Fragment fragment, String fragment_tag){

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPresenter.replaceFragment(fragment,fragmentManager,fragment_tag);

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

        if( fragment instanceof LoginFragment
                || fragment instanceof RegisterFragment
                || fragment instanceof HomeFragment ){

            mPresenter.doublePressExit();
            return;
        }


        //if( fragment instanceof EditProfileFragment){
        //    // if Edit Profile fragment is displayed -> open Profile
        //    replaceFragment(ProfileFragment,"profile_fragment");
        //    // set selected item: Home:
        //    NavigationView navigationView = findViewById(R.id.nav_view);
        //    navigationView.setCheckedItem(R.id.nav_profile);
        //    return;
        //}

        // any other cases: Open Home
        replaceFragment(new HomeFragment(), "home_fragment");
        // set selected item: Home:
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);


        //region OLD code
        /*if( fragment instanceof LoginFragment){
            // if Login fragment is displayed -> Exit app
             mPresenter.doublePressExit();

        }else{
            if( AppUtil.sAuth != null && (fragment instanceof LoginFragment) ){
                // if user is logged in and Main fragment is displayed --> Exit app
                mPresenter.doublePressExit();

            }else{
                // if are fragments on top of the Login fragmen -> Remove top fragment
                removeFragment(fragment);
                // set selected item: Home:
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
        */
        //endregion
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
        int height = AppUtil.DeviceScreenResolution.GetHeight();
        mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);

        mProgressBar.setIndeterminate(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels,250);
        params.setMargins(0,height/3+50,0,0);
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels,250);
        //params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mProgressBar.setVisibility(View.VISIBLE);
        this.addContentView(mProgressBar, params);
        //showProgressBar();

        // Text
        //mProgressBarTextView = new TextView(this, null);
        //mProgressBarTextView.setText("Loading");
        ////mProgressBarTextView.setC
        //LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels,250);
        //params2.setMargins(0,height/3+80,0,0);
        //this.addContentView(mProgressBarTextView, params2);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        switch (id){
            case R.id.nav_home: {
                //TODO
                MainActivity.sInstance.replaceFragment(new HomeFragment(), "home_fragment");
                break;
            }
            case R.id.nav_profile: {
                MainActivity.sInstance.replaceFragment(new ProfileFragment(), "profile_fragment");
                break;
            }
            case R.id.nav_mode: {
                //TODO
                MainActivity.sInstance.replaceFragment(new ModeFragment(), "mode_fragment");
                break;
            }
            case R.id.nav_settings: {
                //TODO
                //MainActivity.sInstance.replaceFragment(new SettingsFragment(), "settings_fragment");
                break;
            }
            case R.id.nav_help: {
                //TODO
                //MainActivity.sInstance.replaceFragment(new HelpFragment(), "help_fragment");
                break;
            }
            case R.id.nav_logout: {
                AppUtil.sAuth.signOut();
                MainActivity.sInstance.replaceFragment(new LoginFragment(), "login_fragment");
                MainActivity.sInstance.lockNavigationDrawer();
                break;
            }
            case R.id.nav_exit: {
                finish();
                break;
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
