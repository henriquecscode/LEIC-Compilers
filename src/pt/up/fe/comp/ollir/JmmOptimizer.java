package pt.up.fe.comp.ollir;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.Config;
import pt.up.fe.comp.visitors.OllirOptimizer;
import pt.up.fe.comp.visitors.SemanticOptimizer;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.visitors.OllirVisitors;
import pt.up.fe.comp.visitors.SemanticOptimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JmmOptimizer implements JmmOptimization {
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var ollirGenerator = new OllirVisitors(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());
        List<Report> reports = semanticsResult.getReports();
        String ollirCode = ollirGenerator.getCode();
        System.out.println(ollirCode);
        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        Map<String, String> configs = semanticsResult.getConfig();
        Boolean isOptimize = configs.getOrDefault("optimize", "false").equals("true");
//        isOptimize = true; // DEBUG
        if (isOptimize) {
            List<Report> reports = new ArrayList<>();
            SemanticOptimizer semanticVisitor = new SemanticOptimizer(semanticsResult.getSymbolTable(), reports);
            semanticVisitor.visit(semanticsResult.getRootNode(), true);
            // new function for the nice thingy
        }
        System.out.println(semanticsResult.getRootNode().toTree());
        return semanticsResult;

    }

    public OllirResult optimize(OllirResult ollirResult) {
        Map<String, String> configs = ollirResult.getConfig();
        Boolean isOptimize = configs.getOrDefault("optimize", "false").equals("true");
//        isOptimize = true; // DEBUG
        if (isOptimize) {
            System.out.println("Optimizing ollir");
            ClassUnit ollirClass = ollirResult.getOllirClass();
            OllirOptimizer optimizer = new OllirOptimizer(ollirClass);
            optimizer.constantPropagation(ollirClass);
        }
        return ollirResult;
    }
}
