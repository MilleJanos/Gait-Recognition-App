package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.HomeFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class HomeFragment extends NavigationMenuFragmentItem implements HomeFragmentPresenter.View {



    // Constants:
    private static final String TAG = "HomeFragment";

    // View members:
    TextView helpTextViewButton;


    // MVP
    private HomeFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new HomeFragmentPresenter(this);
        MainActivity.sInstance.setTitle("Home");

        initView(view);
        bindClickListeners();

        // Unlock fragment:
        MainActivity.sInstance.unlockNavigationDrawer();

        // Init. internal files:
        AppUtil.initInternalFiles();
    }


    private void initView(View view) {
        helpTextViewButton = view.findViewById(R.id.help_textviewbutton);
    }

    private void bindClickListeners() {
        helpTextViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHelpPage();
            }
        });
    }

    private void goToHelpPage(){

        // TODO

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
