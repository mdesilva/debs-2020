package org.desilvahendricksoftware.debs;

import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class DBSCAN implements Serializable{

    private double epsilon;
    private int minPoints;
    private HashSet<Tuple2<Double, Double>> visitedPoints = new HashSet<>();
    public int[] clusters;
    public int[] labels;
    public boolean hasNoiseCluster;
    public int numberOfClustersWithoutNoiseCluster;

    public DBSCAN(double epsilon, int minPoints){
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }


    private ArrayList performDBSCANHelper(Tuple2<Double,Double>[] points){
        int index = 0;
        ArrayList res = new ArrayList();
        visitedPoints.clear();
        while (index < points.length){
            Tuple2 current = points[index];
            if(!visitedPoints.contains(current)){
                visitedPoints.add(current);
                ArrayList<Tuple2> neighbors = getNeighbors(current,points);
                if(neighbors.size() >= this.minPoints){
                    int ind = 0;
                    while(ind < neighbors.size()){
                        Tuple2 curr = neighbors.get(ind);
                        if(!visitedPoints.contains(curr)){
                            visitedPoints.add(curr);
                            ArrayList indivNeighbors = getNeighbors(curr,points);
                            if(indivNeighbors.size() >= this.minPoints){
                                neighbors = mergeRightToLeftCollection(neighbors,indivNeighbors);
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

    private ArrayList<Tuple2> getNeighbors(Tuple2 input, Tuple2<Double,Double>[] points ){
        ArrayList ret = new ArrayList();
        for(int i = 0; i < points.length; i++){
            Tuple2 candidate = points[i];
            if(calculateDistance(candidate,input)<=this.epsilon){
                ret.add(candidate);
            }
        }
        return ret;
    }

    private ArrayList<Tuple2> mergeRightToLeftCollection(ArrayList<Tuple2> neighbors1,ArrayList<Tuple2> neighbors2){
        for (int i = 0; i < neighbors2.size(); i++) {
            Tuple2 tempPt = neighbors2.get(i);
            if (!neighbors1.contains(tempPt)) {
                neighbors1.add(tempPt);
            }
        }
        return neighbors1;
    }

    private static double calculateDistance(Tuple2<Double,Double> pointOne, Tuple2<Double,Double> pointTwo){
        double pow1 = Math.pow(pointTwo.f0-pointOne.f0, 2.0);
        double pow2 = Math.pow(pointTwo.f1-pointOne.f1,2.0);
        return Math.sqrt(pow1 + pow2);
    }

    public void fit(Tuple2<Double, Double>[] points){
        int clusters[] = new int[points.length];

        ArrayList result = performDBSCANHelper(points);
        for(int k = 0; k < points.length; k++){
            boolean flag = false;
            for(int j = 0; j< result.size(); j++){
                ArrayList temp = (ArrayList) result.get(j);
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
