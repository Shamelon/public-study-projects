package expression.exceptions;

import expression.AbstractOperation;
import expression.BinaryOperation;
import expression.MyExpression;

public class CheckedMultiply extends AbstractOperation {
    public CheckedMultiply(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
        this.infForm.add(new BinaryOperation("*", -225061, new CheckedMultiply()));
        this.stringForm.insert(stringFormOperationIndex, '*');
    }

    public CheckedMultiply() {super();}

    @Override
    public int operate(int a, int b) {
        if (a > 0) {
            if ((b > 0) && (Integer.MAX_VALUE / a < b)) {
                throw new OverflowException("Overflow: " + a + " - " + b + " > Integer.MAX_VALUE");
            } else if ((b < 0) && (Integer.MIN_VALUE / a > b)) {
                throw new OverflowException("Overflow: " + a + " * " + b + " < Integer.MIN_VALUE");
            }
        } else if (a < 0) {
            if ((b > 0) && (Integer.MIN_VALUE / b > a)) {
                throw new OverflowException("Overflow: " + a + " * " + b + " < Integer.MIN_VALUE");
            } else if ((b < 0) && (Integer.MAX_VALUE / b > a)) {
                throw new OverflowException("Overflow: " + a + " - " + b + " > Integer.MAX_VALUE");
            }
        }
        return a * b;
    }

}
