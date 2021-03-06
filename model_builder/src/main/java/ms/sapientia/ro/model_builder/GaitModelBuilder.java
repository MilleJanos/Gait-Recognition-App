package ms.sapientia.ro.model_builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;

public class GaitModelBuilder implements IGaitModelBuilder {
    private ArrayList<Attribute> attributes;

    /**
     *
     * @param arffFile - a .arff type file which contains user's features
     * @return a classifier
     */
    public Classifier createModel(String arffFile) throws Exception {
        // Load ARFF file
        /*BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(arffFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArffLoader.ArffReader arff = null;
        try {
            //System.out.println("Im here");
            arff = new ArffLoader.ArffReader(reader);
        } catch (Exception ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }*/


        ArffLoader loader = new ArffLoader();
        try {
            loader.setSource(new File(arffFile));
        } catch (IOException e) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
            throw new Exception(e);
        }

        Instances data = null;
        try {
            data = loader.getDataSet();
        } catch (IOException e) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
            throw new Exception(e);
        }
        data.setClassIndex(data.numAttributes() - 1);

        // Create classifier
        Classifier classifier = new RandomForest();
        try {
            classifier.buildClassifier(data);
        } catch (Exception ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        Enumeration<Attribute> attribs = data.enumerateAttributes();
        attributes = new ArrayList<Attribute>();
        while (attribs.hasMoreElements()) {
            Attribute attribute = attribs.nextElement();
            attributes.add( attribute );
        }
        List<String> userids = new ArrayList<>();
        userids.add("user");
        userids.add("dummy");
        attributes.add( new Attribute("userid", userids));

        return classifier;
    }

    /**
     *
     * @param arffFile - a .arff type file which contains user's features
     * @return a List of Attributes gained from the .arff file
     */
    public ArrayList<Attribute> getAttributes(String arffFile) throws Exception {
        if (attributes != null) {
            return attributes;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(arffFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        ArffLoader.ArffReader arff = null;
        try {
            arff = new ArffLoader.ArffReader(reader);
        } catch (IOException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }

        Instances data = arff.getData();
        Enumeration<Attribute> attribs = data.enumerateAttributes();
        attributes = new ArrayList<Attribute>();

        while (attribs.hasMoreElements()) {
            attributes.add(attribs.nextElement());
        }

        List<String> userids = new ArrayList<>();
        userids.add("user");
        userids.add("dummy");
        attributes.add( new Attribute("userid", userids));
        return attributes;
    }

    /**
     *
     * @param path - the file path of the file which contains user's features
     * @return Instances of user's features
     */
    public Instances loadDataset(String path) throws Exception {
        Instances dataset = null;
        try {
            dataset = ConverterUtils.DataSource.read(path);
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(GaitModelBuilder.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }

        return dataset;
    }

    /**
     *
     * @param traindataset - get Instances from training data
     * @return a Classifier created from given Instnces
     */
    public Classifier buildClassifier(Instances traindataset) throws Exception {
        RandomForest m = new RandomForest();

        try {
            m.buildClassifier(traindataset);

        } catch (Exception ex) {
            Logger.getLogger(GaitModelBuilder.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        return m;
    }

    /**
     *
     * @param model - a classifier made by specifically for the user
     * @param traindataset - creating an Evaluation from this training data
     * @param testdataset - evaluate the model with these test data
     * @return a summary of the evaluation
     */
    public String evaluateModel(Classifier model, Instances traindataset, Instances testdataset) throws Exception {
        Evaluation eval = null;
        try {
            // Evaluate classifier with test dataset
            eval = new Evaluation(traindataset);
            eval.evaluateModel(model, testdataset);
        } catch (Exception ex) {
            Logger.getLogger(GaitModelBuilder.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        return eval.toSummaryString("", true);
    }

    /**
     *
     * @param model - this is the model which we want to save
     * @param modelpath - this is a path (contains the file name), where we want to save the model
     */
    public void saveModel(Classifier model, String modelpath) throws Exception {

        try {
            SerializationHelper.write(modelpath, model);
        } catch (Exception ex) {
            Logger.getLogger(GaitModelBuilder.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
    }
}
