/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ms.sapientia.ro.feature_extractor;
/**
 * FeatureExtractorException is a custom exception used in
 * FeatureExtractorLibrary
 *
 * @author Krisztian Nemeth
 * @version 1.0
 * @since 23 ‎July, ‎2018
 */
public class FeatureExtractorException extends Exception {

    FeatureExtractorException(String message) {
        super(message);
    }
}
