package expression;

public class Divide extends AbstractOperation {
    public Divide(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("/", -225067, new Divide()));
        this.stringForm.insert(stringFormOperationIndex, '/');
    }
    public Divide() {}

    @Override
    public int operate(int a, int b) {
        return a / b;
    }
}
