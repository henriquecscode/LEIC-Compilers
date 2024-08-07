package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DFA {
    private final Method method; // Data Flow Analysis
    private final Dictionary dict = new Dictionary();
    HashMap<Instruction, BasicBlock> blocks = new HashMap<>();

    public DFA(Method method) {
        this.method = method;
        this.assembleBlocks();
        this.calculateDefinitions();
        this.calculateBlockSets();
        this.aggregateGens();
        this.stepGens();
        System.out.println("Calculated DFA for method " + method.getMethodName());
    }


    private void assembleBlocks() {
        List<Node> successors = this.method.getBeginNode().getSuccessors();
        List<Instruction> leaders = new ArrayList<>();
        if (successors.size() == 0) {
            return;
        }
        if (!(successors.get(0) instanceof Instruction)) {
            return;
        }
        Instruction parentInstruction = (Instruction) successors.get(0);
        BasicBlock block = new BasicBlock(parentInstruction);
        blocks.put(parentInstruction, block);
        this.separateBlocks(block, leaders, parentInstruction);
    }

    private void separateBlocks(BasicBlock block, List<Instruction> leaders, Instruction start) {
        List<Node> successors = start.getSuccessors();
        while (true) {
            if (successors.size() == 1) {
                Node suc = successors.get(0);
                if (suc.getNodeType() == NodeType.END) {
                    return;
                }
                Instruction instruction = (Instruction) suc;
                if (blocks.containsKey(instruction)) {
                    BasicBlock sucBlock = blocks.get(instruction);
                    block.addSucBlock(sucBlock);
                    return;
                }
                if (instruction.getPredecessors().size() > 1) {
                    // Found what is probably a label
                    if (!blocks.containsKey(instruction)) {
                        BasicBlock block1 = new BasicBlock(instruction);
                        blocks.put(instruction, block1);
                        block.addSucBlock(block1);
                        separateBlocks(block1, leaders, instruction);
                    }
                    // end of block
                    return;
                }
                if (instruction.getInstType() == InstructionType.GOTO) {
                    Instruction newLeaderInstruction = (Instruction) (instruction).getSucc1();
                    if (blocks.containsKey(newLeaderInstruction)) {
                        BasicBlock sucBlock = blocks.get(newLeaderInstruction);
                        block.addSucBlock(sucBlock);
                        // The destination of the goto has already been accounted for
                    } else {
                        BasicBlock block1 = new BasicBlock(newLeaderInstruction);
                        blocks.put(newLeaderInstruction, block1);
                        block.addSucBlock(block1);
                        separateBlocks(block1, leaders, newLeaderInstruction);
                    }
                    // Is a goto instruction
                    // end of block

                    return;
                }
                block.addInstruction(instruction);
                successors = instruction.getSuccessors();
            } else {
                if (successors.get(0).getNodeType() == NodeType.END && successors.get(1).getNodeType() == NodeType.END) {
                    // buildCFG seems to be dumb
                    return;
                }
                // create two blocks
                Instruction inst1 = (Instruction) successors.get(0);
                BasicBlock block1 = null, block2 = null;
                if (!blocks.containsKey(inst1)) {
                    block1 = new BasicBlock(inst1);
                    blocks.put(inst1, block1);
                    block.addSucBlock(block1);
                }

                Instruction gotoInst = (Instruction) successors.get(1);
                Instruction inst2 = (Instruction) (gotoInst).getSucc1();
                if (!blocks.containsKey(inst2)) {
                    block2 = new BasicBlock(inst2);
                    blocks.put(inst2, block2);
                    block.addSucBlock(block2);

                }
                if (block1 != null) {
                    separateBlocks(block1, leaders, inst1);
                }
                if (block2 != null) {
                    separateBlocks(block2, leaders, inst2);
                }
                return;
            }
        }

    }


    private void calculateDefinitions() {
        for (BasicBlock block : blocks.values()) {
            block.addEntriesToDictionaries(dict);
        }
    }

    private void aggregateGens() {
        for (BasicBlock block : blocks.values()) {
            block.setAggregateStart(dict);
        }

        Boolean changes = true;
        Integer counter = 0;
        while (changes) {
            counter++;
            changes = false;
            for (BasicBlock block : blocks.values()) {
                Boolean aggregateChanges = block.calculateAggregate();
                changes = changes || aggregateChanges;
            }
        }
    }

    private void stepGens(){
        for (BasicBlock block : blocks.values()) {
            block.calculateAggregateStep(dict);
        }
    }

    private void calculateBlockSets() {
        for (BasicBlock block : blocks.values()) {
            block.calculateSets(dict);
        }
    }

}
