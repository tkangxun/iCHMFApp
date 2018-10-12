package symbolFeature;


import com.example.travis.ichmfapp.symbollib.*;

import java.io.*;

public class SymbolFeature {

    private static int symbolPointCount = 50;
    private static int pointScale = 100;
    private static double[] angleWithHorizon;
    private static int horizonAngleCount = 12;
    private static double[] turningAngle;
    private static int turningAngleCount = 18;
    private static int classNo;
    private static int featureNo = symbolPointCount * 2 + horizonAngleCount + turningAngleCount + 3;
    private static double[] features = new double[featureNo];
    private static double[] pointX = new double[symbolPointCount];
    private static double[] pointY = new double[symbolPointCount];
    //features
    private static int strokeCount;
    private int intersection;
    private int lineNo;
    private int curveNo;
    // x,y coordinates
    /*50 points. For each point the
    following 7 features were extracted: (a) normalized x
    coordinate, (b) normalized y coordinate, (c) direction
    angle ?(x) of the curve according to the x axis (cosine
    ?(x)), (d) direction angle ?(x) of the curve according
    to the y axis direction (sine ?(x)), (e) curvature
    according to x axis, (f) curvature according to y axis
    and (g) the position of stylus (up or down).*/

    /*coordinates
    of the resampled points, sines and cosines of the
    angle made by the line segments joining the points in the
    stroke, and the sines and cosines of the turning angle between
    the line segments and centre of gravity of the symbol.
    The centre of gravity of the symbol is sum(xi)/Nx , sum(yi)/Ny .*/
    public static int[] oneStroke = {48, 49, 50, 51, 52, 54, 55, 56, 57, 67, 71, 76, 77, 78, 79, 83,
            85, 86, 87, 90, 97, 98, 99, 100, 101, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
            115, 117, 118, 119, 121, 122, 40, 41, 46, 47, 60, 62, 91, 93, 94, 123, 125, 126, 945, 946, 949,
            952, 956, 961, 963, 966, 8721, 8722, 8730, 8733, 8734, 8747};
    public static int[] twoStroke = {52, 53, 55, 65, 66, 68, 70, 71, 74, 75, 77, 78, 80, 81, 82, 84, 86, 88,
            89, 90, 98, 102, 104, 105, 106, 107, 109, 110, 112, 116, 118, 120, 121, 122,
            43, 58, 60, 61, 62, 91, 93, 94, 215, 952, 955, 956, 8594, 8721, 8776, 8804, 8805};
    public static int[] threeStroke = {65, 69, 70, 72, 73, 75, 78, 89, 107, 37, 42, 177, 247, 8719, 8721,
            8800, 8804, 8805};
    public static int[] fourStroke = {69, 77, 87, 8721,};

    public SymbolFeature(int classID, double[] feature) {
        classNo = classID;
        features = feature;
    }

    public SymbolFeature() {
    }

