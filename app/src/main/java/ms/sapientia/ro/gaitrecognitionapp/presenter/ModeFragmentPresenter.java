package ms.sapientia.ro.gaitrecognitionapp.presenter;

public class ModeFragmentPresenter {

    // Members
    private View view;



    // Interface:
    public interface View{
        void showProgressBar();
        void hideProgressBar();
    }

    // Constructor
    public ModeFragmentPresenter(View view){
        this.view = view;
    }




}
