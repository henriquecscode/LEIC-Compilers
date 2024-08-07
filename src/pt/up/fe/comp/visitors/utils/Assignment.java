package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.Instruction;

public class Assignment {

    Instruction inst;
    public Assignment(Instruction inst){
        this.inst = inst;
    }

    public Instruction getInst() {
        return inst;
    }
}
