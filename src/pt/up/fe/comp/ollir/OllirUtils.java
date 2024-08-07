package pt.up.fe.comp.ollir;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

import java.util.List;

public class OllirUtils {
    int test;

    /*myClass{
    */
    public static String classIntroduction(String className, String superName){
        StringBuilder result=new StringBuilder();
        if(!superName.equals("")){
            result.append(className +" extends " + superName+"{\n");
        }
        else{
            result.append(className +"{\n");
        }
        return result.toString();
    }

    /*
    * .construct myClass().V{
    *   invokespecial(this,"<init>").V;
    * */
    public static String classConstructor(String className){
        StringBuilder result=new StringBuilder();
        result.append(".construct "+ className+"().V {\n" + "invokespecial(this,\"<init>\").V;");
        return result.toString();
    }

    public static String closeBrackets(){
        return "\n}\n";
    }

    /*
    .method public sum(A.array.i32).i32{--> public int sum(int [] A)

    parameters need to have the variable name + type
    */
    public static String method(String methodName, List<String> parameters,String returnType){
        StringBuilder result = new StringBuilder(".method public ");

        result.append(methodName +"(");
        if(parameters==null){
            result.append(")");
        }
        else{
            result.append(String.join(",",parameters));
            result.append(")");
        }
        String variable = OllirUtils.getVariableType(returnType);
       /* if(variable.equals(".array.i32")){
            result.append(".array");
        }else{*/
        result.append(variable);
       // }
        result.append("{\n");
        return result.toString();
    }

    /*
    * .method public static main(args.array.String).V*/
    public static String mainMethod(){
        return ".method public static main(args.array.String).V{\n";
    }

    public static String getVariableType(String type){
        StringBuilder result = new StringBuilder();
        switch (type){
            case "int array":
                result.append(".array.i32");
                break;
            case "int":
                result.append(".i32");
                break;
            case "boolean":
                result.append(".bool");
                break;
            case "void":
                result.append(".V");
                break;
            default:
                result.append(".").append(type);
                break;
        }
        return result.toString();
    }

    public static String getVariableType(Type symbol){
        StringBuilder result = new StringBuilder();
        if(symbol.isArray()){
            result.append(".array");
        }
        switch(symbol.getName()){
            case "int":
                result.append(".i32");
                break;
            case "boolean":
                result.append(".bool");
                break;
            case "void":
                result.append(".V");
                break;
            default:
                result.append(".").append(symbol.getName());
                break;
        }
        return result.toString();
    }

    public static String variable(Symbol symbol){
        StringBuilder result = new StringBuilder(symbol.getName());
        result.append(getVariableType(symbol.getType()));
        return result.toString();
    }

    public static String methodArg(String type, String idntf){
        StringBuilder result= new StringBuilder();
        result.append(idntf + OllirUtils.getVariableType(type));
        return result.toString();
    }

    public static String binOp(String leftside, String typeVariable, String rightside, String op){
        return leftside +" "+op+typeVariable+" "+rightside+";\n";
    }

    public static String assignmentType(String op){
        if(op.equals("+") || op.equals("-") || op.equals("/") || op.equals("*")){
            return ".i32";
        }
        else if(op.equals("&&") || op.equals("<") ||op.equals("!") || op.equals(">=")){
            return ".bool";
        }
        else{
            return "";
        }
    }

    //ret.i32 t1.i32;
    public static String ret(String ret, String returnValue){
        return "ret."+returnValue+" " +ret+ "."+returnValue ;
    }

    public static String retNewObject(String ret, String returnValue){ //Idk if this is even needed
        return "ret."+returnValue+" " +newObject(ret)+ "."+returnValue;
    }

    public static String retNewArray(String size, String returnValue){
        return "ret."+returnValue+" "+newArray(size)+"."+returnValue;
    }

    /*
    * invokestatic(io,"println",t2.String,t4.i32).V -->io.println("val = ", this.get());
    * invokestatic(io,"println",aux2.i32).V -->io.println(new Fac().compFac(10))
    * */
    public static String invokestatic(String className, String methodName, Type returnType, String parameters){
        if(parameters.equals("")) {
            return String.format("invokestatic(%s, \"%s\")%s;\n", className, methodName, getVariableType(returnType));
        }
        else{
            return String.format("invokestatic(%s, \"%s\", %s)%s;\n", className, methodName, parameters, getVariableType(returnType));
        }
    }

    /*
    * invokevirtual(c1.myClass, "put", 2.i32).V;  // c1.put(2);
    * invokevirtual(A.myClass,"m1").V;} //A.m1()
    * */
    public static String invokevirtual(String variable, String methodName, Type returnType, String parameters, String className){
        if(parameters.equals("")){
            return String.format("invokevirtual(%s, \"%s\")%s;\n", variable != "this" ? variable + "." + className : "this", methodName, getVariableType(returnType));
        }
        else{
            return String.format("invokevirtual(%s, \"%s\", %s)%s;\n", variable != "this" ? variable + "." + className : "this", methodName, parameters, getVariableType(returnType));
        }
    }

    /*
    * invokespecial(aux1.Fac,"<init>").V */
    public static String invokespecial(String variable, String methodName, Type returnType, String parameters){
        if(parameters.equals("")){
            return String.format("invokespecial(%s, \"%s\")%s;\n", variable != null ? variable : "this", methodName, getVariableType(returnType));
        }
        else{
            return String.format("invokespecial(%s, \"%s\", %s)%s;\n", variable != null ? variable : "this", methodName, parameters, getVariableType(returnType));
        }
    }

    public static String arraylength(String variable){
        return "arraylength("+variable+").i32";
    }

    //putfield(this, a.i32, $1.n.i32).V;  //this.a=n;
    public static String putfield(MutableSymbol variable, String value){
        return "putfield(this, "+variable.getName() + getVariableType(variable.getType()) + ", "+value+").V";
    }

    //t1.i32 :=.i32 getfield(this, a.i32).i32; //temp = this.a
    public static String getfield(MutableSymbol variable){
        return "getfield(this, "+variable.getName()+ getVariableType(variable.getType())+")"+getVariableType(variable.getType()) + ";\n";
    }

    //.field private a.i32; //int a;
    public static String field(Symbol variable){

        return ".field public "+variable(variable)+";"+"\n";
    }

    public static String invokespecial(Symbol variable){
        return "invokespecial("+variable(variable)+"\"<init>\").V;";
    }

    //A.myClass := myClass new(myClass).myClass; //myClass A = new myClass();
    public static String newObject(String className){
        return "new("+className+")."+className;
    }

    //C.array :=.array new(array, t1.i32).array;  //int[] C = new int[A.length];
    public static String newArray(String size){
        return "new(array, "+size+".i32).array";
    }

    public static String variable(Symbol variable, String parameter){
        if(parameter==null){
            return variable(variable);
        }
        return parameter +variable(variable);
    }

    public static String arrayaccess(Symbol variable, String parameter, String index){
        if(parameter == null){
            return variable.getName()+"["+index+"]"+getVariableType(new Type(variable.getType().getName(),false));
        }
        return parameter+ "."+ variable.getName()+"["+index+"]"+getVariableType(new Type(variable.getType().getName(),false));
    }

}


