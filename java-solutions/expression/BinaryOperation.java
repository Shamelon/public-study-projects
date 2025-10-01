package expression;

public class BinaryOperation extends AbstractElement  {
    protected final int hashCode;
    protected String op;
    protected AbstractOperation operation;
    public BinaryOperation(String op, int hashCode, AbstractOperation operation) {
        super(op);
        this.op = op;
        this.hashCode = hashCode;
        this.operation = operation;
    }

    @Override
    public int evaluate(int x) {
        return 0;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return 0;
    }

    public int operate(int a, int b) {
        return operation.operate(a, b);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
