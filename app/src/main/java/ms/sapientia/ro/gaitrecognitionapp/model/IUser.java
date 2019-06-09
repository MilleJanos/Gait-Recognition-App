package ms.sapientia.ro.gaitrecognitionapp.model;

public interface IUser {

    String getEmail();
    String getFirstName();
    String getLastName();
    String getPassword();
    boolean isValidData();

}
