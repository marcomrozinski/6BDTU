package dk.dtu.compute.course02324.mini_java.semantics;

import dk.dtu.compute.course02324.mini_java.model.*;

public abstract class ProgramVisitor {
    /** Visits a sequence of statements. */
    abstract public void visit(Sequence sequence);

    /** Visits a variable declaration. */
    abstract public void visit(Declaration declaration);

    /** Visits a print statement. */
    abstract public void visit(PrintStatement declaration);

    /** Visits a while-loop statement. */
    abstract public void visit(WhileLoop whileLoop);

    /** Visits an assignment statement. */
    abstract public void visit(Assignment assignment);

    /** Visits a literal expression. */
    abstract public void visit(Literal literal);

    /** Visits a variable reference. */
    abstract public void visit(Var var);

    /** Visits an operator expression. */
    abstract public void visit(OperatorExpression operatorExpression);

}
