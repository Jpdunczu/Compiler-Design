/*
   --   identifiers must be stored per the language standard.
   --   Case Sensitive
   --   symbol table entries must be expandable.  
   --   the table must be able to handle multiple entries of the  same name.   
   --   FIND should be an efficient routine; it will be called often. INSERT  should  be  at least relatively efficient.   
   --   FIND must return more than a simple boolean;  
   --   retrieving the information in the symbol table 
   --   FIND and INSERT must be separate routines; 

 */
package cs4110.homework.pkg1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
/**
 *
 * @author joshuaduncan
 */
public class SymbolTable {
    
    private int scope;  // global to keep track of the scope we are currently creating Tokens for.
    private int scopeCount; // global to keep track of the total number of scopes.
    private ArrayList[] table; 
    private final ArrayList<ArrayList[]> stack;
    private ArrayList<LexicalAnalyzer.Token> list;
    LexicalAnalyzer.Token token;
    Outputer output;
    int offSet;
    
    
    public SymbolTable(Outputer output){
        stack = new <ArrayList[]>ArrayList();
        stack.add(0, new ArrayList[11]);
        this.output = output;
    }
    
    public void insertEntry(LexicalAnalyzer.Token token){
        
        
        this.offSet = offSet + token.offSet;
        int hash = getHash(token.lexeme);
        
        switch (token.lexeme.toLowerCase()){
            case "begin":
                table = new ArrayList[11];
                this.scopeCount++;
                stack.add(scopeCount, table);
                scope = scopeCount;
                break;
            case "end":
                scope -= 1;
                break;
            default:
                insert(token, hash);
                break;
        }
    }
 
    
    /*************
    *   DISPLAY  *
    *************/
    public void displayList(ArrayList<LexicalAnalyzer.Token> list){
        for( LexicalAnalyzer.Token temp : list ) {
            System.out.print(temp.lexeme+", ");
        }
    }
    
    public void display(){
        int currentScope = 0;
        while( currentScope <= scopeCount ){
            
            System.out.println("\nDisplaying Tokens in Scope: "+currentScope);
                table = new ArrayList[11];
                if( stack.get(currentScope) != null )
                    table = stack.get(currentScope);
                for( int j = 0; j < table.length; j++ ){
                    if( table[j] != null )
                        displayList(table[j]);
                } 
            System.out.println("\n");
            currentScope++;
        }
    }
    
    /************
    *   FIND    *
    ************/
    public LexicalAnalyzer.Token find(String value){
        int hash = getHash(value);
        int currentScope = 0;
        while( currentScope <= scopeCount ){
            table = stack.get(currentScope);
            if( table[hash] != null ){
                list = table[hash];
                for( int j = 0; j < list.size(); j++ ){
                    if(list.get(j).lexeme.equals(value)){
                        return list.get(j);
                    }
                }
            }else{
                return null;
            }
            currentScope++;
        }
        return null;
    }
    
    /************
    *   INSERT  *
    ************/
    public void insert(LexicalAnalyzer.Token token, int hash){
        table = stack.get(scope);
        if( table[hash] == null )
            table[hash] = new <LexicalAnalyzer.Token>ArrayList();
        
        table[hash].add(token);
        //LexicalAnalyzer.Token test = (LexicalAnalyzer.Token)table[hash].get(0);
        output.dTree("SymbolTable insert: "+token.lexeme+" of type: "+token.strType+", at Offset: "+offSet+" and isConstant="+token.isConstant);
        //System.out.println("SymbolTable inserted: "+test.lexeme+" of type: "+test.strType+" in scope: "+scope+", at hash: "+hash+" and isConstant="+token.isConstant);
        stack.set(scope,table);
        //ArrayList[] table2 = stack.get(scope);
        //LexicalAnalyzer.Token tester = (LexicalAnalyzer.Token)table2[hash].get(0);
        //System.out.println("SymbolTable.insert: tester.lexeme = "+tester.lexeme);
        
    }
    
    /********************
    *   FIND IN SCOPES  *
    ********************/
    public LexicalAnalyzer.Token findInCurrentScope(String value){
        int hash = getHash(value);
        table = stack.get(scope);
            if( table[hash] != null ){                  
                list = table[hash];    
                for( int j = 0; j < list.size(); j++ ){
                    if(list.get(j).lexeme.equals(value)) {
                        return list.get(j);
                    }
                }
            }
        return null;
    }
    
    public LexicalAnalyzer.Token findInAllScopes(String value){
        //System.out.println("SymbolTable.findInAllScopes: value = "+value);
        int hash = getHash(value);
        int currentScope = 0;
        while( currentScope <= scopeCount ){
            //System.out.println("SymbolTable.findInAllScopes: currentScope = "+currentScope);
            table = stack.get(currentScope);
            if( table[hash] != null ){
                list = table[hash];
                for( int j = 0; j < table[hash].size(); j++ ){
                    if(list.get(j).lexeme.equals(value)){
                        //System.out.println("SymbolTable.findInAllScopes: list.get(j).lexeme = "+list.get(j).lexeme);
                        return list.get(j);
                    }
                }
            } //else {System.out.println("SymbolTable.findInAllScopes: "+" couldnt find: "+value+", table[hash] == null.");}
            currentScope++;
        }
        return null;
    }
    
    public int getHash(String value) {
        int hash = value.hashCode()%11;
        if( hash < 0 ){
            hash *= -1;
        }
        return hash;
    }
    
    public void changeOffSet(int num){
        this.offSet += num;
    }
}
