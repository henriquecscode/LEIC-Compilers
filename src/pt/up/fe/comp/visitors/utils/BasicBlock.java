package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.*;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {
    List<Instruction> instructions = new ArrayList<>();
    List<BasicBlock> successor = new ArrayList<>();
    List<BasicBlock> predecessor = new ArrayList<>();
    Instruction leader, exit;

    //For each instruction what happens
    List<Definition> genDefs = new ArrayList<>();
    List<String> killDefs = new ArrayList<>();
    //For each instruction what is available inside the block
    List<List<Definition>> stepGen = new ArrayList<>();
    List<List<String>> stepKill = new ArrayList<>();
    // As a whole, what the blocks does
    Map<String, Definition> outGen = new HashMap<>();
    List<String> outKill = new ArrayList<>();

    // Fixed point operation
    public Set<Definition> inResult = new HashSet<>();
    public Set<Definition> outResult = new HashSet<>();

    //
    public List<List<Definition>> stepResult = new ArrayList<>();


    BasicBlock(Instruction leader) {
        instructions.add(leader);
        this.leader = leader;
        this.exit = leader;
    }

    BasicBlock(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void addInstruction(Instruction inst) {
        this.instructions.add(inst);
        exit = inst;
    }

    public void addSucBlock(BasicBlock block) {
        if (!this.successor.contains(block)) {
            this.successor.add(block);
        }
        if (!block.predecessor.contains(this)) {
            block.predecessor.add(this);
        }
    }

    public void addEntriesToDictionaries(Dictionary dict) {
        for (Instruction inst : instructions) {
            Definition def = null;
            if (inst.getInstType() == InstructionType.ASSIGN) {
                Operand assignee = (Operand) ((AssignInstruction) inst).getDest();
                String name = assignee.getName();
                def = dict.addDefinition(name, inst);
            }
            this.updateSets(def);
        }
    }

    public void updateSets(Definition def) {
        if (def == null) {
            genDefs.add(null);
            killDefs.add(null);
        } else {
            genDefs.add(def);
            killDefs.add(def.getName());
            stepKill.add(new ArrayList<>(killDefs));
        }
    }

    public void calculateSets(Dictionary dict) {
        outKill = killDefs; // Killing is based on definitions, period
        for (Instruction inst : instructions) {
            if (inst instanceof AssignInstruction) {
                String name = BasicBlock.getInstructionVar((AssignInstruction) inst);
                Definition def = dict.getDefinition(inst);
                outGen.put(name, def); // This will automatically override definitions
            }
            stepGen.add(new ArrayList<>(outGen.values()));
        }
    }

    static public String getInstructionVar(AssignInstruction inst) {
        return ((Operand) (inst).getDest()).getName();
    }

    public void setAggregateStart(Dictionary dict) {
        if (this.leader.getId() != 1) {
            inResult = new HashSet<>(dict.generators.values());
        } else {
            inResult = new HashSet<>();
        }
        outResult = new HashSet<>(dict.generators.values());
    }

    public Boolean calculateAggregate() {
        Boolean changedIn = this.calculateAggregateIn();
        Boolean changeOut = this.calculateAggregateOut();
        return changedIn || changeOut;
    }

    public Boolean calculateAggregateIn() {
        if (predecessor.size() == 0) {
            return false;
        }
        HashSet tempResult = new HashSet<>(predecessor.get(0).outResult);
//        inResult = predecessor.get(0).outResult;
        for (int i = 1; i < predecessor.size(); i++) {
            tempResult.retainAll(predecessor.get(i).outResult);
        }

        Boolean changes = !inResult.equals(tempResult);
        inResult = new HashSet<>(tempResult);
        return changes;
    }

    public Boolean calculateAggregateOut() {
        HashMap<String, Definition> tempResult = new HashMap<>();
        for (Definition def : inResult) {
            String varName = def.getName();
            if (outKill.contains(varName)) {
                continue;
            } else {
                tempResult.put(varName, def);
            }
        }
        HashMap<String, Definition> tempAfterSub = new HashMap<>(tempResult);
        for (Definition def : outGen.values()) {
            tempAfterSub.put(def.getName(), def);
        }
        HashSet<Definition> setTempResult = new HashSet<>(tempAfterSub.values());
        Boolean changes = !outResult.equals(setTempResult);
        outResult = new HashSet<>(setTempResult);
        return changes;
    }

    public void calculateAggregateStep(Dictionary dict){
        HashMap<String, Definition> iterator = new HashMap<>();
        for(Definition def: inResult){
            iterator.put(def.getName(), def);
        }

        for (Instruction inst : instructions) {
            if (inst instanceof AssignInstruction) {
                String name = BasicBlock.getInstructionVar((AssignInstruction) inst);
                Definition def = dict.getDefinition(inst);
                iterator.put(name, def); // This will automatically override definitions
            }
            stepResult.add(new ArrayList<>(iterator.values()));
        }

    }
}
