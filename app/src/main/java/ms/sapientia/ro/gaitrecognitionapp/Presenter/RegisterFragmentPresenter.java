package ms.sapientia.ro.gaitrecognitionapp.Presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ms.sapientia.ro.gaitrecognitionapp.Presenter.interfaces.IRegisterPresenter;


public class RegisterFragmentPresenter extends Fragment implements IRegisterPresenter {// implements View.OnClickListener{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view){

        // FIND VIEW BY IDs:

    }

    @Override
    public void onRegister(String email, String password, String password2) {

    }

/*
    @Override
    public void onClick(View v) {

    }
*/

}
