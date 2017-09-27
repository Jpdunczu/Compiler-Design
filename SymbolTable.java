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

/**
 *
 * @author joshuaduncan
 */
public class SymbolTable {
    
    private int scope;  // global so the value is the same when applied to instance objects.
    private int scopeCount; // global to keep track of the total number of scopes.
    private ArrayList[] table; //global so it lasts the life of the class.
    private int innerRow;  //global variable to reduce time spent in loops once a Token is found in a search.
    
    SymbolTable(ArrayList<String> entry){
        table = new ArrayList[51];
        
        //create the Lexemes
        for(int i = 0; i < entry.size(); i++){  
            int hash = entry.get(i).hashCode()%51;  
            if( hash < 0){
                hash*= -1;
            }
            /*
            *    Check/update scope
            */
            if(entry.get(i).equals("{")){ 
                this.scopeCount++;
                newScope(1);
            } else if (entry.get(i).equals("}")){
                newScope(-1);
            }
            if( table[hash] != null){
                if( !findInCurrentScope(entry.get(i), hash)){   
                    if( !findInAllScopes(entry.get(i),hash) ){
                        insert(entry.get(i),hash);              //...insert as new Token
                    }else{
                        insertDuplicate(entry.get(i), hash);    //...insert as Token with same name in different scope.
                    }    
                }
            }else{
                ArrayList<Token> list = new ArrayList();
                table[hash]= list;
                insert(entry.get(i),hash);
            }
        }
        
    }
    
    /*
     * Function to modify the global scope.
     */
    private void newScope(int value){
        if( value < 0 && this.scope <= 0 ){
            // code for future exception and error reporting
            return;
        }
        this.scope = scopeCount + value;
    }
    
    /*************
    *   DISPLAY  *
    *************/
    public void display(){
        ArrayList<Token> display = new ArrayList();
        String duplicate;
        for( int i = 0; i < table.length; i++ ){
            if(table[i] != null){
                ArrayList<Token> list = table[i];
                for( int j = 0; j < list.size(); j++ ){
                    duplicate = "NO";
                    if( list.get(j).getCount() > 1 ){
                        duplicate = "YES";
                    }
                    System.out.print("Token: " + list.get(j).getValue() + "\tScope: " + list.get(j).getScope() + 
                            "\tDuplicate:"+ duplicate + "\tCount: "+ list.get(j).getCount() + "\n");
                }
                
            }
        }
    }
    
    /************
    *   FIND    *
    ************/
    public Token find(String value){
        int hash = value.hashCode()%51;
        if( hash < 0 ){
            hash *= -1;
        }
        if( table[hash] != null ){
            ArrayList<Token> list = table[hash];
            for( int j = 0; j < list.size(); j++ ){              
                if( list.get(j).getValue().equals(value) ){
                    return list.get(j).getThis();
                }
            }
        }
        return null;
    }
    
    /************
    *   INSERT  *
    ************/
    public void insert(String value, int loc){
        Token newToken = new Token(value,this.scope);
        newToken.setCount(1);
        table[loc].add(newToken);
    }
    
    private void insertDuplicate(String value, int hash) {
        Token newToken = new Token(value,this.scope);
        Token temp = (Token) table[hash].get(innerRow);
        newToken.setCount(temp.getCount() +1);
        table[hash].add(newToken);
    }
    
    /********************
    *   FIND IN SCOPES  *
    ********************/
    public boolean findInCurrentScope(String value, int loc){
            if( table[loc] != null ){
                ArrayList<Token> list = table[loc];
                for( int j = 0; j < list.size(); j++ ){
                    if(list.get(j).getValue().equals(value)){
                        if(list.get(j).getScope() == this.scope){
                            return true;
                        }
                    }
                }
            }
        return false;
    }
    
    public boolean findInAllScopes(String value, int loc){
            if( table[loc] != null ){
                ArrayList<Token> list = table[loc];
                for( int j = 0; j < list.size(); j++ ){
                    if(list.get(j).getValue().equals(value)){
                        this.innerRow = j;
                        return true;
                    }
                }
            }
        return false;
    }

    
}
