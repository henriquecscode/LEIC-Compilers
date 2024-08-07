package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.visitors.*;

import java.util.*;

public class SymbolTableImp implements SymbolTable {

    private JmmParserResult parserResult;
    private JmmNode root;

    private List<String> imports = new ArrayList<>();
    private String className;
    private String superName=null;
    private List<Symbol> fields = new ArrayList<>();
    private Map<String, MethodExpr> methods = new HashMap<>();
    private List<String> methodsNames;
    private List<Report> reports;

    SymbolTableImp(JmmParserResult parserResult){
        this.root = parserResult.getRootNode();
        this.parserResult = parserResult;

        calculateImports();
        calculateClassName();
        calculateSuper();
        calculateFields();
        calculateMethods();

        semanticAnalysis();

        System.out.println("Symbol table calculated");
    }

    private void calculateImports(){
        ImportCollectorVisitor importcollector = new ImportCollectorVisitor();
        importcollector.visit(this.root, this.imports);
    }

    private void calculateClassName(){
        List<String> classNameRef = new ArrayList<>();
        ClassNameCollectorVisitor classNamecollector = new ClassNameCollectorVisitor();
        classNamecollector.visit(this.root, classNameRef);
        this.className = classNameRef.get(0);
    }

    private void calculateSuper(){
        List<String> superNameRef = new ArrayList<>();
        SuperCollectorVisitor superCollector = new SuperCollectorVisitor();
        superCollector.visit(this.root, superNameRef);
        this.superName = superNameRef.size() != 0 ? superNameRef.get(0) : "";
    }

    private void calculateFields() {
        FieldsCollectorVisitor fieldsVisitorCollector = new FieldsCollectorVisitor();
        fieldsVisitorCollector.visit(this.root, this.fields);
    }

    private void calculateMethods(){
        MethodsCollectorVisitor methodsVisitorCollector = new MethodsCollectorVisitor();
        methodsVisitorCollector.visit(this.root, this.methods);
        this.methodsNames = new ArrayList<>(this.methods.keySet());
    }

    private void semanticAnalysis(){
        List<Report> reports = new ArrayList<Report>();
        MethodSemanticAnalyserVisitor semanticVisitor = new MethodSemanticAnalyserVisitor(this, reports);
        semanticVisitor.visit(this.root, true);
        this.reports = semanticVisitor.reports;
// Other solution would be to return the reports. We are going to try it like this now
        //        Reports is going to be the semantic errors if there are any;
    }

    /**
     * Function to check existence of method.
     * May be used for operator overloading implementation.
     * @param methodSignature - name of method to check if exists.
     * @return true (it exists), false (doesn't exist).
     */
    public boolean hasMethod(String methodSignature) {
        return methods.containsKey(methodSignature);
    }

    public List<String> getImports(){
        return this.imports;
    }

    public String getClassName(){
        return this.className;
    }

    public String getSuper(){
        return this.superName;
    }

    public List<Symbol> getFields() {
        return this.fields;
    }

    public List<String> getMethods(){
        return this.methodsNames;
    }

    public Type getReturnType(String methodSignature){
        return this.methods.get(methodSignature).getSymbol().getType();
    }

    public List<Symbol> getParameters(String methodSignature){
        return this.methods.get(methodSignature).getParameters();
    }

    public List<Symbol> getLocalVariables(String methodSignature){
        return this.methods.get(methodSignature).getLocal();

    }

    public List<Report> getReports(){
        return reports;
    }
}