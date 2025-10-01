package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedAdd extends AbstractOperation {
    public CheckedAdd(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("+", -225023, new CheckedAdd()));
        this.stringForm.insert(stringFormOperationIndex, '+');
    }
    public CheckedAdd() {super();}

    @Override
    public int operate(int a, int b) {
        if (a <= 0) {
            if (Integer.MIN_VALUE - a > b) {
                throw new OverflowException("Overflow: " + a + " + " + b + " < Integer.MIN_VALUE");
            }
        } else {
            if (Integer.MAX_VALUE - a < b) {
                throw new OverflowException("Overflow: " + a + " + " + b + " > Integer.MAX_VALUE");
            }
        }
        return a + b;
    }
}
