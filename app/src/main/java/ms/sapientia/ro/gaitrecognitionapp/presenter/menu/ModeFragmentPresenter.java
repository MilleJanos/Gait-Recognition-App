package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

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


    // Methods:


    public void Prepare4Train(){
        // TODO
    }

    public void Prepare4Authentication(){
        // TODO
    }

    public void Prepare4DataCollection(){
        // TODO
    }


}
