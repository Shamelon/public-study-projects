package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedSet extends AbstractOperation {

    public CheckedSet(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("set", -225023, new CheckedSet()));
        this.stringForm.insert(stringFormOperationIndex, "set");
    }
    public CheckedSet() {super();}

    @Override
    public int operate(int a, int b) {
        return a | (1 << b);
    }
}
