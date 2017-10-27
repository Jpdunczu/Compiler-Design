/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshuaduncan
 */
public class SyntaxAnalyzer {
    
    SymbolTable st;
    LexicalAnalyzer scanner;
    
    String programName;
    
    // check for first 3 non-terminals
    boolean procToken;
    boolean firstIDToken;
    boolean isToken;
    
    boolean beginProgram;
    
    
    /***********************************
    *   VARIABLES FOR OUTPUT DISPLAY   *
    ************************************/
    String lmd = new String();
    
    SyntaxAnalyzer(){
        
    }
    
    public void loadFile(File file) {
        try{
            scanner = new LexicalAnalyzer(file);
        } catch (Exception e){
            
        }
        st = new SymbolTable();
    }
   
    void parse(LexicalAnalyzer.Token token) throws IOException {
        System.out.print("\nThis tokens type is: " +token.type);
            switch(token.type){
                case 16:
                    if(procToken == true) {
                        throwException(token,"Program is invalid, two 'procedure' keywords found.");
                        break;
                    }
                    procToken = true;   // match PROCTOKEN
                    System.out.print(" PROCTOKEN matched.");
                    nextToken();
                    break;
                case 1:
                    if(procToken == true) {
                        if(firstIDToken == false) {
                            firstIDToken = true;    // match IDTOK
                            programName = token.lexeme; // set the program name;
                            System.out.print(" IDTOK matched. Program name is: "+programName);
                            nextToken();
                            break;
                        }
                        else{
                            System.out.println(" {decl} ");
                            appendLMD("3");     // add rule 3 to LMD list
                            checkDecl(token);   // {decl}
                            appendLMD("4");     // add rule 4 to LMD list;
                            break;
                        }
                    } else {
                        throwException(token,"Program is invalid, expected an 'IDTOK'");
                        break;
                    }
                case 13:
                    if(procToken == true && firstIDToken == true) {
                        if(isToken == false) {
                            isToken = true;     // match ISTOKEN
                            beginProgram = true;
                            System.out.print(" ISTOK matched. ");
                            nextToken();
                            break;
                        }
                    } else {
                        throwException(token, "Program is invalid, expected 'IS'.");
                        break;
                    }
                case 7:
                    if(beginProgram == true) {
                        addToTable(token.lexeme);   // match BEGINTOK
                        appendLMD("1");     // add rule 1 to LMD list
                        checkStat(getNextToken());
                        appendLMD("2");     // add rule 2 to LMD list
                        nextToken();
                        break;
                    } else {
                        throwException(token, "Program is invalid.");
                        break;
                    }
                case 10:
                    if(beginProgram == true) {
                        if(st.findInCurrentScope("BEGIN")) {
                            addToTable(token.lexeme);
                            nextToken();
                            break;
                        }
                    } else {
                        throwException(token,"Invalid statement closure, no BEGIN found.");
                        break;
                    }
                default:
                    throwException(token,"Program is invalid.");
            }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    
    public void start(){
        try {
            scanner.startScanning();
            nextToken();
        } catch (IOException ex) {
            Logger.getLogger(SyntaxAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void nextToken() throws IOException {
        parse(scanner.getToken());
    }
    
    public LexicalAnalyzer.Token getNextToken() throws IOException{
        return scanner.getToken();
    }
    
    private void throwException(LexicalAnalyzer.Token token, String message) {
        System.out.println("Terminating parse in routine at line: "+token.foundAt+" on Token: "+token.lexeme+" Error:"+message );
        System.out.println("LMD: "+lmd);
    }
    
    private void addToTable(String entry){
        st.insertEntry(entry);
    }
    
    private void appendLMD(String str){
        this.lmd = lmd.concat(", "+str);
    }
    
    /*****************************
    *   DECL DECLS  |   epsilon  *
    *****************************/
    private void checkDecl(LexicalAnalyzer.Token token) throws IOException {
        
        addToTable(token.lexeme);   // add the IDTOK to the table...
        LexicalAnalyzer.Token t = getNextToken();   // get the next token in the program and...
        
        switch(t.type){
            case 24:    //  ':'
                addToTable(t.lexeme);
                appendLMD("5");     // add rule 3 to the LMD list
                checkRest(getNextToken());
                break;
            default:
                throwException(t,"Invalid declaration.");
        }
    }

    /************
    *   REST    *
    ************/
    private void checkRest(LexicalAnalyzer.Token token) throws IOException {
        LexicalAnalyzer.Token t;
        switch(token.type){
            case 3:     // BASTYPTOK
                addToTable(token.lexeme);
                if ((t = getNextToken()).type == 23){   // ;
                    addToTable(t.lexeme);
                    appendLMD("6");     //  add rule 6 to LMD list
                    nextToken();
                    break;
                }
            case 8:     // CONSTTOK
                addToTable(token.lexeme);
                switch((t = getNextToken()).type) {
                    case 3:     // BASTYPTOK
                        if((t = getNextToken()).type == 25) {    // ASTOK
                            addToTable(t.lexeme);
                            if((t = getNextToken()).type == 2) {    // LITTOK
                                addToTable(t.lexeme);
                                if((t = getNextToken()).type == 23) {   // ;
                                    addToTable(t.lexeme);
                                    appendLMD("7");     // add rule 7 to LMD list
                                    nextToken();
                                    break;
                                } else { throwException(t,"Invalid rule 7, expected a ';'"); break; }
                            } else { throwException(t,"Invalid rule 7, expected a literal value."); break; }
                        } else { throwException(t,"Invalid rule 7, expected assignment."); break; }
                    default:
                       throwException(t, "Invalid rule 7.");
                }
            default:
                throwException(token, "Invalid rest.");
        }
    }
    
    /************************************
    *   STATMNT STATS   |   epsilon     *
    ************************************/
    private void checkStat(LexicalAnalyzer.Token token) throws IOException {
        System.out.println("checkStat reached");
        switch(token.type){
            case 1:     //  IDTOK
                addToTable(token.lexeme);
                appendLMD("8");     // stat --> assignstat
                checkAssignmentStat(getNextToken());
                break;
            case 12:    //  IFTOK
                addToTable(token.lexeme);
                appendLMD("9");     // stat --> ifstat
                checkIfStat(getNextToken());
                break;
            case 11:    //  READTOK
                addToTable(token.lexeme);
                appendLMD("10");    // stat --> readstat
                checkReadStat(getNextToken());
                break;
            case 17:
            case 18:    // WRITETOK
                addToTable(token.lexeme);
                appendLMD("11");    // stat --> writestat
                checkWriteStat(getNextToken());
                break;
            case 9:     // DECTOK
                addToTable(token.lexeme);
                appendLMD("12");    // stat --> blockstat
                checkBlockStat(getNextToken());
                break;
            case 20:    //  WHILETOK
                addToTable(token.lexeme);
                appendLMD("13");    // stat --> loopstat
                checkLoopStat(getNextToken());
                break;
            default:
                throwException(token, "Statement is invalid.");
        }
    }

    private void checkAssignmentStat(LexicalAnalyzer.Token nextToken) throws IOException {
        System.out.println();
        switch(nextToken.type){
            case 25:    //  :=
                addToTable(nextToken.lexeme);   
                appendLMD("24");    // express --> term expprime
                checkExpres(getNextToken());
                break;
            default:
                throwException(nextToken, "Invalid rule 24, expected an expression.");
        }
    }

    private void checkIfStat(LexicalAnalyzer.Token nextToken) {
        System.out.println("checkIfStat");    
    }

    private void checkReadStat(LexicalAnalyzer.Token nextToken) {
        System.out.println("checkReadStat");    
    }

    private void checkWriteStat(LexicalAnalyzer.Token nextToken) {
        System.out.println("checkWriteStat");    
    }

    private void checkBlockStat(LexicalAnalyzer.Token nextToken) {
        System.out.println("checkBlockStat");    
    }
    

    private void checkLoopStat(LexicalAnalyzer.Token nextToken) {
        System.out.println("checkLoopStat");   
    }

    private boolean checkExpres(LexicalAnalyzer.Token nextToken) throws IOException {
        if(checkTerm(nextToken)){
            appendLMD("24");
            return true;
        }
        return false;
    }

    private boolean checkTerm(LexicalAnalyzer.Token nextToken) throws IOException {
        if(checkRelFactor(nextToken)){
            appendLMD("27");
            return true;
        }
        return false;
    }

    private boolean checkRelFactor(LexicalAnalyzer.Token nextToken) throws IOException {
        if(checkFactor(nextToken)){ // cannot be epsilon
            appendLMD("30");
            checkFactorPrime(getNextToken());
            return true;
        }
        return false;
    }

    private boolean checkFactor(LexicalAnalyzer.Token nextToken) throws IOException {
        LexicalAnalyzer.Token t;
        switch(nextToken.type){
            case 15:    //  NOTTOK
                addToTable(nextToken.lexeme);   // add the NOT
                if(checkFactor((t = getNextToken()))){  // get the next token and check if it's a factor
                    addToTable(t.lexeme);
                    appendLMD("33");    //  factor --> NOTTOK factor
                    return true;
                } else {
                    throwException(t, "Invalid rule 33, expected a factor.");
                    return false;
                }
            case 1:     //  IDTOK
                addToTable(nextToken.lexeme);
                appendLMD("24");    // factor --> idnonterm
                return true;
            case 2:     //  LITTOK
                addToTable(nextToken.lexeme);
                appendLMD("25");    //  factor --> LITTOK
                return true;
            case 21:    // '('
                addToTable(nextToken.lexeme);
                if(checkExpres((t = getNextToken()))) { // check if there is a valid 'expression'
                    if( (t = getNextToken()).type == 22){   //  ')'
                        addToTable(t.lexeme);
                        appendLMD("36");    //  factor --> '(' express ')'
                        return true;
                    } else { throwException(t, "Invalid rule 36, expected a ')'"); return false; }
                } else { throwException(t, "Invalid rule 36, expected an expression."); return false; }
            default:
                return false;
        }
    }

    private void checkFactorPrime(LexicalAnalyzer.Token nextToken) throws IOException {
        LexicalAnalyzer.Token t;
        switch(nextToken.type) {
            case 6: //  RELOPTOK
                addToTable(nextToken.lexeme);
                appendLMD("31");    // factorprime --> RELOPTOK factor
                if(checkFactor((t = getNextToken()))) {
                    addToTable(t.lexeme);
                } else { throwException(t, "Invalid rule 31, expected factor.");  }
            default:
                appendLMD("32");    //  factorprin --> epsilon
                checkTermPrime(nextToken);
        }
    }

    private void checkTermPrime(LexicalAnalyzer.Token nextToken) throws IOException {
        LexicalAnalyzer.Token t;
        switch(nextToken.type) {
            case 5: //  MULOPTOK
                appendLMD("28");    // termprime --> MULOPTOK relfactor termprime
                addToTable(nextToken.lexeme);
                if(checkRelFactor((t = getNextToken()))) {
                    addToTable(t.lexeme);
                    checkTermPrime(getNextToken());
                } else { throwException(t, "Invalid rule 28, expected relfactor"); }
            default:
                appendLMD("29");    // termprim --> epsilon
                checkExprime(nextToken);    // back to 27: express --> term exprime
        }
    }

    private void checkExprime(LexicalAnalyzer.Token nextToken) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
