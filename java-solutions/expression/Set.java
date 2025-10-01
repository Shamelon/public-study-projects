package expression;

public class Set extends AbstractOperation {

    public Set(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("set", -225023, new Set()));
        this.stringForm.insert(stringFormOperationIndex, "set");
    }
    public Set() {super();}

    @Override
    public int operate(int a, int b) {
        return a | (1 << b);
    }
}
