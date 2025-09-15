public class Grid {
    public boolean isDetonated;
    public boolean used;
    public int symbol;

    public Grid(int symbol) {
        this.symbol = symbol;
        this.used = false;
        this.isDetonated = false;
    }
}