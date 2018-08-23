package libsvm;

/**
 * Created by Travis on 23/8/2018.
 */

public class svm_problem implements java.io.Serializable
{
    public int l;
    public double[] y;
    public svm_node[][] x;
}

