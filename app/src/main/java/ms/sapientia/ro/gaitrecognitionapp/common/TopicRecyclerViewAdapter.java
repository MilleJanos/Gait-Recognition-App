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

public class TopicRecyclerViewAdapter extends RecyclerView
        .Adapter<TopicRecyclerViewAdapter.TopicObjectHolder> {

    private static String TAG = "MyRecyclerViewAdapter";
    private ArrayList<TopicObject> mDataset;
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
    public TopicObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
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

    public String toTopicIdFormat(int id){
        String preChar = "Q";
        int padding = 4;
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
