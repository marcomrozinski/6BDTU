package dk.dtu.compute.course02324.mini_java;

import dk.dtu.compute.course02324.mini_java.model.*;
import dk.dtu.compute.course02324.mini_java.semantics.*;

import static dk.dtu.compute.course02324.mini_java.utils.Shortcuts.*;
import static dk.dtu.compute.course02324.mini_java.model.Operator.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/*
 * Problemer og rettelser :
 *
 * 1. Brug af variabel før initialisering:
 *    int i;
 *    int j = i = 2 + (i = 3);
 *    --> Problem: 'i' bruges før den er initialiseret, hvilket ikke er tilladt i Java. Så vidt jeg har forstået (har googlet og læst om det)
 *    --> Løsning: Initialisér 'i' først, f.eks. 
 *              du har skrevet int i; hvilket betyder at i ikke er en variabel. Du skal deklarere den som int i = 0;
 *              og derefter kan du bruge den i udtrykket.
 *              Det kan gøres ved at ændre linjen til:
 *              'int i = 0;'
 *
 * 2. Dobbelt deklaration af variabel 'j':
 *    new Declaration(INT, new Var("j")),
 *    new Declaration(FLOAT, new Var("j"), ...)
 *     Problem: Samme variabel erklæres to gange med forskellig type.
 *     Løsning: Brug unikke navne eller undgå gentagen deklaration.
 *
 * 3. Brug af udeklareret variabel 'k':
 *    Assignment(Var("i"), Var("k"))
 *    --> Problem: 'k' er ikke deklareret før brug.
 *    --> Løsning: Tilføj deklaration af 'k' før den bruges.
 *
 * 4. While-løkke med ikke-boolean betingelse:
 *    WhileLoop(Var("i"), ...)
 *    --> Problem: Betingelsen er en int, men skal være en boolean (f.eks. i >= 0).
 *    --> Løsning: Brug en sammenligning: OperatorExpression(GTE, Var("i"), Literal(0))
 *      int i = 5;
        while (i) {
                // fejler! i er en int, ikke en boolean
        }

        int i = 5;
        while (i > 0) {
                // nu er betingelsen 'i > 0', som er en boolean
                i--;
        }
 * 5. Forkerte assert-beskeder:
 *    assertEquals(x, ..., "Value of variable j should be " + x);
 *    --> Problem: Teksten siger 'j', men det handler om variablen 'x'.
 *    --> Løsning: Ret teksten til at nævne den korrekte variabel. Kig evt. på hvilken variabl du reelt kalder, måske jeg har misforstået. 
 */



/**
 * These are some basic tests of the MiniJava for computing the types and
 * evaluating expressions.
 */
public class TestMiniJava{

    private ProgramTypeVisitor ptv;

    private ProgramExecutorVisitor pev;

    /**
     *  Sets up the visitors for type checking and execution.
     */
    @BeforeEach
    public void setUp() {
        ptv = new ProgramTypeVisitor();
        pev = new ProgramExecutorVisitor(ptv);
    }

    @Test
    public void testCorrectProgramWithInts() {
        int i;
        int j = i = 2 + (i = 3) ;

        Statement statement = new Sequence(
                new Declaration(INT, new Var("i")),
                new Declaration(
                        INT,
                        new Var("j"),
                        new Assignment(
                                new Var("i"),
                                new OperatorExpression(
                                        PLUS2,
                                        new IntLiteral(2),
                                        new Assignment(
                                                new Var("i"),
                                                new IntLiteral(3)
                                        )))
                )
        );

        ptv.visit(statement);
        if (!ptv.problems.isEmpty()) {
            fail("The type visitor did detect typing problems, which should not be there!");
        }

        pev.visit(statement);

        Set<String> variables = new HashSet<>(List.of("i", "j"));
        for (Var var: ptv.variables) {
            variables.remove(var.name);

            if (var.name.equals("i")) {
                assertEquals(i, pev.values.get(var), "Value of variable i should be " + i + ".");
            } else if (var.name.equals("j")) {
                assertEquals(j, pev.values.get(var), "Value of variable j should be " + j + ".");
            } else {
                fail("A non-existing variable " + var.name + " occurred in evaluation of program.");
            }
        }
        assertEquals(0, variables.size(), "Some variables have not been evaluated");
    }

