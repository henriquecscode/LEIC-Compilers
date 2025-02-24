package pt.up.fe.comp.jmm.ollir;

import pt.up.fe.comp.Config;
import pt.up.fe.comp.MethodSemanticAnalyserVisitor;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Stage deals with optimizations performed at the AST level and at the OLLIR level.<br>
 * Note that for Checkpoint 2 (CP2) only the {@link JmmOptimization#toOllir(JmmSemanticsResult)} has to be developed.
 * The other two methods are for Checkpoint 3 (CP3).
 */
public interface JmmOptimization {

    /**
     * Step 1 (for CP3): optimize code at the AST level
     * 
     * @param semanticsResult
     * @return
     */
    default JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return semanticsResult;
    }

    /**
     * Step 2 (for CP2): convert the AST to the OLLIR format
     * 
     * @param semanticsResult
     * @return
     */
    OllirResult toOllir(JmmSemanticsResult semanticsResult);

    /**
     * Step 3 (for CP3): optimize code at the OLLIR level
     * 
     * @param ollirResult
     * @return
     */
    default OllirResult optimize(OllirResult ollirResult) {
        return ollirResult;
    }
}