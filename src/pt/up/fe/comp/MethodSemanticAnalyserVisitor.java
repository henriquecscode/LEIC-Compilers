package pt.up.fe.comp;

import java_cup.runtime.symbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.Stage.*;
import pt.up.fe.comp.visitors.MethodExpr;
import pt.up.fe.specs.util.SpecsCollections;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.System.*;
import static pt.up.fe.comp.jmm.report.ReportType.ERROR;
import static pt.up.fe.comp.jmm.report.Stage.SEMANTIC;

public class MethodSemanticAnalyserVisitor extends PreorderJmmVisitor<Boolean, Boolean> {
    private SymbolTable symbolTable;
    public List<Report> reports;
    private String curMethod;

    public MethodSemanticAnalyserVisitor(SymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        addVisit("MethodDeclarationPublic", this::visitMethod);
    }

    private Type visitExpression(JmmNode node) {
        Type type = new Type("", false);
        String kind = node.getKind();
        if (kind.equals("ExpressionIndependent")) {
            type = visitExpressionIndependent(node);
        } else if (kind.equals("BinOp")) {
            type = visitBinOp(node);
        } else if (kind.equals("ExpressionIndependent3")) {
            type = visitObject(node);
        } else if (kind.equals("Variable")) {
            type = visitExpressionIndependent(node);
        } else {
            if (node.get("op") != null) {
                return new Type(node.get("op"), false);
            }
        }
        return type;
    }

    private Type visitExpressionIndependent(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        JmmNode child = children.get(0);
        String childKind = child.getKind();
        Type expressionType = new Type("", false);

        if (childKind.equals("ExpressionIndependent") || childKind.equals("ExpressionIndependent2")) {
            expressionType = visitExpressionIndependent(child);
        } else if (childKind.equals("BinOp")) {
            // Length case.
            expressionType = visitBinOp(child);
        } else if (childKind.equals("Idntf")) {
            expressionType = visitIdenfifier(node);
//            Do stuff with identifier
        } else if (childKind.equals("ExpressionIndependent3")) {
            expressionType = visitObject(node);
        } else if (childKind.equals("IntegerLiteral")) {
            expressionType = new Type("int", false);
        } else if (childKind.equals("UnaryOp")) {
            expressionType = visitUnaryOp(child, new Type("", false), false);
//            if (child.get("op").equals("!")) {
//                expressionType = visitUnaryOp(child, new Type("",false), false);
//            } else {
//                // Inside an expression, there can never be an UnaryOp that isn't exclamation.
//                System.out.println("Inside an Expression, there can never be an UnaryOp that isn't exclamation.");
//                //exception
//            }

        } else {
            // Unexpected child node in expression
            //exception
            System.out.println("Unexpected child node in Expression.");
        }
        return expressionType;
    }

    private Type visitBinOp(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        if (children.size() != 2) {
            // report
            reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "Operator " + node.get("op") + " is binary, thus needs exactly two operands."));
            return new Type("", true);
        }
        Type expectedOpType, leftType, rightType, returnType;
        leftType = visitExpression(children.get(0));
//            Return the type of the operation
        String op = node.get("op");
        if (op.equals("assign")) {
            expectedOpType = leftType;
            returnType = new Type("void", false);
        } else if (op.equals("and")) {
            expectedOpType = new Type("boolean", false);
            returnType = new Type("boolean", false);
        } else if (op.equals("Lessthan")) {
            expectedOpType = new Type("int", false);

            returnType = new Type("boolean", false);
        } else if (op.equals("mult") || op.equals("div") || op.equals("add") || op.equals("sub")) {
            expectedOpType = new Type("int", false);
            returnType = new Type("int", false);
        } else {
            expectedOpType = new Type("", false);
            returnType = new Type("", false);
        }
        rightType = visitExpression(children.get(1));

        if (symbolTable.getImports().contains(leftType.getName()) && symbolTable.getImports().contains(rightType.getName())) {
            return new Type("", true);
        } else if (!correspondentType(leftType, expectedOpType)) {
            //Error
            //Left type is not of expected type
            reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "The left operand of a binary operator has unexpected type."));
        } else if (!correspondentType(leftType, rightType)) {
            // ths thing
            if ((leftType.getName().equals("") && !rightType.getName().equals("")) || (!leftType.getName().equals("") && rightType.getName().equals(""))) {
                /**
                 * Here we do nothing, just let it through.
                 * We're dealing with a case where an actual type (like int) is being
                 * compared to our general type, used for methods and fields whose type
                 * aren't explicitly defined (imports, extends, etc).
                 * The rule then becomes: if one is general and the other isn't,
                 * then we assume it works, as the tests told us.
                 *
                 * General Type would be Type("", *) - only name dictates if it's general.
                 * Being an array or not doesn't matter to us, here.
                 */
            }else if (!(leftType.getName().equals(symbolTable.getSuper()) && rightType.getName().equals(symbolTable.getClassName()))){
                //Error
                // Left and right side have conflicting types
                reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "A binary operator's operands must have the same type."));
            }
        }
