package ms.sapientia.ro.gaitrecognitionapp;

import android.support.v4.app.Fragment;

abstract public class FragmentBase extends Fragment {

    /**
     * This method is responsible to initialize the views
     */
    abstract protected void bindViews();

    /**
     * This method is responsible to set on click listeners for the views
     */
    abstract protected void bindClickListeners();
}
