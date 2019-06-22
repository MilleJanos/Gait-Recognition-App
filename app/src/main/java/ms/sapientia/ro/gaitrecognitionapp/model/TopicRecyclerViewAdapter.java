package ms.sapientia.ro.gaitrecognitionapp.model;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.R;

public class TopicRecyclerViewAdapter extends RecyclerView
        .Adapter<TopicRecyclerViewAdapter.TopicObjectHolder> {

    private static String TAG = "MyRecyclerViewAdapter";
    private ArrayList<TopicObject> mDataset;
    private static MyClickListener myClickListener;

    public static class TopicObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView title;
        TextView description;

        public TopicObjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_textView);
            description = (TextView) itemView.findViewById(R.id.desc_auth_textview);
            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public TopicRecyclerViewAdapter(ArrayList<TopicObject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public TopicObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);

        TopicObjectHolder dataObjectHolder = new TopicObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(TopicObjectHolder holder, int position) {
        try {
            holder.title.setText(mDataset.get(position).getmTitle());
            holder.description.setText(mDataset.get(position).getmDescription());
        }catch (Exception e){
            Log.i(TAG, "onBindViewHolder: position: " +position);
            e.printStackTrace();
        }
    }

    public void addItem(TopicObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

}
