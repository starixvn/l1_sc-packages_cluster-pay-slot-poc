import java.util.ArrayList;

public class Prize {
    public int symbol;
    public int link;
    public ArrayList<Coordinate> position;
    public double winScore;
    public double multi;
    public Prize(){
        this.link = 0;
        this.position = new ArrayList<>();
        this.winScore = 0;
        this.multi = 1;
    }
}