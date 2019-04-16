package ms.sapientia.ro.model_builder;

import ms.sapientia.ro.FeatureExtractor;
//import ms.sapientia.ro.feature_extractor.FeatureExtractor;
import ms.sapientia.ro.feature_extractor.Settings;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void main(String[] args) {
        String userName = "WsY044SgeaeZtDrQKVpRyWpo7hx1";

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
        }

        //Merge two arff file in user's features file, for binary classification
        mergeEquallyArffFiles(FEATURE_DUMMY_FILE, FEATURE_USER_FILE);

        /*try{
            createAndSaveModel(FEATURE_USER_FILE, MODEL_FILE);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, e);
        }*/

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier = builder.createModel(FEATURE_USER_FILE);

        ArrayList<Attribute> attributes = builder.getAttributes(FEATURE_USER_FILE);

        System.out.println( "NumAttributes: "+attributes.size() );
        IGaitVerification verifier = new GaitVerification();
        //IUtil utility = new Util();
        System.out.println("Probability: " + verifier.verifyUser(classifier, attributes, TEST_RAW_FILE_USER));

    }


    /**
     *
     * @param rawDataFile
     * @param featureFile - file path contains file name to give where we want to save the features
     * @param userName
     * set up feature extraction settings, then use the FeatureExtractor
     */
    public static void createFeaturesFileFromRawFile(String rawDataFile, String featureFile, String userName){
        Settings.usingFrames(128);
        Settings.setInputHasHeader(true);
        Settings.setOutputHasHeader(true); // full arff, no header
        Settings.setOutputFileType(Settings.FileType.ARFF);
        Settings.setDefaultUserId(userName);
        Settings.setNumFramesIgnored(4); //ignoring noisy data


        try {
            FeatureExtractor.extractFeaturesFromCsvFileToFile(rawDataFile, featureFile);
        } catch (Exception e) {
            //e.printStackTrace();
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void createAndSaveModel(String userFeatureFilePath, String userModelFilePath){
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
        //String evalsummary = gaitModelBuilder.evaluateModel(ann, traindataset, testdataset);
        //System.out.println("Evaluation: " + evalsummary);

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

        //String item1 = line.split("\\{")[0].split(" ")[];
        //String item2 = line2.split("\\{")[0].split(" ")[1];
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



        Scanner scanner2 = null;
        try {
            scanner2 = new Scanner(new File(output));
        } catch (Exception ex) {
            System.out.println("File not found: " + output);
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(output, false);
        } catch (Exception ex) {
            System.out.println("File not found: " + output);
        }

        OutputStream out = new FileOutputStream(output);

        /*
            Iterate trought files name list
        */


        for(String input:files) {

            counter ++;

            // open input file
            Scanner inScanner = null;
            try {
                inScanner = new Scanner(new File(input));
            } catch (Exception ex) {
                System.out.println("File not found: " + input);
            }

            if(counter == 1){   // Print the header only once
                //int n = 1024;
                //byte[] buf = new byte[n];
                //InputStream in = new FileInputStream(input);
                //int b = 0;
                //while ( (b = in.read(buf)) >= 0) {
                //    out.write(buf, 0, b);
                //    out.flush();
                //}
                String line;
                while( inScanner.hasNextLine()){
                    line = inScanner.nextLine().trim();
                    line += "\n";
                    out.write(line.getBytes(), 0, line.length());
                    out.flush();
                }
                //out.close();
                continue;
            }else{
                boolean printFromNext = false;
                //int n = 1024;
                //byte[] buf = new byte[n];
                //InputStream in = new FileInputStream(input);
                //int b = 0;
                //while ( (b = in.read(buf)) >= 0) {
                //    String str = new String(buf, StandardCharsets.UTF_8);
                //    String[] splitted = str.split(" ");
                //
                //    if(printFromNext){
                //        out.write(buf, 0, b);
                //        out.flush();
                //    }
                String line;
                while( inScanner.hasNextLine()){
                    line = inScanner.nextLine().trim();

                    if(printFromNext) {
                        line += "\n";
                        out.write(line.getBytes(), 0, line.length());
                        out.flush();
                    }

                    /*
                    String[] splitted = line.split(" ");

                    if(splitted.length >= 2) {
                        if (splitted[1].equals("userid") || splitted[1].equals("userId") || splitted[1].equals("userID")) {
                            printFromNext = true;
                        }
                    }
                    */
                    if(line.equals("@data")){
                        printFromNext = true;
                    }

                }
                //in.close();
                inScanner.close();
                continue;
            }
            /*
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File(input));
            } catch (FileNotFoundException ex) {
                System.out.println("Unable to open file " + input);
            }

            StringBuilder sb = new StringBuilder();





            String line2 = null;
            while (scanner2.hasNextLine()) {
                line2 = scanner2.nextLine().trim();
                sb.append(line2 + "\n");
                if (line2.contains("@attribute userID") || line2.contains("@attribute userid") || line2.contains("@attribute userId")) {
                    break;
                }
                //sb.append(line2 + "\n");
            }



            String line = null;
            if(counter==1) {
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine().trim();
                    if (line.contains("@attribute userID") || line.contains("@attribute userid") || line.contains("@attribute userId")) {
                        break;
                    }
                }


                String item1 = line.split(" ")[2];


                sb.append("@attribute userID {" + item1.substring(1, item1.length() - 1) + "}\n\n");
                while (scanner.hasNextLine()) {

                    line = scanner.nextLine().trim();
                    if (line.equals("@data")) {
                        break;
                    }
                }

            }


            if(counter == 1) {
                while (scanner2.hasNextLine()) {

                    line = scanner2.nextLine().trim();

                    if (line.isEmpty()) {
                        continue;
                    }
                    sb.append(line + "\n");
                }
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

            */
        }

        //out.close();

        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        scanner2.close();

    }
}
