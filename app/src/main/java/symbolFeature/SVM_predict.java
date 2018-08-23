package symbolFeature;

/**
 * Created by Travis on 23/8/2018.
 */

        import com.example.travis.ichmfapp.symbollib.*;

        import libsvm.*;
        import java.io.*;
        import java.util.Collections;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.StringTokenizer;


public class SVM_predict {

    private String model_file_name = ConstantData.modelFile;

    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    private static List predict(String line, svm_model model, int predict_probability) throws IOException {
        List<SVMResult> result = new ArrayList();

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR
                    || svm_type == svm_parameter.NU_SVR) {
                System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                for (int j = 0; j < nr_class; j++) {
                    SVMResult st = new SVMResult();
                    st.setIndex(labels[j]);
                    result.add(st);
                }
            }
        }

        if (line == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

        double target = atof(st.nextToken()); //can't remove
        int m = st.countTokens() / 2;
        svm_node[] x = new svm_node[m];
        for (int j = 0; j < m; j++) {
            x[j] = new svm_node();
            x[j].index = atoi(st.nextToken());
            x[j].value = atof(st.nextToken());
        }

        double v;
        if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
            v = svm.svm_predict_probability(model, x, prob_estimates);
            for (int j = 0; j < nr_class; j++) {
                result.get(j).setProb(prob_estimates[j]);
            }
        } else {
            v = svm.svm_predict(model, x);
        }

        //sort result class by probability in descending order
        //havent tested yet
        Collections.sort(result, new ByProbability());
        if (result.get(0).getIndex() != v) {
            result.get(0).setIndex((int) v);
            result.get(0).setProb(1);
        }
        while (result.size() > 10) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public List run(String test, int probability) {
        int predict_probability = probability;
        List<SVMResult> result = new ArrayList();
        try {
            //BufferedReader input = new BufferedReader(new FileReader(argv[i]));
            //DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
            svm_model model = svm.svm_load_model(model_file_name);
            if (predict_probability == 1) {
                if (svm.svm_check_probability_model(model) == 0) {
                    System.err.print("Model does not support probabiliy estimates\n");
                    System.exit(1);
                }
            } else {
                if (svm.svm_check_probability_model(model) != 0) {
                    System.out.print("Model supports probability estimates, but disabled in prediction.\n");
                }
            }
            result = predict(test, model, predict_probability);
        } catch (IOException e) {
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return result;
    }
}

class ByProbability implements java.util.Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        SVMResult st1 = (SVMResult) o1;
        SVMResult st2 = (SVMResult) o2;
        int sdif = (st1.getProb() - st2.getProb()) > 0 ? -1 : 1;
        return sdif;
    }
}

