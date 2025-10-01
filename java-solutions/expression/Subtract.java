package expression;

public class Subtract extends AbstractOperation {
    public Subtract(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("-", -225037, new Subtract()));
        this.stringForm.insert(stringFormOperationIndex, '-');
    }
    public Subtract() {}
    @Override
    public int operate(int a, int b) {
        return a - b;
    }
}
