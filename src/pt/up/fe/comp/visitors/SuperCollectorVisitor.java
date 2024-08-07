package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;

public class SuperCollectorVisitor extends PreorderJmmVisitor<List<String>, Boolean> {

    public SuperCollectorVisitor() {
        addVisit("Ext", this::visitExt);

    }

    private Boolean visitExt(JmmNode ExtDecl, List<String> superName) {
        var superNameString = ExtDecl.get("value");
        superName.add(superNameString);
        return true;
    }

}