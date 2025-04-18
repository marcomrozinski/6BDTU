package dk.dtu.compute.course02324.mini_java.semantics;

import dk.dtu.compute.course02324.mini_java.model.*;
import static dk.dtu.compute.course02324.mini_java.model.Operator.*;
import static dk.dtu.compute.course02324.mini_java.utils.Shortcuts.*;

import java.util.*;

import static java.util.Map.entry;
/**
 * The ProgramTypeVisitor class is responsible for checking if a MiniJava program is correct
 * when it comes to "types". It looks at the different elements
 * of the program, such as variables, operations, and expressions, to make sure everything fits together.
 * The class acts like a "checker" that goes through the program step by step and verifies:
 * - Are the variables declared and used properly?
 * - Are the mathematical operations used with the right types (for example integers and floats)?
 * - Is there anything that doesn't make sense, like type mismatches or undeclared variables?
 * If the program has any problems, such as errors in variable use or type violations,
 * the class will collect these issues and store them in a **problems list**.
 */

public class ProgramTypeVisitor extends ProgramVisitor {

    /**
     * This is a very simple map of operators to their possible types.
     * Note the typing of an operator is very simplistic for now; the
     * types of all operands and the result of the operation are the same.<p>
     *
     * TODO Assignment 6a: This map does contain only some few examples of types
     *      on which the operators should work. In Assignment 6a, this list must
     *      be complete for all (primmitive) types of Mini Java on which these
     *      operators make sense.
     */
    /** Supported types for each operator. */
    final private Map<Operator,List<Type>> operatorTypes = Map.ofEntries(
            entry(PLUS2, List.of(INT, FLOAT)),
            entry(MINUS2, List.of(INT, FLOAT)),
            entry(MULT, List.of(INT, FLOAT)),
            entry(MOD, List.of(INT)),
            entry(PLUS1, List.of(INT, FLOAT)),
            entry(MINUS1, List.of(INT, FLOAT)),
            entry(DIV, List.of(INT, FLOAT)));

    /** Stores the resolved type for each expression. */
    final public Map<Expression, Type> typeMapping = new HashMap<>();

    /** Stores all declared variables. */
    final public Set<Var> variables = new HashSet<>();

    /** Stores all detected type-related problems. */
    final public List<String> problems = new ArrayList<>();

    /** Visits a generic statement. */
    public void visit(Statement statement) {
        statement.accept(this);
    }

    /** Visits a sequence of statements. */
    @Override
    public void visit(Sequence sequence) {
        for (Statement substatement: sequence.statements) {
            substatement.accept(this);
        }
    }

    /** Checks if a variable is redeclared or mismatched in type. */
    @Override
    public void visit(Declaration declaration) {
        if (declaration.expression != null) {
            declaration.expression.accept(this);
        }
        Var variable = declaration.variable;
        if (variables.contains(variable)) {
            problems.add("Variable " + variable.name + " declared more than once.");
        } else {
            variables.add(variable);
            typeMapping.put(variable, declaration.type);
            if (declaration.expression != null) {
                Type expressionType = typeMapping.get(declaration.expression);
                if (!declaration.type.equals(expressionType)) {
                    problems.add("Type mismatch for declaration of " +
                            declaration.type.getName() + " " + declaration.variable.name +
                            ": expression is type " + expressionType.getName() + ".");
                }
            }
        }
    }

    /** Validates the expression inside a print statement. */
    @Override
    public void visit(PrintStatement printStatement) {
        printStatement.expression.accept(this);
    }

    /** Ensures the while loop condition is an integer. */
    public void visit(WhileLoop whileLoop) {
        whileLoop.expression.accept(this);
        Type expressionType = typeMapping.get(whileLoop.expression);
        if(!INT.equals(expressionType)) {
            problems.add("Not an int: " + (expressionType != null ? expressionType.getName() : "undefined"));
        }
        whileLoop.statement.accept(this);
    }

    /** Validates assignment type and variable declaration. */
    @Override
    public void visit(Assignment assignment) {
        assignment.expression.accept(this);
        if (variables.contains(assignment.variable)) {
            Type type = typeMapping.get(assignment.variable);
            if (!type.equals(typeMapping.get(assignment.expression))) {
                problems.add("Type mismatch for assignment to variable " +
                        assignment.variable.name + " of type " + type.getName() + ".");
            } else {
                typeMapping.put(assignment, type);
            }
        } else {
            problems.add("Variable " + assignment.variable.name + " not defined.");
        }
    }

    /** Detects the type of a literal expression. */
    @Override
    public void visit(Literal literal) {
        if (literal instanceof IntLiteral) {
            typeMapping.put(literal, INT);
        }  else if (literal instanceof FloatLiteral) {
            typeMapping.put(literal, FLOAT);
        }
    }

    /** Ensures a variable is declared and has a type. */
    @Override
    public void visit(Var var) {
        if (!variables.contains(var)) {
            problems.add("Variable not defined " + var);
        } else if (typeMapping.get(var) == null) {
            problems.add("Variable " + var.name + " does not have a type.");
        }
    }

    /** Checks if operator expression is valid and types are compatible. */
    @Override
    public void visit(OperatorExpression operatorExpression) {
        Type operandType = null;
        for (Expression subexpression: operatorExpression.operands) {
            subexpression.accept(this);
            Type subexpressionType = typeMapping.get(subexpression);
            if (subexpressionType == null) {
                problems.add("A subexpression of " + operatorExpression.operator.getName() + " does not have a type.");
            }
            if (operandType == null) {
                operandType = subexpressionType;
            } else if (!operandType.equals(subexpressionType)) {
                problems.add("Subexpressions of operator do not match for " + operatorExpression.operator.getName() + ".");
            }
        }
        if (operandType != null) {
            List<Type> opTypes = operatorTypes.get(operatorExpression.operator);
            if (opTypes != null && opTypes.contains(operandType)) {
                typeMapping.put(operatorExpression, operandType);
            } else {
                problems.add("Operator does not support the type of its operands. Operator is " + operatorExpression.operator + " and operand type is " + operandType);
            }
        } else {
            problems.add("Subexpression(s) of operand do not have a type: Operator " + operatorExpression.operator);
        }
    }
}
