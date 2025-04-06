package dk.dtu.compute.course02324.mini_java.semantics;

import dk.dtu.compute.course02324.mini_java.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dk.dtu.compute.course02324.mini_java.model.Operator.*;
import static dk.dtu.compute.course02324.mini_java.utils.Shortcuts.FLOAT;
import static dk.dtu.compute.course02324.mini_java.utils.Shortcuts.INT;
import static java.util.Map.entry;
/**
 * This class is responsible for executing a MiniJava program by visiting various nodes
 * (for example, statements, expressions, and declarations) in the program's abstract syntax tree and executing their meaning or behavior.
 * The execution involves evaluating expressions, performing mathematical operations, and handling variables.
 */

public class ProgramExecutorVisitor extends ProgramVisitor {

    final private ProgramTypeVisitor pv;

    final public Map<Expression, Number> values = new HashMap<>();


    /**
     * Functions for performing mathematical operations, such as addition, subtraction,
     * multiplication, division, modulus, and unary operations for integers and floats.
     */


    private final Function<List<Number>,Number> plus2int =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 + arg2; };
    private final Function<List<Number>,Number> plus2float =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 + arg2; };
    private final Function<List<Number>, Number> plus1int =
            args -> +args.get(0).intValue();

    private final Function<List<Number>, Number> plus1float =
            args -> +args.get(0).floatValue();


    private final Function<List<Number>,Number> minus2float =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 - arg2; };

    private final Function<List<Number>,Number> minus2int =
            args -> {
                int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 - arg2;
            };

    private final Function<List<Number>,Number> multfloat =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 * arg2; };

    private final Function<List<Number>,Number> minus1float =
            args -> -args.get(0).floatValue();

    private final Function<List<Number>,Number> minus1int =
            args -> -args.get(0).intValue();

    private final Function<List<Number>,Number> DivInt =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 / arg2; };

    private final Function<List<Number>,Number> DivFloat =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 / arg2; };

    private final Function<List<Number>,Number> ModInt =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 % arg2; };

    private final Function<List<Number>, Number> multint =
            args -> {
                int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 * arg2;
            };

    /**
     * The map below associates each operator for each possible type with a function
     * (lambda expression), that represents the semantics of that operation. These
     * define what happens when the operator needs to be executed.<p>
     *
     * TODO Assignment 6a: This map and the functions above need to be extended in Assignment 6a
     *      (all operations with the respective types required in assignment must be defined above
     *      and added to the mapping below).
     */
    final private Map<Operator, Map<Type, Function<List<Number>,Number>>> operatorFunctions = Map.ofEntries(
            entry(PLUS2, Map.ofEntries(
                    entry(INT, plus2int ),
                    entry(FLOAT, plus2float ) )
            ),
            entry(PLUS1, Map.ofEntries(
                    entry(INT, plus1int ),
                    entry(FLOAT, plus1float ) )
            ),
            entry(MINUS2, Map.ofEntries(
                    entry(FLOAT, minus2float),
                    entry(INT, minus2int) )
            ),
            entry(MULT, Map.ofEntries(
                    entry(FLOAT, multfloat ),
                    entry(INT, multint)
            )),

            entry (MINUS1, Map.ofEntries(
                    entry(FLOAT, minus1float),
                    entry(INT, minus1int))
            ),
            entry(DIV, Map.ofEntries(
                    entry(FLOAT, DivFloat),
                    entry(INT, DivInt))
            ),
            entry(MOD, Map.ofEntries(
                    entry(INT, ModInt)
            ))
    );


    public ProgramExecutorVisitor(ProgramTypeVisitor pv) {
        this.pv = pv;
    }

    public void visit(Statement statement) {
        statement.accept(this);
    }

    @Override
    public void visit(Sequence sequence) {
        for (Statement substatement: sequence.statements) {
            visit(substatement);
        }
    }

    /**
     * Here visit a variable declaration, evaluates its expression if present, and assigns the value to the variable.
     */
    @Override
    public void visit(Declaration declaration) {
        if (declaration.expression != null) {
            declaration.expression.accept(this);
            Number result = values.get(declaration.expression);
            values.put(declaration.variable, result);
        }
    }

    /**
     * This visits and executes a PrintStatement by evaluating its expression and printing the result.
     */
    @Override
    public void visit(PrintStatement printStatement) {
        printStatement.expression.accept(this);
        Number result = values.get(printStatement.expression);
        System.out.println(printStatement.prefix + result);

    }

    /**
     * Executes a while loop by repeatedly evaluating its condition and, if the condition is true,
     * executing the loop's body. The process continues until the condition evaluates to null
     * or a non-positive value.
     * @param whileLoop
     */
    @Override
    public void visit(WhileLoop whileLoop) {
        whileLoop.expression.accept(this);

        whileLoop.expression.accept(this);
        Number value = values.get(whileLoop.expression);

        while (value != null && value.doubleValue() >= 0) {
            whileLoop.statement.accept(this);
            whileLoop.expression.accept(this);
            value = values.get(whileLoop.expression);
        }
    }
/**
 * This method visits an assignment, calculates the value of the expression (on the righthand side),
 * and then stores the computed value in the variable (on the lefthand side).
 * For example, if the code is:
 * x = 5 + 3;`
 * The method will first calculate the value of 5 + 3, which is 8.
 * Then, it will store the value `8` in the variable x.
*/
    @Override
    public void visit(Assignment assignment) {
        assignment.expression.accept(this);
        Number result = values.get(assignment.expression);
        values.put(assignment, result);
        values.put(assignment.variable, result);
    }

    /**
     * Visits a literal (for example integer or float) and stores its value in the values map.
*/
     @Override
    public void visit(Literal literal) {
        if (literal instanceof IntLiteral) {
            values.put(literal, ((IntLiteral) literal).literal);
        }  else if (literal instanceof FloatLiteral) {
            values.put(literal, ((FloatLiteral) literal).literal);
        }
    }

    @Override
    public void visit(Var var) {
        // We do not need to do anything here; if the variable was assigned a
        // value already by an assignment or a declaration, this value will be
        // in the values map already (the respective assignment or declaration
        // should have added this value for variable already).
    }

/**
 * Evaluates an OperatorExpression by visiting its operands, applying the corresponding operator,
 * and storing the computed result in the values map.
*/
 @Override
    public void visit(OperatorExpression operatorExpression) {
        Type type = pv.typeMapping.get(operatorExpression);
        Map<Type,Function<List<Number>,Number>> typeMap = operatorFunctions.get(operatorExpression.operator);

        // Function<List<Number>,Number> function = typeMap != null && type!= null ? typeMap.get(type) : null;
        Function<List<Number>,Number> function = null;
        if (typeMap != null && type!= null ) {
            function = typeMap.get(type);
        }

        if (function == null) {
            throw new RuntimeException("No function of this type available");
        }

        List<Number> args = new ArrayList<>();
        for (Expression subexpression: operatorExpression.operands ) {
            subexpression.accept(this);
            Number arg = values.get(subexpression);
            if (arg == null) {
                throw new RuntimeException("Value of subexpression does not exist");
            }
            args.add(arg);
        }

        Number result = function.apply(args);
        values.put(operatorExpression, result);
    }

}
