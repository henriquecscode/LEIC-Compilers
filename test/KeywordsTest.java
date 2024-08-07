import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;
import java.io.File;

public class KeywordsTest {

    @Test
    public void testClassWithStaticPublicExtends() {
        // 3 different tests.
    }


    @Test
    public void testClassWithAtributes() {
        var parserResults = TestUtils.parse("class A{ int info; }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testClassWithMethods() {
        var parserResults = TestUtils.parse("class A{ public static void main(String[] args){ }} ");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testClassAttributeMethod() {
        var parserResults = TestUtils.parse("class A{ int info; public int information(int info) {  return info; } }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testClassAttributeMethodWithoutVariable() {

    }

    @Test
    public void testClassAttributeMethodWithVariable() {

    }

    /**
     * Daqui em diante, não especificar se tem classes, atributos e métodos.
     * Não existem funções que não sejam métodos de classes, logo isso é inerente aos testes.
     * TESTES PARA TESTAR OUTRAS KEYWORDS.
     */

    @Test
    public void testVariableDeclaration() {
        // Test all primitive types - INT, STRING, boolean etc.
        var parserResults = TestUtils.parse("class A{ int a; }");
        TestUtils.noErrors(parserResults.getReports());
        var parserResults2= TestUtils.parse("class A{ boolean c; }");
        TestUtils.noErrors(parserResults2.getReports());
    }

    @Test
    public void testVariableAssignment() {
        // Includes code from last test - we do an assignment now.
        var parserResults = TestUtils.parse("class A{ public static void main(String[] args){ int a; a=2; } }");
        TestUtils.noErrors(parserResults.getReports());
        var parserResults2= TestUtils.parse("class A{ public static void main(String[] args) { int b; }}");
        TestUtils.noErrors(parserResults2.getReports());
        var parserResults3= TestUtils.parse("class A{public static void main(String[] args){ boolean c; c= !true; } }");
        TestUtils.noErrors(parserResults3.getReports());
    }

    // Conditional Statements - includes loops, because they have an end condition.
    @Test
    public void testWhile() {
        var parserResult = TestUtils.parse("class A{public static void main(String[] args) { int i; i=0; while(i<5){i=1;}}}");
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testIf() {
        // Testing if and length here
        var parserResult=TestUtils.parse("class A { public static void main(String[] args) { int a; if(a.Length()<6){a=a+1;}else{a;} } }");
        TestUtils.noErrors(parserResult.getReports());
        var parserResult1 = TestUtils.parse("class A { public int information(int info) { int i; i=0; if(i<2){ i=1; }else{i=0;} if (i<0) { i = 4; }else{i=0;} }}");
        TestUtils.noErrors(parserResult1.getReports());
    }

    @Test
    public void testIfElse() {
        var parserResult = TestUtils.parse("class A{ public int information(int info) { if (false) { true; } else { false; } } }");
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testIfElseIfElse() {
        var parserResult = TestUtils.parse("class A{ public int information(int info) { int i; i=1; if (i<0) {i=0;} else {i=1;} } }");
        TestUtils.noErrors(parserResult.getReports());

    }
}
