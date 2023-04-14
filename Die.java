import java.util.Random;

public class Die {
    private static Random rand = new Random();

    private final int sides;
    private int face;
    private boolean isRolling = true;

    public Die(int sides) {
        if (sides <= 0) {
            throw new IllegalArgumentException("Cannot create die with less than one side");
        }
        this.sides = sides;
        face = roll();
    }

    public int roll() {
        if (isRolling) {
            face = rand.nextInt(sides) + 1;
        }
        return face;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getSides() {
        return sides;
    }

    public boolean isRolling() {
        return isRolling;
    }

    public void setRolling(boolean isRolling) {
        this.isRolling = isRolling;
    }

    public void toggleRolling() {
        this.isRolling = !isRolling;
    }

}
