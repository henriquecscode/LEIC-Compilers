package pt.up.fe.comp.visitors.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.MutableSymbol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp.jmm.report.ReportType.ERROR;
import static pt.up.fe.comp.jmm.report.Stage.SEMANTIC;

public class Utils {
    public static Symbol getSymbol(JmmNode node) {
        Boolean isIntArray = node.getChildren().get(0).get("op").contains("array");
        Type type;
        if (isIntArray) {
            type = new Type("int", true);
        } else {
            type = new Type(node.getChildren().get(0).get("op"), false);
        }
        return new Symbol(type, node.getChildren().get(1).get("value"));
    }

}
