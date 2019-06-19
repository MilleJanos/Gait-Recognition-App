package ms.sapientia.ro.gaitrecognitionapp.logic;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.security.InvalidParameterException;

import ms.sapientia.ro.gaitrecognitionapp.common.FileUtil;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.IFileCallback;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;

/**
 * This class is made to create custom upload and download transactions
 * only for Gait Recognition application.
 */
public class MyFirebaseController extends FirebaseController {

    // Upload methods:

    /**
     * This method uploads a file into raw folder under user id folder under user id folder
     */
    public static void uploadRawFileIntoStorage(File file, String userId, ICallback callback){
        if( file == null ){
            throw new InvalidParameterException("File can't be empty!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("User id can't be empty!");
        }
        uploadFileIntoStorage(
                file,
                userId,
                FirebaseUtils.STORAGE_RAW_KEY,
                callback );
    }

    /**
     * This method uploads a file into feature folder under user id folder
     */
    public static void uploadFeatureFileIntoStorage(File file, String userId, ICallback callback){
        if( file == null ){
            throw new InvalidParameterException("File can't be empty!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("User id can't be empty!");
        }
        uploadFileIntoStorage(
                file,
                userId,
                FirebaseUtils.STORAGE_FEATURE_KEY,
                callback );
    }

    /**
     * This method uploads a file into model folder under user id folder
     */
    public static void uploadModelFileIntoStorage(File file, String userId, ICallback callback){
        if( file == null ){
            throw new InvalidParameterException("File can't be empty!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("User id can't be empty!");
        }
        uploadFileIntoStorage(
                file,
                userId,
                FirebaseUtils.STORAGE_MODEL_KEY,
                callback );
    }

    /**
     * This method uploads a file into merged/feature folder under user id folder
     */
    public static void uploadMergedFeatureFileIntoStorage(File file, String userId, ICallback callback){
        if( file == null ){
            throw new InvalidParameterException("File can't be empty!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("User id can't be empty!");
        }
        uploadFileIntoStorage(
                file,
                userId,
                FirebaseUtils.STORAGE_MERGED_KEY + "/" + FirebaseUtils.STORAGE_FEATURE_KEY,
                callback );
    }

    /**
     * This method uploads a file into merged/model folder under user id folder
     */
    public static void uploadMergedModelFileIntoStorage(File file, String userId, ICallback callback){
        if( file == null ){
            throw new InvalidParameterException("File can't be empty!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("User id can't be empty!");
        }
        uploadFileIntoStorage(
                file,
                userId,
                FirebaseUtils.STORAGE_MERGED_KEY + "/" + FirebaseUtils.STORAGE_MODEL_KEY,
                callback);
    }

    /**
     * This method uploads a file into a given Firebase Storage folder under user id folder.
     *
     * @param file file to download
     * @param userId user string id
     * @param subDirectories directories in Firebase
     */
    private static void uploadFileIntoStorage(File file, String userId, String subDirectories, ICallback callback){

        StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                        + "/" + userId
                        + "/" + subDirectories
                        + "/" + file.getName()
        );
        FirebaseController.uploadFile(reference, file, callback);

    }

    // Download methods:

    /**
     * This method downloads the file named downloadableFileName from Firebase under user_id/merged/feature.
     * Callback methods will be called in order of the success.
     * Output file will be automatically created.
     *
     * @param downloadableFileName downloadable file name in firebase
     * @param intoFile downloaded file will be saved here
     * @param userId user id
     * @param callback callback methods (leave null in case you don't want methods to call)
     */
    public static void downloadFeatureFile(String downloadableFileName, File intoFile, String userId, IFileCallback callback){

        // Handle inputs:
        if( downloadableFileName.isEmpty() ){
            throw new InvalidParameterException("Parameter: downloadableFileName can't be empty!");
        }
        if( intoFile == null ){
            throw new InvalidParameterException("Parameter: intoFile can't be null!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("Parameter: userId can't be empty!");
        }

        // Create output file:
        FileUtil.createFileIfNotExists(intoFile);

        // Set reference:
        StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                        + "/" + userId
                        + "/" + FirebaseUtils.STORAGE_MERGED_KEY
                        + "/" + FirebaseUtils.STORAGE_FEATURE_KEY
                        + "/" + downloadableFileName
        );

        // Download file and call callback methods:
        new FirebaseController().downloadFile(reference, intoFile, callback);

    }

    /**
     * This method downloads the file named downloadableFileName from Firebase under user_id/merged/model.
     * Callback methods will be called in order of the success.
     * Output file will be automatically created.
     *
     * @param downloadableFileName downloadable file name in firebase
     * @param intoFile downloaded file will be saved here
     * @param userId user id
     * @param callback callback methods (leave null in case you don't want methods to call)
     */
    public static void downloadModelFile(String downloadableFileName, File intoFile, String userId, IFileCallback callback){

        // Handle inputs:
        if( downloadableFileName.isEmpty() ){
            throw new InvalidParameterException("Parameter: downloadableFileName can't be empty!");
        }
        if( intoFile == null ){
            throw new InvalidParameterException("Parameter: intoFile can't be null!");
        }
        if( userId.isEmpty() ){
            throw new InvalidParameterException("Parameter: userId can't be empty!");
        }

        // Create output file:
        FileUtil.createFileIfNotExists(intoFile);

        // Set reference:
        StorageReference reference = FirebaseUtils.firebaseStorage.getReference().child(
                FirebaseUtils.STORAGE_DATA_KEY
                        + "/" + userId
                        + "/" + FirebaseUtils.STORAGE_MERGED_KEY
                        + "/" + FirebaseUtils.STORAGE_MODEL_KEY
                        + "/" + downloadableFileName
        );

        // Download file and call callback methods:
        new FirebaseController().downloadFile(reference, intoFile, callback);

    }




}