//            Even if there was an error lower in the tree, it might still be important to elevate the type of the op
        return returnType;

    }

    private Type visitIdenfifier(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        JmmNode identifierNode = children.get(0);
        String identifierName = identifierNode.get("name");
        Type statementType;
        Symbol identifierSymbol;
        Boolean isThis = false;
        if (identifierName.equals("this")) {
            if (curMethod.equals("main")) {
                // Error cannot do this in static method
                reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "Cannot reference this on static method"));
            }
            statementType = new Type(symbolTable.getClassName(), false);
            isThis = true;
            // do something
        } else if (identifierName.equals("true") || identifierName.equals("false")) {
            statementType = new Type("boolean", false);
        } else {
            statementType = verifySymbol(identifierNode);
        }
        for (int i = 1; i < children.size(); i++) {
            JmmNode unaryOp = children.get(i);
            if (unaryOp.getKind().equals("UnaryOp")) {
                statementType = visitUnaryOp(unaryOp, statementType, isThis);
                isThis = false;
//                statementType = visitSuffix(unaryOp.getChildren().get(0), identifierSymbol);
//                a.getFoo().length;
//                        a.getFoo()[3].b();
            }
        }

        return statementType;
    }

    private Type visitObject(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        Type statementType = new Type("", false);
        Boolean isThis = statementType.getName().equals(symbolTable.getClassName()); // I don't know what do anymore. let's believe it is already handled in visitIdentifier
        for (int i = 0; i < children.size(); i++) { // Is this needed?
            JmmNode currentNode = children.get(i);
            if (currentNode.getKind().equals("UnaryOp")) {
                statementType = visitUnaryOp(currentNode, statementType, isThis);
                isThis = false;
            } else if (currentNode.getKind().equals("BinOp")) {
                statementType = visitBinOp(currentNode);
                //isThis = false;
            } else if (currentNode.getKind().equals("ExpressionIndependent")) {
                statementType = visitExpressionIndependent(currentNode);
                //isThis = false;
            }
        }

        return statementType;
    }

    private Type verifySymbol(JmmNode symbolNode) {
        String symbolName = symbolNode.get("name");
        List<Symbol> locals = symbolTable.getLocalVariables(curMethod);
        Optional<Symbol> localIdentifier, paramIdentifier, attributeIdentifier;
        Optional<String> importIdentifier;

        localIdentifier = locals.stream().filter(symbol -> symbol.getName().equals(symbolName)).findFirst();
        if (localIdentifier.isPresent()) {
            return localIdentifier.get().getType();
        } else {
            List<Symbol> params = symbolTable.getParameters(curMethod);
            paramIdentifier = params.stream().filter(symbol -> symbol.getName().equals(symbolName)).findFirst();
            if (paramIdentifier.isPresent()) {
                return paramIdentifier.get().getType();
            } else {
                if (!curMethod.equals("main")) {
                    // If is not main look in the class attributes
                    List<Symbol> attributes = symbolTable.getFields();
                    attributeIdentifier = attributes.stream().filter(symbol -> symbol.getName().equals(symbolName)).findFirst();
                    if (attributeIdentifier.isPresent()) {
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
                        return new Type("", false);
                    }
                }
                // Error symbol not defined
                reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(symbolNode.get("line")), Integer.parseInt(symbolNode.get("column")), "Symbol " + symbolName + " isn't defined."));
                return new Type("", true);
            }
        }
    }

    private Type visitUnaryOp(JmmNode node, Type operand, Boolean isThis) {
        Type type = new Type("", true);
        List<JmmNode> children = node.getChildren();

//        if (children.size() != 1) {
//            //exception
//            System.out.println("Unary operator must have one sole child node.");
//            return type;
//        }

        JmmNode child = children.get(0);
        String op = node.get("op");

        if (op.equals("point")) {
            type = visitSuffix(child, operand, isThis);
        } else if (op.equals("new")) {
            type = visitNew(node);
        } else if (op.equals("array")) {
            if (!operand.isArray()) {
                // Error
                // Operand is not of array type
                reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "Operand " + operand.getName() + " must be of type array."));
            } else {
                visitArrayIndex(child);
                type = new Type(operand.getName(), false);
            }
            visitExpressionIndependent(child);
        } else if (op.equals("!")) {
            // Exception should never have a binary op after an identifier
            visitBooleanNegation(child);
            type = new Type("boolean", false);
        }
        return type;
    }

    private Type visitSuffix(JmmNode suffix, Type operand, Boolean isThis) {
        if (symbolTable.getImports().contains(operand.getName())) {
            return new Type("", false);
        }

        isThis = operand.getName().equals(symbolTable.getClassName());
        if (isThis) {
            String method = suffix.get("op");
            if (method.equals("length")) {
                // Error
                // "this" object does not have attribute named length (THIS CAN BE WRONG BECAUSE OF LOCAL OR PARAMETER)
                // Check what to do in this case
                // Probably a different error if this does have the attribute
                // Settle with "Can't use length attribute with this keyword" for now at least
                // Check if there can be a field named length? If there is then this is basically irrelevant?
                // We are not allowing this.length()
                reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(suffix.get("line")), Integer.parseInt(suffix.get("column")), "'this' cannot call length method."));
                return new Type("", false);
            }
            //String method = suffix.getChildren().get(0).get("value");
            List<String> methods = symbolTable.getMethods();
            if (methods.contains(method)) {
                List<Symbol> parameters = symbolTable.getParameters(method);
                Type returnType = symbolTable.getReturnType(method);
                List<JmmNode> arguments;

                if (suffix.getChildren().size() == 0) {
                    arguments = new ArrayList<>();
                } else {
                    arguments = suffix.getChildren().get(0).getChildren();
                }

                if (parameters.size() != arguments.size()) {
                    // error
                    // Unexpected number of arguments. Expecting parameters.size()
                    reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(suffix.get("line")), Integer.parseInt(suffix.get("column")), method + " doesn't have the expected number of parameters.\nExpected: " + parameters.size() + ". Found " + arguments.size()));
                }
                int minParameters = Math.min(parameters.size(), arguments.size());
                // Suffix must have the same number of children as the expected method in the symbol table.
                for (var i = 0; i < minParameters; i++) {
                    Type t = visitExpression(arguments.get(i));
                    if (!correspondentType(parameters.get(i).getType(), t)) {
                        // error
                        // In argument i was expecting type parameters.get(i).getType().getName()
                        // got t.getName()
                        reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(suffix.get("line")), Integer.parseInt(suffix.get("column")), "Parameter " + parameters.get(i).getName() + "of method " + method + " isn't of the expected type.\nExpected " + parameters.get(i).getType().getName() + ", got " + t.getName() + "."));
                    }
                }

                return returnType;
            } else {
                if (!symbolTable.getSuper().equals("")) {
                    // Extends a class and therefore it is a any type of object
                    return new Type("", false);
                } else {
                    // error
                    // method not found
                    reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(suffix.get("line")), Integer.parseInt(suffix.get("line")), "Method " + method + " not found."));
                    return new Type("", false);
                }
            }
        } else {

            if (suffix.get("op").equals("length")) {
                // Keyword is a different case
                if (operand.isArray()) {

                } else {
                    //error
                    reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(suffix.get("line")), Integer.parseInt(suffix.get("column")), "Operand " + operand.getName() + " isn't an array."));
                }
                return new Type("int", false);
            }

            List<JmmNode> children = suffix.getChildren();

            if (children.size() == 0) {

            } else if (children.size() == 1) {
                visitSuffixArguments(children.get(0));
            } else {
                // exception
                System.out.println(suffix.get("op") + " call must include, at max, one parameter.");
                // ^^ No idea if this is right, -- Less
            }

            return new Type("", true);
        }
