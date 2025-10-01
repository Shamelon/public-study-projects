package expression.exceptions;

import expression.AbstractOperation;
import expression.MyExpression;
import expression.UnaryOperation;

public class CheckedNegate extends AbstractOperation {
    public CheckedNegate(MyExpression exp) {
        super(exp);
        this.infForm.add(new UnaryOperation("-", 400009, new CheckedNegate()));
        this.stringForm.insert(stringFormOperationIndex, '-');
    }
    public CheckedNegate() {
        super();
    }
    @Override
    public int operate(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new OverflowException("Overflow: - " + a + " > Integer.MAX_VALUE");
        }
        return a * (-1);
    }
}
