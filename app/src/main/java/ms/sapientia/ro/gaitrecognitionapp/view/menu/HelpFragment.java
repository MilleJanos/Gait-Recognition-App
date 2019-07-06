package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.Animator;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.common.TopicRecyclerViewAdapter;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.HelpFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class is responsible to handle the help page.
 */
public class HelpFragment extends NavigationMenuFragmentItem implements HelpFragmentPresenter.View {

    // Constants: members
    private static final String TAG = "HelpFragment";
    // MVP members:
    private HelpFragmentPresenter mPresenter;
    // Members members:
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchView mSearchView;
    private FloatingActionButton mFloatingActionButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.sInstance.setTitle("Help");

        mPresenter = new HelpFragmentPresenter(this);

        initRecyclerView(view);

        initView(view);
        bindClickListeners();

        Animator.Slide( mRecyclerView, 0, 0, 1000, 0 , 1000);
        Animator.Slide( mSearchView, 0, 0, -200, 0 , 2000);
        Animator.Slide( mFloatingActionButton, 200, 0, 0, 0 , 2000);
    }

    /**
     * This method binds the view elements.
     * @param view fragment's view.
     */
    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mSearchView = view.findViewById(R.id.search_view);
        mFloatingActionButton = view.findViewById(R.id.message_floating_action_button);
    }

    /**
     * This method binds the view elements.
     */
    private void bindClickListeners() {

        ((TopicRecyclerViewAdapter) mAdapter).setOnItemClickListener(new TopicRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(TAG, " Clicked on Item " + position);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i(TAG, "onQueryTextSubmit: search field (final): " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.i(TAG, "onQueryTextSubmit: search field: " + s);
                mPresenter.filterData(mAdapter, mRecyclerView, s);
                return false;
            }
        });

        mSearchView.setOnClickListener(v -> {
            mSearchView.requestFocus();
        });

        mFloatingActionButton.setOnClickListener(v -> {
            String to = "millejanos31@gmail.com";
            String body = "(Describe your question here)";

            if( AppUtil.sAuth != null ){

                AppUtil.sendEmail(
                        to,
                        "Question ("+ AppUtil.sAuth.getCurrentUser().getEmail() +")",
                        body);
            }else{

                AppUtil.sendEmail(
                        to,
                        "Question",
                        body);
            }
        });

    }

    /**
     * This method initiates the Recycler View.
     * @param view view of the fragment.
     */
    private void initRecyclerView(android.view.View view){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.sContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TopicRecyclerViewAdapter( mPresenter.getDataSet() );
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).deleteItem(index);
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
