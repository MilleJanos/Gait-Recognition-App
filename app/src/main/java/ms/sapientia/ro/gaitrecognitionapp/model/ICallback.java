package ms.sapientia.ro.gaitrecognitionapp.model;

public interface ICallback {

    void Success(MyFirebaseUser obj);
    void Failure(MyFirebaseUser obj);
    void Error(int error_code);

}
