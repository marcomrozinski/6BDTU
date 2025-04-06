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

    /** Type visitor to look up types for expressions */
    final private ProgramTypeVisitor pv;

    /** Stores computed values for expressions and variables */
    final public Map<Expression, Number> values = new HashMap<>();

    /** Adds two integers */
    private final Function<List<Number>,Number> plus2int =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 + arg2; };

    /** Adds two floats */
    private final Function<List<Number>,Number> plus2float =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 + arg2; };

    /** Unary plus for int */
    private final Function<List<Number>, Number> plus1int =
            args -> +args.get(0).intValue();

    /** Unary plus for float */
    private final Function<List<Number>, Number> plus1float =
            args -> +args.get(0).floatValue();

    /** Subtracts two floats */
    private final Function<List<Number>,Number> minus2float =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 - arg2; };

    /** Subtracts two ints */
    private final Function<List<Number>,Number> minus2int =
            args -> {
                int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 - arg2;
            };

    /** Multiplies two floats */
    private final Function<List<Number>,Number> multfloat =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 * arg2; };

    /** Unary minus for float */
    private final Function<List<Number>,Number> minus1float =
            args -> -args.get(0).floatValue();

    /** Unary minus for int */
    private final Function<List<Number>,Number> minus1int =
            args -> -args.get(0).intValue();

    /** Integer division */
    private final Function<List<Number>,Number> DivInt =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 / arg2; };

    /** Float division */
    private final Function<List<Number>,Number> DivFloat =
            args -> { float arg1 = args.get(0).floatValue();
                float arg2 = args.get(1).floatValue();
                return arg1 / arg2; };

    /** Integer modulo */
    private final Function<List<Number>,Number> ModInt =
            args -> { int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 % arg2; };

    /** Multiplies two ints */
    private final Function<List<Number>, Number> multint =
            args -> {
                int arg1 = args.get(0).intValue();
                int arg2 = args.get(1).intValue();
                return arg1 * arg2;
            };

    /** Maps operators and types to the matching arithmetic function */
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

    /** Constructor initializes type visitor */
    public ProgramExecutorVisitor(ProgramTypeVisitor pv) {
        this.pv = pv;
    }

    /** Evaluates a statement node */
    public void visit(Statement statement) {
        statement.accept(this);
    }

    /** Evaluates a sequence of statements */
    @Override
    public void visit(Sequence sequence) {
        for (Statement substatement: sequence.statements) {
            visit(substatement);
        }
    }

    /** Evaluates a variable declaration */
    @Override
    public void visit(Declaration declaration) {
        if (declaration.expression != null) {
            declaration.expression.accept(this);
            Number result = values.get(declaration.expression);
            values.put(declaration.variable, result);
        }
    }

    /** Evaluates and prints a print statement */
    @Override
    public void visit(PrintStatement printStatement) {
        printStatement.expression.accept(this);
        Number result = values.get(printStatement.expression);
        System.out.println(printStatement.prefix + result);
    }

    /** Repeatedly executes a while-loop */
    @Override
    public void visit(WhileLoop whileLoop) {
        whileLoop.expression.accept(this);
        Number value = values.get(whileLoop.expression);
        while (value != null && value.doubleValue() >= 0) {
            whileLoop.statement.accept(this);
            whileLoop.expression.accept(this);
            value = values.get(whileLoop.expression);
        }
    }

    /** Evaluates an assignment expression */
    @Override
    public void visit(Assignment assignment) {
        assignment.expression.accept(this);
        Number result = values.get(assignment.expression);
        values.put(assignment, result);
        values.put(assignment.variable, result);
    }

    /** Evaluates and stores a literal value */
    @Override
    public void visit(Literal literal) {
        if (literal instanceof IntLiteral) {
            values.put(literal, ((IntLiteral) literal).literal);
        }  else if (literal instanceof FloatLiteral) {
            values.put(literal, ((FloatLiteral) literal).literal);
        }
    }

    /** Leaves variable node unchanged */
    @Override
    public void visit(Var var) {
        // No action needed; variable's value is already stored
    }

    /** Evaluates an operator expression */
    @Override
    public void visit(OperatorExpression operatorExpression) {
        Type type = pv.typeMapping.get(operatorExpression);
        Map<Type,Function<List<Number>,Number>> typeMap = operatorFunctions.get(operatorExpression.operator);

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
