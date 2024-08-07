import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;
import java.io.File;

public class PriorityTest {

    @Test
    public void testLessAnd() {
        var parserResults = TestUtils.parse("class A{ int a; public int information(int a) {   return a < a && 1; } }");
        TestUtils.noErrors(parserResults.getReports());

    }

    @Test
    public void testPlusTimes(){
        var parserResults=TestUtils.parse("class A{int a; public int information(int a){ return 2+3*2;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testExclamationAnd(){
        var parserResults=TestUtils.parse("class A{int a; public int information(int a){return !a + a && b;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testPlusMinus(){
        var parserResults=TestUtils.parse("class A{int a; public int information(int a){return 2+3-2;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testDivide(){
        var parserResults = TestUtils.parse("class A{int a; public int information(int a){return 4/5;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testAdd(){
        var parserResults = TestUtils.parse("class A{int a; public int information(int a){return 2+2+2;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testPriorityInverted(){
        var parserResults=TestUtils.parse("class A{int a; public int information(int a){return id[3].length+3&&!a;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testBooleanAndAnd(){
        var parserResults=TestUtils.parse("class A{int a; public int information(int a){return !a + (a&&b);}}");
        TestUtils.noErrors(parserResults.getReports());
    }
}