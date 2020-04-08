import org.desilvahendricksoftware.debs.DBSCAN;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DBSCANTests {

    private DBSCAN testScan = new DBSCAN(0.5, 3);

    // cluster 0
    private final DBSCAN.Point p1 = new DBSCAN.Point(1.1, 1.23);
    private final DBSCAN.Point p2 = new DBSCAN.Point(1.34, 1.54);
    private final DBSCAN.Point p3 = new DBSCAN.Point(1.01, 1.13);
    private final DBSCAN.Point p4 = new DBSCAN.Point(1.03, 1.24);
    private final DBSCAN.Point p5 = new DBSCAN.Point(1.56, 1.89);

    // cluster 1
    private final DBSCAN.Point p6 = new DBSCAN.Point(4.0, 7.0);
    private final DBSCAN.Point p7 = new DBSCAN.Point(4.02, 7.08);
    private final DBSCAN.Point p8 = new DBSCAN.Point(4.03, 7.02);
    private final DBSCAN.Point p9 = new DBSCAN.Point(4.04, 7.04);

    // noise
    private final DBSCAN.Point p10 = new DBSCAN.Point(23, 19);

    @Test
    public void testCalculateDistance() throws Exception {

        assertEquals(5.0, DBSCAN.calculateDistance(new DBSCAN.Point(1.0, 3.0), new DBSCAN.Point(4.0, 7.0)), 0.0);
        assertEquals(118.6, DBSCAN.calculateDistance(new DBSCAN.Point(23.0, -43.0), new DBSCAN.Point(94.0,-138.0)), 0.001);
        assertEquals(0.0, DBSCAN.calculateDistance(new DBSCAN.Point(0.0, 0.0), new DBSCAN.Point(0.0, 0.0)), 0.0);
        assertEquals(34.195, DBSCAN.calculateDistance(new DBSCAN.Point(9.23,0.35), new DBSCAN.Point(42.345,8.88)), 0.001);

        assertEquals(0.392, DBSCAN.calculateDistance(p1, p2), 0.001);
        assertEquals(0.1118, DBSCAN.calculateDistance(p3, p4), 0.001);
        assertEquals(5.662, DBSCAN.calculateDistance(p5, p6), 0.001);

        assertEquals(true, DBSCAN.calculateDistance(p5, p6) > testScan.getEpsilon());

        // p1's neighbors
        assertEquals(true, DBSCAN.calculateDistance(p1, p2) < testScan.getEpsilon());
        assertEquals(true, DBSCAN.calculateDistance(p1, p3) < testScan.getEpsilon());
        assertEquals(true, DBSCAN.calculateDistance(p1, p4) < testScan.getEpsilon());
        // non-neighbors
        assertEquals(true, DBSCAN.calculateDistance(p1, p5) > testScan.getEpsilon());
        assertEquals(true, DBSCAN.calculateDistance(p1, p6) > testScan.getEpsilon());
    }

    @Test
    public void testGetNeighbors() throws Exception {

        DBSCAN.Point [] points = new DBSCAN.Point[] {p1, p2, p3, p4, p5, p6};

        assertEquals(true, testScan.getNeighbors(p1, points).contains(p2));
        assertEquals(true, testScan.getNeighbors(p1, points).contains(p3));
        assertEquals(true, testScan.getNeighbors(p1, points).contains(p4));

        assertEquals(false, testScan.getNeighbors(p1, points).contains(p1));
        assertEquals(false, testScan.getNeighbors(p1, points).contains(p5));
        assertEquals(false, testScan.getNeighbors(p1, points).contains(p6));

    }

    @Test
    public void testFit() throws Exception {
        DBSCAN.Point [] points = new DBSCAN.Point[] {p1, p10, p8, p3, p4, p5, p6, p7, p2, p9};

        testScan.fit(points);

        assertEquals(0, testScan.clusters[0]);
        assertEquals(-1, testScan.clusters[1]);
        assertEquals(1, testScan.clusters[2]);
        assertEquals(0, testScan.clusters[3]);
        assertEquals(0, testScan.clusters[4]);
        assertEquals(0, testScan.clusters[5]);
        assertEquals(1, testScan.clusters[6]);
        assertEquals(1, testScan.clusters[7]);
        assertEquals(0, testScan.clusters[8]);
        assertEquals(1, testScan.clusters[9]);

    }
}