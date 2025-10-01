package expression;

import java.util.ArrayList;

public interface MyExpression extends Expression, TripleExpression{
    ArrayList<MyExpression> getInfForm();
    @Override
    String toString();
    StringBuilder getStringForm();
    int evaluate(int x);
    boolean equals(Object exp);
    int hashCode();
}
