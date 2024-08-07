package pt.up.fe.comp.visitors;


import pt.up.fe.comp.IntegerLiteral;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.System.*;
import static pt.up.fe.comp.jmm.report.ReportType.ERROR;
import static pt.up.fe.comp.jmm.report.Stage.SEMANTIC;

public class SemanticOptimizer extends PostorderJmmVisitor<Boolean, Boolean> {
    private SymbolTable symbolTable;
    public List<Report> reports;
    private String curMethod;

    public SemanticOptimizer(SymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        addVisit("BinOp", this::visitBinOp);
    }

    public Boolean visitBinOp(JmmNode node, Boolean dummy) {
        System.out.println("visiting a node");
        List<JmmNode> children = node.getChildren();
        if (children.get(0).getKind().equals("ExpressionIndependent") && children.get(1).getKind().equals("ExpressionIndependent")) {
            String op = node.get("op");
            if (children.get(0).getChildren().size() == 1 && children.get(0).getChildren().get(0).getKind().equals("IntegerLiteral") &&
                    children.get(0).getChildren().size() == 1 && children.get(1).getChildren().get(0).getKind().equals("IntegerLiteral")) {
                if (op.equals("assign")) {

                }else if (op.equals("Lessthan")) {
                    Integer val2 = Integer.valueOf(children.get(1).getChildren().get(0).get("value"));
                    Integer val1 = Integer.valueOf(children.get(0).getChildren().get(0).get("value"));
                    Boolean result = val1 < val2;
                    JmmNodeImpl newNode = new JmmNodeImpl("Idntf");
                    newNode.put("name", String.valueOf(result));
                    newNode.put("opt", "constantFolding");
                    replaceNode(node, newNode); // parte 1

                } else {
                    Integer val1 = Integer.valueOf(children.get(0).getChildren().get(0).get("value"));
                    Integer val2 = Integer.valueOf(children.get(1).getChildren().get(0).get("value"));
                    Integer result = null;
                    if (op.equals("mult")) {
                        result = val1 * val2;
                    } else if (op.equals("div")) {
                        result = val1 / val2;
                    } else if (op.equals("sub")) {
                        result = val1 - val2;
                    } else if (op.equals("add")) {
                        result = val1 + val2;
                    }
                    // build the new node (in case it's integer Literal)
                    JmmNodeImpl newNode = new JmmNodeImpl("IntegerLiteral");
                    newNode.put("value", String.valueOf(result));
                    newNode.put("opt", "constantFolding");

                    replaceNode(node, newNode); // parte 1
                }
            }
            else if(children.get(0).getChildren().size() == 1 && children.get(0).getChildren().get(0).getKind().equals("Idntf") && (children.get(0).getChildren().get(0).get("name").equals("true") || children.get(0).getChildren().get(0).get("name").equals("false")) &&
                    children.get(0).getChildren().size() == 1 && children.get(1).getChildren().get(0).getKind().equals("Idntf") && (children.get(1).getChildren().get(0).get("name").equals("true") || children.get(1).getChildren().get(0).get("name").equals("false"))) {
                Boolean val2 = Boolean.valueOf(children.get(0).getChildren().get(0).get("name").equals("true"));
                Boolean val1 = Boolean.valueOf(children.get(1).getChildren().get(0).get("name").equals("true"));
                Boolean result = val1 && val2;
                JmmNodeImpl newNode = new JmmNodeImpl("Idntf");
                newNode.put("name", String.valueOf(result));
                newNode.put("opt", "constantFolding");
                replaceNode(node, newNode); // parte 1


            }
        }
        return true;
    }

    public void replaceNode(JmmNode node, JmmNode newNode){
        if (!node.getJmmParent().getKind().contains("ExpressionIndependent")) {
            JmmNodeImpl newParent = new JmmNodeImpl("ExpressionIndependent");
            newParent.add(newNode);
            newNode = newParent;
        }
        node.replace(newNode);

    }
}