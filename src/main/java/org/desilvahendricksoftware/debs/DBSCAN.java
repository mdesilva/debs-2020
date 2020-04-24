package org.desilvahendricksoftware.debs;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class DBSCAN implements Serializable{

    public static double epsilon;
    public static int minPoints;
    public static HashSet<Point> visitedPoints = new HashSet<>();
    public static ArrayList res = new ArrayList();
    public int[] clusters;
    public int[] labels;
    public boolean hasNoiseCluster;
    public int numberOfClustersWithoutNoiseCluster;

    public DBSCAN(double epsilon, int minPoints){
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }


    //TODO: test
    public ArrayList<ArrayList<Point>> performDBSCANHelper(Point[] points){
        visitedPoints.clear();
        res.clear();

        int spacing = points.length / 2;

        DBSCANHelper t1 = new DBSCANHelper(Arrays.copyOfRange(points, 0, spacing), points);
        t1.start();

        DBSCANHelper t2 = new DBSCANHelper(Arrays.copyOfRange(points, spacing, spacing * 2), points);
        t2.start();

//        DBSCANHelper t3 = new DBSCANHelper(Arrays.copyOfRange(points, spacing*2, spacing*3), points);
//        t3.start();
//
//        DBSCANHelper t4 = new DBSCANHelper(Arrays.copyOfRange(points, spacing*3, points.length), points);
//        t4.start();

        try {
            t1.join();
            t2.join();
//            t3.join();
//            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static ArrayList<Point> getNeighbors(Point input, Point[] points, double epsilon ){
        ArrayList<Point> ret = new ArrayList<Point>();
        for(int i = 0; i < points.length; i++){
            Point candidate = points[i];
            if(calculateDistance(candidate,input) <= epsilon){
                ret.add(candidate);
            }
        }
        return ret;
    }

    public static ArrayList<Point> mergeRightToLeftCollection(ArrayList<Point> neighbors1, ArrayList<Point> neighbors2){
        for (int i = 0; i < neighbors2.size(); i++) {
            Point tempPt = neighbors2.get(i);
            if (!neighbors1.contains(tempPt)) {
                neighbors1.add(tempPt);
            }
        }
        return neighbors1;
    }

    public static double calculateDistance(Point pointOne, Point pointTwo){
        double pow1 = Math.pow(pointTwo.f0-pointOne.f0, 2.0);
        double pow2 = Math.pow(pointTwo.f1-pointOne.f1,2.0);
        return Math.sqrt(pow1 + pow2);
    }


    public void fit(Point[] points){
        int clusters[] = new int[points.length];
        ArrayList<ArrayList<Point>> result = performDBSCANHelper(points);
//        System.out.println(Arrays.toString(points));
        for(int k = 0; k < points.length; k++){
            boolean flag = false;
            for(int j = 0; j< result.size(); j++){
                ArrayList<Point> temp = result.get(j);
                if(temp.contains(points[k])){
                    clusters[k] = j;
                    flag = true;
                    break;
                }
            }
            if(!flag){
                clusters[k] = -1;
            }
        }
        this.clusters = clusters;
        this.labels = ArrayUtils.unique(this.clusters);
        this.hasNoiseCluster = ArrayUtils.contains(this.labels, -1) ?  true : false;
        this.numberOfClustersWithoutNoiseCluster = ArrayUtils.contains(this.labels, -1) ? this.labels.length - 1 : this.labels.length;
    }

}