    public static String getFeature(int classID, StrokeList strokelist) {
        classNo = classID;
        strokeCount = strokelist.size();
        int dotStrokeCount = 0; //count the number of stroke that has only 1 point
        int unusedStroke = 0;

        //added by quxi 2009.12.26
        //ignore the strokes with stroke points less than 2
        for (int i = 0; i < strokelist.size(); i++) {
            if (strokelist.get(i).getTotalStrokePoints() == 2) {
                unusedStroke += 1;
                dotStrokeCount += 2;
            } else if (strokelist.get(i).getTotalStrokePoints() == 1) {
                unusedStroke += 1;
                dotStrokeCount += 1;
            }
        }
        angleWithHorizon = new double[symbolPointCount - strokeCount
        + unusedStroke - dotStrokeCount];
        turningAngle = new double[angleWithHorizon.length - strokeCount
                + unusedStroke];

        double vectorX, vectorY;
        int indexH = 0;
        int indexT = 0;
        int pointIndex = 0;
        int count = 0;
        int strokePointCount = 0;
        for (int c = 0; c < strokeCount; c++) {
            for (int d = 0; d < strokelist.get(c).getTotalStrokePoints(); d++) {
                pointX[pointIndex] = strokelist.get(c).getStrokePoint(d).X;
                pointY[pointIndex] = strokelist.get(c).getStrokePoint(d).Y;
                pointIndex++;
            }
            strokePointCount = strokelist.get(c).getTotalStrokePoints();
            if (strokePointCount > 2) {
                for (int d = 0; d < strokePointCount; d++) {
                    if (d + 1 < strokePointCount) {
                        vectorX = strokelist.get(c).getStrokePoint(d + 1).X - strokelist.get(c).getStrokePoint(d).X;
                        vectorY = strokelist.get(c).getStrokePoint(d + 1).Y - strokelist.get(c).getStrokePoint(d).Y;
                        angleWithHorizon[d + indexH] = Math.acos(vectorX / Math.sqrt(Math.pow(vectorX, 2) + Math.pow(vectorY, 2)));
                        if (vectorY > 0) {
                            angleWithHorizon[d + indexH] = 2 * Math.PI - angleWithHorizon[d + indexH];
                        }
                    }
                }
                for (int j = 0; j < strokePointCount; j++) {
                    if (j + 2 < strokePointCount) {
                        turningAngle[j + indexT] = Math.abs(angleWithHorizon[j + 1 + indexH] - angleWithHorizon[j + indexH]);
                        if (turningAngle[j + indexT] >= Math.PI) {
                            turningAngle[j + indexT] = 2 * Math.PI - turningAngle[j + indexT];
                        }
                    }
                }
                indexH += strokelist.get(c).getTotalStrokePoints() - 1;
                indexT += strokelist.get(c).getTotalStrokePoints() - 2;
            }
        }
        features[0] = strokeCount; // feature 1
        count = getCenter(1);      //feature 2 - 3
        count = getPoints(count);  //feature 4 - 103
        count = getAngles(count, angleWithHorizon); // feature 104 - 115
        count = getTurningAngles(count, turningAngle); //feature 116 - 133
        return featureToString(features);
    }
    //turningAngleCount horizonAngleCount

    private static int getPoints(int count) {
        for (int j = 0; j < symbolPointCount; j++) {
            features[count + j] = pointX[j] / pointScale;
            features[count + j + symbolPointCount] = pointY[j] / pointScale;
        }
        return count + symbolPointCount * 2;
    }

    private static int getCenter(int count) {
        double tempCenterX = 0;
        double tempCenterY = 0;
        for (int j = 0; j < symbolPointCount; j++) {
            tempCenterX += pointX[j];
            tempCenterY += pointY[j];
        }
        features[count] = (tempCenterX / symbolPointCount) / pointScale;
        features[count + 1] = (tempCenterY / symbolPointCount) / pointScale;
        return count + 2;
    }

    private static int getAngles(int count, double[] angleIn) {
        int[] angles = new int[horizonAngleCount];
        int section;
        for (int i = 0; i < horizonAngleCount; i++) {
            angles[i] = 0;
        }
        for (int i = 0; i < angleIn.length; i++) {
            section = (int) Math.floor(angleIn[i] / (2 * Math.PI / horizonAngleCount));
            if (section == horizonAngleCount) {
                section = horizonAngleCount - 1;
            }
            angles[section]++;
        }
        for (int i = 0; i < horizonAngleCount; i++) {
            features[count + i] = angles[i] * 1.0 / angleIn.length;
            //System.out.println("The i is" + (count + i) + " count is " + angles[i]);
            //System.out.println("The i is" + (count + i) + " features is " + features[count + i]);
        }
        return count + horizonAngleCount;
    }

    private static int getTurningAngles(int count, double[] angleIn) {
        int[] angles = new int[turningAngleCount];
        int section;
        for (int i = 0; i < turningAngleCount; i++) {
            angles[i] = 0;
        }
        for (int i = 0; i < angleIn.length; i++) {
            section = (int) Math.floor(angleIn[i] / (Math.PI / turningAngleCount));
            if (section == turningAngleCount) {
                section = turningAngleCount - 1;
            }
            angles[section]++;
        }
        for (int i = 0; i < turningAngleCount; i++) {
            features[count + i] = angles[i] * 1.0 / angleIn.length;
            //System.out.println("The i is" + (count + i) + " count is " + angles[i]);
            //System.out.println("The i is" + (count + i) + " features is " + features[count + i]);
        }
        return count + turningAngleCount;
    }

    private static String featureToString(double[] features) {
        String st = (classNo + " ");
        for (int i = 1; i < features.length + 1; i++) {
            if (features[i - 1] != 0) {
                st += (i + ":" + features[i - 1] + " ");
            }
        }
        return st;
    }

    public static void writeFeatures(String featureString) {
        try {
            File file = new File(ConstantData.trainFile);
            FileWriter fstream = new FileWriter(file, file.exists());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(featureString);
            out.write("\n");
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
