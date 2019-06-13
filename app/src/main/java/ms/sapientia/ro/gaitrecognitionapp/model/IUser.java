package ms.sapientia.ro.gaitrecognitionapp.model;

public interface IUser {

    String getEmail();
    String getFirst_name();
    String getLast_name();
    String getPassword();
    boolean isValidData();

}
