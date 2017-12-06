
package cs4110.homework.pkg1;
import java.util.ArrayList;
/**
 *
 * @author joshuaduncan
 */
public class SymbolTable {
    
    private int scope;  // keeps track of the current scope.
    private int scopeCount; // keeps track of the total number of scopes.
    private ArrayList[] table; // stores tokens from the current scope.
    private final ArrayList<ArrayList[]> stack; // stores all tables which have been created, essentially a stack of tables.
    
    /**********
    *   LIST
    *   
    *   list of tokens belonging to the same hashed location, or in this case 
    *   the same index of the current scopes table.
    */
    private ArrayList<LexicalAnalyzer.Token> list;  
    
    LexicalAnalyzer.Token token;    // tokens which are passed from the Lexical Analyzer to the symbol table for storage.
    Outputer output;    //  used for printing output 
    int offSet; //  keeps track of the current offset of the MIPS pseudo code stack pointer, for code generation. Each token inserted 
                //  to the symbol table contains an integer value which represents the size of the variable, 
                //  integers and booleans == -4, and floats == -8.
    
    /********
    *   SYMBOLTABLE
    * 
    *   @param output
    * 
    *   This is the constructor function for the class, it takes an Outputer 
    *   object as it's parameter, this is just a dummy class which i created  
    *   to handle print messages for debugging purposes so the main code would 
    *   not become too cluttered.
    */
    public SymbolTable(Outputer output){
        stack = new <ArrayList[]>ArrayList();
        stack.add(0, new ArrayList[11]);
        this.output = output;
    }
    
    /****************
    *   INSERTENTRY
    * 
    *   @param token
    * 
    *   This function takes a Token object as it's parameter, it changes the 
    *   offset to make room for the new token being added to the table, gets 
    *   the hashed value of the tokens lexeme and then checks to see if the 
    *   Token is a 'begin' or 'end' token. In either case we only want to change 
    *   the scope and not add the token to the table, otherwise we pass the token 
    *   and the hashed value of the lexeme to be inserted into the table.
    */
    public void insertEntry(LexicalAnalyzer.Token token){
        
        int temp = offSet;  // save the current offset.
        this.offSet = offSet + token.offSet;    //  add the tokens size to the current offset.
        token.offSet = temp;    //  the offset at which the token was stored.
        
        // get the hashed value of the lexeme.
        int hash = getHash(token.lexeme);
        
        //  check if we are beginning or ending a scope.
        switch (token.lexeme.toLowerCase()){
            case "begin":
                table = new ArrayList[11];  // create a new table for the new scope.
                this.scopeCount++;  // increment the scope count
                stack.add(scopeCount, table);   // add the table to the stack 
                scope = scopeCount; //  update the current scope to the scope we are working in.
                break;
            case "end":
                scope -= 1; // back out one scope.
                break;
            default:
                insert(token, hash);    //  insert the Token into the table.
                break;
        }
    }
 
    
    /*************
    *   DISPLAYLIST and DISPLAY
    *
    *   @param list
    * 
    *   This function is used only for debugging purposes, it lets us know which 
    *   tokens were added to the table and in which scope they are located.
    */
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
    *   INSERT 
    *   
     *  @param token
     *  @param hash
     * 
     *  This function takes 2 parameters, the current token passed to Symbol 
     *  Table by the Lexical Analyzer and the hashed integer value of the Tokens 
     *  lexeme. It gets the table from the stack that represents the current scope 
     *  and checks if there is a table created for this scope or else it will 
     *  create a new one. It then adds the Token to the table at the hashed index.
    **/
    public void insert(LexicalAnalyzer.Token token, int hash){
        table = stack.get(scopeCount);
        if( table[hash] == null )
            table[hash] = new <LexicalAnalyzer.Token>ArrayList();
        
        table[hash].add(token);
        output.dTree("SymbolTable insert: "+token.lexeme+" of type: "+token.strType+", at Offset: "+offSet+" and isConstant="+token.isConstant);
        stack.set(scopeCount,table);
    }
    
    /********************
    *   FIND IN SCOPES  *
     * @param value
     * @return 
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
        int hash = getHash(value);
        int currentScope = 0;
        while( currentScope <= scopeCount ){
            table = stack.get(currentScope);
            if( table[hash] != null ){
                list = table[hash];
                for( int j = 0; j < table[hash].size(); j++ ){
                    if(list.get(j).lexeme.equals(value)){
                        return list.get(j);
                    }
                }
            }
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
    
    public int getOffSet(){
        return offSet;
    }
}
