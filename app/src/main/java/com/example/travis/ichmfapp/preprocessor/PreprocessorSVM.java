package com.example.travis.ichmfapp.preprocessor;

import com.example.travis.ichmfapp.symbollib.*;
import com.example.travis.ichmfapp.symbollib.Stroke;




/**
 * Created by Travis on 9/8/2018.
 */

public class PreprocessorSVM {


    private static int thresHold = 5;
    private static int desiredPointNum = 50;
    private static int closePoint = 20;
    private static Box standardRec = new Box(0, 0, 100, 100);


    public PreprocessorSVM() {}
    /**
     * The wrapper method to call the series of pre-processing steps.
     * @param _inputStrokeList StrokeList to be processed.
     * @return The transformed StrokeList.
     * @see StrokeList
     */
    public static StrokeList preProcessing(StrokeList _inputStrokeList) {
        int pts =0 ;
        for (int i = 0; i < _inputStrokeList.size(); i++) {
            pts +=_inputStrokeList.get(i).getTotalStrokePoints();
        }

        if (pts<5){

            StrokeList dot = new StrokeList();
            Stroke s = new Stroke();
            StrokePoint pt = new StrokePoint(50,50);
            s.addStrokePoint(pt);
            dot.add(s);

            if (_inputStrokeList.size()==2){
                if (Math.abs(_inputStrokeList.get(0).getStrokePoint(0).X -
                        _inputStrokeList.get(1).getStrokePoint(0).X) <= 20) {
                    StrokePoint ptpt = new StrokePoint(50, 150);
                    Stroke dotdot = new Stroke();
                    dotdot.addStrokePoint(ptpt);
                    dot.add(dotdot);
                }
            }
            return dot;
            //return _inputStrokeList;

        }
        return normallizePoint(reSampling(smoothing(normalizing(_inputStrokeList))));
    }

