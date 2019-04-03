package ms.sapientia.ro.gaitrecognitionapp.service;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ms.sapientia.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.MainActivity;

abstract public class ActivityBase extends AppCompatActivity {

    /**
     * This method is responsible to initialize the views
     */
    abstract protected void bindViews();

    /**
     * This method is responsible to set on click listeners for the views
     */
    abstract protected void bindClickListeners();

}
