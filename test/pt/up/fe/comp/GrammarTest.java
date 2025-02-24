/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp;

import org.junit.Test;

public class GrammarTest {

    private static final String IMPORT = "ImportDeclaration";
    private static final String MAIN_METHOD = "MethodDeclaration";
    private static final String INSTANCE_METHOD = "MethodDeclaration";
    private static final String STATEMENT = "Statement";
    private static final String EXPRESSION = "Expression";

    private static void noErrors(String code, String grammarRule) {
        if (grammarRule.isEmpty()) {
            System.out.println(
                    "Name of grammar rule is empty, please define it in the static field at the beginning of the class '"
                            + GrammarTest.class.getName() + "' if test is to be executed");
            return;
        }

        var result = TestUtils.parse(code, grammarRule);
        TestUtils.noErrors(result.getReports());

        System.out.println("Code: " + code + "\n");
        System.out.println("AST:\n\n" + result.getRootNode().toTree());
        System.out.println("\n---------\n");
    }

    private static void noErrors(String code) {
        noErrors(code, "Program");
    }

    @Test
    public void testImportSingle() {
        noErrors("import bar;", IMPORT);
    }

    @Test
    public void testImportMulti() {
        noErrors("import bar.foo.a.b.c.d; import a;", IMPORT);
    }

    @Test
    public void testClass() {
        noErrors("class Foo extends Bar {}");
    }

    @Test
    public void testVarDecls() {
        noErrors("class Foo {int a; int[] b; int c; boolean d; Bar e;}");
    }

    @Test
    public void testVarDeclString() {
        noErrors("int aString;", "VariableDeclaration");
    }

    @Test
    public void testMainMethodEmpty() {
        noErrors("public static void main(String[] args) {}", MAIN_METHOD);
    }

    @Test
    public void testInstanceMethodEmpty() {
        noErrors("public int foo(int anInt, int[] anArray, boolean aBool, int aString) {return a;}",
                INSTANCE_METHOD);
    }

    @Test
    public void testStmtScope() {
        noErrors("{a; b; c;}", STATEMENT);
    }

    @Test
    public void testStmtEmptyScope() {
        noErrors("{}", STATEMENT);
    }

    @Test
    public void testStmtIfElse() {
        noErrors("if(a){ifStmt1;ifStmt2;}else{elseStmt1;elseStmt2;}", STATEMENT);
    }

    @Test
    public void testStmtIfElseWithoutBrackets() {
        noErrors("if(a)ifStmt;else elseStmt;", STATEMENT);
    }

    @Test
    public void testStmtWhile() {
        noErrors("while(a){whileStmt1;whileStmt2;}", STATEMENT);
    }

    @Test
    public void testStmtWhileWithoutBrackets() {
        noErrors("while(a)whileStmt1;", STATEMENT);
    }

    @Test
    public void testStmtAssign() {
        noErrors("a=b;", STATEMENT);
    }

   /* @Test
    public void testIdentAssignInt() {
        noErrors("int c;","VariableDeclaration");
    }

    @Test
    public void testIdentAssign() {
        noErrors("AGbc c;","VariableDeclaration");
    }
*/
    @Test
    public void testIdentAssignInMethod() {
        noErrors("){AGbc c; a[1][2];a[b.length[1] && a < b]=1; b=1;}","MethodBody");
    }

    @Test
    public void testStmtArrayAssign() {
        noErrors("anArray[a]=b;", STATEMENT);
    }

    @Test
    public void testStmtArrayExpr() {
        noErrors("anArray[a][1][2];", STATEMENT);
    }


    @Test
    public void testExprTrue() {
        noErrors("true", EXPRESSION);
    }

    @Test
    public void testExprFalse() {
        noErrors("false", EXPRESSION);
    }

    @Test
    public void testExprThis() {
        noErrors("this", EXPRESSION);
    }

