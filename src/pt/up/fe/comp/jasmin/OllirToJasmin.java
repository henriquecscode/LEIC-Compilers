package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.Config;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;


public class OllirToJasmin {
    private final ClassUnit classUnit;
    private static final FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();
    private Method curMethod;
    private int stackCounter = 0; //numStack
    private int maxStackCounter = 0;
    private int conditional;
    private Element leftNew;
    private String rightSide;
    private String prevNew = "";
    int numLocals = 0;
    Map<String, String> config;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.config = Config.getConfigs();
    }

    public String getFullyQualifiedName(String className) {
        if (classUnit.getSuperClass() != null) {
            for (var importString : classUnit.getImports()) {
                var splittedImport = importString.split("\\.");
                String lastName;

                if (splittedImport.length == 0) {
                    lastName = importString;
                } else {
                    lastName = splittedImport[splittedImport.length - 1];
                }

                if (lastName.equals(className)) {
                    return importString.replace('.', '/');
                }
            }
            if (!classUnit.getImports().equals(Collections.emptyList())) {
                System.out.println("Empty");//throw new RuntimeException("Could not find import for class " + className);
            }
        }
        return "java/lang/Object";
    }

    public String getCode() {
        var code = new StringBuilder();
        code.append(getClassCode());
        System.out.println(code.toString());
        return code.toString();
    }

    private String getMethodsCode() {
        StringBuilder code = new StringBuilder();

        for (var method : classUnit.getMethods()) {
            this.stackCounter = 0;
            if (method.getMethodName().equals(classUnit.getClassName())) {
                // Constructor method - already in constructor code.
                continue;
            }
            curMethod = method;
            code.append(getMethodCode(method));
        }

        return code.toString();
    }

    public String getClassCode() {
        StringBuilder code = new StringBuilder();
        code.append(".class public ").append(classUnit.getClassName()).append("\n");
        code.append(".super " + getFullyQualifiedName(classUnit.getClassName()) + "\n");
        for (Field f : classUnit.getFields()) {
            code.append(".field public ").append(f.getFieldName() + " ").append(getJasminType(f.getFieldType())).append("\n");
        }

        for (Method method : classUnit.getMethods()) {
            this.stackCounter = 0;
            this.maxStackCounter = 0;
            code.append(getCodeMethodHeader(method));
            String instructions = this.getMethodInstructions(method);

            StringBuilder instructionsCode = new StringBuilder();
            if (!method.isConstructMethod()) {
                instructionsCode.append(instructions);
                // also also, aren't the paramList also counting towards the locals size?
                // so we should do + paramList.size() + 1 + method.getVarTable.size();?
                if (method.getVarTable().containsKey("this")) {
                    numLocals = method.getVarTable().size();
                } else {
                    numLocals = method.getVarTable().size() + 1;
                }
                //numLocals = Math.min(1, method.getVarTable().size());
                code.append(".limit locals " + numLocals + "\n");
                code.append(".limit stack " + maxStackCounter + " \n");
                code.append(instructionsCode.toString());
            }
        }
        return code.toString();
    }

    private String readFile(String path) {
        try {
            StringBuilder fileReader = new StringBuilder();
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                fileReader.append(data);
            }
            myReader.close();
            return fileReader.toString();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return "";
    }

    public String getCodeMethodHeader(Method method) {
        StringBuilder code = new StringBuilder();
        if (method.isConstructMethod()) {
            String sups = getFullyQualifiedName(classUnit.getClassName());
            code.append(".method public <init>()V\n" +
                    "\taload_0\n" +
                    "\tinvokenonvirtual " + sups + "/<init>()V\n" +
                    "\treturn\n" +
                    ".end method");
            return code.toString();

        }
        code.append("\n.method public ");
        if (method.isStaticMethod()) {
            code.append("static ");
        }

        code.append(method.getMethodName()).append("(");
        if (method.getMethodName().equals("main")) {
            code.append("[Ljava/lang/String;");
        } else {
            for (Element element : method.getParams()) {
                code.append(getJasminType(element.getType()));
            }
        }
        code.append(")").append(getJasminType(method.getReturnType()) + "\n");
        return code.toString();
    }

    public String getMethodInstructions(Method method) {
        StringBuilder code = new StringBuilder();
        for (Instruction instruction : method.getInstructions()) {
            code.append(getInstructionCode(instruction, method.getVarTable(), method.getLabels()));
            if (instruction instanceof CallInstruction && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                code.append("pop\n");
                this.decrementStackCounter(1);
            }
        }

        code.append("\n.end method\n\n");
        return code.toString();
    }

    private String getHeaderCode() {
        StringBuilder code = new StringBuilder();
        code.append(".class public ").append(classUnit.getClassName()).append("\n");
        code.append(".super ").append(getFullyQualifiedName(classUnit.getSuperClass())).append("\n\n");
        return code.toString();
    }

    private String getConstructorCode() {
        String superQualifiedName = getFullyQualifiedName(classUnit.getSuperClass());
        String constructor = ".method public <init>()V\naload_0\ninvokenonvirtual " + superQualifiedName + "/<init>()V\nreturn\n.end method\n";
        return constructor;
    }

    public String getMethodCode(Method method) {
        var code = new StringBuilder();
        var header = new StringBuilder();

        // First to the code itself and the instructions
        for (var inst : method.getInstructions()) {
            code.append(getInstructionCode(inst, method.getVarTable(), method.getLabels()));
        }
        code.append(".end method\n\n");

        // Then do the header so that we have access to the number of locals and stack
        header.append(".method ").append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        if (method.isStaticMethod()) {
            header.append("static ");
        }

        header.append(method.getMethodName()).append("(");
        var methodParamTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        header.append(methodParamTypes).append(")").append(getJasminType(method.getReturnType())).append("\n");


        header.append(".limit locals " + numLocals + "\n");
        header.append(".limit stack " + maxStackCounter + " \n");
        code.insert(0, header.toString());

        return code.toString();
    }

    public String getJasminType(Type type) {
        StringBuilder code = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();
        if (elementType == ElementType.ARRAYREF) {
            elementType = ((ArrayType) type).getArrayType();
            code.append("[");
        }
        switch (elementType) {
            case BOOLEAN:
                return code.append("Z").toString();
            case OBJECTREF:
                String className = ((ClassType) type).getName();
                return code.append("L" + this.getObject(className) + ";").toString();
            case CLASS:
                return "CLASS";
            case STRING:
                return code.append("Ljava/lang/String").toString();
            case VOID:
                return "V";
            case INT32:
                return code.append("I").toString();
            default:
                return code.toString();
        }
    }

    private String getObject(String className) {
        for (String imp : classUnit.getImports()) {
            if (imp.endsWith("." + className)) {
                return imp.replaceAll("\\.", "/");
            }
        }
        return className;
    }

    public String getInstructionCode(Instruction instruction, HashMap<String, Descriptor> varTable, HashMap<String, Instruction> methodLabels) {
        StringBuilder code = new StringBuilder();
        for (Map.Entry<String, Instruction> entry : methodLabels.entrySet()) {
            if (entry.getValue().equals(instruction)) {
                code.append(entry.getKey()).append(":\n");
            }
        }
        switch (instruction.getInstType()) {
            case ASSIGN:
                return code.append(getAssignmentCode((AssignInstruction) instruction, varTable)).toString();
            case NOPER:
                return code.append(getSingleOpCode((SingleOpInstruction) instruction, varTable)).toString();
            case BINARYOPER:
                return code.append(getBinaryOpCode((BinaryOpInstruction) instruction, varTable)).toString();
            case UNARYOPER:
                return "";
            case CALL:
                return code.append(getCallCode((CallInstruction) instruction, varTable)).toString();
            case BRANCH:
                return code.append(getCondBranchCode((CondBranchInstruction) instruction, varTable)).toString();
            case GOTO:
                return code.append(getGotoCode((GotoInstruction) instruction, varTable)).toString();
            case PUTFIELD:
                return code.append(getPutFieldCode((PutFieldInstruction) instruction, varTable)).toString();
            case GETFIELD:
                return code.append(getGetFieldCode((GetFieldInstruction) instruction, varTable)).toString();
            case RETURN:
                return code.append(getReturnCode((ReturnInstruction) instruction, varTable)).toString();
            default:
                return "dan";
        }
    }


    public String getAssignmentCode(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        StringBuilder right = new StringBuilder();
        Instruction rhs = instruction.getRhs();
        Element left = instruction.getDest();
        this.leftNew = instruction.getDest();
        InstructionType type = rhs.getInstType();
        if (left instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) left;
            code.append("aload" + this.getRegister(operand.getName(), varTable) + "\n");
            this.incrementStackCounter(1);
            code.append(loading(operand.getIndexOperands().get(0), varTable));
            this.decrementStackCounter(1);
//            code.append(loading((ArrayOperand) left, varTable));
        }
