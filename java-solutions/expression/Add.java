package expression;

public class Add extends AbstractOperation {
    public Add(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("+", -225023, new Add()));
        this.stringForm.insert(stringFormOperationIndex, '+');
    }
    public Add() {super();}

    @Override
    public int operate(int a, int b) {
        return a + b;
    }
}
