package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JasminEmitter implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        String jasminCode="";
        try {
            ollirClass.checkMethodLabels();
            ollirClass.buildCFGs();
            ollirClass.outputCFGs();
            ollirClass.buildVarTables();
            jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getCode();
        } catch (OllirErrorException e) {
            throw new RuntimeException(e);
        }

        System.out.println(jasminCode);

        return new JasminResult(ollirResult,jasminCode, ollirResult.getReports());
    }
}
