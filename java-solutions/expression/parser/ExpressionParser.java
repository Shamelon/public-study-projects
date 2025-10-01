package expression.parser;

import expression.*;

import java.util.ArrayList;
import java.util.Iterator;

public class ExpressionParser implements TripleParser {
    public TripleExpression parse(final String source) {
        TripleExpression ans = (TripleExpression) parse(new StringSource(source));
        return ans;
    }

    public static Object parse(final CharSource source) {
        return new TripleExpressionParser(source).parseExpression(false);
    }

    private static class TripleExpressionParser extends BaseParser {
        private final char[] possibleStartsOfBinaryOperations = new char[] {'+', '-', '*', '/', 's', 'c'};
        private final char[] possibleStartsOfUnaryOperations = new char[] {'-'};

        private final char[] possibleStartsOfVariables = new char[] {'x', 'y', 'z'};

        private final int[] possiblePriorities = new int[] {9, 10, 11}; // значения в порядке возрастания
        boolean isBinOpNext = false; // Правда ли, что следующий элемент должен быть бинарной операцией

        public TripleExpressionParser(final CharSource source) {
            super(source);
        }
        /*
        Expression
            BktExpression
                ws, '(', Expression, ')', ws
            ws, Expression, Operation, Expression, ws
            ws, Element, ws
        */
        public MyExpression parseExpression(boolean isBktExpression /*Выражение записано в скобках или нет*/) {
            final MyExpression result;
            ArrayList<MyExpression> expressions = new ArrayList<>(); // Список выражений
            ArrayList<BinOperation> operations = new ArrayList<>(); // Список бинарных операций

            // Заполнение 2 списков - списка выражений и списка операций
            while (!eof()) {
                skipWhitespace();
                // Если встретили открывающуюся скобку - рекурсивно парсим выражение в скобках и добавляем в список выражений
                if (take('(')) {
                    expressions.add(parseExpression(true));
                // Если встретили открывающуюся скобку - скипаем оставшиеся пробелы и выходим из цикла
                } else if (take(')')) {
                    if (isBktExpression) {
                        skipWhitespace();
                        break;
                    } else {
                        throw error("Unexpected ')'");
                    }
                // Проверка, что следующий блок - бинарная операция
                } else if (charIn(possibleStartsOfBinaryOperations)) {
                    if (isBinOpNext) {
                        operations.add(parseBinaryOperation());
                    } else {
                        expressions.add(parseUnaryOperation(take()));
                    }
                // Проверка, что следующий блок - унарная операция
                } else if (charIn(possibleStartsOfUnaryOperations)) {
                    if (!isBinOpNext) {
                        expressions.add(parseUnaryOperation(take()));
                    } else {
                        throw error("Expect start of Binary Operation, given start of Unary Operation");
                    }
                // Проверка, что следующий блок - элемент
                } else {
                    if (!isBinOpNext) {
                        expressions.add(parseElement(false));
                    } else {
                        throw error("Expect start of Binary Operation, given start of Element");
                    }
                }
                skipWhitespace();
            }

            // Применение операций из списка согласно приоритету
            for (int priority : possiblePriorities) {
                Iterator<MyExpression> it1 = expressions.iterator();
                Iterator<BinOperation> it2 = operations.iterator();
                int k1 = 0; // место в списке выражений, куда надо записать получившееся выражение
                int k2 = 0; // место в списке операций, куда надо записать текущую операцию


                MyExpression currExp = it1.next();
                while (it1.hasNext() && it2.hasNext()) {
                    BinOperation currOp = it2.next();
                    if (currOp.priority == priority) {
                        currExp = currOp.convert(currExp, it1.next()); // получаем новое выражение
                        expressions.set(k1, currExp); // записываем в в список
                    } else {
                        currExp = it1.next(); // теперь работаем со следующим выражением
                        k1++;
                        expressions.set(k1, currExp);
                        operations.set(k2, currOp);  // записываем в список неиспользованную операцию
                        k2++;
                    }
                }

                // Удаление пустых элементов в конце
                int endExp = expressions.size();
                for (int i = k1 + 1; i < endExp; i++) {
                    expressions.remove(expressions.size() - 1);
                }

                // Удаление пустых элементов в конце
                int endOp = operations.size();
                for (int i = k2 + 1; i < endOp; i++) {
                    operations.remove(operations.size() - 1);
                }
            }

            // Возврат результата
            result = expressions.get(0);
            return result;
        }

