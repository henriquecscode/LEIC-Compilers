import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class JasminTest {
    @Test
    public void test(){
        var jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/cp2/DynamicLocalsStack.jmm"));
        TestUtils.noErrors(jasminResult);

        /*jasminResult.run();
        * jasminResult.compile();*/

    }
}
