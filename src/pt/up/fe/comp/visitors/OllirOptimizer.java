package pt.up.fe.comp.visitors;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.visitors.utils.ConstantPropagation;
import pt.up.fe.comp.visitors.utils.DFA;

import java.util.*;

public class OllirOptimizer {

    HashMap<Method, DFA> methodDFAs = new HashMap<>();
    public OllirOptimizer(ClassUnit classUnit) {
        classUnit.buildCFGs();
        this.computeCFA(classUnit);
    }

    private void computeCFA(ClassUnit classUnit) {
        for(Method method: classUnit.getMethods()){
            DFA dfa = new DFA(method);
            methodDFAs.put(method, dfa);
        }
    }

    public void constantPropagation(ClassUnit classUnit) {
        for (Method method : classUnit.getMethods()) {
            DFA dfa = methodDFAs.get(method);
            new ConstantPropagation(dfa);
//            this.constantPropagation(method, classUnit, dfa);
        }
    }

    public void constantPropagation(Method method, ClassUnit classUnit, DFA dfa) {
        Map<String, Set<Instruction>> assignmentInstructions = new HashMap<>(); //Mapping between variables and their assignments
        for (Instruction inst : method.getInstructions()) {
            if (inst instanceof AssignInstruction) {
                Operand var = (Operand) ((AssignInstruction) inst).getDest();
                String name = var.getName();
                Set instructions = assignmentInstructions.get(name);
                if (instructions == null) {
                    instructions = new HashSet<>();
                    instructions.add(((AssignInstruction) inst).getRhs());
                    assignmentInstructions.put(name, instructions);
                } else {
                    instructions.add(inst);
                }

            }
        }
        for (Instruction inst : method.getInstructions()) {
            if (inst instanceof AssignInstruction) {
                // Check if it is a instruction that we can act upon, meaning it is not the first assignment?
                Instruction rhs = ((AssignInstruction) inst).getRhs();
                if (rhs instanceof SingleOpInstruction) {
                    Element element = ((SingleOpInstruction) rhs).getSingleOperand();
                    if (element instanceof Operand) {
                        String name = ((Operand) element).getName();
                        if (isConstant(name, assignmentInstructions, method, classUnit)) {
                            // we can substitute the operand
                            Instruction constantAssign = assignmentInstructions.get(name).iterator().next();
                            if (constantAssign instanceof SingleOpInstruction) {
                                Element constant = ((SingleOpInstruction) constantAssign).getSingleOperand();
                                ((SingleOpInstruction) rhs).setSingleOperand(constant);
                            }
                        }
                    }
                } else if (rhs instanceof BinaryOpInstruction) {
                    BinaryOpInstruction biOp = (BinaryOpInstruction) rhs;
                    Element leftElement = biOp.getLeftOperand();
                    if (leftElement instanceof Operand) {
                        String leftName = ((Operand) leftElement).getName();
                        if (isConstant(leftName, assignmentInstructions, method, classUnit)) {
                            // we can substitute the operand
                            Instruction constantAssign = assignmentInstructions.get(leftName).iterator().next();
                            if (constantAssign instanceof SingleOpInstruction) {
                                Element constant = ((SingleOpInstruction) constantAssign).getSingleOperand();
                                biOp.setLeftOperand(constant);
                            }
                        }
                    }
                    Element rightElement = biOp.getRightOperand();
                    if (rightElement instanceof Operand) {
                        String rightName = ((Operand) rightElement).getName();
                        if (isConstant(rightName, assignmentInstructions, method, classUnit)) {
                            // we can substitute the operand
                            Instruction constantAssign = assignmentInstructions.get(rightName).iterator().next();
                            if (constantAssign instanceof SingleOpInstruction) {
                                Element constant = ((SingleOpInstruction) constantAssign).getSingleOperand();
                                biOp.setRightOperand(constant);
                            }
                        }
                    }
                }
            } else if (inst instanceof ReturnInstruction) {
                Element element = ((ReturnInstruction) inst).getOperand();
                if (element instanceof Operand) {
                    String name = ((Operand) element).getName();

                    if (isConstant(name, assignmentInstructions, method, classUnit)) {
                        // we can substitute the operand
                        Instruction constantAssign = assignmentInstructions.get(name).iterator().next();
                        if (constantAssign instanceof SingleOpInstruction) {
                            Element constant = ((SingleOpInstruction) constantAssign).getSingleOperand();
                            ((ReturnInstruction) inst).setOperand(constant);
                        }
                    }
                    System.out.println(inst);
                }
            }
        }
    }

    private Boolean isConstant(String name, Map<String, Set<Instruction>> assignments, Method method, ClassUnit classUnit) {
        //If is field
        List<Field> fieldList = classUnit.getFields();
        Optional<Field> isField = fieldList.stream().filter(x -> x.getFieldName().equals(name)).findFirst();
        if (isField.isPresent()) {
            return false;
        }

        //If is param
        ArrayList<Element> paramList = method.getParams();
        Optional<Element> isParam = paramList.stream().filter(x -> ((Operand) x).getName().equals(name)).findFirst();
        if (isParam.isPresent()) {
            return false;
        }

        // If is a local with more than 1 declaration
        if (assignments.get(name).size() > 1) {
            return false;
        }
        return true;
    }

}
