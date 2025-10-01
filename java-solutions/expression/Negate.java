package expression;

public class Negate extends AbstractOperation {
    public Negate(MyExpression exp) {
        super(exp);
        this.infForm.add(new UnaryOperation("-", 400009, new Negate()));
        this.stringForm.insert(stringFormOperationIndex, '-');
    }
    @Override
    public int operate(int a) {
        return a * (-1);
    }
    public Negate() {
        super();
    }
}