        /*
        Binary Operation
            priority10
                "-"
                "+"
                "set"
                "clear"
            priority9
                "*"
                "/"
         */
        private BinOperation parseBinaryOperation() {
            isBinOpNext = false;
            if (take('+')) {
                return new BinOperation("+", 10);
            } else if (take('-')) {
                return new BinOperation("-", 10);
            } else if (take('*')) {
                return new BinOperation("*", 9);
            } else if (take('/')) {
                return new BinOperation("/", 9);
            } else if (take('s')) {
                expect("et");
                return new BinOperation("set", 11);
            } else if (take('c')) {
                expect("lear");
                return new BinOperation("clear", 11);
            } else {
                throw error("Unknown start of binary operation");
            }
        }
        /*
        Unary Operation
            Negate Expression
                '-', ws, '(', ws, Expression, ws, ')', ws
                '-', ws, Negate Expression, ws

         */
        private MyExpression parseUnaryOperation(char opStart) {
            String op;
            if (opStart == '-') {
                op = "-";
            } else {
                throw error("Unknown start of Unary Operation");
            }
            skipWhitespace();
            if (take('(')) {
                return new UnOperation(op).convert(parseExpression(true));
            } else if (take('-')) {
                return new UnOperation(op).convert(parseUnaryOperation('-'));
            } else if (take('r')) {
                return new UnOperation(op).convert(parseUnaryOperation('r'));
            }else if (between('0', '9') || charIn(possibleStartsOfVariables)) {
                if (op.equals("-")) {
                    // значит это не унарный минус, а отрицательное число
                    return parseElement(true);
                } else {
                    return new UnOperation(op).convert(parseElement(false));
                }
            } else {
                throw error("Unknown Operation");
            }
        }

        /*
        Element
            Const
                digit
                onenine digit, digits
                '-', digit
                '-', onenine digit, digits

            Variable
                'x'
                'y'
                'z'
        */
        private MyExpression parseElement(boolean negative) {
            isBinOpNext = true;
            if (charIn(possibleStartsOfVariables)) {
                if (negative) {
                    return new Negate(new Variable(String.valueOf(take())));
                } else {
                    return new Variable(String.valueOf(take()));
                }
            } else if (take('0')) {
                return new Const(0);
            } else if (between('1', '9')) {
                StringBuilder sb = new StringBuilder();
                while (between('0', '9')) {
                    sb.append(take());
                }
                if (negative) {
                    sb.insert(0, '-');
                }
                return new Const(Integer.parseInt(sb.toString()));
            } else {
                throw error("Unknown beginning of element " + take());
            }
        }

        private void skipWhitespace() {
            while (isWhitespace()) {
                // skip
                take();
            }
        }
        private static class UnOperation {
            final String name;

            private UnOperation(String name) {
                this.name = name;
            }

            public MyExpression convert(MyExpression exp1) {
                return switch (name) {
                    case "-" -> new Negate(exp1);
                    default -> null;
                };
            }
        }

        private static class BinOperation {
            final String name;
            final int priority;
            // "*", "/" - 9
            // Binary "+", "-" - 10
            // "set", "clear" - 11

            public BinOperation(String name, int priority) {
                this.name = name;
                this.priority = priority;
            }

            public MyExpression convert(MyExpression exp1, MyExpression exp2) {
                return switch (name) {
                    case "+" -> new Add(exp1, exp2);
                    case "-" -> new Subtract(exp1, exp2);
                    case "*" -> new Multiply(exp1, exp2);
                    case "/" -> new Divide(exp1, exp2);
                    case "set" -> new Set(exp1, exp2);
                    case "clear" -> new Clear(exp1, exp2);
                    default -> null;
                };
            }
            @Override
            public String toString() {
                return name;
            }
        }
    }
}