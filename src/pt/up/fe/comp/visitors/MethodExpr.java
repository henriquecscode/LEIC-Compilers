package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;

public class MethodExpr {
    public Symbol symbol;
    public List<Symbol> param, local;

    public MethodExpr(Symbol symbol, List<Symbol> param, List<Symbol> local){
        this.symbol = symbol;
        this.param = param;
        this.local = local;
    }

    public boolean isMain(){
        return symbol.getName().equals("main");
    }

    public Symbol getSymbol(){
        return this.symbol;
    }

    public List<Symbol> getParameters(){
        return this.param;
    }

    public List<Symbol> getLocal(){
        return this.local;
    }
}