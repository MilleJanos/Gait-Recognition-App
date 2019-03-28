package ms.sapientia.ro.feature_extractor;
import java.util.ArrayList;
import weka.core.Instances;

/**
 *
 * @author claudiu
 */
public interface IUtil {

    /**
     * Creates a list off Attribute objects from a list of Feature objects.
     *
     * @author Krisztian Nemeth
     * @version 1.0
     * @param features the ArrayList containing the Feature objects
     * @return ArrayList containing the resulting Attribute objects
     * @since 23 ‎July, ‎2018
     */
    public Instances arrayListOfFeaturesToInstances(ArrayList<Feature> features);
}
