package ms.sapientia.ro.model_builder;

import java.util.ArrayList;

import ms.sapientia.ro.commonclasses.Accelerometer;
import weka.classifiers.Classifier;
import weka.core.Attribute;

public interface IGaitVerification {
    public double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, ArrayList<Accelerometer> rawdata, String userName);
    public double verifyUser( Classifier classifier, ArrayList<Attribute> attributes, String rawdata_file,String user_name);
}
