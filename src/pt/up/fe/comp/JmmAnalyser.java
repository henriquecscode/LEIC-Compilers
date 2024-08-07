package pt.up.fe.comp;

import java.util.Collections;
import java.util.List;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

public class JmmAnalyser implements JmmAnalysis {

    SymbolTableImp symbolTable;

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        symbolTable = new SymbolTableImp(parserResult);
        List<Report> reports = symbolTable.getReports();

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
