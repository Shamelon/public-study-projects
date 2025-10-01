package expression;

public class Multiply extends AbstractOperation {
    public Multiply(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("*", -225061, new Multiply()));
        this.stringForm.insert(stringFormOperationIndex, '*');
    }

    public Multiply() {super();}

    @Override
    public int operate(int a, int b) {
        return a * b;
    }

}
