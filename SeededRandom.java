import java.util.Random;

public class SeededRandom {
    private Random random;

    public SeededRandom(long seed) {
        this.random = new Random(seed);
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public double nextDouble() {
        return random.nextDouble();
    }
}
