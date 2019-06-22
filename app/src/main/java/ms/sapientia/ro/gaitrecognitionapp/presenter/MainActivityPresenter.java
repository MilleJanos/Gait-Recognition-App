package ms.sapientia.ro.gaitrecognitionapp.presenter;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class MainActivityPresenter {

    // Members:
    public View mView;
    private boolean mDoubleBackToExitPressedOnce = false;
    private ArrayList<Fragment> mFragmentStack = new ArrayList<>();


    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }

    // Constructor:
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
        //sFirestore = FirebaseStorage.getInstance();
        //sStorageReference = sFirestore.getReference();
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
     * This method replaces fragment on top of the stack.
     * @param fragment new fragment
     * @param fragmentManager application's fragment manager
     * @param fragment_tag string tag
     */
    public void replaceFragment(Fragment fragment, FragmentManager fragmentManager, String fragment_tag){
        /*
        Fragment fragmentTwo = FragmentUtil.getFragmentByTagName(fragmentManager, "Fragment Two");

        // Because fragment two has been popup from the back stack, so it must be null.
        if(fragmentTwo==null)
        {
            fragmentTwo = new FragmentTwo();
        }
        */

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

        //MainActivity.printActivityFragmentList(fragmentManager);


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
}
