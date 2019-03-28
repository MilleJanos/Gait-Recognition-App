package ms.sapientia.ro.model_builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import weka.classifiers.Classifier;

public class TestModelCreation_Main {

    public static final String FEATURE_USER_FILE = "./data/INPUT_ARFF/am_dummy.arff";
    public static void main(String args[]){
        IGaitModelBuilder builder = new GaitModelBuilder();
              System.out.println( FEATURE_USER_FILE);
//
//        try {
//            Scanner scanner = new Scanner( new File( FEATURE_USER_FILE));
//            while( scanner.hasNextLine()){
//                System.out.println( scanner.nextLine());
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        Classifier classifier = builder.createModel(FEATURE_USER_FILE);
        System.out.println(classifier.toString());
    }
}
