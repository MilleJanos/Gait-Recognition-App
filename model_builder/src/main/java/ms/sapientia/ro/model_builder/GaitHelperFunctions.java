package ms.sapientia.ro.model_builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import ms.sapientia.ro.feature_extractor.FeatureExtractor;
import ms.sapientia.ro.feature_extractor.Settings;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class GaitHelperFunctions {
    private static final String MODEL_FILE = "INPUT_ARFF/model_WsY044SgeaeZtDrQKVpRyWpo7hx1.arff";
    private static final String RAW_FILE_USER1   = "INPUT_RAW/rawdata_WsY044SgeaeZtDrQKVpRyWpo7hx1_20181212_133019.csv";
    private static final String RAW_FILE_USER2   = "INPUT_RAW/rawdata_WsY044SgeaeZtDrQKVpRyWpo7hx1_20181212_133059.csv";
    private static final String RAW_FILE_USER3   = "INPUT_RAW/rawdata_WsY044SgeaeZtDrQKVpRyWpo7hx1_20181212_133138.csv";

    private static final String FEATURE_DUMMY_FILE = "INPUT_ARFF/features_Dummy.arff";
    private static final String FEATURE_USER_FILE = "INPUT_ARFF/features_WsY044SgeaeZtDrQKVpRyWpo7hx1.arff";

    private static final String FEATURE_USER_FILE1 = "INPUT_ARFF/features_WsY044SgeaeZtDrQKVpRyWpo7hx1_1.arff";
    private static final String FEATURE_USER_FILE2 = "INPUT_ARFF/features_WsY044SgeaeZtDrQKVpRyWpo7hx1_2.arff";
    private static final String FEATURE_USER_FILE3 = "INPUT_ARFF/features_WsY044SgeaeZtDrQKVpRyWpo7hx1_3.arff";

    private static final String TEST_RAW_FILE_USER = "INPUT_RAW/rawdata_WsY044SgeaeZtDrQKVpRyWpo7hx1_20181212_133218.csv";


    //test
    private static final String JANCSI_FEATURE_USER_FILE = "INPUT_JANCSI/feature_LnntbFQGpBeHx3RwMu42e2yOks32.arff";
    //private static final String JANCSI_FEATURE_DUMMY_FILE= "INPUT_JANCSI/feature_negative_dummy.arff";
    private static final String JANCSI_MODEL_FILE = "INPUT_JANCSI/model_LnntbFQGpBeHx3RwMu42e2yOks32.mdl";
    private static final String JANCSI_TEST_RAW_FILE_USER = "INPUT_JANCSI/rawdata.csv";

    private static final String JANCSI_FEATURE = "INPUT_JANCSI/feature.arff";
    private static final String JANCSI_RAW = "INPUT_JANCSI/rawdata2.csv";

    //test for kriszti
    private static final String TRAIN_KRISZTI_RAW = "INPUT_KRISZTI/train_rawdata_6T2NePdIGOPBQLemTENrJLkGgiR2_20190530.csv";
    private static final String TRAIN_KRISZTI_FEATURES = "INPUT_KRISZTI/train_feature_6T2NePdIGOPBQLemTENrJLkGgiR2_20190530";
    private static final String MODEL_KRISZTI = "INPUT_KRISZTI/model_6T2NePdIGOPBQLemTENrJLkGgiR2.mdl";
    private static final String TEST1_KRISZTI = "INPUT_KRISZTI/test1_rawdata_6T2NePdIGOPBQLemTENrJLkGgiR2_20190530.csv";
    private static final String TEST2_KRISZTI = "INPUT_KRISZTI/test2_rawdata_6T2NePdIGOPBQLemTENrJLkGgiR2_20190530.csv";


    private static final String rawTest = "DATASET/rawdata_ttJMxBAjuHNVLCKhaXNvBTFDbIc2_";
    private static final String featureTest = "DATASET/feature_ttJMxBAjuHNVLCKhaXNvBTFDbIc2";
    private static final String finalRawTest = "DATASET/rawdata_ttJMxBAjuHNVLCKhaXNvBTFDbIc2.csv";

    private static final String rawJancsi = "INPUT_JANCSI/rawdata_LnntbFQGpBeHx3RwMu42e2yOks32_";
    private static final String featureJancsi = "INPUT_JANCSI/feature_LnntbFQGpBeHx3RwMu42e2yOks32";
    private static final String finalRawJancsi = "INPUT_JANCSI/rawdata_LnntbFQGpBeHx3RwMu42e2yOks32.csv";
    private static final String modelJancsi =  "INPUT_JANCSI/model_LnntbFQGpBeHx3RwMu42e2yOks32.mdl";
    private static final String JANCSI_FEATURE_DUMMY_FILE= "INPUT_JANCSI/feature_negative_dummy.arff";



    public static void main(String[] args) throws Exception {
        String userName = "LnntbFQGpBeHx3RwMu42e2yOks32";

        /*String userName = "ttJMxBAjuHNVLCKhaXNvBTFDbIc2";
        //createFeaturesFileFromRawFile(rawTest, featureTest, userName);*/


        /*ArrayList<String> files = new ArrayList<>();

        for(int i=1;i<=3; i++) {
            //if(i==3) continue;
            files.add(rawJancsi + i + ".csv");
        }

        try {
            mergeRawFiles(files, finalRawJancsi);
        }
        catch (Exception e){
            System.out.println("raw file merging error...\n");
        }

        createFeaturesFileFromRawFile(finalRawJancsi, featureJancsi, userName);*/

        /*try{
            createAndSaveModel(featureJancsi+".arff", modelJancsi);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }*/


        /*try{
            createAndSaveModel(JANCSI_FEATURE_USER_FILE, JANCSI_MODEL_FILE);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }

        /*String userName = "6T2NePdIGOPBQLemTENrJLkGgiR2";
        createFeaturesFileFromRawFile(TRAIN_KRISZTI_RAW, TRAIN_KRISZTI_FEATURES, userName);

        mergeEquallyArffFiles(FEATURE_DUMMY_FILE, TRAIN_KRISZTI_FEATURES);

        try{
            createAndSaveModel(TRAIN_KRISZTI_FEATURES, MODEL_KRISZTI);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }


        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier = builder.createModel(TRAIN_KRISZTI_FEATURES);

        ArrayList<Attribute> attributes = builder.getAttributes(TRAIN_KRISZTI_FEATURES);

        System.out.println( "NumAttributes: "+attributes.size() );
        IGaitVerification verifier = new GaitVerification();
        //IUtil utility = new Util();
        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, TEST1_KRISZTI));

        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, TEST2_KRISZTI));
        //System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, RAW_FILE_USER3));*/

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier = builder.createModel(JANCSI_FEATURE);

        ArrayList<Attribute> attributes = builder.getAttributes(JANCSI_FEATURE);

        System.out.println( "NumAttributes: "+attributes.size() );
        IGaitVerification verifier = new GaitVerification();
        //IUtil utility = new Util();
        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, JANCSI_RAW, "dummy"));

    }

    /*String userName = "WsY044SgeaeZtDrQKVpRyWpo7hx1";

        createFeaturesFileFromRawFile(RAW_FILE_USER1,"INPUT_ARFF/features_" + userName + "_1", userName);
        createFeaturesFileFromRawFile(RAW_FILE_USER2,"INPUT_ARFF/features_" + userName + "_2", userName);
        createFeaturesFileFromRawFile(RAW_FILE_USER3,"INPUT_ARFF/features_" + userName + "_3", userName);

        ArrayList<String> files = new ArrayList<>();
        files.add(FEATURE_USER_FILE1);
        files.add(FEATURE_USER_FILE2);
        files.add(FEATURE_USER_FILE3);


        try {
            mergeFeatureFiles(files, FEATURE_USER_FILE);
        }
        catch (Exception e){
            System.out.println("feature file merging error...\n");
        }*/

    //Merge two arff file in user's features file, for binary classification
    //mergeEquallyArffFiles(FEATURE_DUMMY_FILE, FEATURE_USER_FILE);

        /*try{
            createAndSaveModel(FEATURE_USER_FILE, MODEL_FILE);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }*/


        /*try{
            createAndSaveModel(JANCSI_FEATURE_USER_FILE, JANCSI_MODEL_FILE);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }*/


        /*IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier = builder.createModel(JANCSI_FEATURE_USER_FILE);

        ArrayList<Attribute> attributes = builder.getAttributes(JANCSI_FEATURE_USER_FILE);

        System.out.println( "NumAttributes: "+attributes.size() );
        IGaitVerification verifier = new GaitVerification();
        //IUtil utility = new Util();
        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, TEST_RAW_FILE_USER));*/


    //verification
        /*IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier = builder.createModel(JANCSI_FEATURE_USER_FILE);

        ArrayList<Attribute> attributes = builder.getAttributes(JANCSI_FEATURE_USER_FILE);

        System.out.println( "NumAttributes: "+attributes.size() );
        IGaitVerification verifier = new GaitVerification();
        //IUtil utility = new Util();
        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, JANCSI_TEST_RAW_FILE_USER));*/


    /**
     *
     * @param rawDataFile
     * @param featureFile - file path contains file name to give where we want to save the features
     * @param userName
     * set up feature extraction settings, then use the FeatureExtractor
     */
    public static void createFeaturesFileFromRawFile(String rawDataFile, String featureFile, String userName){
        //Settings.usingFrames(128);
        //Settings.setOutputHasHeader(true); // full arff, no header
        //Settings.setOutputFileType(Settings.FileType.ARFF);
        //Settings.setDefaultUserId(userName);
        //Settings.setNumFramesIgnored(4); //ignoring noisy data*/
        Settings.useRecommendedSettingsWithFrames();
        Settings.setDefaultUserId(userName);

        /*Settings.usingFrames(128);
        Settings.setInputHasHeader(true); //input has a header that has to be skipped
        Settings.setOutputHasHeader(true); //output will have a header
        Settings.setOutputFileType(Settings.FileType.ARFF); //output will be an .arff file
        Settings.setDefaultUserId("dummy");

        Settings.usingPreprocessing(true);
        Settings.setUseDynamicPreprocessingThreshold(true);
        Settings.setPreprocessingInterval(128);
        Settings.usingOrientationIndependence(false);*/


        try {
            FeatureExtractor.extractFeaturesFromCsvFileToFile(rawDataFile, featureFile);
        } catch (Exception e) {
            //e.printStackTrace();
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void createAndSaveModel(String userFeatureFilePath, String userModelFilePath) throws Exception {
        GaitModelBuilder gaitModelBuilder = new GaitModelBuilder();

        //the create muxed feature function save the mixed data in the first file to use less space
        Instances dataset = gaitModelBuilder.loadDataset(userFeatureFilePath);

        Filter filter = new Normalize();

        // divide dataset to train dataset 80% and test dataset 20%
        int trainSize = (int) Math.round(dataset.numInstances() * 0.8);
        int testSize = dataset.numInstances() - trainSize;

        dataset.randomize(new Debug.Random(1));// if you comment this line the accuracy of the model will be droped from 96.6% to 80%

        //Normalize dataset
        try {
            filter.setInputFormat(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Instances datasetnor = null;
        try {
            datasetnor = Filter.useFilter(dataset, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances traindataset = new Instances(datasetnor, 0, trainSize);
        Instances testdataset = new Instances(datasetnor, trainSize, testSize);

        // build classifier with train dataset
        //MultilayerPerceptron ann = (MultilayerPerceptron) mg.buildClassifier(traindataset);
        RandomForest ann = (RandomForest) gaitModelBuilder.buildClassifier(traindataset);

        // Evaluate classifier with test dataset
        String evalsummary = gaitModelBuilder.evaluateModel(ann, traindataset, testdataset);
        System.out.println("Evaluation: " + evalsummary);

        //Save model
        gaitModelBuilder.saveModel(ann, userModelFilePath);
    }

    /**
     *
     * @param input - the path of the input file (contains file name)
     * @param output - the output file name and location. This is also an input, but this contains the resume of the merge.
     */
    public static void mergeEquallyArffFiles(String input, String output) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(input));
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file " + input);
        }

        StringBuilder sb = new StringBuilder();
        Scanner scanner2 = null;

        try {
            scanner2 = new Scanner(new File(output));
        } catch (Exception ex) {
            System.out.println("File not found: " + output);
        }

        FileWriter writer = null;

        String line2 = null;
        while (scanner2.hasNextLine()) {
            line2 = scanner2.nextLine().trim();
            if (line2.contains("@attribute userID") || line2.contains("@attribute userid") || line2.contains("@attribute userId")) {
                break;
            }
            sb.append(line2 + "\n");
        }
        String line = null;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();
            if (line.contains("@attribute userID") || line.contains("@attribute userid") || line.contains("@attribute userId")) {
                break;
            }
        }

        String item1 = line.split(" ")[2];
        String item2 = line2.split(" ")[2];
        sb.append("@attribute userID{" + item1.substring(1, item1.length() - 1) + "," + item2.substring(1, item2.length() - 1) + "}\n\n");
        while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();
            if (line.equals("@data")) {
                break;
            }
        }

        int counter = 0;
        while (scanner2.hasNextLine()) {
            line = scanner2.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }
            counter++;
            sb.append(line + "\n");
        }
        //System.out.println(counter);
        try {
            writer = new FileWriter(output, false);
        } catch (Exception ex) {
            System.out.println("File not found: " + output);
        }
        try {
            writer.write(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (scanner.hasNextLine() && counter > 0) {

            line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            counter--;
            try {
                //writer.println(line);
                writer.write(line + "\n");
            } catch (IOException ex) {
                Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        scanner.close();
        scanner2.close();
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param files - a list of files we want to merge
     * @param outFile - the new file location and name, which contains the merged data
     * @throws IOException
     */
    public static void mergeRawFiles(ArrayList<String> files, String outFile) throws IOException {
        int n = 1024;
        OutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[n];
        for (String file : files) {
            InputStream in = new FileInputStream(file);
            int b = 0;
            while ( (b = in.read(buf)) >= 0) {
                out.write(buf, 0, b);
                out.flush();
            }
        }
        out.close();
    }

    /**
     *
     * @param files - list of file we want to merge
     * @param output - file contains the result
     * @throws IOException
     *
     */
    public static void mergeFeatureFiles(ArrayList<String> files, String output) throws IOException {
        int counter = 0;

        for(String input:files) {
            counter ++;
            if(counter > 0){
                int n = 1024;
                OutputStream out = new FileOutputStream(output);
                byte[] buf = new byte[n];
                InputStream in = new FileInputStream(input);
                int b = 0;
                while ( (b = in.read(buf)) >= 0) {
                    out.write(buf, 0, b);
                    out.flush();
                }

                out.close();
                continue;
            }
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File(input));
            } catch (FileNotFoundException ex) {
                System.out.println("Unable to open file " + input);
            }

            StringBuilder sb = new StringBuilder();
            Scanner scanner2 = null;

            try {
                scanner2 = new Scanner(new File(output));
            } catch (Exception ex) {
                System.out.println("File not found: " + output);
            }

            FileWriter writer = null;

            String line2 = null;
            while (scanner2.hasNextLine()) {
                line2 = scanner2.nextLine().trim();
                if (line2.contains("@attribute userID") || line2.contains("@attribute userid") || line2.contains("@attribute userId")) {
                    break;
                }
                sb.append(line2 + "\n");
            }
            String line = null;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.contains("@attribute userID") || line.contains("@attribute userid") || line.contains("@attribute userId")) {
                    break;
                }
            }

            String item1 = line.split(" ")[2];
            //String item2 = line2.split(" ")[2];
            sb.append("@attribute userID {" + item1.substring(1, item1.length() - 1) + "}\n\n");
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.equals("@data")) {
                    break;
                }
            }

            while (scanner2.hasNextLine()) {

                line = scanner2.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }
                sb.append(line + "\n");
            }
            try {
                writer = new FileWriter(output, false);
            } catch (Exception ex) {
                System.out.println("File not found: " + output);
            }
            try {
                writer.write(sb.toString());
            } catch (IOException ex) {
                Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (scanner.hasNextLine()) {

                line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    //writer.println(line);
                    writer.write(line + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            scanner.close();
            scanner2.close();
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

   }
}
