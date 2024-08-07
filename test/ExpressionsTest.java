import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;
import java.io.File;

public class ExpressionsTest {

    @Test
    public void testSimpleReturn(){
        /*var parserResults = SpecsIO.read("./test/inputs/testSimpleReturn.java");
        TestUtils.noErrors(parserResults.getReports());*/
    }


    @Test
    public void testBoolean(){
        var parserResults = TestUtils.parse("class A{ public int information(){return false;}}");
        TestUtils.noErrors(parserResults.getReports());
        /*var parserResults1 = TestUtils.parse("class A{public int information(){return true && false;}}");
        TestUtils.noErrors(parserResults1.getReports());*/
        var parserResults2=TestUtils.parse("class A{public int information(){return !true;}}");
        TestUtils.noErrors(parserResults2.getReports());
    }

    @Test
    public void testId(){
        var parserResults=TestUtils.parse("class A{public int information(){return test;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testNew(){
        var parserResults=TestUtils.parse("class A{public int information(){ var i; i = new Box(); return 0;}}");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testWhile() {
        var parserResult = TestUtils.parse("class A{public int information() { int i; i = 0; while(i<5){i=1;} return i;}}");
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testSimpleLength(){
        var parserResult=TestUtils.parse("class A{public int information(){return test.length;}}");
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testLength(){
        var parserResult=TestUtils.parse("class A{public int information(){int[] test; return test.length;}}");
        TestUtils.noErrors(parserResult.getReports());
    }
}
