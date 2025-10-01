package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedClear extends AbstractOperation {

    public CheckedClear(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("clear", -225023, new CheckedClear()));
        this.stringForm.insert(stringFormOperationIndex, "clear");
    }
    public CheckedClear() {super();}

    @Override
    public int operate(int a, int b) {
        return a & ~(1 << b);
    }
}
