package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmVisitor;

public class JmmVisitorSymbolTable extends AJmmVisitor<Object, String> {

    public JmmVisitorSymbolTable(){
        addVisit("classDeclaration", this::classDeclarationVisit);
        addVisit("BinOp", this::binOpVisit);
    }

    private String binOpVisit(JmmNode node, Object dummy){
        return "";
    }

    private String classDeclarationVisit(JmmNode node, Object dummy){
        return "working?";
    }
}
