package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedSubtract extends AbstractOperation {
    public CheckedSubtract(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("-", -225037, new CheckedSubtract()));
        this.stringForm.insert(stringFormOperationIndex, '-');
    }
    public CheckedSubtract() {}
    @Override
    public int operate(int a, int b) {
        if (a < 0) {
            if (a - Integer.MIN_VALUE < b) {
                throw new OverflowException("Overflow: " + a + " - " + b + " < Integer.MIN_VALUE");
            }
        } else {
            if (a - Integer.MAX_VALUE > b) {
                throw new OverflowException("Overflow: " + a + " - " + b + " > Integer.MAX_VALUE");
            }
        }
        return a - b;
    }
}