//        String class = left.getWtv
//                if(class.equals("ArrayOperand")){ //left.getClass().getName().equals("org.specs.comp.ollir.ArrayOperand")
//                    loading(((left.getName())))
//                    loading((left.operand.getName))
//                    Push the aareference and the index into the stack. and that should be enough?
//        }
//        if left.get
        switch (type) {
            case NOPER:
                code.append(loading(((SingleOpInstruction) rhs).getSingleOperand(), varTable));
                break;
            case GETFIELD:
                Element classEle = ((GetFieldInstruction) rhs).getFirstOperand();
                Element field = ((GetFieldInstruction) rhs).getSecondOperand();

                code.append(loading(classEle, varTable));
                code.append("getfield ").append(this.classUnit.getClassName()).append("/");
                code.append(((Operand) field).getName()).append(" ").append(getJasminType(field.getType())).append("\n");
                break;
            case BINARYOPER:
                Element rightElement = ((BinaryOpInstruction) rhs).getRightOperand();
                Element leftElement = ((BinaryOpInstruction) rhs).getLeftOperand();

                Operand o = (Operand) instruction.getDest();
                OperationType operationType = ((BinaryOpInstruction) rhs).getOperation().getOpType();

                BinaryOpInstruction binOp = (BinaryOpInstruction) instruction.getRhs();


                //iinc is only used in this types of cases: i=i+1
                if (operationType == OperationType.ADD && !((BinaryOpInstruction) rhs).getLeftOperand().isLiteral() && ((Operand) binOp.getLeftOperand()).getName().equals(o.getName()) && ((BinaryOpInstruction) rhs).getRightOperand().isLiteral()
                        && Integer.parseInt(((LiteralElement) binOp.getRightOperand()).getLiteral()) == 1) {
                    code.append("iinc ").append(varTable.get(o.getName()).getVirtualReg()).append(" 1\n");
                    return code.toString();
                } else if (operationType == OperationType.ADD && ((BinaryOpInstruction) rhs).getLeftOperand().isLiteral() && !((BinaryOpInstruction) rhs).getRightOperand().isLiteral()
                        && ((Operand) binOp.getRightOperand()).getName().equals(o.getName()) && Integer.parseInt(((LiteralElement) binOp.getLeftOperand()).getLiteral()) == 1) {
                    code.append("iinc ").append(varTable.get(o.getName()).getVirtualReg()).append(" 1\n");
                    return code.toString();
                } else if (operationType == OperationType.ADD || operationType == OperationType.DIV || operationType == OperationType.MUL || operationType == OperationType.SUB) {

                    code.append(loading(leftElement, varTable));
                    code.append(loading(rightElement, varTable));
                    code.append(getOp(operationType));
                }
                if (operationType == OperationType.ANDB) {
                    code.append(loading(leftElement, varTable)).append("ifeq " + getLabel() + "\n");
                    this.decrementStackCounter(1);

                    code.append(loading(rightElement, varTable)).append("ifeq " + getLabel() + "\n");
                    this.decrementStackCounter(1);

                    code.append("iconst_1\n").append("goto " + getEndIf() + "\n").append(getLabel() + ":\n").append("    iconst_0\n").append(getEndIf() + ":\n");
                    this.conditional++;
                    this.incrementStackCounter(1); //iconst

                }
                if (operationType == OperationType.LTH || operationType == OperationType.LTE) {
                    String leftExp = loading(leftElement, varTable);
                    String rightExp = loading(rightElement, varTable);
                    code.append(leftExp + rightExp).append(this.getRelationalOp(operationType, getLabel()))
                            .append("iconst_0\n").append("goto ").append(getEndIf() + "\n")
                            .append(getLabel() + ":\n").append("    iconst_1\n").append(getEndIf() + ":\n");
                    //if_icmp->-2, iconst->+1
                    //-2+1= -1
                    this.decrementStackCounter(1);
                    this.conditional++;
                }

                break;
            case CALL:
                code.append(getCallCode((CallInstruction) rhs, varTable));
                code.append(storing((Operand) left, varTable));
                return code.toString();
            default:
                return "";


        }

        code.append(storing((Operand) left, varTable));
        return code.toString();
    }

    public String getOp(OperationType operationType) {
        StringBuilder result = new StringBuilder();
        switch (operationType) {
            case MUL:
                result.append("imul \n");
                break;
            case DIV:
                result.append("idiv \n");
                break;
            case ADD:
                result.append("iadd \n");
                break;
            case SUB:
                result.append("isub \n");
                break;
            default:
                throw new NotImplementedException(this);
        }
        this.decrementStackCounter(1);
        return result.toString();
    }

    public String getSingleOpCode(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        code.append(loading(instruction.getSingleOperand(), varTable));
        return code.toString();
    }

    public String getBinaryOpCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        switch (instruction.getOperation().getOpType()) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                code.append(this.getIntegerOpCode(instruction, varTable));
                return code.toString();
            case LTH:
            case LTE:
            case ANDB:
            case NOTB:
                code.append(this.getBooleanOpCode(instruction, varTable));
                return code.toString();
            default:
                return "steve";
        }
    }

    public String getIntegerOpCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        String left = loading(instruction.getLeftOperand(), varTable);
        String right = loading(instruction.getRightOperand(), varTable);


        switch (instruction.getOperation().getOpType()) {
            case ADD:
                code.append(left + right + "iadd\n");
                break;
            case SUB:
                code.append(left + right + "isub\n");
                break;
            case MUL:
                code.append(left + right + "imul\n");
                break;
            case DIV:
                code.append(left + right + "idiv\n");
                break;
            default:
                return "robin";
        }
        this.decrementStackCounter(1);
        return code.toString();
    }

    public String getBooleanOpCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        switch (instruction.getOperation().getOpType()) {
            case LTH:
            case LTE:
                String left = loading(instruction.getLeftOperand(), varTable);
                String right = loading(instruction.getRightOperand(), varTable);
                code.append(left + right).append(this.getRelationalOp(instruction.getOperation().getOpType(), getLabel()))
                        .append("iconst_0\n").append("goto ").append(getEndIf() + "\n")
                        .append(getLabel() + ":\n").append("iconst_1\n").append(getEndIf() + ":\n");
                //if_icmp->-2, iconst->+1
                //-2+1= -1
                this.decrementStackCounter(1);
                break;
            case ANDB:
                code.append(loading(instruction.getLeftOperand(), varTable)).append("ifeq " + getLabel() + "\n");
                this.decrementStackCounter(1);

                code.append(loading(instruction.getRightOperand(), varTable)).append("ifeq " + getLabel() + "\n");
                this.decrementStackCounter(1);

                code.append("iconst_1\n").append("goto " + getEndIf() + "\n").append(getLabel() + "\n").append("iconst_0\n").append(getEndIf() + ":\n");
                this.incrementStackCounter(1); //iconst
                break;
            case NOTB:
                code.append(loading(instruction.getLeftOperand(), varTable)).append("ifne " + getLabel() + "\n")
                        .append("iconst_1\n").append("goto " + getEndIf() + "\n").append("iconst_0\n").append(getEndIf() + ":\n");
                break;
            default:
                return "";
        }

        this.conditional++;
        return code.toString();
    }

    public String getRelationalOp(OperationType operationType, String label) {
        StringBuilder code = new StringBuilder();
        switch (operationType) {
            case LTH:
                //See if config has optimize flag as true and do this: -o
                if (config.get("optimize").equals("true")) {
                    code.append("if_icmplt ").append(getLabel() + "\n");
                } else {
                    code.append("isub\n").append("iflt ").append(getLabel() + "\n");
                }

                break;
            case LTE:
                code.append("if_icmple ").append(getLabel() + "\n");
                break;
            default:
                return "";
        }
        return code.toString();
    }

    public String getCallCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        switch (instruction.getInvocationType()) {
            case invokestatic:
                code.append(this.getInvokeStatic(instruction, varTable, ((ClassType) instruction.getFirstArg().getType()).getName()));
                this.decrementStackCounter(instruction.getNumOperands());
                break;
            case invokespecial:
                code.append(this.getInvokeSpecial(instruction, varTable, ((ClassType) instruction.getFirstArg().getType()).getName()));
//                this.decrementStackCounter(instruction.getNumOperands());
                break;
            case invokevirtual:
                code.append(this.getInvokeVirtual(instruction, varTable, ((ClassType) instruction.getFirstArg().getType()).getName()));
                this.decrementStackCounter(instruction.getNumOperands());
                break;
            case arraylength:
                code.append(this.loading(instruction.getFirstArg(), varTable));
                code.append("arraylength\n");
                break;
            case NEW:
                code.append(this.getNewCode(instruction, varTable));
                break;
            default:
                return "emma";
        }
        return code.toString();
    }

    private String getInvokeVirtual(CallInstruction method, HashMap<String, Descriptor> varTable, String className) {
        StringBuilder code = new StringBuilder();
        ArrayList<Element> operands = method.getListOfOperands();
        Type returnType = method.getReturnType();
        Element firstArg = method.getFirstArg();

        String name = ((ClassType) firstArg.getType()).getName();
        String methodCall = ((LiteralElement) method.getSecondArg()).getLiteral();

        code.append(loading(firstArg, varTable));

        for (Element operand : operands) {
            code.append(loading(operand, varTable));
        }

        code.append("invokevirtual ").append(className).append(".");
        code.append(methodCall.replace("\"", ""));

        code.append("(");

        for (var operand : operands) {
            code.append(getArgumentCode(operand));
        }
        code.append(")");

        code.append(getJasminType(returnType)).append("\n");

        return code.toString();
    }

    private String getInvokeSpecial(CallInstruction method, HashMap<String, Descriptor> varTable, String className) {
        StringBuilder code = new StringBuilder();
        ArrayList<Element> parameters = method.getListOfOperands();
        String methodName = ((LiteralElement) method.getSecondArg()).getLiteral().replace("\"", "");
        Element classElement = method.getFirstArg();
        Type returnType = method.getReturnType();

        for (Element param : parameters) {
            code.append(loading(param, varTable));
        }
        code.append("invokespecial ").append(((ClassType) classElement.getType()).getName());
        code.append(".").append(methodName);

        code.append("(");

        for (var operand : parameters) {
            code.append(getArgumentCode(operand));
        }

        code.append(")");
        code.append(getJasminType(returnType)).append("\n");

        return code.toString();
    }

    public String getInvokeStatic(CallInstruction method, HashMap<String, Descriptor> varTable, String className) {
        var result = new StringBuilder();

        ArrayList<Element> parameters = method.getListOfOperands();
        Type returnType = method.getReturnType();
        for (Element param : parameters) {
            result.append(loading(param, varTable));
        }
        result.append("invokestatic ");

        var methodClass = ((Operand) method.getFirstArg()).getName();
        result.append(methodClass).append(".");

        String methodName = ((LiteralElement) method.getSecondArg()).getLiteral();
        result.append(methodName.replace("\"", ""));

        result.append("(");

        for (var operand : parameters) {
            result.append(getArgumentCode(operand));
        }

        result.append(")");

        result.append(getJasminType(method.getReturnType()));
        result.append("\n");


        return result.toString();
    }

    private String getArgumentCode(Element operand) {
        StringBuilder result = new StringBuilder();

        result.append(getJasminType(operand.getType()));
        return result.toString();
    }

    //TODO
    public String getCondBranchCode(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        code.append(loading(((SingleOpInstruction) instruction.getCondition()).getSingleOperand(), varTable));
        code.append("iconst_1\n");
        String label = instruction.getLabel();
        this.incrementStackCounter(2);
        code.append("if_icmpeq " + label + "\n");
        return code.toString();
    }

    public String getGotoCode(GotoInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        code.append("goto " + instruction.getLabel() + "\n");
        return code.toString();
    }

    public String getPutFieldCode(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        code.append(this.loading(instruction.getFirstOperand(), varTable)); //push object
        code.append(this.loading(instruction.getThirdOperand(), varTable)); //push const element
        this.decrementStackCounter(2);
        code.append("putfield ").append(this.classUnit.getClassName()).append("/").append(((Operand) instruction.getSecondOperand()).getName()).append(" ").append(getJasminType(instruction.getSecondOperand().getType())).append("\n");
        return code.toString();
    }

    public String getGetFieldCode(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        Element classEle = instruction.getFirstOperand();
        Element field = instruction.getSecondOperand();

        code.append(loading(classEle, varTable));
        code.append("getfield ").append(this.classUnit.getClassName()).append("/");
        code.append(((Operand) field).getName()).append(" ").append(getJasminType(field.getType())).append("\n");

        return code.toString();
    }

    public String getReturnCode(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        if (!instruction.hasReturnValue() || instruction.getOperand().getType().getTypeOfElement().equals(ElementType.VOID)) {
            return "return";
        }
        switch (instruction.getOperand().getType().getTypeOfElement()) {
            case INT32:
            case BOOLEAN:
                code.append(loading(instruction.getOperand(), varTable));
                this.decrementStackCounter(1);
                code.append("ireturn");
                break;
            case ARRAYREF:
            case OBJECTREF:
                code.append(loading(instruction.getOperand(), varTable));
                this.decrementStackCounter(1);
                code.append("areturn");
                break;
            default:
                break;

        }
        return code.toString();
    }

    public String getNewCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        switch (instruction.getFirstArg().getType().getTypeOfElement()) {
            case ARRAYREF:
                code.append(this.loading(instruction.getListOfOperands().get(0), varTable));
                code.append("newarray int\n");
                break;
            case OBJECTREF:
                this.incrementStackCounter(2);
                code.append("new " + this.getObject(((Operand) instruction.getFirstArg()).getName()) + "\ndup\n");
                this.prevNew = this.getObject(((Operand) instruction.getFirstArg()).getName());
                //code.append("new ").append(getJasminType(instruction.getReturnType()).append("\n").append("dup\n");
                break;
            default:
                return "killian";

        }
        return code.toString();
    }

    private void incrementStackCounter(int n) {
        this.stackCounter += n;
        if (stackCounter > maxStackCounter) {
            maxStackCounter = stackCounter;
        }
    }

    private void decrementStackCounter(int n) {
        this.stackCounter -= n;
    }

    private String getRegister(String varName, HashMap<String, Descriptor> varTable) {
        //https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
        //According to wikipedia we have aload_0, aload_1, aload_2, aload_3
        //According to the official Jasmin Website the other aload (and even those I guess) are supposed to be given out like this:
        //aload <var-num>

        int register = varTable.get(varName).getVirtualReg();
        if (register > 3) {
            return " " + register;
        } else {
            return "_" + register;
        }

    }

    private String loading(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        if (element instanceof LiteralElement) { //Element isn't an array
            String number = ((LiteralElement) element).getLiteral();
            this.incrementStackCounter(1);
            int value = Integer.parseInt(number);
            /*
             * bipush <int>, sipush <int> --> push int onto the stack (bipush is a byte and sipush is a short, so 2 bytes)
             * ldc is followed by a constant(integer, floating point number or quoted string --> 8 bytes
             * iconst_m1 (-1), iconst_0, iconst_1, iconst_2, iconst_3, iconst_4, iconst_5*/

            //if(config.get("optimize").equals("true")){
            if (value == -1) {
                code.append("iconst_m1" + "\n");
            } else if (value > -1 && value < 6) {
                code.append("iconst_" + number + "\n");
            } else if (value <= 127 && value >= -128) {
                code.append("bipush " + number + "\n");
            } else if (value >= -32768 && value <= 32767) {
                code.append("sipush " + number + "\n");
            } else {
                code.append("ldc " + number + "\n");
            }
           /* }
            else{
                code.append("ldc " + number + "\n");
            }*/

        } else if (element instanceof ArrayOperand) { //Array
            ArrayOperand operand = (ArrayOperand) element;
            code.append("aload" + this.getRegister(operand.getName(), varTable) + "\n");
            this.incrementStackCounter(1);
            code.append(loading(operand.getIndexOperands().get(0), varTable));
            this.decrementStackCounter(1);
            //iload arrayref, index -> value load an int from an array
            code.append("iaload\n");
        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case THIS:
                    this.incrementStackCounter(1);
                    code.append("aload_0\n");
                    break;
                //Boolean and i32 are both treated like int in jasmin --> use iload
                case INT32:
                case BOOLEAN:
                    this.incrementStackCounter(1);
                    code.append("iload" + this.getRegister(operand.getName(), varTable) + "\n");
                    break;
                case OBJECTREF:
                case ARRAYREF:
                    this.incrementStackCounter(1);
                    code.append("aload" + this.getRegister(operand.getName(), varTable) + "\n");
                    break;
                default:
                    return "";
            }
        }
        return code.toString();
    }

    private String storing(Operand operand, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();
        ElementType type = operand.getType().getTypeOfElement();
        Boolean isArray = false;
        if (operand instanceof ArrayOperand) {
            isArray = true;
        }
        String op = "";
        String normal;
        Integer decrement;
        switch (type) {
            case INT32:
            case BOOLEAN:
                normal = "istore" + this.getRegister(operand.getName(), varTable);
                op = isArray ? "iastore" : normal;
                this.decrementStackCounter(1);
                code.append(op + "\n");
                break;
            case OBJECTREF:
            case ARRAYREF:
                normal = "astore" + this.getRegister(operand.getName(), varTable);
                op = isArray ? "aastore" : normal;
                this.decrementStackCounter(1);
                code.append(op + "\n");
                break;
            default:
                return "";
        }
        return code.toString();
    }

    private String getLabel() {
        String label = "label" + this.conditional;
        return label;
    }

    private String getEndIf() {
        return "endif" + this.conditional;
    }


}
