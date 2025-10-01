package expression;

public class Variable extends AbstractElement {
    protected String name;
    public Variable(String var) {
        super(var);
        name = var;
    }


    @Override
    public int evaluate(int x) {
        return x;
    }

    @Override
    public int hashCode() {
        if (name.equals("x")) {
            return -225023;
        }
        if (name.equals("y")) {
            return -225037;
        }
        if (name.equals("z")) {
            return -225061;
        } else {
            return 0;
        }
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return switch (name) {
            case "x" -> x;
            case "y" -> y;
            case "z" -> z;
            default -> -1;
        };
    }
}
