public class Config {

    public static int SC = 9;
    public static int space = 10;
    public static int maxPayout = 10000;
    public static int gridSize = 6;
    public static final int singleGridMaxMulti = 30;
    // Configuration
    public static final double[][] payout = new double[][]{
            {0},
            {0, 0, 0, 0, 0, 1.00, 1.20, 1.50, 2.00, 2.50, 5.0, 8.0, 15.0, 30.0, 50.0, 200.0}, // 1
            {0, 0, 0, 0, 0, 0.80, 1.00, 1.20, 1.50, 2.00, 4.0, 6.0, 12.0, 25.0, 40.0, 100.0}, // 2
            {0, 0, 0, 0, 0, 0.60, 0.80, 1.00, 1.20, 1.50, 3.0, 5.0, 10.0, 20.0, 30.0, 80.0}, // 3
            {0, 0, 0, 0, 0, 0.50, 0.60, 0.80, 1.00, 1.20, 2.5, 4.0, 8.0, 15.0, 25.0, 60.0}, // 4
            {0, 0, 0, 0, 0, 0.40, 0.50, 0.60, 0.80, 1.00, 2.0, 3.0, 5.0, 10.0, 15.0, 40.0}, // 5
            {0, 0, 0, 0, 0, 0.30, 0.40, 0.50, 0.60, 0.80, 1.5, 2.5, 4.0, 8.0, 12.0, 30.0}, // 6
            {0, 0, 0, 0, 0, 0.25, 0.30, 0.40, 0.50, 0.60, 1.2, 2.0, 3.0, 6.0, 10.0, 25.0}, // 7
            {0, 0, 0, 0, 0, 0.20, 0.25, 0.30, 0.40, 0.50, 1.0, 1.5, 2.5, 5.0, 8.0, 20.0}, // 8
    };

    public static int[] featureRageMeterRequire = new int[]{12, 25, 40, 60};

    public static int[] fgRoundsArray = new int[]{0, 0, 0, 10, 12, 15, 20};
    public static int[] phase4replaceSymbol = new int[]{1, 2, 3, 4,};
    public static double[] phase4replaceSymbolWeight = new double[]{10, 12, 14, 16};
    public static double phase4replaceSymbolWeightSum = sumArray(phase4replaceSymbolWeight);

    public static int[] buyFeatureSCNum = new int[]{3, 4, 5, 6};
    public static double[] buyFeatureSCNumWeight = new double[]{800, 152, 30, 10}; // super
//    public static double[] buyFeatureSCNumWeight = new double[]{72, 21, 5, 2}; // buy

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
