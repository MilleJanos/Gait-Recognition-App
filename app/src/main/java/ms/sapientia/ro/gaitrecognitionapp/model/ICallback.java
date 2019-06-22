package ms.sapientia.ro.gaitrecognitionapp.model;

public interface ICallback<T> {

    void Success(T user);
    void Failure();
    void Error(int error_code);

}
