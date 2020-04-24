package org.desilvahendricksoftware.debs;

import java.util.ArrayList;

public class DBSCANHelper extends Thread {
    private Point[] points;
    private Point[] allPoints;
    public DBSCANHelper(Point[] points,Point[] allPoints){
        this.points = points;
        this.allPoints = allPoints;
    }

    @Override
    public void run() {
            int index = 0;
            while (index < points.length){
                Point current = points[index];
                if(!DBSCAN.visitedPoints.contains(current)){
                    DBSCAN.visitedPoints.add(current);
                    ArrayList<Point> neighbors = DBSCAN.getNeighbors(current,allPoints,DBSCAN.epsilon);
                    if(neighbors.size() >= DBSCAN.minPoints){
                        int ind = 0;
                        while(ind < neighbors.size()){
                            Point curr = neighbors.get(ind);
                            if(!DBSCAN.visitedPoints.contains(curr)){
                                DBSCAN.visitedPoints.add(curr);
                                ArrayList<Point> indivNeighbors = DBSCAN.getNeighbors(curr,allPoints,DBSCAN.epsilon);
                                if(indivNeighbors.size() >= DBSCAN.minPoints){
                                    neighbors = DBSCAN.mergeRightToLeftCollection(neighbors, indivNeighbors);
                                }
                            }
                            ind++;
                        }
                        DBSCAN.res.add(neighbors);
                    }
                }
                index++;
            }
            return;
        }
}
