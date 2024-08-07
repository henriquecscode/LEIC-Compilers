package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;

public class ClassNameCollectorVisitor extends PreorderJmmVisitor<List<String>,Boolean>{
    public ClassNameCollectorVisitor() {
        addVisit("ClassDeclaration", this::visitClassName);
    }

    private Boolean visitClassName(JmmNode nodeClass, List<String> className) {
        var classNameString = nodeClass.get("value");
        className.add(classNameString);
        return true;
    }
}






