public class Config {

    public static int SC = 9;
    public static int space = 10;
    public static int maxPayout = 10000;
    public static int gridSize = 6;
    public static final int singleGridMaxMulti = 30;
    // Configuration
    public static final double[][] payout = new double[][]{
            {0},
            {0, 0, 0, 0, 0, 1.00, 1.50, 2.00, 2.50, 3.00, 6.00, 10.0, 15.0, 30.0, 40.0, 200.0}, // 1
            {0, 0, 0, 0, 0, 0.80, 1.20, 1.50, 2.00, 2.50, 5.00, 8.00, 12.0, 25.0, 30.0, 100.0}, // 2
            {0, 0, 0, 0, 0, 0.75, 1.00, 1.20, 1.50, 2.00, 4.00, 6.00, 10.0, 20.0, 25.0, 50.0}, // 3
            {0, 0, 0, 0, 0, 0.60, 0.80, 1.00, 1.20, 1.50, 3.00, 5.00, 8.00, 15.0, 20.0, 40.0}, // 4
            {0, 0, 0, 0, 0, 0.50, 0.60, 0.80, 1.00, 1.20, 2.50, 4.00, 6.00, 10.0, 15.0, 30.0}, // 5
            {0, 0, 0, 0, 0, 0.40, 0.50, 0.60, 0.80, 1.00, 2.00, 3.00, 5.00, 8.00, 12.0, 25.0}, // 6
            {0, 0, 0, 0, 0, 0.30, 0.40, 0.50, 0.60, 0.80, 1.50, 2.50, 4.00, 6.00, 10.0, 20.0}, // 7
            {0, 0, 0, 0, 0, 0.25, 0.30, 0.40, 0.50, 0.60, 1.00, 2.00, 3.00, 5.00, 8.00, 15.0}, // 8
    };

    public static int[] featureRageMeterRequire = new int[]{12, 25, 40, 60};
    public static int[] fgRoundsArray = new int[]{0, 0, 0, 10, 12, 15, 20};

    public static int[] phase4replaceSymbol = new int[]{1, 2, 3, 4,};
    public static double[] phase4replaceSymbolWeight = new double[]{10, 12, 14, 16};
    public static double phase4replaceSymbolWeightSum = sumArray(phase4replaceSymbolWeight);

    public static int[] buyFeatureSCNum = new int[]{3, 4, 5, 6};
    public static double[] buyFeatureSCNumWeight = new double[]{783, 265, 10, 5}; // Super
//    public static double[] buyFeatureSCNumWeight = new double[]{859, 150, 20, 10}; // Buy

    public static double buyFeatureSCNumWeightSum = sumArray(buyFeatureSCNumWeight);

    public static int minLinkRequirement = 5;


    static double sumArray(double[] weight) {
        double total = 0;
        for (double v : weight) {
            total += v;
        }
        return total;
    }
}
