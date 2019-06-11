package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.MainFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class MainFragment extends Fragment implements MainFragmentPresenter.View {

    // View members:
    TextView helpTextViewButton;

    // MVP
    private MainFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new MainFragmentPresenter(this);
        initView(view);
        bindClickListeners();

        // Unlock fragment
        MainActivity.sInstance.unlockNavigationDrawer();
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
