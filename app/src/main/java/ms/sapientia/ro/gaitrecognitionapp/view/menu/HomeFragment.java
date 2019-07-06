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

/**
 * This class is responsible to help navigat the user.
 *
 * @author MilleJanos
 */
public class HomeFragment extends NavigationMenuFragmentItem implements HomeFragmentPresenter.View {

    // Constants members:
    private static final String TAG = "HomeFragment";
    // MVP members:
    private HomeFragmentPresenter mPresenter;
    // View members:
    private TextView mHelpTextViewButton;
    private ImageView mOpenDrawerHelpImageViewButton;
    private ImageView mOpenDrawerHelpImageView;
    private boolean mAnimationLock; // to prevent spamming the mOpenDrawerHelpImageViewButton button


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

    /**
     * This method initiates the view elements.
     * @param view fragments view.
     */
    private void initView(View view) {
        mHelpTextViewButton = view.findViewById(R.id.help_textviewbutton);
        mOpenDrawerHelpImageViewButton = view.findViewById(R.id.open_drawer_help_button);
        mOpenDrawerHelpImageView = view.findViewById(R.id.help_arrow_imageView);
        mOpenDrawerHelpImageView.setVisibility( View.INVISIBLE );
    }

    /**
     * This method binds the listeners to the view elements.
     */
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

    /**
     * This method opens the help page.
     */
    private void goToHelpPage(){
        MainActivity.sInstance.replaceFragment(new HelpFragment(), "help_fragment");
        ((NavigationView) MainActivity.sInstance.findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_help);
    }

    /**
     * This method shows the progress bar.
     */
    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    /**
     * This method hides the progress bar.
     */
    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }
}
