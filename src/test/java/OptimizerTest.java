import src.org.stepik.bogolepov.lexer.Lexer;
import src.org.stepik.bogolepov.optimizer.Optimizer;
import src.org.stepik.bogolepov.optimizer.strategies.*;
import org.junit.Test;
import src.org.stepik.bogolepov.node.Node;
import src.org.stepik.bogolepov.parser.Parser;

import java.io.StringReader;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Created by sbogolepov on 09/05/2017.
 */
public class OptimizerTest {

    private Node astFromString(String str) throws Exception {
        return new Parser(new Lexer(new StringReader(str))).parse();
    }

    public void check(String input, String output) throws Exception {
        Node root = astFromString(input);
        defaultOptimizer().optimize(root);
        assertThat(root).isEqualTo(astFromString(output));
    }

    private static Optimizer defaultOptimizer() {
        return new Optimizer(Arrays.asList(
                new DeMorganStrategy(),
                new InvertLiteral(),
                new NotNotStrategy(),
                new BinaryLiteralStrategy(),
                new ChainReducerStrategy(),
                new RemoveParensStrategy()
        ));
    }

    @Test
    public void notNotTest() throws Exception {
        check("NOT (NOT x)", "x");
    }

    @Test
    public void chainWithParentheses() throws Exception {
        check("(x OR y) OR y", "x OR y");
    }

    @Test
    public void badChain() throws Exception {
        check("x OR y OR z OR w", "x OR y OR z OR w");
        check("x OR y OR y OR x", "x OR y");
    }

    @Test
    public  void parenthesis() throws Exception {
        check("(((x)))", "x");
        check("NOT (x)", "NOT x");
    }

    @Test
    public void equalChains() throws Exception {
        check("(x OR y OR z) AND (x OR z OR y)", "x OR y OR z");
    }

    @Test
    public void invertLiteralTest() throws Exception {
        check("NOT TRUE", "FALSE");
    }

    @Test
    public void symmetry() throws Exception {
        check("a OR b", "b OR a");
        check("a OR b OR c", "c OR a OR b");
    }

    @Test
    public void deMorganTest() throws Exception {
        check("NOT (x OR y)", "NOT x AND NOT y");
    }

    @Test
    public void literalAndIdTest() throws Exception {
        check("TRUE AND id", "id");
        check("TRUE OR NOT id", "TRUE");
    }

    @Test
    public void complex() throws Exception {
        check("NOT(NOT dog AND NOT cat AND NOT FALSE)", "dog OR cat");
        check("NOT (TRUE OR FALSE OR TRUE OR FALSE) AND (x OR y OR (x OR y))", "FALSE");
    }

    @Test
    public void simpleTest() throws Exception {
        check("x OR y OR TRUE", "TRUE");
    }

    @Test
    public void toBeOrToBe() throws Exception {
        check("x OR x", "x");
    }

    @Test
    public void toBeOrToBeOrToBeOrToBe() throws Exception {
        check("x OR x OR x OR x", "x");
    }

    @Test
    public void toBeOrToBeOrToNotBeOrToBe() throws Exception {
        check("x OR x OR NOT x OR x", "TRUE");
    }

    @Test
    public void toBeOrNotToBe() throws Exception {
        check("x OR NOT x", "TRUE");
    }

    @Test
    public void failed() throws Exception {
//        check("NOT(NOT(a OR b AND c)) AND c", "(a OR b) AND c");
    }
}
