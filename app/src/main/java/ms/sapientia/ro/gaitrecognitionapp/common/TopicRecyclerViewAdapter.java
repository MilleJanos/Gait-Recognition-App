package ms.sapientia.ro.gaitrecognitionapp.common;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.model.TopicObject;

/**
 * This class is adapter of the Recycler View found on Help page.
 *
 * @author MilleJanos
 */
public class TopicRecyclerViewAdapter extends RecyclerView.Adapter<TopicRecyclerViewAdapter.TopicObjectHolder> {

    // Constant members:
    private static String TAG = "MyRecyclerViewAdapter";
    private final String INDEX_START_CHARACTER = "Q";
    private final int INDEX_PADDING = 4;
    // View members:
    private ArrayList<TopicObject> mDataset;
    // Listener member:
    private static MyClickListener myClickListener;

    public static class TopicObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView idTextView;
        TextView questionTextView;
        TextView answerTextView;

        public TopicObjectHolder(View itemView) {
            super(itemView);
            idTextView = (TextView) itemView.findViewById(R.id.question_id_textView);
            questionTextView = (TextView) itemView.findViewById(R.id.question_textView);
            answerTextView = (TextView) itemView.findViewById(R.id.answer_textView);
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
    public TopicObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);

        TopicObjectHolder dataObjectHolder = new TopicObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(TopicObjectHolder holder, int position) {
        holder.idTextView.setText( toTopicIdFormat( mDataset.get(position).getId() ) );
        holder.questionTextView.setText( mDataset.get(position).getQuestion() );
        holder.answerTextView.setText( mDataset.get(position).getAnswer() );
    }

    /**
     * This method adds a new TopicObject item to the Recycler view.
     * @param dataObj to add
     * @param index his index
     */
    public void addItem(TopicObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    /**
     * This method removes the TopicObject item at the given index.
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
     * This method formats the id into custom question format.
     * @param id to format
     * @return the formatted id in string
     */
    public String toTopicIdFormat(int id){
        String preChar = INDEX_START_CHARACTER;
        int padding = INDEX_PADDING;
        String returnStr = preChar;
        String numStr = Integer.toString(id);

        if( numStr.length() > 4 ){
            return "Q" + numStr;
        }

        for( int i=0; i< padding - numStr.length(); ++i ){
            returnStr += "0";
        }

        returnStr += numStr;

        return  returnStr;
    }

}
