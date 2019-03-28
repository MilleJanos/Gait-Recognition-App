package ms.sapientia.ro.model_builder;

import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.io.InputStream;
import java.util.ArrayList;

public interface IGaitModelBuilder {
    public Classifier createModel(String arffFile);
    public Classifier createModel(InputStream arffStream);
    public ArrayList<Attribute> getAttributes(String arffFile);
}
