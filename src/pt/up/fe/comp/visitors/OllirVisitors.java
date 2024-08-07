package pt.up.fe.comp.visitors;

import java_cup.runtime.symbol;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.MutableSymbol;
import pt.up.fe.comp.ollir.OllirUtils;
import pt.up.fe.comp.visitors.utils.Utils;

import java.util.*;

import static pt.up.fe.comp.jmm.report.ReportType.ERROR;
import static pt.up.fe.comp.jmm.report.Stage.SEMANTIC;

public class OllirVisitors extends AJmmVisitor<MutableSymbol, Integer> {
    public final String VARIABLE = "t";
    private final SymbolTable symbolTable;
    private final StringBuilder result;
    private int auxVariable = 0; //Reset by method
    private int auxLabel = 0; //No reset by method
    private ArrayList<String> classVariables = new ArrayList<>();
    private ArrayList<String> methodVariables = new ArrayList<>();
    private ArrayList<String> method_args = new ArrayList<>();
    private String curmethod;
    private String test = "";

    public OllirVisitors(SymbolTable symbolTable) {
        this.result = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit("Start", this::startVisit);
        addVisit("ClassDeclaration", this::classVisit);
        addVisit("MethodDeclarationPublic", this::visitMethods);
        addVisit("MainMethod", this::visitMainMethod);
        addVisit("Statement", this::visitStatement);
        addVisit("ExpressionIndependent", this::visitExpressionIndependent);
        addVisit("ExpressionIndependent2", this::visitExpressionIndependent);

        addVisit("IfStatement", this::visitIfStatement);

        addVisit("IfBody", this::visitIfBody);
        addVisit("WhileCondition", this::visitWhileCondition);
        addVisit("WhileBody", this::visitWhileBody);
        addVisit("ElseBody", this::visitElseBody);
        addVisit("WhileStatement", this::visitWhileStatement);
        addVisit("Variable", this::visitVariable);
        addVisit("BinOp", this::visitBinOp);
    }

    public String getVariable() {
        auxVariable++;
        return VARIABLE + auxVariable;
    }

    public String getLabel() {
        auxLabel++;
        return "LABEL" + auxLabel;
    }

    public String getCode() {
        return result.toString();
    }

    public Integer startVisit(JmmNode start, MutableSymbol symbol) {
        for (String i : this.symbolTable.getImports()) {
            result.append("import " + i + ";\n");
        }
        result.append("\n");

        for (JmmNode child : start.getChildren()) {
            visit(child);
        }
        return 0;
    }

