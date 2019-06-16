package ms.sapientia.ro.gaitrecognitionapp.model;

import java.io.File;

public interface IFileCallback {

    void Success(File file);
    void Failure();
    void Error(int error_code);

}
