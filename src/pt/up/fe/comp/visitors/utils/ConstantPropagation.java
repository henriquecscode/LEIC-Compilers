package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.*;

import java.util.List;
import java.util.Optional;

public class ConstantPropagation {
    private final DFA dfa;

    public ConstantPropagation(DFA dfa) {
        this.dfa = dfa;
        this.constantPropagation();
    }

    private void constantPropagation() {
        Integer counter = 0;
        Boolean changes = true;
        Boolean newChanges;
        while (changes) {
            changes = false;
            for (BasicBlock block : dfa.blocks.values()) {
                newChanges = constantPropagation(block);
                changes = changes || newChanges;
            }
            counter++;
        }
        System.out.println("Fixed constant propagation point in " + String.valueOf(counter) + " iterations");
    }

    private Boolean constantPropagation(BasicBlock block) {
        Boolean changes = false;
        for (int i = 0; i < block.instructions.size(); i++) {
            List<Definition> definitions = block.stepResult.get(i);
            Instruction inst = block.instructions.get(i);

            if (inst instanceof AssignInstruction) {
                // Check if it is a instruction that we can act upon, meaning it is not the first assignment?
                Instruction rhs = ((AssignInstruction) inst).getRhs();
                if (rhs instanceof SingleOpInstruction) {
                    Element element = ((SingleOpInstruction) rhs).getSingleOperand();
                    if (element instanceof Operand) {
                        String name = ((Operand) element).getName();
                        Optional<Definition> def = definitions.stream().filter(x -> x.getName().equals(name)).findFirst();
                        if (def.isPresent()) {
                            // We have encountered a previous definition of this
                            Element ele = def.get().getElement();
                            if (ele != null) {
                                if (!((SingleOpInstruction) rhs).getSingleOperand().equals(ele)) {
                                    changes = true;
                                }
                                ((SingleOpInstruction) rhs).setSingleOperand(ele);
                            }
                        }
                    }
                } else if (rhs instanceof BinaryOpInstruction) {
                    BinaryOpInstruction biOp = (BinaryOpInstruction) rhs;
                    Element leftElement = biOp.getLeftOperand();
                    if (leftElement instanceof Operand) {
                        String leftName = ((Operand) leftElement).getName();
                        Optional<Definition> leftDef = definitions.stream().filter(x -> x.getName().equals(leftName)).findFirst();
                        if (leftDef.isPresent()) {
                            // We have encountered a previous definition of this
                            Element leftEle = leftDef.get().getElement();
                            if (leftEle != null) {
                                if (!biOp.getLeftOperand().equals(leftEle)) {
                                    changes = true;
                                }
                                biOp.setLeftOperand(leftEle);
                            }
                        }
                    }
                    Element rightElement = biOp.getRightOperand();
                    if (rightElement instanceof Operand) {
                        String rightName = ((Operand) rightElement).getName();
                        Optional<Definition> rightDef = definitions.stream().filter(x -> x.getName().equals(rightName)).findFirst();
                        if (rightDef.isPresent()) {
                            // We have encountered a previous definition of this
                            Element rightEle = rightDef.get().getElement();
                            if (rightEle != null) {
                                if (!biOp.getRightOperand().equals(rightEle)) {
                                    changes = true;
                                }
                                biOp.setRightOperand(rightEle);
                            }
                        }
                    }
                }
            } else if (inst instanceof ReturnInstruction) {
                Element element = ((ReturnInstruction) inst).getOperand();
                if (element instanceof Operand) {
                    String name = ((Operand) element).getName();
                    Optional<Definition> def = definitions.stream().filter(x -> x.getName().equals(name)).findFirst();
                    if (def.isPresent()) {
                        Element ele = def.get().getElement();
                        if (ele != null) {
                            if (!((ReturnInstruction) inst).getOperand().equals(ele)) {
                                changes = true;
                            }
                            ((ReturnInstruction) inst).setOperand(ele);
                        }
                    }
                }
            }

        }
        return changes;
    }
}
