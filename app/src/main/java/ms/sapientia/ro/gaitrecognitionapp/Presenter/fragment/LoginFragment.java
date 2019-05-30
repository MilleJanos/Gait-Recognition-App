package ms.sapientia.ro.gaitrecognitionapp.Presenter.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.Presenter.fragment.RegisterFragment;

public class LoginFragment extends Fragment implements View.OnClickListener{

    // View:
    EditText mEmailEditText;
    EditText mPasswordEditText;
    Button mLoginButton;
    TextView mRegisterTextViewButton;
    TextView mForgottPasswordTextViewButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        //ConstraintLayout layout = (ConstraintLayout) getView().findViewById(R.id.login_fragment);
        //final int sdk = android.os.Build.VERSION.SDK_INT;
        //if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        //    layout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road) );
        //} else {
        //    layout.setBackground(ContextCompat.getDrawable(getContext(), R.mipmap.image_asphalt_road));
        //}
    }

    private void initView(View view){
        mEmailEditText = getView().findViewById(R.id.email_editText);
        mPasswordEditText = getView().findViewById(R.id.password_editText);
        mLoginButton = getView().findViewById(R.id.login_button);
        mRegisterTextViewButton = getView().findViewById(R.id.sign_up_textviewbutton);
        mForgottPasswordTextViewButton = getView().findViewById(R.id.forgot_password_textviewbutton);
    }
    private void changeFragment(){
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onClick(View view) {
        //switch (view.getId()){
        //    case R.id.button:
        //    {
        //
        //        break;
        //    }
        //}
    }
}
