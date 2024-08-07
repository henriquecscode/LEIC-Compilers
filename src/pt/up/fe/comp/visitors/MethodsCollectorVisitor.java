package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.visitors.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodsCollectorVisitor extends PreorderJmmVisitor<Map<String, MethodExpr>, Boolean> {

    public MethodsCollectorVisitor() {
        addVisit("MethodDeclarationPublic", this::visitMethods);
    }

    private List<Symbol> visitMethodArguments(JmmNode methodArguments) {
        List<Symbol> params = new ArrayList<>();
        for (JmmNode i : methodArguments.getChildren()) {
            params.add(Utils.getSymbol(i));
        }
        return params;
    }

    private List<Symbol> visitMethodBody(JmmNode methodBody) {
        List<Symbol> locals = new ArrayList<>();
        for (JmmNode i : methodBody.getChildren()) {
            if (i.getKind().equals("VariableDeclaration")) {
                locals.add(Utils.getSymbol(i));
            } else {
                break;
            }
        }
        return locals;
    }

    private Boolean visitMethods(JmmNode methodDecl, Map<String, MethodExpr> methods) {
        // Get a symbol for the type and name of the method -> Two first variables unless is a main
        // Get a list of symbols for the parameters -> Inside MethodArguments
        // Get a list of symbols for the local variables -> Inside Method body

        MethodExpr method;
        Symbol symbol;
        List<Symbol> params = new ArrayList<>();
        List<Symbol> locals;

        List<JmmNode> children = methodDecl.getChildren();

        //Method public always either has 2 children or 4 children
        if (children.size() > 2) {
            // Method symbol
            symbol = Utils.getSymbol(methodDecl);

            // Method arguments if existent
            JmmNode child2 = children.get(2);
            if (child2.getKind().equals("MethodArguments"))
            {
                params = visitMethodArguments(child2);
            }
        } else { // Is main
            // Main Symbol
            symbol = new Symbol(new Type("static void", false), "main");

            // Main parameters
            params.add(new Symbol(new Type("String", true), "args"));
        }

        // Get method body
        JmmNode methodBody = children.get(children.size() - 1);
        locals = visitMethodBody(methodBody);

        // Make method data structure
        method = new MethodExpr(symbol, params, locals);
        methods.put(symbol.getName(), method);

        return true;

    }
}