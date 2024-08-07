package pt.up.fe.comp.ollir;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Optional;

public class MutableSymbol {
    private Type type;
    private String name;
    private Optional<MutableSymbol> index;

    public MutableSymbol(String name) {
        this.type = new Type("void", false);
        this.name = name;
        this.index = null;
    }

    public void setSymbol(MutableSymbol symbol){
        if (symbol.isArray()) {
            this.setType(symbol.getType());
            if(name.equals("int")){
                this.setIndex(symbol.getIndex());
            }

        } else {
            this.setType(symbol.getType());
        }
    }
    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIndex(MutableSymbol symbol) {
        this.index = Optional.ofNullable(symbol);
    }

    public Boolean isArray(){
        return this.type.isArray();
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public MutableSymbol getIndex(){
        return index.get();
    }

    public String getOllir() {
        if (this.type.isArray()) {
            if(this.index!= null) {
                return this.name + "[" + this.index.get().getOllir() + "]" + OllirUtils.getVariableType(this.type.getName());
            }
            else{
                return this.name + OllirUtils.getVariableType(this.type);

            }
        } else {
            return this.name + OllirUtils.getVariableType(this.type.getName());
        }
    }

    @Override
    public String toString() {
        return "Symbol [type=" + type + ", name=" + name + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MutableSymbol other = (MutableSymbol) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public String print() {
        var builder = new StringBuilder();

        builder.append(getType().print() + " " + getName());

        return builder.toString();
    }
}