    @Test
    public void testExprId() {
        noErrors("a", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral() {
        noErrors("10", EXPRESSION);
    }

    @Test
    public void testExprParen() {
        noErrors("(10)", EXPRESSION);
    }

    @Test
    public void testExprMemberCall() {
        noErrors("foo.bar(10, a, true)", EXPRESSION);
    }

    @Test
    public void testExprMemberCallChain() {
        noErrors("callee.level1().level2(false, 10).level3(true)", EXPRESSION);
    }


    @Test
    public void testExprLength() {
        noErrors("a.length", EXPRESSION);
    }

    @Test
    public void testExprLengthChain() {
        noErrors("a.length.length", EXPRESSION);
    }

    @Test
    public void testArrayAccess() {
        noErrors("a[10]", EXPRESSION);
    }

    @Test
    public void testArrayAccessChain() {
        noErrors("a[10][20]", EXPRESSION);
    }

    @Test
    public void testParenArrayChain() {
        noErrors("(a)[10]", EXPRESSION);
    }

    @Test
    public void testCallArrayAccessLengthChain() {
        noErrors("callee.foo()[10].length", EXPRESSION);
    } 

    @Test
    public void testExprNot() {
        noErrors("!true", EXPRESSION);
    }

    @Test
    public void testExprNewArray() {
        noErrors("new int[!a]", EXPRESSION);
    }

    @Test
    public void testExprNewClass() {
        noErrors("new Foo()", EXPRESSION);
    }

    @Test
    public void testExprMult() {
        noErrors("2 * 3", EXPRESSION);
    }

    @Test
    public void testExprDiv() {
        noErrors("2 / 3", EXPRESSION);
    }

    @Test
    public void testExprMultChain() {
        noErrors("1 * 2 / 3 * 4", EXPRESSION);
    }

    @Test
    public void testExprAdd() {
        noErrors("2 + 3", EXPRESSION);
    }

    @Test
    public void testExprSub() {
        noErrors("2 - 3", EXPRESSION);
    }

    @Test
    public void testExprAddChain() {
        noErrors("1 + 2 - 3 + 4", EXPRESSION);
    }

    @Test
    public void testExprRelational() {
        noErrors("1 < 2", EXPRESSION);
    }

    @Test
    public void testExprRelationalChain() {
        noErrors("1 < 2 < 3 < 4", EXPRESSION);
    }

    @Test
    public void testExprLogical() {
        noErrors("1 && 2", EXPRESSION);
    }

    @Test
    public void testExprLogicalChain() {
        noErrors("1 && 2 && 3 && 4", EXPRESSION);
    }

    @Test
    public void testExprChain() {
        noErrors("1 && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }

    @Test
    public void testExprChainVariable() {
        noErrors("a && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }


    @Test
    public void testExprChainVariable1() {
        noErrors("a[1] && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }

    @Test
    public void testExprChainVariable2() {
        noErrors("a[1][2] && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }
    @Test
    public void testExprChainVariableAnd() {
        noErrors("a[1][2] && 2 < 3 + a[1][2].length - 5 * 6 / 7;", "Statement");
    }

    @Test
    public void testExprChainVariableLessWeird() {
        noErrors("a.length[2] < 3 + 4 - 5 * 6 / 7;", "Statement");
    }

    @Test
    public void testExprChainVariableLess() {
        noErrors("a[1][2][3][4][5].length[2] < 3 + 4 - 5 * 6 / 7;", "Statement");
    }

    @Test
    public void testExprChainVariableLess2() {
        noErrors("a < 3 + 4 - 5 * 6 / 7;", "Statement");
    }

    @Test
    public void testArrayExpr() {
        noErrors("a[1][2]", EXPRESSION);
    }

    @Test
    public void testArrayStatement() {
        noErrors("a[1][2][3][4][5];", STATEMENT);
    }

    @Test
    public void testTicTacToe(){
        noErrors("class Fac{\n" +
                "    public int compFac(int num){\n" +
                "        int num_aux;\n" +
                "        if(true){\n" +
                "            num_aux = 1;\n" +
                "        }\n" +
                "        else{\n" +
                "            num_aux = num * (this.compFac(num-1));}\n" +
                "            return num_aux;\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "    public static void main (String [] args){\n" +
                "        io.println(new Fac().compFac(10));\n" +
                "    }\n" +
                "    \n" +
                "}\n");
    }
}
