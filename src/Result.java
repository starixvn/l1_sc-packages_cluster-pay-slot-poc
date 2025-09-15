import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Result {
    // Fg Keep the info until the Fg end
    String category;
    int rageCounter = 0;
    int[][] gridMulti= new int[][]{};
    boolean[] featureTriggered = new boolean[]{false, false, false, false};

    Queue<Integer> featureQueue = new ArrayDeque<>();



    // Fg Update Every Round
    int reelSetIndex;
    int[] stopIndex = new int[6];
    ArrayList<ArrayList<Grid>> reels = new ArrayList<>();
    double winScore = 0;
}
