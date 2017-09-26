/*
   --   iden
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
    private ArrayList[] table; //global so it lasts the life of the class.
    
    /*
        global variables for multiple entries of the same value, once an entry 
        is found to exist, these can be set to access the location directly in 
        another method without having to re-loop;
    */
    private int innerRow;
        
    SymbolTable(ArrayList<String> entry){
        table = new ArrayList[51];
        boolean start = false;                  // bool to check for proper start syntax.
        
        //create the Lexemes
        for(int i = 0; i < entry.size(); i++){  
            int hash = entry.get(i).hashCode()%51;  
            
            /*
            *    Check/update scope
            */
            if(entry.get(i).equals("{")){   
                if(!start)
                    start = true;
                else 
                    newScope(1);
            } else if (entry.get(i).equals("}")){
                newScope(-1);
            }
            
            if( !findInAllScopes(entry.get(i),hash) ){
                insert(entry.get(i),hash);  
            }else{
                insertDuplicate(entry.get(i), hash);
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
        this.scope += value;
    }
    
    public int getScope(){
        return this.scope;
    }
    
    public void display(){
        for( int i = 0; i < table.length; i++ ){
            if(table[i] != null){
                ArrayList<Token> list = table[i];
                for( int j = 0; j < list.size(); j++ ){
                    System.out.print(list.get(j).getValue());
                    //System.out.print(list.get(j).getScope());
                    if( list.get(j).getCount() == 1 )
                        System.out.print("\n");
                }
                
            }
        }
    }
    
    public Token find(String value){
        int hash = value.hashCode()%51;
        ArrayList<Token> list = table[hash];
        for( int j = 0; j < list.size(); j++ ){              
            if( list.get(j).getValue().equals(value) ){
                return list.get(j).getThis();
            }
        }
        return null;
    }
    
    public void insert(String value, int loc){
        Token newToken = new Token(value,this.scope);
        newToken.setCount(1);
        table[loc].add(newToken);
    }
    
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

    private void insertDuplicate(String value, int hash) {
        Token newToken = new Token(value,this.scope);
        Token temp = (Token) table[hash].get(innerRow);
        newToken.setCount(temp.getCount() +1);
        table[hash].add(newToken);
    }
}
