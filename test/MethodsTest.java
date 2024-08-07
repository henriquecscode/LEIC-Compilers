import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;
import java.io.File;

public class MethodsTest {

    /***
     * @Test
     *       public void testExpression() {
     *       var parserResults = TestUtils.parse("2+3\n10+20\n");
     *       TestUtils.noErrors(parserResults.getReports());
     *       /*
     *       parserResult.getReports().get(0).getException().get().printStackTrace();
     *       System.out.println();
     *       var analysisResult = TestUtils.analyse(parserResult);
     *       System.out.println(analysisResult);
     ***/

    @Test
    public void testNoClassNoImport() {
        // Must fail. The code is empty.
        var parserResults = TestUtils.parse("");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testClassWithoutImport() {
        // Must pass. It's an empty class, with no imports.
        var parserResults = TestUtils.parse("class A{ }");
    }

    @Test
    public void testClassFromFile(){
        /*var parserResults = SpecsIO.read("./test/inputs/Empty.java");
        TestUtils.noErrors(parserResults.getReports());*/
    }

}
