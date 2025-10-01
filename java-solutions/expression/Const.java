package expression;

public class Const extends AbstractElement {
    protected int value;
    public Const(int c) {
        super(c);
        value = c;
    }
    @Override
    public int evaluate(int x) {
        return value;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
