package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.ProfileFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class ProfileFragment extends NavigationMenuFragmentItem implements ProfileFragmentPresenter.View {

    private static final String TAG = "ProfileFragment";

    // MVP:
    private ProfileFragmentPresenter mPresenter;

    // View members:
    private static TextView userName;
    private static TextView userEmail;
    private static TextView authScore;
    private static TextView collectedScore;
    private static Button refreshButton;
    private static Button clearButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new ProfileFragmentPresenter(this);
        initView(view);
        bindClickListeners();

        refreshProfileInformationsUI();
    }

    private void initView(View view) {
        userName = view.findViewById(R.id.user_name_textview);
        userEmail = view.findViewById(R.id.user_email_textview);
        authScore = view.findViewById(R.id.auth_value_textview);
        collectedScore = view.findViewById(R.id.data_collected_value_textview);
        refreshButton = view.findViewById(R.id.refresh_button);
        clearButton = view.findViewById(R.id.clear_button);

    }

    private void bindClickListeners() {
        refreshButton.setOnClickListener(v -> refreshProfileInformations() );
        clearButton.setOnClickListener(v -> resetAuthScore());
    }

    public static void refreshProfileInformationsUI(){
            setUserName( AppUtil.sUser.last_name, AppUtil.sUser.first_name);
            setEmail( AppUtil.sAuth.getCurrentUser().getEmail());
            setAuthenticationScore( AppUtil.sUser.authenticaiton_avg, true );
            setCollectedDataScore( AppUtil.sUser.raw_count );
    }

    private void refreshProfileInformations(){
        refresh_sUser(() -> refreshProfileInformationsUI());
    }

    private void resetAuthScore(){
        AppUtil.sUser.authenticaiton_avg = 0;
        AppUtil.sUser.authenticaiton_values.clear();
        FirebaseController.setUserObject( AppUtil.sUser );
        refreshProfileInformationsUI();
    }


    private static void setUserName(String firstName, String lastName){
        userName.setText( firstName + " " + lastName );
    }

    private static void setEmail(String email){
        userEmail.setText( email );
    }

    private static void setAuthenticationScore(double score, boolean usePercentage){
        if( usePercentage ){
            int percentage = (int) Math.floor( score * 100 );
            authScore.setText ( percentage + "%" );
        }else{
            authScore.setText ( score + "");
        }
    }

    private static void setCollectedDataScore(double score){
        collectedScore.setText ( score + "" );
    }

    private static void refresh_sUser(IAfter afterIt){
        new FirebaseController().getUserObjectById(AppUtil.sUser.id, new ICallback() {
            @Override
            public void Success(MyFirebaseUser user) {
                AppUtil.sUser = user;
                afterIt.Do();
            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext,"ERROR: 14", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failure: ERROR: 14: Can't download user object");
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext,"ERROR: 15", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: ERROR: 15: Can't download user object");
            }
        });
    }

    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }
}
