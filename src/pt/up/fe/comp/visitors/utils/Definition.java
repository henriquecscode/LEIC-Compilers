package pt.up.fe.comp.visitors.utils;

import org.specs.comp.ollir.AssignInstruction;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.SingleOpInstruction;

import java.util.Objects;

public class Definition {

    private final Instruction inst;
    String name;
    Integer index;

    Definition(String name, Integer index, Instruction instruction) {
        this.name = name;
        this.index = index;
        this.inst = instruction;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        Definition def = (Definition) o;
        return name.equals(def.getName()) && index.equals(def.getIndex());
    }

    public Instruction getInst() {
        return inst;
    }

    public Element getElement() {

        if (inst instanceof AssignInstruction) {

            AssignInstruction assign = (AssignInstruction) inst;
            Instruction rhs = assign.getRhs();
            if (rhs instanceof SingleOpInstruction) {
                Element constant = ((SingleOpInstruction) rhs).getSingleOperand();
                return constant;
            }
            return null;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

    @Override
    public String toString() {
        return name + "_" + String.valueOf(index);
    }
}
