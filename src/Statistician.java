public class Statistician {

    public double totalWin;
    public double ngTotalWin;
    public double hitFreq;
    public double ngHitFreq;
    public double fgHitFreq;
    public double fgTotalWin;
    public double fgTriggerFreq;
    public double MSE;
    public double ngCascadeFreq;
    public double fgCascadeFreq;
    public double maxWin;
    public double[] ngPrizeDist = new double[14];
    public double[] fgPrizeDist = new double[14];
    public double[] ngRageMeterFreq;
    public double[] fgRageMeterFreq;

    public Statistician() {
        this.totalWin = 0;
        this.ngTotalWin = 0;
        this.hitFreq = 0;
        this.ngHitFreq = 0;
        this.ngCascadeFreq = 0;
        this.fgCascadeFreq = 0;
        this.fgTotalWin = 0;
        this.fgTriggerFreq = 0;
        this.MSE = 0;
        this.maxWin = 0;
        this.ngRageMeterFreq = new double[4];
        this.fgRageMeterFreq = new double[4];
    }

}