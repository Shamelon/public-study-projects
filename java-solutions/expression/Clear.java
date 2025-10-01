package expression;

public class Clear extends AbstractOperation {

    public Clear(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("clear", -225023, new Clear()));
        this.stringForm.insert(stringFormOperationIndex, "clear");
    }
    public Clear() {super();}

    @Override
    public int operate(int a, int b) {
        return a & ~(1 << b);
    }
}
