package expression.exceptions;

import expression.AbstractOperation;
import expression.MyExpression;
import expression.UnaryOperation;

public class CheckedCount extends AbstractOperation {
    public CheckedCount(MyExpression exp) {
        super(exp);
        this.infForm.add(new UnaryOperation("count", 400093, new CheckedCount()));
        this.stringForm.insert(stringFormOperationIndex, "count");
    }
    public CheckedCount() {
        super();
    }
    @Override
    public int operate(int a) {
        return Integer.bitCount(a);
    }
}
