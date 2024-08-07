import org.junit.Test;

import pt.up.fe.comp.TestUtils;

public class ImportTest {

    @Test
    public void testWithoutImportKeyword() {
        // Must fail. Import should be the first keyword.
        var parserResults = TestUtils.parse("java.lang;\nclass A{ }");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testWithoutSemicolonKeyword() {
        // Must fail. Import statement must end with ;
        var parserResults = TestUtils.parse("import java\nclass A{ }");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testWithImport(){
        // Must pass.
        var parserResults = TestUtils.parse("import java;\nclass A{ }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testWithDoublFolderImport(){
        // Must pass.
        var parserResults = TestUtils.parse("import java.a;\nclass A{ }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testWithDoubleFolderImportBadSyntax(){
        // Must fail.
        var parserResults = TestUtils.parse("import java.a.;\nclass A{ }");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testWithMultiFolderImport(){
        // Must pass.
        var parserResults = TestUtils.parse("import java.a.b.c.d.e.f.g;\nclass A{ }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testWithDoubleImportWithoutSemicolon(){
        // Must fail.
        var parserResults = TestUtils.parse("import java import foo;\nclass A{ }");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testWithDoubleImportWithoutSemicolon2(){
        // Must fail.
        var parserResults = TestUtils.parse("import java; import foo\nclass A{ }");
        TestUtils.mustFail(parserResults.getReports());
    }

    @Test
    public void testWithDoubleImport(){
        // Must fail.
        var parserResults = TestUtils.parse("import java; import foo;\nclass A{ }");
        TestUtils.noErrors(parserResults.getReports());
    }

    @Test
    public void testWithMultipleMultipleFolderImport(){
        // Must fail.
        var parserResults = TestUtils.parse("import java;import foo;import a.b.c; import bar; import a.b.c.d.e.f.g;\nclass A{ }");
        TestUtils.noErrors(parserResults.getReports());
    }
}
