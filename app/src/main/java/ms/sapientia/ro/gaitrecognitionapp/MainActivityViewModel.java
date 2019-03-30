package ms.sapientia.ro.gaitrecognitionapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = "MainActivityViewModel";

    private MutableLiveData<Boolean> mIsProgressUpdating = new MutableLiveData<>();
    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: connected to service");
            MyService.MyBinder binder = (MyService.MyBinder) iBinder;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.postValue(null);
        }
    };


    public LiveData<Boolean> getIsProgressUpdating(){
        return mIsProgressUpdating;
    }

    public LiveData<MyService.MyBinder> getBinder(){
        return mBinder;
    }

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public void setIsUploading(Boolean isUploading){
        mIsProgressUpdating.postValue(isUploading);
    }

}
