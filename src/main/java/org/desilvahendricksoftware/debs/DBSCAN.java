package org.desilvahendricksoftware.debs;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class DBSCAN implements Serializable{

    private double epsilon;
    private int minPoints;
    private HashSet<Point> visitedPoints = new HashSet<>();
    public int[] clusters;
    public int[] labels;
    public boolean hasNoiseCluster;
    public int numberOfClustersWithoutNoiseCluster;

    public DBSCAN(double epsilon, int minPoints){
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }

    public double getEpsilon() {
        return this.epsilon;
    }

    public int getMinPoints() {
        return this.minPoints;
    }

    //TODO: test
    public ArrayList<ArrayList<Point>> performDBSCANHelper(Point[] points){
        int index = 0;
        ArrayList<ArrayList<Point>> res = new ArrayList<>();
        visitedPoints.clear();
        while (index < points.length){
            Point current = points[index];
            if(!visitedPoints.contains(current)){
                visitedPoints.add(current);
                ArrayList<Point> neighbors = getNeighbors(current,points);
                if(neighbors.size() >= this.minPoints){
                    int ind = 0;
                    while(ind < neighbors.size()){
                        Point curr = neighbors.get(ind);
                        if(!visitedPoints.contains(curr)){
                            visitedPoints.add(curr);
                            ArrayList<Point> indivNeighbors = getNeighbors(curr,points);
                            if(indivNeighbors.size() >= this.minPoints){
                                neighbors = mergeRightToLeftCollection(neighbors, indivNeighbors);
                            }
                        }
                        ind++;
                    }
                    res.add(neighbors);
                }
            }
            index++;
        }
        return res;
    }

    public ArrayList<Point> getNeighbors(Point input, Point[] points ){
        ArrayList<Point> ret = new ArrayList<Point>();
        for(int i = 0; i < points.length; i++){
            Point candidate = points[i];
            if(calculateDistance(candidate,input) <= this.epsilon){
                ret.add(candidate);
            }
        }
        return ret;
    }

    public ArrayList<Point> mergeRightToLeftCollection(ArrayList<Point> neighbors1, ArrayList<Point> neighbors2){
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