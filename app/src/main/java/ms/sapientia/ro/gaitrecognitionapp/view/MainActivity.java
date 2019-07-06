package ms.sapientia.ro.gaitrecognitionapp.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.MainActivityPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.LoginFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.auth.RegisterFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.EditProfileFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.HelpFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.HomeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ModeFragment;
import ms.sapientia.ro.gaitrecognitionapp.view.menu.ProfileFragment;

/**
 * This class is the main core of the application.
 * All fragments are shown in the MainActivities FrameLayout.
 *
 * @author MilleJanos
 */
public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View, NavigationView.OnNavigationItemSelectedListener{

    // Constant members:
    private final String TAG = "MainActivity";
    private final String TAG_NAME_FRAGMENT = "FragmentList";
    private final String BACK_STACK_ROOT_TAG = "root_fragment";
    // Static members:
    public static MainActivity sInstance;
    public static Context sContext;
    public static boolean sIsProgressBarShown = false;
    public static boolean sFirstRun = false; // only run the intro animation once on login page;
    // View Members:
    private static MainActivityPresenter mPresenter;
    private ProgressBar mProgressBar;
    private TextView mProgressBarTextView;
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private FrameLayout mProgressBarDismiss;
    // Members:
    public IAfter mOnProgressBarDismissed = null;


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
        mPresenter.RequestInternetPermission();

        // Get FragmentManager & FragmentTransaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Bind views and listeners
        bindViews();
        bindClickListeners();

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

        // Lock navigation drawer:
        lockNavigationDrawer();

        // Check if app is started by Notification click:
        // askForStoppingService();
    }

    /**
     * This method binds view elements
     * to click listeners.
     */
    protected void bindClickListeners() {
        mProgressBarDismiss.setOnClickListener( v -> {
            Dismiss();
        });

        NavigationView navigationView = MainActivity.sInstance.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        headerView.findViewById(R.id.nav_header_name).setOnClickListener( v -> {
            goToEditProfile();
        });

        headerView.findViewById(R.id.nav_header_email).setOnClickListener( v -> {
            goToEditProfile();
        });

    }

    /**
     * This method initiates the toolbar view.
     */
    private void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * This method initiates the navigation drawer view.
     */
    private void initNavigationMenuDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.getMenu().getItem(0).setChecked(true);

        // Default selected item (Home)
        navigationView.setCheckedItem(R.id.nav_home);

        // Start locked (unlock after login)
        lockNavigationDrawer();

        // Set name and email:


    }

    /**
     * This method refreshes the informations from AppUtil sUser and sAuth
     */
    public void refreshNavigationMenuDraverNameAndEmail(){

        String userName;
        if( AppUtil.sUser.first_name.isEmpty() || AppUtil.sUser.last_name.isEmpty() ){
            ((TextView) findViewById(R.id.nav_header_name)).setTextColor( R.color.colorGray5 );
            userName = "Press here to set your name.";
        }else{
            ((TextView) findViewById(R.id.nav_header_name)).setTextColor( R.color.colorBlack );
            userName = AppUtil.sUser.first_name + " " + AppUtil.sUser.last_name;
        }
        String email = AppUtil.sAuth.getCurrentUser().getEmail();

        NavigationView navigationView = MainActivity.sInstance.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.nav_header_name)).setText( userName );
        ((TextView) headerView.findViewById(R.id.nav_header_email)).setText( email );
    }

    /**
     * This method locks the navigation drawer in closed state.
     */
    public void lockNavigationDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        // hide toolbar:
        hideToolbar();
    }

    /**
     * This method ullocks the navigation drawer.
     */
    public void unlockNavigationDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        // show toolbar:
        showToolbar();
    }

    /**
     * This method shows the toolbar.
     */
    public void hideToolbar(){
        getSupportActionBar().hide();
    }

    /**
     * This method hides the toolbar.
     */
    public void showToolbar(){
        getSupportActionBar().show();
    }

    /**
     * This method binds the view elements
     * using findViewById().
     */
    protected void bindViews() {
        mProgressBarDismiss = findViewById(R.id.progress_bar_darker_frameLayout);
    }

    /**
     * This method opens the edit profile page.
     */
    private void goToEditProfile(){
        closeNavigationDrawer();
        replaceFragment(new EditProfileFragment(), "edit_profile_fragment");
        ((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_profile);
    }

    /**
     * This method is a custom onDismiss.
     * It is called when the darker panel (which is filling the screen) is pressed.
     * Runs the method specified for it.
     */
    public void Dismiss(){
        if( mOnProgressBarDismissed != null ){
            mOnProgressBarDismissed.Do();
            hideProgressBar();
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
     * Removes the fragment from fragment stack.
     * @param fragment fragment which should be removed
     */
    public void removeFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPresenter.removeFragment(fragment, fragmentManager);
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
    public void showProgressBar(IAfter after) {
        mOnProgressBarDismissed = after;
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarDismiss.setVisibility( View.VISIBLE );
        sIsProgressBarShown = true;
    }

    /**
     * This method makes the progress bar visible.
     */
    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        sIsProgressBarShown = true;
    }

    /**
     * This method makes the progress bar invisible.
     */
    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBarDismiss.setVisibility( View.GONE );
        sIsProgressBarShown = false;
    }

    /**
     * This method opens the navigation menu drawer.
     */
    public void openNavigationDrawer(){
        if (mDrawer != null && ( ! mDrawer.isDrawerOpen(GravityCompat.START) ) ) {
            mDrawer.openDrawer(GravityCompat.START);
            return;
        }
    }

    /**
     * This method closes the navigation menu drawer.
     */
    public void closeNavigationDrawer(){
        if (mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
            return;
        }
    }

    /**
     * This method runs when item is selected in navigation drawer.
     * Opens the selected page.
     * @param menuItem item clicked.
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        switch (id){
            case R.id.nav_home: {
                MainActivity.sInstance.replaceFragment(new HomeFragment(), "home_fragment");
                break;
            }
            case R.id.nav_profile: {
                MainActivity.sInstance.replaceFragment(new ProfileFragment(), "profile_fragment");
                break;
            }
            case R.id.nav_mode: {
                MainActivity.sInstance.replaceFragment(new ModeFragment(), "mode_fragment");
                break;
            }
            //case R.id.nav_settings: {
            //    //MainActivity.sInstance.replaceFragment(new SettingsFragment(), "settings_fragment");
            //    break;
            //}
            case R.id.nav_help: {
                MainActivity.sInstance.replaceFragment(new HelpFragment(), "help_fragment");
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

    /**
     * This method runs when the device's back button is pressed.
     * If the user is on the login fragment or is logged in and is on the
     * main fragmentt then only exits the app on double press.
     * Any other cases removes the top fragment from stack.
     */
    @Override
    public void onBackPressed() {

        // Close drawer menu:
        closeNavigationDrawer();

        // Change fragment:
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if( fragment instanceof LoginFragment
                || fragment instanceof RegisterFragment
                || fragment instanceof HomeFragment ){

            mPresenter.doublePressExit();
            return;
        }

        if( fragment instanceof EditProfileFragment){
            // if Edit Profile fragment is displayed -> open Profile
            replaceFragment(new ProfileFragment(),"profile_fragment");
            // set selected item: Home:
            ((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_profile);
            return;
        }

        // any other cases: Open Home
        replaceFragment(new HomeFragment(), "home_fragment");
        // set selected item: Home:
        ((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_home);

    }

    // public static boolean sStartedFromNotification = true;
    // /**
    //  * This method asks the user, if he/she wants to stop the running service
    //  */
    // public void askForStoppingService(){
    //     // ONLY FOR FUTURE USAGES
    //     String boolStr = getIntent().getStringExtra("started_by_notification");
    //     if( boolStr != null && boolStr.equals("true") ){
    //         sStartedFromNotification = true;
    //     }else{
    //         sStartedFromNotification = false;
    //     }
    // }

}

/*
EXAMPLE HOW TO REACH DRAWER MENU HEADER ITEMS

navigationMenuUserName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
navigationMenuEmail =    navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

OPEN NAVIGATION DRAWER

DrawerLayout mDrawerLayout = (DrawerLayout) getView().findViewById(R.id.nav_view);
mDrawerLayout.openDrawer(mDrawerLayout);
*/
