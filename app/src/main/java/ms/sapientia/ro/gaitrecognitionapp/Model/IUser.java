package ms.sapientia.ro.gaitrecognitionapp.Model;

public interface IUser {

    String getEmail();
    String getFirstName();
    String getLastName();
    String getPassword();
    boolean isValidData();

}
