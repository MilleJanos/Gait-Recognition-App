package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.Animator;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.HomeFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class HomeFragment extends NavigationMenuFragmentItem implements HomeFragmentPresenter.View {



    // Constants:
    private static final String TAG = "HomeFragment";

    // View members:
    private TextView mHelpTextViewButton;
    private ImageView mOpenDrawerHelpImageViewButton;
    private ImageView mOpenDrawerHelpImageView;
    private boolean mAnimationLock; // to prevent spamming the mOpenDrawerHelpImageViewButton button

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
        mHelpTextViewButton = view.findViewById(R.id.help_textviewbutton);
        mOpenDrawerHelpImageViewButton = view.findViewById(R.id.open_drawer_help_button);
        mOpenDrawerHelpImageView = view.findViewById(R.id.help_arrow_imageView);
        mOpenDrawerHelpImageView.setVisibility( View.INVISIBLE );
    }

    private void bindClickListeners() {
        mHelpTextViewButton.setOnClickListener(v -> goToHelpPage());

        mOpenDrawerHelpImageViewButton.setOnClickListener( v -> {

            if( ! mAnimationLock ) {

                mAnimationLock = true; // Lock

                // Show arrow:
                mOpenDrawerHelpImageView.setVisibility(View.VISIBLE);
                // Move then hide arrow:
                Animator.Slide(mOpenDrawerHelpImageView, 0, 0, 100, 0, 1000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animator.Slide(mOpenDrawerHelpImageView, 0, 0, 0, -60, 1000);
                        mOpenDrawerHelpImageView.setVisibility(View.INVISIBLE);
                        mAnimationLock = false; // Unlock
                    }
                }, 1500);
            }
        });

    }

    private void goToHelpPage(){
        MainActivity.sInstance.replaceFragment(new HelpFragment(), "help_fragment");
        ((NavigationView) MainActivity.sInstance.findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_help);
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
