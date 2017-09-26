/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

/**
 *
 * @author joshuaduncan
 */
public class Token {
    private String value;
    private int scope;
    private int count;
    
    Token(String value, int scope) {
        this.value = value;
        this.scope = scope;
    }
    
    public String getValue(){
        return this.value;
    }
    
    public int getScope(){
        return this.scope;
    }
    
    public Token getThis(){
        return this;
    }
    
    public void setCount(int count){
        this.count += count;
    }
    
    public int getCount(){
        return this.count;
    }
}
