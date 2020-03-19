package calculator;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String s = sc.nextLine();
            if (s.matches("^/.*")) {
                parseCommand(s);
                continue;
            }
            if (Calculator.parseExpression(s))
                Calculator.printResult();
        }
    }

    private static void parseCommand(String s) {
        if ("/help".equals(s)) {
            System.out.println("Calculate arithmetic operations from the standard input, support" +
                    "Integer and BigInteger numbers");
        } else if ("/exit".equals(s)) {
            System.out.println("Bye!");
            System.exit(0);
        } else {
            System.out.println("Unknown command");
        }
    }
}