package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.visitors.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FieldsCollectorVisitor extends PreorderJmmVisitor<List<Symbol>, Boolean> {

    public FieldsCollectorVisitor() {
        addVisit("ClassDeclaration", this::visitFields);
    }

    private Boolean visitFields(JmmNode classDecl, List<Symbol> fields) {
        List<JmmNode> validChildren = new ArrayList<>();

        for (JmmNode i : classDecl.getChildren()) {
            if (i.getKind().equals("VariableDeclaration")) {
                validChildren.add(i);
            }
        };

        for (JmmNode i : validChildren) {
            // Make this an util function that, from a node with a type and an identifier gets a symbol
            Symbol s = Utils.getSymbol(i);
            fields.add(s);
        }

        return true;
    }
}