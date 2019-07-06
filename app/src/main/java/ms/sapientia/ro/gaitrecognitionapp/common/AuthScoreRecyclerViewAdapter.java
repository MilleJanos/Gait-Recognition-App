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

/**
 * This class is adapter of the Recycler View found on Profile page.
 *
 * @author MilleJanos
 */
public class AuthScoreRecyclerViewAdapter extends RecyclerView.Adapter<AuthScoreRecyclerViewAdapter.AuthScoreObjectHolder> {

    // Constant members:
    private static final String TAG = "AuthScoreRecyclerViewAd";
    // View members:
    private ArrayList<ms.sapientia.ro.gaitrecognitionapp.model.AuthScoreObject> mDataset;
    // Listener members:
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
        holder.idTextView.setText( toAuthIdFormat( mDataset.get(position).getId() ) );
        holder.scoreTextView.setText( toScoreFormat( mDataset.get(position).getScore() ) );
    }

    /**
     * This method adds a new AuthScoreObject item to the Recycler view.
     * @param dataObj to add
     * @param index his index
     */
    public void addItem(AuthScoreObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    /**
     * This method removes the AuthScoreObject item at the given index.
     * @param index to remove
     */
    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    /**
     * This method returns the item count of the Recycler View.
     * @return item count of the Recycler View
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

    /**
     * This method formats the authentication it value.
     * @param id to format
     * @return formatted id in string
     */
    public String toAuthIdFormat(int id){
        return  id + ".";
    }

    /**
     * This method formats the score value.
     * @param score to format
     * @return formatted score in string
     */
    public String toScoreFormat(double score){
        return ((int)(score * 100)) + "%";
    }

}