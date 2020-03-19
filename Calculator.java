package calculator;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    private static HashMap<String, BigInteger> map = new HashMap<>();
    private static Deque<String> result = new ArrayDeque<>();
    private static Deque<Operators> opStack = new ArrayDeque<>();
    private static Deque<BigInteger> stack = new ArrayDeque<>();
    private static final Pattern REG = Pattern.compile("[+-/*^]+|[A-Za-z]+|^-?\\d+|\\d+|[()]");
    private static final String NUM = "[A-Za-z]+|^-?\\d+|\\d+";
    private static final String OP = "[+-/*^]+|[()]";

    public static boolean parseExpression(String exp) {
        //If variable then save it
        if (exp.contains("=")) {
            saveVar(exp);
            return false;
        }
        if (exp.length() == 0)
            return false;

        try {
            intoPostfix(exp);
            calculate();
            return true;
        } catch (UnknownVarException e) {
            System.out.println("Unknown variable");
            return false;
        } catch (InvalidId e) {
            System.out.println("Invalid identifier");
            return false;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid expression");
            return false;
        } finally {
            result = new ArrayDeque<>();
            opStack = new ArrayDeque<>();
        }
    }

    public static void saveVar(String line) {
        String[] arr = line.split("\\s*=\\s*");
        if (arr.length != 2 || (!arr[1].matches("^-?\\d+\\s*|\\d+\\s*$") &&
                !arr[1].matches("^[A-Za-z]+\\s*$"))) {
            System.out.println("Invalid assignment");
            return;
        }
        if (!arr[0].matches("\\s*[A-Za-z]+")) {
            System.out.println("Invalid identifier");
            return;
        }
        BigInteger x;
        if (arr[1].matches("[A-Za-z]+\\s*$")) {
            if (!map.containsKey(arr[1])) {
                System.out.println("Unknown variable");
                return;
            }
            x = map.get(arr[1]);
        } else {
            x = new BigInteger(arr[1]);
        }
        map.put(arr[0], x);
    }

    public static BigInteger getNum(String in) throws NumberFormatException, UnknownVarException, InvalidId {
        if (in.matches("[A-Za-z]+")) {
            if (!map.containsKey(in)) {
                throw new UnknownVarException();
            }
            return map.get(in);
        } else if (in.matches("^-?\\d+|\\d+")) {
            return new BigInteger(in);
        } else {
            throw new InvalidId();
        }
    }

    public static void intoPostfix(String line) throws NumberFormatException {
        line = line.replaceAll("\\s+", "");
        while (line.length() > 0) {
            Matcher mat = REG.matcher(line);
            if (mat.find()) {
                String res = mat.group();
                if (res.matches(NUM))
                    result.offerLast(res);
                else if (res.matches(OP)) {
                    Operators op = checkOp(res);
                    if (op == null)
                        throw new NumberFormatException();
                    if (op == Operators.CL_PAR) {
                        while (!opStack.isEmpty() && opStack.peekLast() != Operators.OP_PAR) {
                            result.offerLast(opStack.pollLast().getSign().toString());
                        }
                        if (opStack.isEmpty())
                            throw new NumberFormatException();
                        opStack.pollLast();
                    } else if (opStack.isEmpty() || opStack.peekLast() == Operators.OP_PAR ||
                            opStack.peekLast().getPriority() < op.getPriority() || op == Operators.OP_PAR) {
                        opStack.offerLast(op);
                    } else if (opStack.peekLast().getPriority() >= op.getPriority()) {
                        while (!opStack.isEmpty() && opStack.peekLast() != Operators.OP_PAR &&
                                opStack.peekLast().getPriority() >= op.getPriority()) {
                            result.offerLast(opStack.pollLast().getSign().toString());
                        }
                        opStack.offerLast(op);
                    }
                }
                line = line.replaceFirst(REG.pattern(), "");
            } else {
                throw new NumberFormatException();
            }
        }
        while(!opStack.isEmpty()) {
            if (opStack.peekLast() == Operators.OP_PAR) {
                throw new NumberFormatException();
            }
            result.offerLast(opStack.pollLast().getSign().toString());
        }
    }

    public static Operators checkOp(String op) {
        if (op.matches("[*]"))
            return Operators.MULT;
        else if (op.matches("[/]"))
            return Operators.DIV;
//        else if (op.matches("[\\^]"))
//            return Operators.POW;
        else if (op.matches("[+]+"))
            return Operators.ADD;
        else if (op.matches("[-]+")) {
            if (op.length() % 2 == 0) {
                return Operators.ADD;
            } else
                return Operators.SUB;
        } else if (op.matches("[(]"))
            return Operators.OP_PAR;
        else if (op.matches("[)]"))
            return Operators.CL_PAR;
        return null;
    }

    private static void calculate() throws UnknownVarException, InvalidId, NumberFormatException {
        while(!result.isEmpty()) {
            String s = result.pollFirst();
            if (s.matches(NUM)) {
                stack.offerLast(getNum(s));
            } else {
                BigInteger b = stack.pollLast();
                BigInteger a = stack.pollLast();
                switch(s) {
                    case "+":
                        stack.offerLast(a.add(b));
                        break;
                    case "-":
                        stack.offerLast(a.subtract(b));
                        break;
                    case "*":
                        stack.offerLast(a.multiply(b));
                        break;
                    case "/":
                        if (b.compareTo(BigInteger.ZERO) == 0) {
                            System.out.println("Can't delete on zero!");
                            System.exit(1);
                        }
                        stack.offerLast(a.divide(b));
                        break;
                        //Not with BigIntegers
                    /*
                    case "^":
                        stack.offerLast((int)a.pow(b));
                        break;
                     */
                }
            }
        }
    }

    public static void printResult() {
        System.out.println(stack.pollFirst());
    }
}

enum Operators {
    ADD(1, '+'),
    SUB(1, '-'),
    MULT(2, '*'),
    DIV(2, '/'),
//    POW(3, '^'),
    OP_PAR(4, '('),
    CL_PAR(4, ')');

    private int priority;
    private Character sign;
    Operators(int priority, Character sign) {
        this.priority = priority;
        this.sign = sign;
    }

    public int getPriority() {
        return this.priority;
    }

    public Character getSign() {
        return this.sign;
    }
}

class UnknownVarException extends Exception {

}

class InvalidId extends Exception {

}

