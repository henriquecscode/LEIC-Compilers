package pt.up.fe.comp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        Map<String, String> config = new HashMap<>();
        parseArgs(args, config);
        SpecsLogs.info("Input code: " + config.get("inputFile"));

        // Create config
        String input = "";
        if (!config.containsKey("inputFile")) {
            return;
        } else {
            input = config.get("inputFile");
        }
        Config.setConfigs(config);
        config = Config.getConfigs();

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Is this thing even needed?
//        List<Report> reports= parserResult.getReports();
//        if (reports.size() > 0){
//            for(Report report: reports){
//                System.err.println(report);
//            }
//        }

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());
        System.out.println(parserResult.getRootNode().toTree());

        // Instantiate JmmAnalysis
        JmmAnalyser analyser = new JmmAnalyser();

        // Analysis stage
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        // Check if there are parsing errors
        TestUtils.noErrors(analysisResult.getReports());

        var optimizer = new JmmOptimizer();
       /* var optimizationResult = optimizer.toOllir(analysisResult);
        TestUtils.noErrors(optimizationResult);*/

        //JASMIN STAGE
        // Semantic optimization
        var optimizationResultStep1 = optimizer.optimize(analysisResult);

        // Ollir Stage
        var optimizationResultStep2 = optimizer.toOllir(optimizationResultStep1);

        var ollirResult = optimizer.optimize(optimizationResultStep2);

        TestUtils.noErrors(ollirResult);

        // Jasmin stage
        var jasminEmitter = new JasminEmitter();

        var jasminResult = jasminEmitter.toJasmin(ollirResult);

        TestUtils.noErrors(jasminResult);

// ... add remaining stages
    }

    private static void parseArgs(String[] args, Map<String, String> config) {
        if (args.length == 0) {
            //Read from input
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                config.put("inputFile", br.readLine());
            } catch (Exception e) {
                throw new RuntimeException("Could not read input", e);
            }
        } else {

            //Read from file
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-o")) {
                    config.put("optimize", "true");
                } else if (args[i].equals("-reg")) {
                    if (i == args.length - 1) {
                        config.put("registerAllocation", "-1");
                    }
                    i += 1;
                    try {
                        Integer noRegs = Integer.parseInt(args[i]);
                        config.put("registerAllocation", args[i]);

                    } catch (NumberFormatException ex) {
                        i -= 1;
                    }

                } else {
                    File input_file = new File(args[i]);
                    if (input_file.isFile()) {
                        String inputString = SpecsIo.read(args[i]);
                        config.put("inputFile", inputString);
                    } else {
                        config.put("inputFile", args[i]);
                    }
                }
            }
        }
    }
}
