public class Config {

    public static int SC = 9;
    public static int space = 10;
    public static int maxSingleRoundsPayout = 10000;
    public static int gridSize = 6;
    public static final int singleGridMaxMulti = 20;
    // Configuration
    public static final double[][] payout = new double[][]{
            {0},
            {0, 0, 0, 0, 0, 1.00, 1.20, 1.50, 2.00, 2.50, 5.0, 8.0, 15.0, 30.0, 60.0, 250.0}, // 1
            {0, 0, 0, 0, 0, 0.80, 1.00, 1.20, 1.50, 2.00, 4.0, 6.0, 12.0, 25.0, 50.0, 120.0}, // 2
            {0, 0, 0, 0, 0, 0.60, 0.80, 1.00, 1.20, 1.50, 3.0, 5.0, 10.0, 20.0, 40.0, 100.0}, // 3
            {0, 0, 0, 0, 0, 0.50, 0.60, 0.80, 1.00, 1.20, 2.5, 4.0, 8.0, 15.0, 30.0, 60.0}, // 4
            {0, 0, 0, 0, 0, 0.40, 0.50, 0.60, 0.80, 1.00, 2.0, 3.0, 5.0, 10.0, 20.0, 40.0}, // 5
            {0, 0, 0, 0, 0, 0.30, 0.40, 0.50, 0.60, 0.80, 1.5, 2.5, 4.0, 8.0, 15.0, 30.0}, // 6
            {0, 0, 0, 0, 0, 0.25, 0.30, 0.40, 0.50, 0.60, 1.2, 2.0, 3.0, 6.0, 12.0, 25.0}, // 7
            {0, 0, 0, 0, 0, 0.20, 0.25, 0.30, 0.40, 0.50, 1.0, 1.5, 2.5, 5.0, 10.0, 20.0}, // 8
    };

    public static int[] featureRageMeterRequire = new int[]{15, 30, 50, 80};

    // space is 8, Scatter is 9
    public static int[] fgRoundsArray = new int[]{0, 0, 0, 10, 15, 20, 25};


    public static int[] phase4replaceSymbol = new int[]{1, 2, 3, 4,};
    public static double[] phase4replaceSymbolWeight = new double[]{10, 12, 14, 15};
    public static double phase4replaceSymbolWeightSum = sumArray(phase4replaceSymbolWeight);



    public static int minLinkRequirement = 5;


    static double sumArray(double[] weight) {
        double total = 0;
        for (double v : weight) {
            total += v;
        }
        return total;
    }
}
