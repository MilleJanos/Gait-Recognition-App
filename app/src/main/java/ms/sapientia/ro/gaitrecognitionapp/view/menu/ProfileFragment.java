package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
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
    private TextView userName;
    private TextView userEmail;
    private static TextView authScore;
    private static TextView collectedScore;
    private Button clearAuthScoreButton;


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

        initProfileInformations();
    }

    private void initView(View view) {
        userName = view.findViewById(R.id.user_name_textview);
        userEmail = view.findViewById(R.id.user_email_textview);
        authScore = view.findViewById(R.id.auth_value_textview);
        collectedScore = view.findViewById(R.id.data_collected_value_textview);
        clearAuthScoreButton = view.findViewById(R.id.clear_average_button);

    }

    private void bindClickListeners() {
        clearAuthScoreButton.setOnClickListener(v -> click_clear_button());
    }

    private void click_clear_button(){
        resetAuthScore();
    }

    private void initProfileInformations(){
        userEmail.setText(AppUtil.sUser.last_name + " " + AppUtil.sUser.first_name);
        refreshScores();
    }

    private void resetAuthScore(){
        AppUtil.sUser.authenticaiton_avg = 0;
        FirebaseController.setUserObject( AppUtil.sUser );
    }

    public static void refreshScores(){
        new FirebaseController().getUserObjectById(AppUtil.sUser.id, new ICallback() {
            @Override
            public void Success(MyFirebaseUser user) {
                setAuthScore(user.authenticaiton_avg);

            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext, "Error: (5)",Toast.LENGTH_LONG).show();
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext, "Error: (6)",Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void setAuthScore(double score){
        int percantage = (int) score * 100;
        authScore.setText ( percantage + "%" );
    }

    private static void setCollectedDataScore(int score){
        collectedScore.setText( score + "" );
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
