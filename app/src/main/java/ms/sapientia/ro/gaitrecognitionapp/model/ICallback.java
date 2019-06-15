package ms.sapientia.ro.gaitrecognitionapp.model;

public interface ICallback {

    void Success(MyFirebaseUser user);
    void Failure();
    void Error(int error_code);

}
