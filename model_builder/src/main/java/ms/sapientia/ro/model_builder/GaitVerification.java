package ms.sapientia.ro.model_builder;

import ms.sapientia.ro.FeatureExtractor;
import ms.sapientia.ro.commonclasses.Accelerometer;
import ms.sapientia.ro.feature_extractor.Feature;
import ms.sapientia.ro.feature_extractor.FeatureExtractorException;
import ms.sapientia.ro.feature_extractor.IUtil;
import ms.sapientia.ro.feature_extractor.Settings;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ms.sapientia.ro.FeatureExtractor.extractFeaturesFromArrayListToArrayListOfFeatures;


public class GaitVerification implements IGaitVerification {
    private double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, ArrayList<Accelerometer> rawdata, IUtil utility, String userName) {
        ArrayList<Feature> features = null;
        try{
            features = extractFeaturesFromArrayListToArrayListOfFeatures(rawdata, userName);
        }
        catch (FeatureExtractorException ex){
            Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        }

        Instances instances = null;
        instances = utility.arrayListOfFeaturesToInstances(features);

        int NUM_ATTRIBUTES = attributes.size();

        double probability = 0;
        for(int i=0; i <= features.size(); i++){
            // for (Feature f : features) {
            //double values[] = this.feature2DoubleArray(f, NUM_FEATURES);
            try {
                double result[] = classifier.distributionForInstance(instances.get(i));
                System.out.println(result[0] + ", " + result[1]);
                probability += result[ 0 ];
            } catch (Exception ex) {
                Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return probability/features.size();
    }

    private double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, String rawdata_file, IUtil utility, String userName) {
        System.out.println(Settings.getAllSettings());

        ArrayList<Feature> features = null;
        try {
            features = FeatureExtractor.extractFeaturesFromCsvFileToArrayListOfFeatures(rawdata_file);
        } catch (FeatureExtractorException ex) {
            Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Feature -> Instance
        Instances instances = null;

        try {
            instances = utility.arrayListOfFeaturesToInstances(features);
        }
        catch (Exception ex){
            Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

        int NUM_FEATURES = attributes.size();

        double probability = 0;
        //for (Feature f : features) {
        for(int i=0; i < features.size(); i++){
            double values[] = this.feature2DoubleArray(features.get(i), NUM_FEATURES);
            try {
                double result[] = classifier.distributionForInstance(instances.get(i));
                System.out.println(result[0] + ", " + result[1]);
                probability += result[ 0 ];
            } catch (Exception ex) {
                Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return probability/features.size();
    }

    public double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, ArrayList<Accelerometer> rawdata, String userName) {
        Settings.usingFrames(128);
        Settings.setInputHasHeader(false);
        Settings.setOutputHasHeader(true);
        Settings.setOutputFileType(Settings.FileType.ARFF);

        //Settings.setDefaultUserId("mj");
        System.out.println(Settings.getAllSettings());

        ArrayList<Feature> features = null;
        try{
            //String userName = "ttJMxBAjuHNVLCKhaXNvBTFDbIc2";
            features = extractFeaturesFromArrayListToArrayListOfFeatures(rawdata, userName);
        }
        catch (FeatureExtractorException ex){
            Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        }

        Instances instances = new Instances("toverify", attributes, 0);
        instances.setClassIndex(instances.numAttributes() - 1);

        int NUM_FEATURES = attributes.size();

        double probability = 0;
        for (Feature f : features) {
            double values[] = this.feature2DoubleArray(f, NUM_FEATURES);
            instances.add(new DenseInstance(1.0, values));

            Instance instance = instances.lastInstance();
            instance.setDataset(instances);
            //System.out.println(instance);
            try {
                double result[] = classifier.distributionForInstance(instance);
                System.out.println(result[0] + ", " + result[1]);
                probability += result[ 0 ];
            } catch (Exception ex) {
                Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return probability/features.size();
    }

    public double verifyUser(Classifier classifier, ArrayList<Attribute> attributes, String rawdata_file) {
        Settings.usingFrames(128);
        Settings.setInputHasHeader(true);
        Settings.setOutputHasHeader(true);
        Settings.setOutputFileType(Settings.FileType.ARFF);
        //todo kiegesziteni


        //Settings.setDefaultUserId("mj");
        System.out.println(Settings.getAllSettings());

        ArrayList<Feature> features = null;
        try {
            features = FeatureExtractor.extractFeaturesFromCsvFileToArrayListOfFeatures(rawdata_file);
        } catch (FeatureExtractorException ex) {
            Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Feature -- Instance
        Instances instances = new Instances("toverify", attributes, 0);
        instances.setClassIndex(instances.numAttributes() - 1);

        int NUM_FEATURES = attributes.size();

        double probability = 0;
        for (Feature f : features) {
            double values[] = this.feature2DoubleArray(f, NUM_FEATURES);
            instances.add(new DenseInstance(1.0, values));

            Instance instance = instances.lastInstance();
            instance.setDataset(instances);
            //System.out.println(instance);
            try {
                double result[] = classifier.distributionForInstance(instance);
                //System.out.println(result[0] + ", " + result[1]);
                probability += result[ 1 ]; //0-dummy 1-user
            } catch (Exception ex) {
                Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return probability/features.size();

    }

    private double[] feature2DoubleArray(Feature feature, int numFeatures) {
        double[] array = new double[numFeatures];
        array[0] = feature.getMinX();
        array[1] = feature.getMinY();
        array[2] = feature.getMinZ();
        array[3] = feature.getMinMag();

        array[4] = feature.getAvgAccelerationX();
        array[5] = feature.getAvgAccelerationY();
        array[6] = feature.getAvgAccelerationZ();
        array[7] = feature.getAvgAccelerationMag();

        array[8] = feature.getStdDevX();
        array[9] = feature.getStdDevY();
        array[10] = feature.getStdDevZ();
        array[11] = feature.getStdDevMag();

        array[12] = feature.getAvgAbsDiffX();
        array[13] = feature.getAvgAbsDiffY();
        array[14] = feature.getAvgAbsDiffZ();
        array[15] = feature.getAvgAbsDiffMag();

        array[16] = feature.getZeroCrossingX();
        array[17] = feature.getZeroCrossingY();
        array[18] = feature.getZeroCrossingZ();

        double[] xbins = feature.getBinsX();
        for (int i = 0; i < xbins.length; ++i) {
            array[19 + i] = xbins[i];
        }

        double[] ybins = feature.getBinsY();
        for (int i = 0; i < ybins.length; ++i) {
            array[29 + i] = ybins[i];
        }

        double[] zbins = feature.getBinsZ();
        for (int i = 0; i < zbins.length; ++i) {
            array[39 + i] = zbins[i];
        }

        double[] mbins = feature.getBinsMag();
        for (int i = 0; i < mbins.length; ++i) {
            array[49 + i] = mbins[i];
        }
        return array;
    }
}
