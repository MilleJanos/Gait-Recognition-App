package ms.sapientia.ro.gaitrecognitionapp.model;

/**
 * This interface is to create simple function pointer for asynchronous methods.
 */
public interface ICallback<T> {

    void Success(T user);
    void Failure();
    void Error(int error_code);

}