    @Test
    public void testCorrectlyTypedProgramWithFloats() {
        float i;
        float j = i = 2.75f - ( i = 3.21f );

        Statement statement =
                new Sequence(
                        new Declaration(FLOAT, new Var("i")),
                        new Declaration(
                                FLOAT,
                                new Var("j"),
                                new Assignment(
                                        new Var("i"),
                                        new OperatorExpression(
                                                MINUS2,
                                                new FloatLiteral(2.75f),
                                                new Assignment(
                                                        new Var("i"),
                                                        new FloatLiteral(3.21f)
                                                )
                                        )
                                )
                        )
                );

        ptv.visit(statement);
        if (!ptv.problems.isEmpty()) {
            fail("The type visitor did detect typing problems, which should not be there!");
        }
        pev.visit(statement);

        Set<String> variables = new HashSet<>(List.of("i", "j"));
        for (Var var: ptv.variables) {
            variables.remove(var.name);

            if (var.name.equals("i")) {
                assertEquals(i, pev.values.get(var), "Value of variable i should be " + i + ".");
            } else if (var.name.equals("j")) {
                assertEquals(j, pev.values.get(var), "Value of variable j should be " + j + ".");
            } else {
                fail("A non-existing variable " + var.name + " occurred in evaluation of program.");
            }
        }
        assertEquals(0, variables.size(), "Some variables have not been evaluated");

    }

    @Test
    public void testWronglyTypedProgram() {
        int i;
        int j = i = 2 + (i = 3) ;

        Statement statement =
                new Sequence(
                        new Declaration(FLOAT, new Var("i")),
                        new Declaration(INT, new Var("j")),
                        new Declaration(
                                FLOAT,
                                new Var("j"),
                                new Assignment(
                                        new Var("i"),
                                        new OperatorExpression(
                                                MINUS2,
                                                new FloatLiteral(2.75f),
                                                new Assignment(
                                                        new Var("i"),
                                                        new FloatLiteral(3.21f)
                                                )
                                        )
                                )
                        ),
                        Assignment(Var("i"), Var("k")),
                        Assignment(Var("k"), Literal(3))
                );

        ptv.visit(statement);

        if (ptv.problems.isEmpty()) {
            fail("No type problems detected in a mistyped statement!");
        }
    }

    @Test
    public void testLoopProgram() {
        int i = 5;
        int j = 0;
        int sum = 0;
        while ( i >= 0 ) {
            j = i;
            while ( j >= 0 ) {
                sum = sum + j;
                j = j - 1;
                // println(" i: ", i);
                // println(" j: ", j);
            };
            i = i - 1;
        };

        Statement statement = Sequence(
                Declaration(INT, Var("i"), Literal(5)),
                Declaration(INT, Var("sum"), Literal(0)),
                WhileLoop(
                        Var("i"),
                        Sequence(
                                Declaration(INT, Var("j"), Var("i")),
                                WhileLoop(
                                        Var("j"),
                                        Sequence(
                                                Assignment(
                                                        Var("sum"),
                                                        OperatorExpression(PLUS2,
                                                                Var("sum"),
                                                                Var("j")
                                                        )
                                                ),
                                                Assignment(
                                                        Var("j"),
                                                        OperatorExpression(MINUS2,
                                                                Var("j"),
                                                                Literal(1)
                                                        )
                                                ),
                                                PrintStatement(" i: ", Var("i")),
                                                PrintStatement(" j: ", Var("j"))
                                        )
                                ),
                                Assignment(
                                        Var("i"),
                                        OperatorExpression(MINUS2,
                                                Var("i"),
                                                Literal(1)
                                        )
                                )
                        )
                )
        );

        ptv.visit(statement);
        if (!ptv.problems.isEmpty()) {
            fail("The type visitor did detect typing problems, which should not be there!");
        }
        pev.visit(statement);

        Set<String> variables = new HashSet<>(List.of("i", "j", "sum"));
        for (Var var: ptv.variables) {
            variables.remove(var.name);

            if (var.name.equals("i")) {
                assertEquals(i, pev.values.get(var), "Value of variable i should be " + i + ".");
            } else if (var.name.equals("j")) {
                assertEquals(j, pev.values.get(var), "Value of variable j should be " + j + ".");
            } else if (var.name.equals("sum")) {
                assertEquals(sum, pev.values.get(var), "Value of variable sum should be " + sum + ".");
            } else {
                fail("A non-existing variable " + var.name + " occurred in evaluation of program.");
            }
        }
        assertEquals(0, variables.size(), "Some variables have not been evaluated");
    }

