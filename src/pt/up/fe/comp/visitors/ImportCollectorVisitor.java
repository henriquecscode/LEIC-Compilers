package pt.up.fe.comp.visitors;

import org.specs.comp.ollir.AccessModifiers;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;
import java.util.stream.Collectors;

public class ImportCollectorVisitor extends PreorderJmmVisitor<List<String>, Boolean> {

    public ImportCollectorVisitor() {
        addVisit("ImportDeclaration", this::visitImport);
    }


    private Boolean visitImport(JmmNode importDecl, List<String> imports) {


        var importString = importDecl.getChildren().stream()
                .map(id -> id.get("value"))
                .collect(Collectors.joining("."));



        imports.add(importString);


        return true;
    }
}