    public Integer classVisit(JmmNode node, MutableSymbol data) {
        result.append(OllirUtils.classIntroduction(symbolTable.getClassName(), symbolTable.getSuper()));


        for (JmmNode child : node.getChildren()) {
            //Need to see the class variables first
            if (child.getKind().equals("VariableDeclaration")) {
                visitClassVar(child);
            }
        }

        result.append(OllirUtils.classConstructor(symbolTable.getClassName()));
        result.append(OllirUtils.closeBrackets());

        //Get Methods
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("MethodDeclarationPublic")) {
                visit(child);
                result.append(OllirUtils.closeBrackets());
            }
        }
        result.append(OllirUtils.closeBrackets());
        return 0;
    }

    public void visitClassVar(JmmNode node) {
        Symbol s = Utils.getSymbol(node);
        result.append(OllirUtils.field(s));
    }

    public Integer visitMainMethod(JmmNode node, MutableSymbol symbol) {
        result.append(OllirUtils.mainMethod());
        return 0;
    }

    public Integer visitMethods(JmmNode node, MutableSymbol symbol) {
        boolean isMain = false;
        String type = "";
        method_args = new ArrayList<>();
        auxVariable = 0;
        boolean hasArguments = false;
        /*var methodSignature = node.getJmmChild(1).get("name");
        var params=symbolTable.getParameters(methodSignature);
        var paramCode = params.stream().map(symbol -> OllirUtils.getVariableType(symbol.getType())).collect(Collectors.joining(","));
        result.append(paramCode);

        result.append(OllirUtils.getVariableType(symbolTable.getReturnType(methodSignature)));*/


        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("MainMethod")) {
                isMain = true;
                hasArguments = true;
                curmethod = "main";
                visit(child);
            } else if (child.getKind().equals("Type")) {
                type = child.get("op");
            } else if (child.getKind().equals("Idntf")) {
                curmethod = child.get("value");
            } else if (child.getKind().equals("MethodArguments")) {
                hasArguments = true;
                List<String> args = visitMethodArgs(child);
                method_args.addAll(args);
                result.append(OllirUtils.method(curmethod, method_args, type));
            } else {
                if (!hasArguments) {
                    result.append(OllirUtils.method(curmethod, null, type));
                }
                visitMethodBody(child, type); //MethodBody
            }
        }
        if (type.equals("void") || curmethod.equals("main")) {
            result.append("ret.V;");
        }

        return 0;
    }

    public Integer visitType(JmmNode node, MutableSymbol symbol) {
        return 0;
    }

    public String visitMethodArg(JmmNode node) {
        String arg = "", type = "", idntf = "";
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("Type")) {
                type = child.get("op");
            }
            if (child.getKind().equals("Idntf")) {
                idntf = child.get("value");
            }
        }
        arg = OllirUtils.methodArg(type, idntf);
        return arg;
    }

    public Integer visitMethodBody(JmmNode node, String returnType) {
        /*
         * Children:
         * Statement
         * */

        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("ReturnStatement")) {
                visitReturnStatement(child);

            }
            if (child.getKind().equals("VariableDeclaration")) {
                // visitClassVar(child);
            }
            if (child.getKind().equals("Statement")) {
                visitStatement(child, null);
            }
        }
        return 0;
    }

    private void visitReturnStatement(JmmNode node) {
        MutableSymbol symbol = new MutableSymbol(getVariable());
        JmmNode child = node.getChildren().get(0);
        auxVisitExpression(child, symbol);
        Boolean isArray = symbolTable.getReturnType(curmethod).isArray();
        String bridge = "";
        if (isArray) {
            bridge = ".array";
        }
        String type = OllirUtils.getVariableType(symbol.getType());
        String string = "";
        if (type.equals(".array.i32")) {
            string = "ret" + type + " ";
            string += symbol.getName() + type + ";";
        } else {
            string = "ret" + bridge + type + " ";//type somehow
            string += symbol.getName() + bridge + type + ";";
        }

        result.append(string);
    }

    public List<String> visitMethodArgs(JmmNode node) {
        List<String> args = new ArrayList<>();
        for (JmmNode child : node.getChildren()) {
            args.add((visitMethodArg(child)));
        }
        return args;
    }

    public Integer visitStatement(JmmNode node, MutableSymbol symbol) {
        /*
         * Children:
         * ExpressionIndependent2
         * IfStatement
         * Statement
         * WhileStatement
         * UnaryOp (for new)
         * */
        for (JmmNode child : node.getChildren()) {

            if (child.getKind().equals("Statement")) {
                visitStatement(child, symbol);
            } else if (child.getKind().equals("IfStatement")) {
                visitIfStatement(child, symbol);
            } else if (child.getKind().equals("ExpressionIndependent") || child.getKind().equals("ExpressionIndependent2")) {
                visitExpressionIndependent(child, symbol);
            } else if (child.getKind().equals("WhileStatement")) {
                visitWhileStatement(child, symbol);
            } else if (child.getKind().equals("VariableDeclaration")) {
                //visitClassVar(child);
            }
        }
        return 0;
    }

    public Integer visitExpressionIndependent(JmmNode node, MutableSymbol symbol) {
        List<JmmNode> children = node.getChildren();
        JmmNode child = children.get(0);

        // First two can have more children
        if (child.getKind().equals("ExpressionIndependent") || child.getKind().equals("ExpressionIndependent2")) {
//            visitExpressionIndependent(child, symbol);


            // Trial code
            if (children.size() > 1) {
                Type specialAssignType = new Type("void", false);
                if (symbol != null) {
                    specialAssignType = symbol.getType();
                }
                MutableSymbol prevSymbol = new MutableSymbol(getVariable());
                visitExpressionIndependent(child, prevSymbol); // Visit Identifier sets the name
                MutableSymbol iterativeSymbol;

                for (int i = 1; i < children.size(); i++) {
                    iterativeSymbol = new MutableSymbol(getVariable());
                    iterativeSymbol.setType(specialAssignType);
                    visitUnaryOp(children.get(i), prevSymbol, iterativeSymbol, symbol!=null);
                    prevSymbol = iterativeSymbol;
                }
//                symbol.setSymbol(prevSymbol);
//                symbol.setType(prevSymbol.getType());
//                String string = symbol.getOllir() + ":="
//                        + OllirUtils.getVariableType(prevSymbol.getType().getName()) + " "
//                        + prevSymbol.getOllir() + ";\n";

                if (symbol != null) {
                    String string = symbol.getOllir() + " :="
                            + OllirUtils.getVariableType(symbol.getType().getName()) + " "
                            + prevSymbol.getOllir() + ";\n";
                    result.append(string);
                }
//                result.append(string);
            } else {
                visitExpressionIndependent(child, symbol);
            }
        } else if (child.getKind().equals("Idntf")) {
            if (children.size() > 1) {
                Type specialAssignType = new Type("void", false);
                if (symbol != null) {
                    specialAssignType = symbol.getType();
                }
                MutableSymbol prevSymbol = new MutableSymbol(getVariable());
                visitIdentifier(child, prevSymbol); // Visit Identifier sets the name
                MutableSymbol iterativeSymbol;

                for (int i = 1; i < children.size(); i++) {
                    iterativeSymbol = new MutableSymbol(getVariable());
                    iterativeSymbol.setType(specialAssignType);
                    visitUnaryOp(children.get(i), prevSymbol, iterativeSymbol, symbol!=null);

//                    if(prevSymbol.getType().getName().equals(symbolTable.getClassName())){
//                        //Using a this, we know the type
//                        visitUnaryOp(children.get(i), prevSymbol, iterativeSymbol);
//                    }
//                    else{
//                        // Using an import, we will only know the type because we assume that there is no chaining
//                        // And the type is that of the variable being assigned (in the symbol)
//                        visitUnaryOp(children.get(i), prevSymbol, symbol);
//                    }
                    prevSymbol = iterativeSymbol;
                }
//                if (prevSymbol.getType().equals(new Type("void", false))) {
//                    prevSymbol.setSymbol(symbol);
//                } else {
//                    symbol.setSymbol(prevSymbol);
//                }
//                String string = symbol.getOllir() + " :="
//                        + OllirUtils.getVariableType(prevSymbol.getType().getName()) + " "
//                        + prevSymbol.getOllir() + ";\n";

//                result.append(string);
                if (symbol != null) {
                    symbol.setSymbol(prevSymbol);
                    String string = symbol.getOllir() + " :="
                            + OllirUtils.getVariableType(symbol.getType()) + " "
                            + prevSymbol.getOllir() + ";\n";
                    result.append(string);
                }

            } else {
                visitIdentifier(child, symbol);
//                String string = symbol.getOllir() + " :="
//                        + OllirUtils.getVariableType(symbol.getType().getName()) + " "
//                        + symbol.getOllir() + ";\n";
//                result.append(string);
            }
        }
        // Can only have one child
        else if (child.getKind().equals("BinOp")) {
            visitBinOp(child, symbol); //Puts the type in the symbol already
        } else if (child.getKind().equals("IntegerLiteral")) {
            String value = child.get("value");
            symbol.setType(new Type("int", false));
            result.append(symbol.getOllir() + " :=.i32 " + value + ".i32;\n");
        } else if (child.getKind().equals("UnaryOp")) {
            visitUnaryOp(child, null, symbol, null); // DEBUG
        }

        return 0;//The temp or named variable that is the "return" of the expression independent
    }

    public String visitSuffixArguments(JmmNode node, MutableSymbol symbol) {
        StringBuilder stringBuilder = new StringBuilder();
        MutableSymbol symbolArg;
        stringBuilder.toString();
        List<String> strings = new ArrayList<>();
        List<MutableSymbol> symbols = verifyMethod(node, symbol);
        for (int i = 0; i < node.getChildren().size(); i++) { //we can have more than one argument
            JmmNode child = node.getChildren().get(i);
            symbolArg = new MutableSymbol(getVariable());
            symbolArg.setType(symbols.get(i).getType());
            auxVisitExpression(child, symbolArg);

            strings.add(symbolArg.getOllir());
        }
        String result = String.join(", ", strings);
        return result;//concat the string with ","
    }

    public Integer visitUnaryOp(JmmNode node, MutableSymbol prevSymbol, MutableSymbol symbol, Boolean assign) {
        /*
        array
        point
        new*/
        String op = node.get("op");
        List<String> variablesNames = new ArrayList<String>();
        List<Type> variablesTypes = new ArrayList<Type>();
        for (Symbol local : symbolTable.getLocalVariables(curmethod)) {
            variablesNames.add(local.getName());
            variablesTypes.add(local.getType());
        }
        JmmNode child = node.getChildren().get(0);
        if (op.equals("!")) {
            // boolean negation
            symbol.setType(new Type("boolean", false));
            MutableSymbol symbol2 = new MutableSymbol(getVariable());
            visitExpressionIndependent(child, symbol2);
            String falseLabel = getLabel(), endLabel = getLabel();
            // We can replace this by an xor when we actually know how to do that
            String negative = "if (" + symbol2.getOllir() + ") goto " + falseLabel + ";\n";
            negative += symbol2.getOllir() + ":=.bool 1.bool;\n goto " + endLabel + ";\n";
            negative += falseLabel + ":\n" + symbol2.getOllir() + ":=.bool 0.bool;\n";
            negative += endLabel + ":\n";
            symbol.setSymbol(symbol2);
            symbol.setName(symbol2.getName());
            result.append(negative);
        } else if (op.equals("point")) {
            String method = child.get("op");
            //Boolean isThis = (prevSymbol.getType().getName().equals(symbolTable.getClassName())) || (method.equals(curmethod));
            Boolean isThis = !(symbolTable.getImports().contains(prevSymbol.getName()));
            String parameters = "";
            if (method.equals("length")) { //arraylength($1.A.array.i32).i32
                symbol.setType(new Type("int", false));
                String string = symbol.getOllir() + " :=" + ".i32 " + OllirUtils.arraylength(prevSymbol.getName() + "." + prevSymbol.getType().getName());
                symbol.setType(new Type("int", false));
                result.append(string + ";\n");
            } else {
                if (child.getChildren().size() > 0) {
                    parameters = visitSuffixArguments(child.getChildren().get(0), prevSymbol);
                }
                String string = "";
                if (isThis) {
                    //method
                    Type returnType;
                    if ((prevSymbol.getType().getName().equals(symbolTable.getClassName())) || (method.equals(curmethod))) {
                        returnType = symbolTable.getReturnType(method);
                        symbol.setType(returnType);
                        int index = variablesNames.indexOf(prevSymbol.getName());
                        if (index != -1) {
                            string += OllirUtils.invokevirtual(prevSymbol.getName(), method, returnType, parameters, variablesTypes.get(index).getName());
                        } else {
                            string += OllirUtils.invokevirtual(prevSymbol.getName(), method, returnType, parameters, symbolTable.getClassName());
                        }
                    } else {
                        returnType = symbol.getType();
                        symbol.setType(returnType);
                        int index = variablesNames.indexOf(prevSymbol.getName());
                        if (index != -1) {
                            string += OllirUtils.invokevirtual(prevSymbol.getName(), method, returnType, parameters, variablesTypes.get(index).getName());
                        } else {
                            string += OllirUtils.invokevirtual(prevSymbol.getName(), method, returnType, parameters, symbol.getType().getName());

                        }
                    }

                    //need to put symbolTable.getLocalVariables(curmethod) get names in a list and then check if prevSymbol.getName() is there if so get the type of that variable
                    //symbol.setType(returnType);
                    //a = Import.method();
//                    MyImport.method().length;
//                    invokevirtual().array.i32;

                    //t1.i32:=.i32 arraylength($1.A.array.i32).i32; int[A.length]
                    // Probably here add assignment to the incoming symbol
                    //string += OllirUtils.invokevirtual(prevSymbol.getName(), method, returnType, parameters, symbolTable.getClassName());
                } else {
                    // function
                    // Probably here add asignment to the incoming symbol
                    //if(!prevSymbol.getType().getName().equals("void")){
                    //  string += OllirUtils.invokestatic(prevSymbol.getName() +"."+ prevSymbol.getType().getName() , method, symbol.getType(), parameters);
                    //}
                    //else{
                    string += OllirUtils.invokestatic(prevSymbol.getName(), method, symbol.getType(), parameters);
                    //}
                }
                // Was null but now we will need to assign because we actually know the type
                if (assign) {
                    string = symbol.getOllir() + " :=" + OllirUtils.getVariableType(symbol.getType()) + " " + string;
                }
                result.append(string);
            }
            /*invokestatic-->functions
             * invokevirtual-->methods*/


        } else if (op.equals("new")) {
            String className = child.get("name");
            if (className.equals("int array")) {
                MutableSymbol index = new MutableSymbol(getVariable());
                auxVisitExpression(node.getChildren().get(1), index);
                symbol.setType(new Type("int", true));
                symbol.setIndex(index);
                String string = test + ".array :=.array " + OllirUtils.newArray(index.getName());
                test = "";
                result.append(string + ";\n");
            } else {
                symbol.setType(new Type(child.get("name"), false));


                result.append(symbol.getOllir() + " :=." + className + " " + OllirUtils.newObject(className) + ";\n");
                result.append("invokespecial(" + symbol.getOllir() + ",\"<init>\").V;\n");
            }

        } else if (op.equals("array")) {
            MutableSymbol index = new MutableSymbol(getVariable());
            auxVisitExpression(child, index);
            prevSymbol.setIndex(index);
            symbol.setType(new Type(prevSymbol.getType().getName(), false));
            String string = symbol.getOllir() + " :=" + OllirUtils.getVariableType(symbol.getType().getName())
                    + " " + prevSymbol.getOllir();
            result.append(string + "\n;");
        }
        return 0;
    }

    public Integer visitBinOp(JmmNode node, MutableSymbol symbol) {
        /*assign
         * mult
         * div
         * add
         * sub
         * LessThan
         * and
         * */





        List<JmmNode> children = node.getChildren();
        JmmNode leftExp = children.get(0);
        JmmNode rightExp = children.get(1);
//        auxVisitExpression(children.get(0), symbol);
        MutableSymbol left = new MutableSymbol(getVariable());
        if (leftExp.getKind().equals("Variable")) {
            left.setName(leftExp.getChildren().get(0).get("name"));
        }
        MutableSymbol right = new MutableSymbol(getVariable());
        String op = node.get("op");
        String string = "";
        if (op.equals("assign")) {
            //Symbol is irrelevant because assignment is statement level

            Integer isField = visitVariable(leftExp, left);
            if (left.getType().isArray()) {
                test = left.getName();
            }
            right.setType(left.getType());
            auxVisitExpression(rightExp, right);
            if (isField == 1) {
                // left -> this.nameOfField
                string = OllirUtils.putfield(left, right.getOllir());
            } else {
                if(rightExp.getChildren().get(0).getNumChildren() != 0){
                   if(leftExp.getChildren().get(0).getKind().equals("Idntf") && rightExp.getChildren().get(0).getChildren().get(0).getKind().equals("Idntf")){
                       if(leftExp.getChildren().get(0).get("name").equals(rightExp.getChildren().get(0).getChildren().get(0).get("name"))){
                           return 0;
                       }
                   }
                }
                if (left.getType().isArray() && rightExp.getChildren().get(0).getKind().equals("IntegerLiteral")) {
                    string = left.getOllir() + " :=" + OllirUtils.getVariableType(left.getType().getName()) + " " + right.getOllir();
                } else if (!(left.getType().isArray())) {
                    string = left.getOllir() + " :=" + OllirUtils.getVariableType(left.getType().getName()) + " " + right.getOllir();
                }
            }
        } else {

            if (op.equals("and")) {
                left.setType(new Type("boolean", false));
                right.setType(new Type("boolean", false));
            } else if (op.equals("Lessthan") || op.equals("mult") || op.equals("div") || op.equals("add") || op.equals("sub")) {
                left.setType(new Type("int", false));
                right.setType(new Type("int", false));
            }

            auxVisitExpression(leftExp, left);
            auxVisitExpression(rightExp, right);

            if (op.equals("and")) {
                symbol.setType(new Type("boolean", false));
                string = symbol.getOllir() + " :=.bool " + left.getOllir() + " &&.bool " + right.getOllir();
            } else if (op.equals("Lessthan")) {
                symbol.setType(new Type("boolean", false));
                string = symbol.getOllir() + " :=.bool " + left.getOllir() + " <.bool " + right.getOllir();
            } else if (op.equals("mult") || op.equals("div") || op.equals("add") || op.equals("sub")) {
                String stringOp;
                if (op.equals("mult")) {
                    stringOp = "*";
                } else if (op.equals("div")) {
                    stringOp = "/";
                } else if (op.equals("add")) {
                    stringOp = "+";
                } else {
                    stringOp = "-";
                }

                if (children.get(0).getChildren().get(0).getKind().equals("Idntf")) {
                    if(children.get(0).getJmmParent().getKind().equals("BinOp")){
                        if(children.get(0).getJmmParent().get("op").equals("add")){
                            if(children.get(0).getJmmParent().getChildren().get(0).getChildren().get(0).getKind().equals("Idntf")){
                                if(children.get(0).getChildren().get(0).get("name").equals(children.get(0).getJmmParent().getChildren().get(0).getChildren().get(0).get("name"))){
                                    if (rightExp.getChildren().get(0).getKind().equals("IntegerLiteral") && rightExp.getChildren().get(0).get("value").equals("1")){
                                        string = children.get(0).getJmmParent().getChildren().get(0).getChildren().get(0).get("name")+".i32" + " :=.i32 " + left.getOllir() + " " + stringOp + ".i32 " + "1.i32";
                                        result.append(string+";\n");
                                        return 0;
                                    }
                                }
                            }
                        }
                    }
                }
                symbol.setType(new Type("int", false));
                string = symbol.getOllir() + " :=.i32 " + left.getOllir() + " " + stringOp + ".i32 " + right.getOllir();

            }
        }
        if (!(string.equals(""))) {
            result.append(string + ";\n");
        }
        return 0;
    }

    public Integer visitElseBody(JmmNode node, MutableSymbol symbol) {
        /*else: ...*/

        return 0;
    }

    public Integer visitWhileStatement(JmmNode node, MutableSymbol symbol) {
        /*
         * Children:
         * WhileCondition
         * WhileBody*/
//        code 1= cond
//            code 2 = body
//        return [LABEL label1] ++code1
//                ++ [LABEL label2] ++code2
//                ++ [JUMP label1, LABEL label3
        if (node.getChildren().size() >= 2) {
            List<JmmNode> children = node.getChildren();
            String label1 = getLabel();
            String label2 = getLabel();
            String label3 = getLabel();
            result.append(label1 + ":\n");
            visitCondition(children.get(0), label2, label3);
            result.append(label2 + ":\n");
            visitStatement(children.get(1).getChildren().get(0), null);
            result.append("goto " + label1 + ";\n" + label3 + ":\n");
            return 0;
        }
        return 0;
    }

    public Integer visitWhileBody(JmmNode node, MutableSymbol symbol) {
        return 0;
    }

    public Integer visitWhileCondition(JmmNode node, MutableSymbol symbol) {
        return 0;
    }

    public Integer visitIfBody(JmmNode node, MutableSymbol symbol) {
        return 0;
    }

    public Integer visitIfStatement(JmmNode node, MutableSymbol symbol) {
        /*
         * Children:
         * IfCondition
         * IfBody
         * ElseBody*/

        /*if (...) goto else
        * ....
        goto endif*/
        List<JmmNode> children = node.getChildren();
        String label1 = getLabel();
        String label2 = getLabel();
        String label3 = getLabel();
        visitCondition(children.get(0), label1, label2);
        result.append(label1 + ":\n");
        // do if body
        visitStatement(children.get(1).getChildren().get(0), null);
        result.append("goto " + label3 + ";\n");
        result.append(label2 + ":\n");
        // do else body
        if (children.size() > 2) {
            visitStatement(children.get(2).getChildren().get(0), null);
        } else if (children.size() == 2) {
            visitStatement(children.get(1).getChildren().get(0), null);
        }
        result.append(label3 + ":\n");
//        visitCondition(node, symbol);
        return 0;
    }

    public void visitCondition(JmmNode node, String trueLabel, String falseLabel) {
        MutableSymbol symbol = new MutableSymbol(getVariable());
        symbol.setType(new Type("boolean", false));
        auxVisitExpression(node.getChildren().get(0), symbol);
        String string = "if (" + symbol.getOllir() + ") goto " + trueLabel + ";\n";
        string += "goto " + falseLabel + ";\n";
        result.append(string);
    }

    public Integer visitIfCondition(JmmNode node, String label1, String label2) {
        MutableSymbol symbol = new MutableSymbol(getVariable());
        auxVisitExpression(node, symbol);
        List<JmmNode> children = node.getChildren();
        JmmNode child = children.get(0);
        String childKind = child.getKind();
        if (childKind.equals("BinOp")) {

        }

        // Ha 3 casos
        // E uma bin op que da return a um boolean (and ou menor ou igual)
        // E uma expressao com um valor inteiro (acho qie isto nao pode acontecer)?
        // E uma expressao com um valor booleano
        // if condition go to label 1;
        //go to label 2;
        result.append("goto " + label2 + ";");
        return 0;
    }

    private void auxVisitExpression(JmmNode node, MutableSymbol symbol) {
        Type type = new Type("", false);
        String kind = node.getKind();
        if (kind.equals("ExpressionIndependent") || kind.equals("ExpressionIndependent3")) {
            visitExpressionIndependent(node, symbol);
        } else if (kind.equals("BinOp")) {
            visitBinOp(node, symbol);
        } else if (kind.equals("ExpressionIndependent3")) {
            type = visitObject(node, symbol);
        } else if (kind.equals("Variable")) {
            visitExpressionIndependent(node, symbol);
        }
    }

    private Type visitObject(JmmNode node, MutableSymbol symbol) {
        return new Type("void", false);
    }


    public Integer visitVariable(JmmNode node, MutableSymbol symbol) {
        Type type;
        Integer isField = 0;
        if (node.getChildren().size() == 1) {
            String idntf = node.getChildren().get(0).get("name");
            type = verifySymbol(symbolTable, symbol, idntf, curmethod);
            if (symbol.getName().equals("this." + idntf)) {
                isField = 1;
                symbol.setName(idntf);
            }
        } else { //array
            String idntf = node.getChildren().get(0).get("name");
            type = verifySymbol(symbolTable, symbol, idntf, curmethod);
            MutableSymbol index = new MutableSymbol(getVariable());
            auxVisitExpression(node.getChildren().get(1).getChildren().get(0), index);
            symbol.setIndex(index);
        }
        symbol.setType(type);
        return isField;
    }

    public void visitCondition(JmmNode node, MutableSymbol symbol) {
        List<JmmNode> children = node.getChildren();
        JmmNode condition = children.get(0);
        JmmNode body = children.get(1);

        visitExpressionIndependent(condition, symbol);

        visitStatement(body, symbol);

    }

    public Integer visitIdentifier(JmmNode node, MutableSymbol symbol) {
        String identifierName = node.get("name");

        if (identifierName.equals("this")) {
            symbol.setType(new Type(symbolTable.getClassName(), false));
            symbol.setName("this"); //TODO See if this doesn't mean we have variable assignment with "this" and just method calls
            // do something
        } else if (identifierName.equals("true") || identifierName.equals("false")) {
            symbol.setType(new Type("boolean", false));
            String value = identifierName.equals("true") ? "1" : "0";
            String string = symbol.getOllir() + " :=" + OllirUtils.getVariableType("boolean") + " " + value + OllirUtils.getVariableType("boolean") + ";\n";
            result.append(string);
        } else {
            MutableSymbol newSymbol = new MutableSymbol("");
            Type type = verifySymbol(symbolTable, newSymbol, identifierName, curmethod);
            symbol.setType(type);
            newSymbol.setType(type);
            if (newSymbol.getName().equals("this." + identifierName)) {
                newSymbol.setName(identifierName);
                String string = symbol.getOllir() + ":=" + OllirUtils.getVariableType(symbol.getType()) + " " + OllirUtils.getfield(newSymbol);
                result.append(string);
//                say that symbol variable = to the get field?
            } else {
                symbol.setSymbol(newSymbol);
                symbol.setName(newSymbol.getName());
            }
        }
        return 0;
    }

    public static Type verifySymbol(SymbolTable symbolTable, MutableSymbol symbol, String symbolName, String curMethod) {
        List<Symbol> locals = symbolTable.getLocalVariables(curMethod);
        Optional<Symbol> localIdentifier, paramIdentifier, attributeIdentifier;
        Optional<String> importIdentifier;

        localIdentifier = locals.stream().filter(syl -> syl.getName().equals(symbolName)).findFirst();
        if (localIdentifier.isPresent()) {
            symbol.setName(symbolName);
            return localIdentifier.get().getType();
        } else {
            List<Symbol> params = symbolTable.getParameters(curMethod);
            paramIdentifier = params.stream().filter(syl -> syl.getName().equals(symbolName)).findFirst();
            if (paramIdentifier.isPresent()) {
                Integer paramIndex = params.indexOf(paramIdentifier.get()) + 1;
                String paramIndexStr = paramIndex.toString();
                //symbol.setName("$" + paramIndexStr + "." + symbolName);
                symbol.setName(symbolName);
                return paramIdentifier.get().getType();
            } else {
                if (!curMethod.equals("main")) {
                    // If is not main look in the class attributes
                    List<Symbol> attributes = symbolTable.getFields();
                    attributeIdentifier = attributes.stream().filter(syl -> syl.getName().equals(symbolName)).findFirst();
                    if (attributeIdentifier.isPresent()) {
                        symbol.setName("this." + symbolName);
                        return attributeIdentifier.get().getType();
                    }
                }
                List<String> imports = symbolTable.getImports();
                if (imports.size() != 0) {
                    importIdentifier = imports.stream().filter(importDecl -> {
                        List<String> paths = Arrays.asList(importDecl.split("\\."));
                        return paths.get(paths.size() - 1).equals(symbolName);
                    }).findFirst();
                    if (importIdentifier.isPresent()) {
                        symbol.setName(symbolName);
                        return new Type("void", true);
                    }
                }
            }
        }
        return new Type("void", false);
    }


    public List<MutableSymbol> verifyMethod(JmmNode node, MutableSymbol symbol) {
        List<String> methods = symbolTable.getMethods();
        MutableSymbol newMut;
        List<MutableSymbol> mutableParameters = new ArrayList<>();
        if (symbol.getType().getName().equals(symbolTable.getClassName())) {
            List<Symbol> parameters = symbolTable.getParameters(node.getJmmParent().get("op"));
            for (int i = 0; i < parameters.size(); i++) {
                newMut = new MutableSymbol("");
                newMut.setType(parameters.get(i).getType());
                mutableParameters.add(newMut);

            }

        } else {
            for (int i = 0; i < node.getChildren().size(); i++) {
                newMut = new MutableSymbol(getVariable());
                mutableParameters.add(newMut);
            }
            // Need to go by the one that we receive from the outside
            // List of void with the number of the node's children
        }
        return mutableParameters;
    }
}
