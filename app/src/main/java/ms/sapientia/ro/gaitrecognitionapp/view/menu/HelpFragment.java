package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.model.TopicObject;
import ms.sapientia.ro.gaitrecognitionapp.model.TopicRecyclerViewAdapter;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.HelpFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

public class HelpFragment extends NavigationMenuFragmentItem implements HelpFragmentPresenter.View {

    private static final String TAG = "HelpFragment";

    // MVP:
    private HelpFragmentPresenter mPresenter;
    // Members:
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


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





    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
    }

    private void bindClickListeners() {

        ((TopicRecyclerViewAdapter) mAdapter).setOnItemClickListener(new TopicRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(TAG, " Clicked on Item " + position);
            }
        });

    }

    private void initRecyclerView(View view){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.sContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TopicRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).deleteItem(index);
    }

    private ArrayList<TopicObject> getDataSet() {

        ArrayList results = new ArrayList<TopicObject>();
        for (int index = 0; index < 20; index++) {
            TopicObject obj = new TopicObject("Question " + index,
                    "Answer " + index);
            results.add( obj );
        }

        return results;
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
