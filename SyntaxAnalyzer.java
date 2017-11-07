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
    
    // there is no empty token, this is used internally for the epsilon rule.
    LexicalAnalyzer.Token empty;    
    
    String programName;
    
    /***********************************
    *   VARIABLES FOR OUTPUT DISPLAY   *
    ************************************/
    Outputer output;
    String lmd = new String();  // Left Most Derivation 
    
    SyntaxAnalyzer(Outputer output){ this.output = output; }
    
    public void loadFile(File file) {
        try{
            scanner = new LexicalAnalyzer(file);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        st = new SymbolTable(output);
    }
    
    public void start() throws IOException {
        LexicalAnalyzer.Token t;
        scanner.startScanning();
        nextToken();
    }
   
    void parse(LexicalAnalyzer.Token token) throws IOException {
        
        LexicalAnalyzer.Token t;
            switch(token.type) {
                case 16:    // PROCTOK
                    //  program --> PROCTOK IDTOK ISTOK decls BEGINTOK stats  ENDTOK IDTOK ';'
                    if( ( t = getNextToken()).type == 1 ) { //  IDTOK
                        programName = t.lexeme;
                        if( (t = getNextToken()).type == 13) {  //  ISTOK
                            checkDecls( (t = getNextToken()));
                        } else { throwException(t,"Program is invalid, expected ISTOK."); break; }
                    } else {
                        throwException(t,"Program is invalid, expected a IDTOK.");
                        break;
                    }
                    break;
                default:
                    throwException(token,"parse: Program is invalid.");
                    break;
            }
    }
    
    void match(){
    }
    
    /*****************************
    *   DECL DECLS  |   epsilon  *
    *****************************/
    // Rule 3-5: decls --> decl decls 
    private boolean checkDecls(LexicalAnalyzer.Token token) throws IOException {
        
        //System.out.println(token.lexeme + " is in checkDecls, type is "+token.type);
        if(token.type == 1) {    // IDTOK
            appendLMD("3");
            decl(token);
            return true;
        } else { 
            appendLMD("4"); //  decls --> epsilon
            checkBlockStat(token);
            return false;
        } 
    }
    
    private void decl(LexicalAnalyzer.Token token) throws IOException {
        
        //System.out.println(token.lexeme + " is in decl, type is "+token.type);
        //  IDTOK
        LexicalAnalyzer.Token t = getNextToken();   // get the next token in the program and...
        
        switch(t.type) {
            case 24:    //  ':'
                // rule 5:  decl --> IDTOK ':' rest 
                appendLMD("5");
                checkRest(getNextToken(), token);
                checkDecls(getNextToken());
                break;
            default:
                throwException(t,"Invalid declaration, expected ':' after IDTOK.");
                break;
        }
    }

    /************
    *   REST    *
    ************/
    // Rule 6,7: rest --> BASTYPTOK  ';'  |   CONSTTOK BASTYPTOK ASTOK LITTOK ';'
    private void checkRest(LexicalAnalyzer.Token token, LexicalAnalyzer.Token idToken) throws IOException {
        
        //System.out.println(token.lexeme + " is in checkRest, type is "+token.type);
        LexicalAnalyzer.Token t;
        switch(token.type) {
            case 3:     // BASTYPTOK
                idToken.changeType(token.lexeme); 
                
                if ((t = getNextToken()).type == 23) {   // ';'
                    appendLMD("6");     //  rest --> BASTYPTOK  ';'
                    addToTable(idToken);
                    break;
                } else { throwException(t,"Invalid rule 6, expected ';'."); break;}
            case 8:     
                // CONSTTOK
                idToken.setIsConstant();
                
                switch((t = getNextToken()).type) {
                    case 3:     // BASTYPTOK
                        idToken.changeType(t.lexeme); 
                        
                        if((t = getNextToken()).type == 25) {    // ASTOK
                            if((t = getNextToken()).type == 2) {    // LITTOK
                                if((t = getNextToken()).type == 23) {   // ;
                                    appendLMD("7");     // rest --> CONSTTOK BASTYPTOK ASTOK LITTOK ';'
                                    addToTable(idToken);
                                    break;
                                } else { throwException(t,"Invalid rule 7, expected a ';'."); break; }
                            } else { throwException(t,"Invalid rule 7, expected a literal value."); break; }
                        } else { throwException(t,"Invalid rule 7, expected assignment."); break; }
                    default:
                       throwException(t, "checkRest: Invalid rule 7.");
                       break;
                }
                break;
            default:
                throwException(token, "Invalid rest.");
                break;
        }
    }
    
    /************************************
    *   STATMNT STATS   |   epsilon     *
    ************************************/
    //  Rule 1,2: stats --> statmnt stats | epsilon
    //  Rule 8-13: statmnt --> assignstat  |  ifstat   |  readstat   |  writestat |  blockst   |  loopst 
    private boolean checkStat(LexicalAnalyzer.Token token) throws IOException {
        appendLMD("1");
        //System.out.println(token.lexeme + " is in checkStat, type is "+token.type);
        switch(token.type) {
            case 1:     
                
                appendLMD("8");     // stat --> assignstat
                //System.out.println("checkStat is calling checkAssignStat from case 1");
                checkAssignmentStat(getNextToken());
                //System.out.println("checkStat is calling checkStat from case 1");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    
                    return true;
                } else {
                    return false;
                }
            case 12:    //  IFTOK
                appendLMD("9");     // stat --> ifstat
                //System.out.println("checkStat is calling checkIfStat from case 12");
                checkIfStat(getNextToken());
                //System.out.println("checkStat is calling checkStat from case 12");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    return true;
                } else {
                    return false;
                }
            case 11:    //  READTOK
                appendLMD("10");    // stat --> readstat
                //System.out.println("checkStat is calling checkReadStat from case 11");
                checkReadStat(getNextToken());
                //System.out.println("checkStat is calling checkStat from case 11");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    return true;
                } else {
                    return false;
                }
            case 17:
            case 18:    // WRITETOK
                appendLMD("11");    // stat --> writestat
                //System.out.println("checkStat is calling checkWriteStat from case 18");
                checkWriteStat(getNextToken());
                //System.out.println("checkStat is calling checkStat from case 18");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    return true;
                } else {
                    return false;
                }
            case 7: //  BEGIN  Rule 19: blockstat --> declpart   BEGINTOK   stats   ENDTOK  ';'
                appendLMD("12");
                appendLMD("21");    // Rule 21: declpart --> empty
                //System.out.println("checkStat is calling checkBlockStat from case 7");
                checkBlockStat(token);
                //System.out.println("checkStat is calling checkStat from case 7");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    return true;
                } else {
                    return false;
                }
            case 9: //  DECTOK
                appendLMD("12");    // stat --> blockstat
                appendLMD("20");    // Rule 20: declpart --> DECTOK decl decls
                //System.out.println("checkStat is calling checkDecls from case 9");
                checkDecls(getNextToken());   //  declpart --> DECLTOK decl decls
                return true;
                
            case 20:    //  WHILETOK
                appendLMD("13");    // stat --> loopstat
                //System.out.println("checkStat is calling checkLoopStat");
                checkLoopStat(token);
                //System.out.println("checkStat is calling checkStat from case 20");
                if( checkStat(getNextToken()) ) {  // Rule 1: stat --> statmnt stats
                    return true;
                } else {
                    return false;
                }
            default:
                appendLMD("2"); // stat --> epsilon
                //System.out.println("checkStat is calling setEmptyToken()");
                setEmptyToken(token);
                return false;
        }
    }

    /*****************
    *   ASIGNSTAT    *
    *****************/
    //  Rule 14:  assignstat --> idnonterm  ASTOK express ';'
    private void checkAssignmentStat(LexicalAnalyzer.Token nextToken) throws IOException {
        
        appendLMD("14");
        //System.out.println(nextToken.lexeme + " is in checkAssignStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type){
            case 25:    //  := 
                
                //System.out.println("checkAssignStat is calling checkExpress");
                if( checkExpres(getNextToken()) ){
                    t = getNextToken();
                } else {
                    //System.out.println("checkAssignStat is calling getEmptyToken() expects ';'token.");
                    t = getEmptyToken();
                }
                if( (t).type == 23){  //  ';'
                } else { throwException(t, "Invalid assignment statement, statement must end with a ';'."); break; }
                break;
            default:
                throwException(nextToken, "Invalid rule 24, expected an ASTOK ' := '.");
                break;
        }
    }

    /**************
    *   IFSTAT    *
    **************/
    // Rule 15: ifstat --> IFTOK express THENTOK  stats ENDTOK IFTOK  ';'
    private void checkIfStat(LexicalAnalyzer.Token nextToken) throws IOException {
        appendLMD("15");
        //System.out.println(nextToken.lexeme + " is in checkIfStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        //System.out.println("checkIfStat is calling checkExpress");
        if( checkExpres(nextToken) ) {
            t = getNextToken();
        } else {
            //System.out.println("checkIfStat is calling getEmptyToken() expects THENTOK");
            t = getEmptyToken();
        }
            
            switch((t).type){
                case 19:    // THENTOK
                    
                    //System.out.println("checkIfStat is calling checkStat");
                    if( checkStat( ( t = getNextToken()) ) ){
                        t = getNextToken();
                    } else { 
                        //System.out.println("checkIfStat is calling getEmptyToken() expects ENDTOK");
                        t = getEmptyToken(); 
                    }
                    
                    if( (t).type == 10 ) { //  ENDTOK
                        if( (t = getNextToken()).type == 12 ) { // IFTOK
                            if( (t = getNextToken()).type == 23 ) { //  ';'
                                // END IF;
                            } else { throwException(t,"Invalid IFSTAT, expected a ';'."); break; }
                        } else { throwException(t,"Invalid IFSTAT, expected a IFTOK."); break; }
                    } else { throwException(t,"Invalid IFSTAT expected a ENDTOK."); break; }
                    break;
                default:
                    throwException(nextToken,"Invalid IF statement.");
                    break;
            }
    }
    
    /****************
    *   READSTAT    *
    ****************/
    // Rule 16: readstat --> READTOK '(' idnonterm ')' ';'
    private void checkReadStat(LexicalAnalyzer.Token nextToken) throws IOException {
        appendLMD("16");
        //System.out.println(nextToken.lexeme + " is in checkReadStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        if( nextToken.type == 21 ) {    //  '('
            if( (t = getNextToken()).type == 1) {   //  IDTOK
                
                if( (t = getNextToken()).type == 22 ) { //  ')'
                    if( (t = getNextToken()).type == 23 ) { //  ';'
                        //  '(' IDTOK ')' ';'
                    } else { throwException(t,"Invalid READSTAT, expected ';'."); }
                } else { throwException(t, "Invalid READSTAT, expected ')'."); }
            } else {  throwException(t, "Invalid READSTAT, expected an IDTOK."); }
        } else { throwException(nextToken, "Invalid READSTAT, expected '('."); }
    }

    /*****************
    *   WRITESTAT    *
    *****************/
    // Rule 17: writestat --> WRITETOK '('  writeexp ')' ';'
    private void checkWriteStat(LexicalAnalyzer.Token nextToken) throws IOException {
        appendLMD("17");
        //System.out.println(nextToken.lexeme + " is in checkWriteStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        if( nextToken.type == 21 ) {    //  '('
            if( (t = getNextToken()).type == 26) {   //  STRLITTOK
                appendLMD("22");    // Rule 22: writeexp --> STRLITTOK
                t = getNextToken();
            } else {  
                
                appendLMD("23");    //  Rule 23: writeexp --> expression
                //System.out.println("checkWriteStat is calling checkExpress");
                if( checkExpres(t) ){
                    t = getNextToken();
                } else {
                    //System.out.println("checkWriteStat is calling getEmptyToken() expects ')'token.");
                    t = getEmptyToken();
                }
            }
            if( t.type == 22 ) { //  ')'    <-- get next token from empty token.
                if( (t = getNextToken()).type == 23 ) { //  ';'
                    // writestat done
                } else { throwException(t,"Invalid writeSTAT, expected ';'."); }
            } else { throwException(t, "Invalid writeSTAT, expected ')'."); }
        } else { throwException(nextToken, "Invalid writeSTAT, expected '('."); }
    }
    
    /****************
    *   LOOPSTAT    *
    ****************/
    // Rule 18: loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
    private void checkLoopStat(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkLoopStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type){
            case 20:    //  WHILETOK
                appendLMD("18");    // loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
                //System.out.println("checkLoopStat is calling checkExpress");
                if( checkExpres((t = getNextToken())) ) {   // if valid express...
                    t = getNextToken();
                } else {
                    //System.out.println("checkLoopStat is calling getEmptyToken() expects LOOPTOK");
                    t = getEmptyToken();
                }
                    
                    if( (t).type == 14 ){ // LOOPTOK 
                        //System.out.println("checkLoopStat is calling checkStat");
                        if( checkStat(getNextToken()) ) {
                            t = getNextToken();
                        } else {
                            //System.out.println("checkLoopStat is calling getEmptyToken() expects ENDTOK");
                            t = getEmptyToken();
                        }
                        
                        //System.out.println("checkLoopStat is calling getEmptyToken() expects ENDTOK");
                        if( (t).type == 10 ) {  // ENDTOK
                            if( (t = getNextToken()).type == 14 ) {  // LOOPTOK
                                if( (t = getNextToken()).type == 23 ) { //  ';'
                                    // end loop;
                                } else { throwException(t, "Invalid Loopst: expected a ';'."); break; }
                            } else { throwException(t, "Invalid Loopst: expected keyword LOOP."); break; }
                        } else { throwException(t, "Invalid Loopst: expected keyword END."); break; }
                    } else { throwException(t, "Invalid Loopst: expected keyword LOOP."); break; }
                    break;
            default:
                throwException(nextToken,"Invalid Loop st.");
                break;
        }
    }

    /*****************
    *   BLOCKSTAT    *
    *****************/
    // Rule 19: blockstat --> declpart   BEGINTOK   stats   ENDTOK  ';'
    private void checkBlockStat(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkBlockStat, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type) {
            case 7:     //  BEGIN
                appendLMD("19");
                addToTable(nextToken);
                
                //System.out.println("checkBlockStat is calling checkStat");
                if( checkStat( (t = getNextToken()) ) ){
                    t = getNextToken();
                } else { 
                    //System.out.println("checkBlockStat is calling getEmptyToken() expecting ENDTOK");
                    t = getEmptyToken(); 
                }
                
                if( (t).type == 10){ //  ENDTOK
                    addToTable(t);
                    t = getNextToken();
                    
                    if( t.type == 1 && t.lexeme.equals(programName) ) {
                        if( (t = getNextToken()).type == 23){ //  ';'
                            // program end;
                            //System.out.println("PROGRAM END");
                        } else { throwException(t,"Program is invalid."); break; }
                    } 
                    
                    else if ( t.type == 23 ) {   //  ';'
                        //  end;
                    } else { throwException(t,"Invalid block statement, expected ';'."); break; }
                } else { throwException(nextToken,"Invalid block statement, expected an ENDTOK."); break; }
                break;
            default:
                throwException(nextToken,"Invalid Blockstat, expected a BEGINTOK.");
                break;
        }
    }
    
    /****************
    *   EXPRESSION  *
    ****************/
    // Rule 24: express --> term exprime
    private boolean checkExpres(LexicalAnalyzer.Token nextToken) throws IOException {
        appendLMD("24");    // express --> term expprime
        //System.out.println(nextToken.lexeme + " is in checkExpres, type is "+nextToken.type);
        
        //System.out.println("checkExpress is calling checkTerm");
        if(checkTerm(nextToken)) {   // term
            
            //System.out.println("checkExpress is calling checkExprime( getNextToken() )");
            return checkExprime(getNextToken());
            
        }else { //  exprime
            //System.out.println("checkExpress is calling checkExprime( getEmptyToken() )");
            return checkExprime(getEmptyToken());
        }
    }

    /************
    *   TERM    *
    ************/
    // Rule 27: term --> relfactor termprime
    private boolean checkTerm(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkTerm, type is "+nextToken.type);
        //System.out.println("checkTerm is calling checkRelFactor");
        appendLMD("27");
        if(checkRelFactor(nextToken)) {  // relfactor
            
            //System.out.println("checkTerm is calling checkTermPrime( getNextToken() )");
            return checkTermPrime(getNextToken());
        } else { 
            //System.out.println("checkTerm is calling checkTermPrime( getEmptyToken() )");
            return checkTermPrime(getEmptyToken()); 
        }
    }
    
    /****************
    *   RELFACTOR   *
    ****************/
    // Rule 30: relfactor --> factor factorprime
    private boolean checkRelFactor(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkRelfactor, type is "+nextToken.type);
        //System.out.println("checkRelFactor is calling checkFactor");
        appendLMD("30");
        checkFactor(nextToken); // cannot be epsilon
        
            
        //System.out.println("checkRelFactor is calling checkFactorPrime");
        return checkFactorPrime(getNextToken());
    }

    /************
    *   FACTOR  *
    ************/
    //  Rule 33-36: factor --> NOTTOK factor  |  idnonterm   |  LITTOK  |  '('  express  ')' 
    private void checkFactor(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkFactor, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type){
            case 15:    //  NOTTOK
                appendLMD("33"); 
                   //  factor --> NOTTOK factor
                //System.out.println("checkFactor is calling checkFactor");
                checkFactor( (t = getNextToken()) ); // get the next token and check if it's a factor
                break;
            case 1:     //  IDTOK
                appendLMD("34");    // factor --> idnonterm
                appendLMD("37");    // idnonterm --> IDTOK
                break;
            case 2:     //  LITTOK
                appendLMD("35");    //  factor --> LITTOK
                
                break;
            case 21:    // '('
                appendLMD("36"); //  factor --> '(' express ')'
                //System.out.println("checkFactor is calling checkExpress");
                if(checkExpres((t = getNextToken()))) { // check if there is a valid 'expression'
                    t = getNextToken();
                } else {
                    //System.out.println("checkFactor is calling getEmptyToken() expects ')'token.");
                    t = getEmptyToken();
                }
                    if( (t).type == 22){   //  ')'
                            // done
                    } else { throwException(t, "Invalid rule 36, expected a ')'"); }
                break;
            default:
                throwException(nextToken,"Invalid program, expected a FACTOR");
        }
    }
    
    /****************
    *   FACTORPRIME *
    ****************/
    // Rule 31,32: factorprime --> RELOPTOK factor |  <empty>
    private boolean checkFactorPrime(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkFactorPrime, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type) {
            case 6: //  RELOPTOK
                appendLMD("31");    // factorprime --> RELOPTOK factor
                
                //System.out.println("checkFactorPrime is calling checkFactor");
                checkFactor((t = getNextToken()));
                return true;
            default:
                appendLMD("32");    //  factorprime --> epsilon
                //System.out.println("checkFactorPrime is calling setEmptyToken()");
                setEmptyToken(nextToken);
                return false;
                
        }
    }
    
    /****************
    *   TERMPRIME   *
    ****************/
    // Rule 28,29: termprime --> MULOPTOK relfactor termprime  |  <empty> 
    private boolean checkTermPrime(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkTermPrime, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type) {
            case 5: //  MULOPTOK
                appendLMD("28");   
                //System.out.println("checkTermPrime is calling checkTerm");
                return checkTerm((t = getNextToken()));
            default:
                appendLMD("29");    // termprime --> epsilon
                //System.out.println("checkTermPrime is calling checkExprime");
                return checkExprime(nextToken); // back to 27: express --> term exprime
                //  only {express} calls {term}, which calls {termprime}, if {termprime} is "empty" 
                //  we are no longer in a {term} and the next token goes back to {express} to 
                //  the next child which is {expprime}.
        }
    }

    /****************
    *   EXPPRIME    *
    ****************/
    //  Rule 25,26: expprime --> ADDOPTOK express(term expprime)  |  <empty>  
    private boolean checkExprime(LexicalAnalyzer.Token nextToken) throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkExprime, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(nextToken.type){
            case 4: //  ADDOPTOK
                appendLMD("25");    // expprime --> ADDOPTOK term termprime
                //System.out.println("checkExprime is calling checkExpress");
                return checkExpres(( t = getNextToken() ));    // {term expprime} = express
            default:
                appendLMD("26");    //    exprime --> epsilon 
                //System.out.println("checkExprime is calling setEmptyToken()");
                setEmptyToken(nextToken); 
                return false;
                //  From the call hierarchy if we reach this point and the next token does not belong to {exprime}, 
                //  {expprimes} parent {express} does not have any other child past {expprime}. Because calls to {express} 
                //  can come from multiple places, we set the next token in a variable accessible to whoever called {express}.
        }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    public void nextToken() throws IOException {
        parse(scanner.getToken());
    }
    
    public LexicalAnalyzer.Token getNextToken() throws IOException{
        return scanner.getToken();
    }
    
    private void throwException(LexicalAnalyzer.Token token, String message) {
        output.setErrors("\n--> line: "+token.foundAt+" on Token: "+token.lexeme+",  "+message );
    }
    
    private void addToTable(LexicalAnalyzer.Token entry){
        st.insertEntry(entry);
        //appendLMD(lmd);
    }
    
    private void appendLMD(String str){
        output.setLMD(str);
    }
    
    public void printOutput(){
        output.print();
    }
    
    /***************
    *   EPSILON    *
    ***************/
    // there is no epsilon token, this is to deal with the epsilon rule.
    public void setEmptyToken(LexicalAnalyzer.Token t){
        //System.out.println("Setting empty token "+t.lexeme);
        this.empty = t;
    }
    
    public LexicalAnalyzer.Token getEmptyToken(){
        //System.out.println("Getting empty token "+this.empty.lexeme);
        return this.empty;
    }
}
