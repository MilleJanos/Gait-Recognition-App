package ms.sapientia.ro.model_builder;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;

public interface IGaitModelBuilder {
    public Classifier createModel(String arffFile) throws Exception;
    public ArrayList<Attribute> getAttributes(String arffFile) throws Exception;
}
