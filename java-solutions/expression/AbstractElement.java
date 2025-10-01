package expression;

import java.util.ArrayList;

public abstract class AbstractElement implements MyExpression {
    protected StringBuilder stringForm;

    public AbstractElement(String el) {
        stringForm = new StringBuilder();
        this.stringForm.append(el);
    }

    public AbstractElement(int el) {
        stringForm = new StringBuilder();
        this.stringForm.append(el);
    }

    @Override
    public ArrayList<MyExpression> getInfForm() {
        return null;
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
    public boolean equals(Object exp) {
        if (exp == null) {
            return false;
        } else if (this.stringForm.subSequence(0,this.stringForm.length()).toString().equals(exp.toString())) {
            return true;
        } else {
            return false;
        }
    }
}
