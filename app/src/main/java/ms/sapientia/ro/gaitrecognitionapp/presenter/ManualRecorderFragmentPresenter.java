package ms.sapientia.ro.gaitrecognitionapp.presenter;

public class ManualRecorderFragmentPresenter {

    private View view;

    // Interface:
    public interface View{
        void initProgressBar();
        void showProgressBar();
        void hideProgressBar();
    }

    // Constructor
    public ManualRecorderFragmentPresenter(View view){
        this.view = view;
    }




}
