package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedDivide extends AbstractOperation {
    public CheckedDivide(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("/", -225067, new CheckedDivide()));
        this.stringForm.insert(stringFormOperationIndex, '/');
    }
    public CheckedDivide() {}

    @Override
    public int operate(int a, int b) {
        if (b == 0) {
            throw new DivisionByZeroException("Division by zero: " + a + " / " + b);
        } else if ((a == Integer.MIN_VALUE) && (b == -1)) {
            throw new OverflowException("Overflow: " + a + " / " + b + " > Integer.MAX_VALUE");
        }
        return a / b;
    }
}
