package dk.dtu.compute.course02324.mini_java.semantics;

import dk.dtu.compute.course02324.mini_java.model.*;

import java.util.HashMap;
import java.util.Map;

public class ProgramSerializerVisitor extends ProgramVisitor  {

    /** StringBuilder to accumulate program output */
    private StringBuilder result = new StringBuilder();

    /** Tracks current indentation level */
    private int indentLevel = 0;

    /** Defines a single indentation unit */
    final private String INDENT = "  ";

    /** Adds indentation spaces to result based on indent level */
    private String addIndentation() {
        String indent = "";
        for (int i=0; i < indentLevel; i++) {
            result.append(INDENT);
        }
        return indent;
    }

    /** Visits any type of statement */
    public void visit(Statement statement) {
        statement.accept(this);
    }

    /** Serializes a sequence of statements with correct formatting */
    @Override
    public void visit(Sequence sequence) {
        for (Statement statement: sequence.statements) {
            addIndentation();
            statement.accept(this);
            if (statement instanceof WhileLoop) {
                result.append(System.lineSeparator());
            } else {
                result.append(";" + System.lineSeparator());
            }
        }
    }

    /** Serializes a variable declaration and optional assignment */
    @Override
    public void visit(Declaration declaration) {
        result.append(declaration.type.getName() + " " + declaration.variable.name);
        if (declaration.expression != null) {
            result.append(" = ");
            declaration.expression.accept(this);
        }
    }

    /** Serializes a print statement including prefix and optional expression */
    @Override
    public void visit(PrintStatement printStatement) {
        result.append("System.out.println(\"" + printStatement.prefix + "\"");
        if (printStatement.expression != null) {
            result.append(" + ");
            printStatement.expression.accept(this);
        }
        result.append(")");
    }

    /** Serializes a while-loop with its condition and body */
    @Override
    public void visit(WhileLoop whileLoop) {
        result.append("while ( ");
        whileLoop.expression.accept(this);
        result.append(" >= 0 ) {" + System.lineSeparator());
        indentLevel++;
        whileLoop.statement.accept(this);
        indentLevel--;
        addIndentation();
        result.append("}");
    }

    /** Serializes an assignment of a value to a variable */
    @Override
    public void visit(Assignment assignment) {
        result.append(assignment.variable.name  + " = ");
        assignment.expression.accept(this);
    }

    /** Serializes a literal value (int or float) */
    @Override
    public void visit(Literal literal) {
        if (literal instanceof IntLiteral) {
            result.append(((IntLiteral) literal).literal);
        } else if (literal instanceof FloatLiteral) {
            result.append(((FloatLiteral) literal).literal + "f");
        } else {
            assert false;
        }
    }

    /** Serializes a variable reference */
    @Override
    public void visit(Var var) {
        result.append(var.name);
    }

    /** Serializes an operator expression, including correct formatting and precedence */
    @Override
    public void visit(OperatorExpression operatorExpression) {
        if (operatorExpression.operands.size() == 0) {
            result.append(operatorExpression.operator.getName() +"()");
        } else if (operatorExpression.operands.size() == 1) {
            result.append(operatorExpression.operator.getName() + " ");
            operatorExpression.operands.getFirst().accept(this);
        } else if (operatorExpression.operands.size() == 2) {
            operandToString(operatorExpression.operator, operatorExpression.operands.getFirst(),0);
            result.append(" " + operatorExpression.operator.getName() + " ");
            operandToString(operatorExpression.operator, operatorExpression.operands.getLast(), 1);
        } else {
            result.append(operatorExpression.operator.getName() + "(");
            boolean first = true;
            for (Expression operand : operatorExpression.operands) {
                if (!first) {
                    result.append(", ");
                } else {
                    first = false;
                }
                operand.accept(this);
            }
            result.append(")");
        }
    }

    /** Serializes a single operand considering operator precedence */
    private void operandToString(Operator operator, Expression expression, int number) {
        if (expression instanceof OperatorExpression) {
            OperatorExpression operatorExpression = (OperatorExpression) expression;
            if (operatorExpression.operator.precedence > operator.precedence ||
                    (operatorExpression.operator.precedence == operator.precedence &&
                            ((operator.associativity == Associativity.LtR && number == 0) ||
                                    (operator.associativity == Associativity.RtL && number == 1)))) {
                expression.accept(this);
            } else {
                result.append("( ");
                expression.accept(this);
                result.append(" )");
            }
        } else if (expression instanceof Assignment) {
            result.append("( ");
            expression.accept(this);
            result.append(" )");
        } else {
            expression.accept(this);
        }
    }

    /** Returns the complete serialized program as a string */
    public String result() {
        return result.toString();
    }
}