    /**
     * Classify two StrokeList based on their closeness.
     * @param s1 First StrokeList to be classified.
     * @param s2 Second StrokeList to be classified.
     * @return True if two inputs satisfy the condition, False otherwise.
     */
    public static boolean preClassify(StrokeList s1, StrokeList s2) {
        //If the two start points are close enough return true;
        //If the trow end points are close enough return true;
        //else return False;
        if (distance(s1.get(0).getStrokePoint(0), s2.get(0).getStrokePoint(0)) <= closePoint) {
            return true;
        } else if (distance(s1.get(s1.size() - 1).getStrokePoint(
                s1.get(s1.size() - 1).getTotalStrokePoints() - 1),
                s2.get(s2.size() - 1).getStrokePoint(
                        s2.get(s2.size() - 1).getTotalStrokePoints() - 1)) <= closePoint) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resample each stroke in given list to have averagely same
     * distance between the stroke points.
     * @param _inputStrokeList
     * @return
     */
    private static StrokeList reSampling(StrokeList _inputStrokeList) {
        for (int k = 0; k < _inputStrokeList.size(); k++) {
            int i = 0;
            while (i < _inputStrokeList.get(k).getTotalStrokePoints()) {
                if ((i + 1) < _inputStrokeList.get(k).getTotalStrokePoints()) {
                    int dis = (int) distance(_inputStrokeList.get(k).getStrokePoint(i),
                            _inputStrokeList.get(k).getStrokePoint(i + 1));
                    if (dis < thresHold) {
                        _inputStrokeList.get(k).getStrokePoints().remove(
                                _inputStrokeList.get(k).getStrokePoint(i + 1));
                    } else if (dis > thresHold) {
                        StrokePoint p1 = _inputStrokeList.get(k).getStrokePoint(i);
                        StrokePoint p2 = _inputStrokeList.get(k).getStrokePoint(i + 1);
                        double m = (double) (p2.X - p1.X) / dis;
                        double n = (double) (p2.Y - p1.Y) / dis;
                        StrokePoint sp = new StrokePoint(
                                (p1.X + thresHold * m),
                                (p1.Y + thresHold * n));
                        _inputStrokeList.get(k).getStrokePoints().add(i + 1, sp);
                        i++;
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
            }
        }
        return _inputStrokeList;
    }

    private static StrokeList normallizePoint(StrokeList _inputStrokeList) {
        //find the actual distance
        int totalPoint = countTotalPoint(_inputStrokeList);
        double newThres = (totalPoint * 1.0 / desiredPointNum) * thresHold;
        for (int k = 0; k < _inputStrokeList.size(); k++) {
            int i = 0;
            while (i < _inputStrokeList.get(k).getTotalStrokePoints()) {
                if ((i + 1) < _inputStrokeList.get(k).getTotalStrokePoints()) {
                    double dis = distance(_inputStrokeList.get(k).getStrokePoint(i),
                            _inputStrokeList.get(k).getStrokePoint(i + 1));
                    if (dis < newThres) {
                        _inputStrokeList.get(k).getStrokePoints().remove(
                                _inputStrokeList.get(k).getStrokePoint(i + 1));
                    } else if (dis > newThres) {
                        StrokePoint p1 = _inputStrokeList.get(k).getStrokePoint(i);
                        StrokePoint p2 = _inputStrokeList.get(k).getStrokePoint(i + 1);
                        double m = (double) (p2.X - p1.X) / dis;
                        double n = (double) (p2.Y - p1.Y) / dis;
                        StrokePoint sp = new StrokePoint(
                                (p1.X + newThres * m),
                                p1.Y + newThres * n);
                        _inputStrokeList.get(k).getStrokePoints().add(i + 1, sp);
                        i++;
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
            }
        }
        int totalPoints = countTotalPoint(_inputStrokeList);
        int pointDifference = totalPoints - desiredPointNum;
        int i = _inputStrokeList.size() - 1;
        while (i >= 0 && pointDifference != 0) {
            Stroke st = _inputStrokeList.get(i);
            i--;
            int pointChange = (int) 1.0 * pointDifference * st.getTotalStrokePoints() / totalPoints;
            while (pointChange > 0) {
                st.removeStrokePoint(st.getTotalStrokePoints() - 1);
                pointChange--;
            }
            while (pointChange < 0) {
                StrokePoint p1 = st.getStrokePoint(st.getTotalStrokePoints() - 1);
                StrokePoint p2 = st.getStrokePoint(st.getTotalStrokePoints() - 2);
                double m = (double) (p1.X - p2.X) / newThres;
                double n = (double) (p1.Y - p2.Y) / newThres;
                StrokePoint sp = new StrokePoint((p1.X + newThres * m), p1.Y + newThres * n);
                st.addStrokePoint(sp);
                pointChange++;
            }
        }

        //adjustment
        totalPoints = countTotalPoint(_inputStrokeList);
        if (totalPoints != desiredPointNum) {
            int largestStroke = -1;
            int strokePointCount = 0;
            for (int k = 0; k < _inputStrokeList.size(); k++) {
                Stroke st = _inputStrokeList.get(k);
                if (strokePointCount < st.getTotalStrokePoints()) {
                    largestStroke = k;
                    strokePointCount = st.getTotalStrokePoints();
                }
            }
            pointDifference = totalPoints - desiredPointNum;
            Stroke st = _inputStrokeList.get(largestStroke);
            while (pointDifference > 0) {
                st.removeStrokePoint(st.getTotalStrokePoints() - 1);
                pointDifference--;
            }
            while (pointDifference < 0) {
                StrokePoint p1 = st.getStrokePoint(st.getTotalStrokePoints() - 1);
                StrokePoint p2 = st.getStrokePoint(st.getTotalStrokePoints() - 2);
                double m = (double) (p1.X - p2.X) / newThres;
                double n = (double) (p1.Y - p2.Y) / newThres;
                StrokePoint sp = new StrokePoint(
                        (p1.X + newThres * m),
                        p1.Y + newThres * n);
                st.addStrokePoint(sp);
                pointDifference++;
            }
        }
        return _inputStrokeList;
    }

    private static int countTotalPoint(StrokeList _inputStrokeList) {
        int totalPoint = 0;
        for (int k = 0; k < _inputStrokeList.size(); k++) {
            totalPoint += _inputStrokeList.get(k).getTotalStrokePoints();
        }
        return totalPoint;
    }

    /**
     * Smoothen each strok in given list by relocating the stroke point
     * to smoother angular position.
     * @param _inputStrokeList StrokeList to be processed.
     * @return Transformed StrokeList object.
     * @see StrokeList
     */
    private static StrokeList smoothing(StrokeList _inputStrokeList) {
        for (int c = 0; c < _inputStrokeList.size(); c++) {
            for (int d = 0; d < _inputStrokeList.get(c).getTotalStrokePoints(); d++) {
                if (d + 2 < _inputStrokeList.get(c).getTotalStrokePoints()) {
                    StrokePoint averagePoint = new StrokePoint(
                            (_inputStrokeList.get(c).getStrokePoint(d).X + _inputStrokeList.get(c).getStrokePoint(d + 1).X + _inputStrokeList.get(c).getStrokePoint(d + 2).X) / 3,
                            (_inputStrokeList.get(c).getStrokePoint(d).Y + _inputStrokeList.get(c).getStrokePoint(d + 1).Y + _inputStrokeList.get(c).getStrokePoint(d + 2).Y) / 3);
                    _inputStrokeList.get(c).setStrokePoint(averagePoint, d + 1);
                }
            }
        }
        return _inputStrokeList;
    }

    /**
     * Normalize size of each input stroke to a standard size.
     * @param _inputStrokeList StrokeList to be processed.
     * @return Transformed StrokeList object.
     * @see StrokeList
     */
    private static StrokeList normalizing(StrokeList _inputStrokeList) {
        double left = 10000, right = -10000, top = 10000, bottom = -10000;
        Stroke _strokeTemp;

        for (int i = 0; i < _inputStrokeList.size(); i++) {
            _strokeTemp = _inputStrokeList.get(i);
            for (int j = 0; j < _strokeTemp.getTotalStrokePoints(); j++) {
                StrokePoint temp = _strokeTemp.getStrokePoint(j);
                if (temp.X < left) {
                    left = temp.X;
                }
                if (temp.X > right) {
                    right = temp.X;
                }
                if (temp.Y < top) {
                    top = temp.Y;
                }
                if (temp.Y > bottom) {
                    bottom = temp.Y;
                }
            }
        }

        double width = right - left + 1;
        double height = bottom - top + 1;
        double scale = Math.max(width, height) / standardRec.getWidth();

        StrokeList result = new StrokeList();
        Stroke tempStroke;

        for (int c = 0; c < _inputStrokeList.size(); c++) {
            tempStroke = new Stroke();
            for (int d = 0; d < _inputStrokeList.get(c).getTotalStrokePoints(); d++) {
                tempStroke.addStrokePoint(
                        new StrokePoint(
                                (_inputStrokeList.get(c).getStrokePoint(d).X - left) / scale,
                                (_inputStrokeList.get(c).getStrokePoint(d).Y - top) / scale));
            }
            result.add(tempStroke);
        }
        return result;

    }

    /**
     * Get the distance between two stroke point.
     * @param p1 First stroke point for calculation.
     * @param p2 Second stroke point for calculation.
     * @return The distance between the two stroke point.
     */
    private static double distance(StrokePoint p1, StrokePoint p2) {
        return Math.sqrt((p1.X - p2.X) * (p1.X - p2.X) + (p1.Y - p2.Y) * (p1.Y - p2.Y));
    }

}
