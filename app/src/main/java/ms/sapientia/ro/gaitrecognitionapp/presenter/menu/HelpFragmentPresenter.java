package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.common.TopicRecyclerViewAdapter;
import ms.sapientia.ro.gaitrecognitionapp.model.TopicObject;

/**
 * This class is the presenter of the HelpFragment class.
 *
 * @author MilleJanos
 */
public class HelpFragmentPresenter {

    // Members
    private View view;
    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

    public HelpFragmentPresenter(View view){
        this.view = view;
    }


    /**
     * This method recalculates the filter based on the string input given.
     * After filtering, refreshes the adapter data set.
     * If the input contains more words, then the filter will return the list of results
     * which contains the given words.
     * Words order does not matter.
     *
     * @param search text to be found, can contain more words.
     */
    public void filterData(RecyclerView.Adapter adapter, RecyclerView recyclerView, String search){
        ArrayList<TopicObject> data = getDataSet();
        ArrayList<TopicObject> filteredData = new ArrayList<>();
        search = search.trim();
        String[] words = search.split(" ");
        boolean indexItemFound = false;

        if( search.length() > 1 || search.toLowerCase().substring(0,0).equals("q") ) {
            // - Filter by id:

            try {
                int id = Integer.parseInt(search.substring(1, search.length()));

                TopicObject withId = filterById(data, id);

                if (withId != null) {
                    filteredData.add(withId);
                    indexItemFound = true;
                }
            }catch (Exception e){
                // Item not found, try: Filter by content
            }
        }

        if( ! indexItemFound ) {
            // - Filter by content:

            filteredData = data;

            // remove the items which are not containing the word from filterByWord:
            for (int i = 0; i < words.length; ++i) {
                filteredData = filterByWord(filteredData, words[i]);
            }
        }

        // Refresh adapter items:
        adapter = new TopicRecyclerViewAdapter( filteredData );
        recyclerView.setAdapter(adapter);
    }

    /**
     * This method returns the data set.
     * @return data set.
     */
    public ArrayList<TopicObject> getDataSet() {

        ArrayList results = new ArrayList<TopicObject>();

        results = fillDataSetWithLocalData();

        // for (int index = 0; index < 20; index++) {
        //     TopicObject obj = new TopicObject(index+1,"Question " + index,
        //             "Answer " + index);
        //     results.add( obj );
        // }

        return results;
    }

    /**
     * This method selects only the items of the list which contains the given word.
     * @param toFilter filter the word from this list.
     * @param word the word to be found.
     * @return filtered list.
     */
    private ArrayList<TopicObject> filterByWord(ArrayList<TopicObject> toFilter, String word){
        boolean questionContains;
        boolean answerContains;

        ArrayList<TopicObject> filtered = new ArrayList<>();

        for(TopicObject item : toFilter){
            questionContains = false;
            answerContains = false;

            if (  item.getQuestion().replaceAll("[^a-zA-Z\"' ]", "").toLowerCase().contains(word.toLowerCase())  ) {
                // word found
                questionContains = true;
            }

            if (  item.getAnswer().replaceAll("[^a-zA-Z ]", "").toLowerCase().contains(word.toLowerCase())  ) {
                // not found
                answerContains = true;
            }

            if( questionContains || answerContains ){
                filtered.add( item );
            }
        }

        return filtered;
    }

    /**
     * This method selects only the item of the list which has the given id.
     * @param toFilter filter the word from this list.
     * @param id id of the item to be returned.
     * @return TopicObject with the given id, returns null if not found.
     */
    private TopicObject filterById(ArrayList<TopicObject> toFilter, int id){
        ArrayList<TopicObject> filtered = new ArrayList<>();
        for(TopicObject item : toFilter){
            if( item.getId() == id ){
                return item;
            }
        }
        return null;
    }

    /**
     * This method fills the data set with local data.
     * @return data set.
     */
    private ArrayList<TopicObject> fillDataSetWithLocalData(){
        ArrayList results = new ArrayList<TopicObject>();

        results.add( new TopicObject(1,"How to get started?","In first step you have to train in order to authenticate yourself.") );
        results.add( new TopicObject(2,"How to train?","Go to Mode menu then select Train. After starting the service you have to put the device in to your pocket the walk in a straight line until you hear a beep sound. Then pick your phone then stop the service.") );
        results.add( new TopicObject(3,"\"No enough data\"?","This message pops up when there is no enough walk information. Try walking until you hear a beep sound.") );
        results.add( new TopicObject(4,"How to stop service if app was closed?","Tap on the service's notification and press \"yes\".") );
        results.add( new TopicObject(5,"Low authentication score?","Happens if during train you stumble or stop, etc.\nTry retraining.") );
        results.add( new TopicObject(6,"How to authenticate?","You have to train before try to authenticate.") );
        results.add( new TopicObject(7,"Can I close the screen while training?","Yes.\nScreen can be turned off in any phase of train, authenticate or data collect mode.") );
        results.add( new TopicObject(8,"How can I upload gait pattern for administrators?","Use the Data Collecting mode. It can capture more data then train mode, but without training.") );
        results.add( new TopicObject(9,"How to set profile picture?","Go to Profile page, then press the profile picture.") );
        results.add( new TopicObject(10,"Where can I write my question","Tap on the floating button in the bottom right corner to send email to developers.") );


        return results;
    }


}
