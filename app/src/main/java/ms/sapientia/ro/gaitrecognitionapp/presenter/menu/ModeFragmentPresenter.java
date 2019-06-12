package ms.sapientia.ro.gaitrecognitionapp.presenter.menu;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

import ms.sapientia.ro.gaitrecognitionapp.service.BackgroundService;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

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

    /**
     * This method starts the service if is not running.
     */
    public void StartServiceIfNotRunning(){
        if( ! isServiceRunning(BackgroundService.NAME) ){
            StartService();
        }else{
            Toast.makeText( MainActivity.sContext, "Service is already running", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method starts the service.
     */
    public void StartService() {
        Intent serviceIntent = new Intent(MainActivity.sInstance, BackgroundService.class);
        //String INPUT_EXTRA_KEY = "input_extra";
        //serviceIntent.putExtra(Common.INPUT_EXTRA_KEY, input);
        MainActivity.sInstance.startService(serviceIntent);
    }

    /**
     * This method stops the service.
     */
    public void StopService() {
        Intent serviceIntent = new Intent(MainActivity.sInstance, BackgroundService.class);
        MainActivity.sInstance.stopService(serviceIntent);
    }

    /**
     * Checks the service running state.
     */
    public boolean isServiceRunning(String service_lass_name){
        final ActivityManager activityManager = (ActivityManager) MainActivity.sContext.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(service_lass_name)){
                return true;
            }
        }
        return false;
    }


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
