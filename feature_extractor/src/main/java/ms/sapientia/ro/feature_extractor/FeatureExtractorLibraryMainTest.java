package ms.sapientia.ro.feature_extractor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import ms.sapientia.ro.feature_extractor.FeatureExtractorException;

/**
 * FeatureExtractorLibraryMainTest is the class I used to test the
 * FeatureExtractor class
 *
 * @author Krisztian Nemeth
 * @version 1.0
 * @since 23 ‎July, ‎2018
 */
public class FeatureExtractorLibraryMainTest {

    /**
     * This is the function that contains invocations of the FeatureExtractor
     * class functions with the specified settings
     *
     * @author Krisztian Nemeth
     * @version 1.0
     * @since 23 ‎July, ‎2018
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //THESE ARE ALL JUST TEST
        //THEY CAN GIVE YOU INSIGHT ABOUT THE LIBRARY'S USAGE

        //String IOFolder = "../FeatureExtractorLibrary_IO_files/";
        String current = null;
        try {
            current = new File( "." ).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Current dir:"+current);
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" +currentDir);

        //**************
        String IOFolder = "./data/";
        String inputFileName = "data_ttJMxBAjuHNVLCKhaXNvBTFDbIc2_20181122_161810.csv";
        String outputFileName = "features_ttJMxBAjuHNVLCKhaXNvBTFDbIc2_20181122_161810";

        //settings regarding the input and output files
        ms.sapientia.ro.feature_extractor.Settings.setInputHasHeader(true); //input has a header that has to be skipped
        ms.sapientia.ro.feature_extractor.Settings.setOutputHasHeader(true); //output will have a header
        ms.sapientia.ro.feature_extractor.Settings.setOutputFileType(ms.sapientia.ro.feature_extractor.Settings.FileType.ARFF); //output will be an .arff file
        ms.sapientia.ro.feature_extractor.Settings.setDefaultUserId("dummy");

        //if we would like to use walking cycles based feature extraction
        //Settings.usingCycles();
        //Settings.setNumStepsIgnored(1); //ignoring first and last step

        //if we would like to use walking cycles based feature extraction
        ms.sapientia.ro.feature_extractor.Settings.usingFrames(128); //using frames made of 128 datapoints
        ms.sapientia.ro.feature_extractor.Settings.setNumFramesIgnored(2); //ignoring first and last 2 frames (256 datapoints in this scenario)

        //now based on the previous settings we are extracting features from the input file
        try {
            //extracting into a file
            FeatureExtractor.extractFeaturesFromCsvFileToFile(IOFolder + inputFileName, outputFileName);

            //extracting into a list
            List<Feature> featureList = FeatureExtractor.extractFeaturesFromCsvFileToArrayListOfFeatures(IOFolder + inputFileName);
            //System.out.println(featureList);
        } catch (FeatureExtractorException ex) {
            Logger.getLogger(FeatureExtractorLibraryMainTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        //now we are extracting features from an ArrayList<Feature>
        //first we read the data
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(IOFolder + inputFileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file " + IOFolder + inputFileName);
            System.exit(1);
        }

        if (ms.sapientia.ro.feature_extractor.Settings.getInputHasHeader() && scanner.hasNextLine()) {
            scanner.nextLine();
        }

        ArrayList<Accelerometer> dataset = new ArrayList<>();
        while (scanner.hasNextLine()) {  //lines starting the first index

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }
            String items[] = line.split(",");
            if (items.length != 5) {
                System.out.println("Corrupted input file error");
                return;
            }
            dataset.add(new Accelerometer(Long.parseLong(items[0]), //timesptamp
                    Double.parseDouble(items[1]), //X
                    Double.parseDouble(items[2]), //Y
                    Double.parseDouble(items[3]), //Z
                    Integer.parseInt(items[4])));                    //stepNumber
        }

        //exract features from the dataset
        try {
            //extracting into a file
            FeatureExtractor.extractFeaturesFromArrayListToFile(dataset, outputFileName, ms.sapientia.ro.feature_extractor.Settings.getDefaultUserId());

            //extracting into a list
            List<Feature> featureList2 = FeatureExtractor.extractFeaturesFromArrayListToArrayListOfFeatures(dataset, ms.sapientia.ro.feature_extractor.Settings.getDefaultUserId());
            //System.out.println(featureList2);
        } catch (FeatureExtractorException ex) {
            Logger.getLogger(FeatureExtractorLibraryMainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