//        return new Type("Object", true); //Maybe not this

    }

    private void visitSuffixArguments(JmmNode suffixArguments) {
        for (JmmNode argument : suffixArguments.getChildren()) {
            visitExpression(argument);
        }
    }

    private Type visitNew(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        JmmNode child = children.get(0);
        String op = child.get("name");
        if (op.equals("int array")) {
            if (children.size() != 2) {
                //exception
                System.out.println("Wrong declaration.");
                // also don't know if it's right? --Less
            }
            visitArrayIndex(children.get(1));
            return new Type("int", true);
        } else {
            return new Type(op, false);
        }
    }

    private void visitBooleanNegation(JmmNode node) {
        String kind = node.getKind();
        Type booleanExpression = new Type("", false);
        booleanExpression = visitExpression(node);
        Type booleanType = new Type("boolean", false);
        if (!correspondentType(booleanExpression, booleanType)) {
            // Error
            // Negation must be of a boolean type
            reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "Expression being negated must be of type boolean"));
        }
    }

    private void visitArrayIndex(JmmNode node) {
        Type arrayIndex = visitExpressionIndependent(node);
        Type intType = new Type("int", false);
        if (!correspondentType(arrayIndex, intType)) {  // add object type
            // Error
            // Index is not of type int
            reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), "Indexing of array " + node.getJmmChild(0).get("name") + " must be done with integer values."));
        }
    }

    private void visitCondition(JmmNode node) {
        List<JmmNode> children = node.getChildren();
        JmmNode condition = children.get(0);
        JmmNode body = children.get(1);

        Type conditionType = visitExpressionIndependent(condition);
        Type expectedOpType = new Type("boolean", false);
        if (!correspondentType(conditionType, expectedOpType)) { // Add object type
            // Error
            // Condition is not of type bool
            reports.add(new Report(ERROR, SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("line")), "Conditional expression isn't of boolean type."));
        }

        if (node.getKind().equals("IfStatement")) {
            if (condition.getJmmChild(0).getJmmChild(0).getKind().equals("Idntf")) {
                String conditionValue = condition.getJmmChild(0).getJmmChild(0).get("name");

                if (conditionValue.equals("true")) {
                    node.removeJmmChild(2);
                } else if (conditionValue.equals("false")) {
                    node.removeJmmChild(1);
                }
            }
        } else {
            if (condition.getJmmChild(0).getJmmChild(0).getKind().equals("Idntf")) {
                String conditionValue = condition.getJmmChild(0).getJmmChild(0).get("name");

                if (conditionValue.equals("false")) {
                    node.removeJmmChild(1);
                    return;
                }
            }
        }

        visitStatement(body);

    }

    private void visitWhile(JmmNode node) {
        visitCondition(node);
    }

    private void visitIf(JmmNode node) {
        visitCondition(node);
    }

    private void visitReturnStatement(JmmNode method, JmmNode returnSt) {
        // Check if return matches method.
        Type methodType, returnType;

        if (method.getChildren().get(0).getKind().equals("MainMethod")) {
            // Main methods are different, apparently.
            if (method.getChildren().get(1).get("op").contains("array")) {
                methodType = new Type(method.getChildren().get(1).get("op").split(" ")[0], true); // if it doesn't work, check online.
            } else {
                methodType = new Type(method.getChildren().get(1).get("op"), false);
            }
        } else {
            if (method.getChildren().get(0).get("op").contains("array")) {
                methodType = new Type(method.getChildren().get(0).get("op").split(" ")[0], true); // if it doesn't work, check online.
            } else {
                methodType = new Type(method.getChildren().get(0).get("op"), false);
            }
        }
        returnType = visitExpression(returnSt.getChildren().get(0));

        if (correspondentType(returnType, methodType)) {
            // It's correct.
            return;
        }

        reports.add(new Report(ReportType.ERROR, SEMANTIC, Integer.parseInt(returnSt.get("line")), Integer.parseInt(returnSt.get("column")), "Return statement in method does not match the return type of corresponding method."));
    }

    private void visitVariableDeclaration(JmmNode node) {
        // I don't think this needs to do anything.
        // If there was a problem with a variable declaration,
        // it wouldn't pass the grammar, pretty sure?
    }

    private void visitStatement(JmmNode node) {
        String childKind;
        for (JmmNode child : node.getChildren()) {
            childKind = child.getKind();
            if (childKind.equals("Statement")) {
                visitStatement(child);
            } else if (childKind.equals("ExpressionIndependent") || childKind.equals("ExpressionIndependent2")) {
                visitExpressionIndependent(child);
            } else if (childKind.equals("ExpressionIndependent3")) {
                visitObject(child);
            } else if (childKind.equals("WhileStatement")) {
                visitWhile(child);
            } else if (childKind.equals("IfStatement")) {
                visitIf(child);
            } else if (childKind.equals("VariableDeclaration")) {
                visitVariableDeclaration(node);
            } else if (childKind.equals("ReturnStatement")) {
                // visitReturnStatement(node);
            } else {
                // exception
                System.out.println("Node not implemented");
            }
        }
    }

    private Boolean correspondentType(Type type1, Type type2) {
        /*if (anyType(type1) || anyType(type2)) {
            return true;
        } else */
        if (type1.getName() == "" || type2.getName() == "") {
            return type1.isArray() == type2.isArray();
        } else {
            return type1.equals(type2);
        }
    }

    /*
    private Boolean anyType(Type type) {
        return type.getName().equals("") && type.isArray();
    }
    */

    public Boolean visitMethod(JmmNode root, Boolean dummy) {
        if (root.getChildren().get(0).getKind().equals("MainMethod")) {
            curMethod = "main";
        } else {
            curMethod = root.getChildren().get(1).get("value");
        }

        List<Symbol> methodLocals = symbolTable.getLocalVariables(curMethod);
        for (JmmNode node : root.getChildren()) {
            if (node.getKind().equals("MethodBody")) {
                for (var statement : node.getChildren()) {
                    if (statement.getKind().equals("VariableDeclaration")) {
                        continue;
                    } else if (statement.getKind().equals("Statement")) {
                        visitStatement(statement);
                    } else if (statement.getKind().equals("ReturnStatement")) {
                        visitReturnStatement(root, statement);
                    }
                }
            }
        }
        return true;

    }
}
