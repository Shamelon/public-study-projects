package expression;

import java.util.ArrayList;

public abstract class AbstractOperation implements MyExpression {
    protected ArrayList<MyExpression> infForm;
    protected StringBuilder stringForm;
    protected int stringFormOperationIndex;

    public AbstractOperation(MyExpression exp1, MyExpression exp2) {

        this.infForm = new ArrayList<MyExpression>(3);
        if (exp1.getInfForm() != null) { // if exp1 not var, const or operation
            for (MyExpression el : exp1.getInfForm()) {
                this.infForm.add(el);
            }
        } else {
            this.infForm.add(exp1);
        }
        if (exp2.getInfForm() != null) { // if exp2 not var, const or operation
            for (MyExpression el : exp2.getInfForm()) {
                this.infForm.add(el);
            }
        } else {
            this.infForm.add(exp2);
        }

        this.stringForm = new StringBuilder();
        this.stringForm.append('(');
        this.stringForm.append(exp1.getStringForm());
        this.stringFormOperationIndex = stringForm.length() + 1; // index of operation
        this.stringForm.append("  ");
        this.stringForm.append(exp2.getStringForm());
        this.stringForm.append(')');
    }

    public AbstractOperation(MyExpression exp) {
        this.infForm = exp.getInfForm();
        if (this.infForm == null) { // Если exp - простой элемент
            this.infForm = new ArrayList<>(2);
            this.infForm.add(exp);
        }

        this.stringForm = new StringBuilder();
        this.stringForm.append('(');
        this.stringForm.append(exp.getStringForm());
        this.stringForm.append(')');
        this.stringFormOperationIndex = 0; // index of operation
    }

    public AbstractOperation() {}

    public int operate(int a, int b) {return 0;};

    public int operate(int a) {return 0;};

    @Override
    public ArrayList<MyExpression> getInfForm() {
        return this.infForm;
    }

    @Override
    public StringBuilder getStringForm() {
        return this.stringForm;
    }

    @Override
    public String toString() {
        return this.stringForm.subSequence(0,this.stringForm.length()).toString();
    }

    @Override
    public int evaluate(int x) {
        int[] stek = new int[infForm.size()];
        int curr = 0;
        for (MyExpression el : infForm) {
            if (el instanceof Const | el instanceof Variable) {
                stek[curr] = el.evaluate(x);
                curr++;
            } else if (el instanceof BinaryOperation) {
                stek[curr - 2] = ((BinaryOperation) el).operate(stek[curr - 2], stek[curr - 1]);
                curr--;
                stek[curr] = 0;
            } else if (el instanceof UnaryOperation) {
                stek[curr - 1] = ((UnaryOperation) el).operate(stek[curr - 1]);
            }
        }
        return stek[0];
    }

    @Override
    public int evaluate(int x, int y, int z) {
        int[] stek = new int[infForm.size()];
        int curr = 0;
        for (MyExpression el : infForm) {
            if (el instanceof Const | el instanceof Variable) {
                stek[curr] = el.evaluate(x, y, z);
                curr ++;
            } else if (el instanceof BinaryOperation) {
                stek[curr - 2] = ((BinaryOperation) el).operate(stek[curr - 2], stek[curr - 1]);
                curr --;
                stek[curr] = 0;
            } else if (el instanceof UnaryOperation) {
                stek[curr - 1] = ((UnaryOperation) el).operate(stek[curr - 1]);
            }
        }
        return stek[0];
    }

    @Override
    public int hashCode() {
        int hashcode = 0;
        for (MyExpression el : this.infForm) {
            hashcode += el.hashCode();
            hashcode *= 31;
        }
        return hashcode;
    }

    @Override
    public boolean equals(Object exp) {
        if (!(exp instanceof AbstractOperation)) {
            return false;
        } else if (this.hashCode() == exp.hashCode()) {
            return true;
        } else {
            return false;
        }
    }
}
