package ms.sapientia.ro.gaitrecognitionapp.model;

public interface ICallback {

    Object Success(MyFirebaseUser obj);
    Object Failure(MyFirebaseUser obj);
    void Error(int error_code);

}
