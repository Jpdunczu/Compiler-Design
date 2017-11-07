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
    private ArrayList[] table; //global so it lasts the life of the class.
    private ArrayList<ArrayList[]> stack = new ArrayList();
    private ArrayList<LexicalAnalyzer.Token> list;
    private static List<String> keywords;
    LexicalAnalyzer.Token token;
    Outputer output;
    private int offSet;
    
    
    public SymbolTable(){
        // default constructor
        keywords = new ArrayList<>(Arrays.asList("begin", "constant", "declare", "end", "get", "if", 
                "is", "loop", "not", "procedure", "put", "put_line", "then", "while"));
    }
    public SymbolTable(Outputer output){
        this.output = output;
    }
    
    public void insertEntry(LexicalAnalyzer.Token token){
        stack.add(0, new ArrayList[11]);
        //create the Lexemes
        //for(int i = 0; i < entry.size(); i++)
        
        this.offSet = offSet + token.offSet;
        int hash = getHash(token.lexeme);
        /*
         *    Check/update scope
         */
        switch (token.lexeme.toLowerCase()) {
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
    public void displayList(ArrayList<Token> list){
        for( Token temp : list ) {
            System.out.print(temp.getValue()+", ");
        }
    }
    
    public void display(){
        int currentScope = 0;
        while( currentScope <= scopeCount ) {
            
            System.out.println("\nDisplaying Tokens in Scope: "+currentScope);
                table = new ArrayList[11];
                if( stack.get(currentScope) != null )
                    table = stack.get(currentScope);
                for( int j = 0; j < table.length; j++ ) {
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
        while( currentScope <= scopeCount ) {
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
        
        //System.out.println("\nInsert:: Scope: " + scope + " Value: " + value+"\n");
        if ( findInCurrentScope(token.lexeme) == null ){
            table = stack.get(scope);
        
        if( table[hash] == null ) 
                table[hash] = new <Token>ArrayList();
            table[hash].add(token);
            output.dTree(token.lexeme+" is type: "+token.strType+", Offset: "+offSet+" and isConstant="+token.isConstant);
            stack.set(scope,table);
        }
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
    
    public LexicalAnalyzer.Token findInAllScopes(String value) {
        int hash = getHash(value);
        int currentScope = 0;
        while( currentScope <= scopeCount ) {
            table = stack.get(currentScope);
            if( table[hash] != null ){
                list = table[hash];
                for( int j = 0; j < list.size(); j++ ){
                    if(list.get(j).lexeme.equals(value)){
                        return list.get(j);
                    }
                }
            }
            currentScope++;
        }
        return null;
    }
    
    public static int isKeyword(String word) {
        for( String keyword : keywords ){
            if( keyword.equals(word) )
                return keywords.indexOf(word);
        } 
        return -1;
    }
    
    public int getHash(String value) {
        int hash = value.hashCode()%11;
        if( hash < 0 ){
            hash *= -1;
        }
        return hash;
    }
}
