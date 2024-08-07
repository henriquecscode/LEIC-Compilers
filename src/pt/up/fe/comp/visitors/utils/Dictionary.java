package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Operand;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dictionary {
    HashMap<Instruction, Definition> generators = new HashMap<>();
    HashMap<String, Definition> latestDefinition = new HashMap<>();
    HashMap<String, List<Definition>> definitions = new HashMap<>();
    Dictionary(){}

    public Definition addDefinition(String name, Instruction inst){
        Definition latest = this.latestDefinition.get(name);
        Integer index = 0;
        if(!definitions.containsKey(name)){
            ArrayList<Definition> nameDefinitions = new ArrayList<>();
            definitions.put(name, nameDefinitions);
            index = 0;
        }
        else{
            List<Definition> nameDefinitions = definitions.get(name);
            index = nameDefinitions.get(nameDefinitions.size()-1).getIndex()+1;
        }
        Definition newDefinition = new Definition(name, index, inst);
        latestDefinition.put(name, newDefinition);
        generators.put(inst, newDefinition);
        definitions.get(name).add(newDefinition);
        return newDefinition;
    }

    public Definition getDefinition(Instruction instruction){
        return generators.get(instruction);
    }

//    for each isntruction ->
//    instruction -> basic block -> definitions -> somehow we can calculate the available definitions in eacch part of the cfg!
}
