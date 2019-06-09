package ms.sapientia.ro.gaitrecognitionapp.presenter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.firebase.FirebaseApp;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.MainActivity;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;

public class MainActivityPresenter {

    public View view;

    public MainActivityPresenter(View view) {
        this.view = view;
    }

    public void InitFirebase(){
        FirebaseApp.initializeApp( MainActivity.Instance );
        FirebaseUtils.Init( MainActivity.Instance );
    }

    public void addFragmentToStack(Fragment fragment, FragmentManager fragmentManager, String fragment_tag){
        /*
        Fragment fragmentTwo = FragmentUtil.getFragmentByTagName(fragmentManager, "Fragment Two");

        // Because fragment two has been popup from the back stack, so it must be null.
        if(fragmentTwo==null)
        {
            fragmentTwo = new FragmentTwo();
        }
        */
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, fragment, fragment_tag);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        //MainActivity.printActivityFragmentList(fragmentManager);


    }

    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }

}
