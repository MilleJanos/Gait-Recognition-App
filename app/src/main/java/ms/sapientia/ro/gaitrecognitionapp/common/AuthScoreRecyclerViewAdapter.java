package ms.sapientia.ro.gaitrecognitionapp.common;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.model.AuthScoreObject;

public class AuthScoreRecyclerViewAdapter extends RecyclerView.Adapter<AuthScoreRecyclerViewAdapter.AuthScoreObjectHolder> {

    private static final String TAG = "AuthScoreRecyclerViewAd";
    private ArrayList<ms.sapientia.ro.gaitrecognitionapp.model.AuthScoreObject> mDataset;
    private static MyClickListener myClickListener;

    public static class AuthScoreObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView idTextView;
        TextView scoreTextView;

        public AuthScoreObjectHolder(View itemView) {
            super(itemView);
            idTextView = (TextView) itemView.findViewById(R.id.auth_score_id_textView);
            scoreTextView = (TextView) itemView.findViewById(R.id.auth_score_value_textView);
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

    public AuthScoreRecyclerViewAdapter(ArrayList<AuthScoreObject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public AuthScoreObjectHolder onCreateViewHolder(ViewGroup parent,int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_card_view, parent, false);

        AuthScoreObjectHolder dataObjectHolder = new AuthScoreObjectHolder(view);
        return dataObjectHolder;
    }


    @Override
    public void onBindViewHolder(AuthScoreObjectHolder holder, int position) {
        holder.idTextView.setText( toAuthIdIdFormat( mDataset.get(position).getId() ) );
        holder.scoreTextView.setText( toScoreFormat( mDataset.get(position).getScore() ) );
    }

    public void addItem(AuthScoreObject dataObj, int index) {
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

    public String toAuthIdIdFormat(int id){
        return  id + ".";
    }

    public String toScoreFormat(double score){
        return ((int)(score * 100)) + "%";
    }

}