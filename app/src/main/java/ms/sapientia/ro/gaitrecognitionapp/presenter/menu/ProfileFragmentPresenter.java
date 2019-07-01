package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

import java.util.ArrayList;

import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.AuthScoreObject;

public class ProfileFragmentPresenter {

    // Members
    private View view;

    public ProfileFragmentPresenter(View view){
        this.view = view;
    }

    public ArrayList<AuthScoreObject> getDataSet() {

        ArrayList<AuthScoreObject> retArray = new ArrayList<>();
        int index = 0;

        for(Double score : AppUtil.sUser.authenticaiton_values){

            retArray.add( new AuthScoreObject(++index, score) );
        }
        return retArray;
    }

    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }
}