    @Test
    public void testPrintAndAdditionalOperators() {
        int i = - + -1 + 7 - 1;
        float x = - + -1.5f + 7.0f - 1.0f;
        int j = 36 % 7;
        int k = 36 / 7;
        float y = 36.0f / 7.0f;

        Sequence printStatements = Sequence(
                Declaration(INT,
                        Var("i"),
                        OperatorExpression(MINUS2,
                                OperatorExpression(PLUS2,
                                        OperatorExpression(MINUS1,
                                                OperatorExpression(PLUS1,
                                                        Literal(-1)
                                                )
                                        ),
                                        Literal(7)
                                ),
                                Literal(1)
                        )
                ),
                PrintStatement(" - + -1 + 7 - 1: ",
                        Var("i")
                ),
                Declaration(FLOAT,
                        Var("x"),
                        OperatorExpression(MINUS2,
                                OperatorExpression(PLUS2,
                                        OperatorExpression(MINUS1,
                                                OperatorExpression(PLUS1,
                                                        Literal(-1.5f)
                                                )
                                        ),
                                        Literal(7.0f)
                                ),
                                Literal(1.0f)
                        )
                ),
                PrintStatement(" - + -1.5f + 7.0f - 1.0f: ",
                        Var("x")
                ),
                Declaration(INT,
                        Var("j"),
                        OperatorExpression(MOD,
                                Literal(36),
                                Literal(7)
                        )
                ),
                PrintStatement("36 % 7: ",
                        Var("j")
                ),
                Declaration(INT,
                        Var("k"),
                        OperatorExpression(DIV,
                                Literal(36),
                                Literal(7)
                        )
                ),
                PrintStatement("36 / 7: ",
                        Var("k")
                ),
                Declaration(FLOAT,
                        Var("y"),
                        OperatorExpression(DIV,
                                Literal(36.0f),
                                Literal(7.0f)
                        )
                ),
                PrintStatement("36.0f / 7.0: ",
                        Var("y")
                )
        );


        ptv.visit(printStatements);
        if (!ptv.problems.isEmpty()) {
            fail("The type visitor did detect typing problems, which should not be there!");
        }

        pev.visit(printStatements);

        Set<String> variables = new HashSet<>(List.of("i", "x", "j", "k", "y"));
        for (Var var: ptv.variables) {
            variables.remove(var.name);

            if (var.name.equals("i")) {
                assertEquals(i, pev.values.get(var), "Value of variable i should be " + i + ".");
            } else if (var.name.equals("x")) {
                assertEquals(x, pev.values.get(var), "Value of variable j should be " + x + ".");
            } else if (var.name.equals("j")) {
                assertEquals(j, pev.values.get(var), "Value of variable j should be " + j + ".");
            } else if (var.name.equals("k")) {
                assertEquals(k, pev.values.get(var), "Value of variable j should be " + k + ".");
            } else if (var.name.equals("y")) {
                assertEquals(y, pev.values.get(var), "Value of variable j should be " + y + ".");
            } else {
                fail("A non-existing variable " + var.name + " occurred in evaluation of program.");
            }
        }
        assertEquals(0, variables.size(), "Some variables have not been evaluated");
    }
    @Test
    public void testMultiplicationOperators() {
        int i = 3 * 5;
        float x = 3.5f * 2.0f;

        Statement statement = Sequence(
                Declaration(INT,
                        Var("i"),
                        OperatorExpression(MULT,
                                Literal(3),
                                Literal(5)
                        )
                ),
                Declaration(FLOAT,
                        Var("x"),
                        OperatorExpression(MULT,
                                Literal(3.5f),
                                Literal(2.0f)
                        )
                ),
                PrintStatement("3 * 5 = ", Var("i")),
                PrintStatement("3.5f * 2.0f = ", Var("x"))
        );

        ptv.visit(statement);
        if (!ptv.problems.isEmpty()) {
            fail("The type visitor did detect typing problems, which should not be there!");
        }

        pev.visit(statement);

        Set<String> variables = new HashSet<>(List.of("i", "x"));
        for (Var var: ptv.variables) {
            variables.remove(var.name);

            if (var.name.equals("i")) {
                assertEquals(i, pev.values.get(var), "Value of variable i should be " + i + ".");
            } else if (var.name.equals("x")) {
                assertEquals(x, pev.values.get(var), "Value of variable x should be " + x + ".");
            } else {
                fail("Non-existing variable " + var.name + " occurred");
            }
        }
        assertEquals(0, variables.size(), "Some variables have not been evaluated");
    }

    @Test
    public void testDivisionByZero() {
        try {
            Statement statement = Sequence(
                    Declaration(INT,
                            Var("i"),
                            OperatorExpression(DIV,
                                    Literal(5),
                                    Literal(0)
                            )
                    )
            );

            ptv.visit(statement);
            pev.visit(statement);
            fail("Division by zero should throw an exception");
        } catch (ArithmeticException e) {
            // Expected behavior
        }
    }

}
