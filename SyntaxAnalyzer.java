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
    private LexicalAnalyzer.Token currentToken;
    
    
    String programName;
    boolean runtime;  
    
    
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
        getToken();
        while(currentToken.type != -1){
            parse(currentToken);
        }
    }
   
    //  program --> PROCTOK IDTOK ISTOK decls BEGINTOK stats  ENDTOK IDTOK ';'
    void parse(LexicalAnalyzer.Token parseToken) throws IOException {
        
            switch(parseToken.type) {
                case 16:    // PROCTOK
                    getToken();
                    if( (currentToken).type == 1 ) { //  IDTOK
                        programName = currentToken.lexeme;
                        getToken();
                        if( (currentToken).type == 13) {  //  ISTOK
                            getToken();
                            checkDecls();
                            runtime = true;
                        } else { throwException(currentToken,"Program is invalid, expected ISTOK."); break; }
                    } else {
                        throwException(currentToken,"Program is invalid, expected a IDTOK.");
                        break;
                    }
                    break;
                default:
                    if(runtime){
                        checkStat();
                    } else {
                        throwException(parseToken,"parse: Program is invalid.");
                    }
            }
    }
    
    void match(LexicalAnalyzer.Token matchToken, int type) throws IOException{
        if(matchToken.type == type){
            System.out.println("matched: "+matchToken.lexeme+" with type "+type);
            getToken();
        } else { throwException(matchToken," Error:"+matchToken.lexeme+" does not match type "+type+" "); }
    }
    
    /***************
    *   DECL DECLS *
    ***************/
    // Rule 3-5: decls --> decl decls | epsilon
    private void checkDecls() throws IOException {
        
        if(currentToken.type == 1){
            appendLMD("3,"); //  decls --> decl decls
            decl(currentToken);
            
        } else {
            appendLMD("4\n"); //  decls --> epsilon
            //  epsilon
        }
    }
    
    private void decl(LexicalAnalyzer.Token declToken) throws IOException {
        
        //  IDTOK
        match(declToken,1);    // match IDTOK
        match(currentToken,24);    // ';'
        appendLMD("5,"); // rule 5:  decl --> IDTOK ':' rest 
        checkRest(declToken);
        checkDecls();
    }

    /************
    *   REST    *
    ************/
    // Rule 6,7: rest --> BASTYPTOK  ';'  |   CONSTTOK BASTYPTOK ASTOK LITTOK ';'
    private void checkRest(LexicalAnalyzer.Token idToken) throws IOException {
        
        switch(currentToken.type) {
            case 3:     // BASTYPTOK
                idToken.changeType(currentToken.lexeme);
                match(currentToken,3);  //  BASTYPTOK
                 
                match(currentToken,23); //  ;
                appendLMD("6\n");     //  rest --> BASTYPTOK  ';'
                addToTable(idToken);
                break;
            case 8:     
                // CONSTTOK
                match(currentToken,8);
                idToken.setIsConstant();
                idToken.changeType(currentToken.lexeme);
                match(currentToken,3);  // BASTYPTOK
                 
                match(currentToken,25); // ASTOK
                match(currentToken,2);  // LITTOK
                match(currentToken,23); // ;
                appendLMD("7\n");         // rest --> CONSTTOK BASTYPTOK ASTOK LITTOK ';'
                addToTable(idToken);
                break;
            default:
                throwException(currentToken, "Invalid rest.");
                break;
                
        }
    }
    
    /************************************
    *   STATMNT STATS   |   epsilon     *
    ************************************/
    //  Rule 1,2: stats --> statmnt stats | epsilon
    //  Rule 8-13: statmnt --> assignstat  |  ifstat   |  readstat   |  writestat |  blockst   |  loopst 
    private void checkStat() throws IOException {
        appendLMD("1,");
        //System.out.println(token.lexeme + " is in checkStat, type is "+token.type);
        switch(currentToken.type) {
            case 1: 
                appendLMD("8,");     // stat --> assignstat
                //System.out.println("checkStat is calling checkAssignStat from case 1");
                checkAssignmentStat(currentToken);
                checkStat();
                break;
            case 12:    //  IFTOK
                appendLMD("9,");     // stat --> ifstat
                checkIfStat();
                checkStat();
                break;
            case 11:    //  READTOK
                appendLMD("10,");    // stat --> readstat
                checkReadStat();
                checkStat();
                break;
            case 17:
            case 18:    // WRITETOK
                appendLMD("11,");    // stat --> writestat
                checkWriteStat();
                checkStat();
                break;
            case 7: //  BEGIN  Rule 19: blockstat --> declpart   BEGINTOK   stats   ENDTOK  ';'
                appendLMD("12,");    //  Rule 12: statmt --> blockstat
                appendLMD("21,");    //  Rule 21: declpart --> empty
                checkBlockStat();
                checkStat();
                break;
            case 9: //  DECTOK
                appendLMD("12,");    // stat --> blockstat
                appendLMD("20,");    // Rule 20: declpart --> DECTOK decl decls
                match(currentToken,9);  //  DECTOK
                checkDecls();   //  declpart --> DECLTOK decl decls
                checkStat();
                break;
            case 20:    //  WHILETOK
                appendLMD("13,");    // stat --> loopstat
                checkLoopStat();
                checkStat();
                break;
            default:
                appendLMD("2\n"); // stat --> epsilon
                
        }
    }

    /*****************
    *   ASIGNSTAT    *
    *****************/
    //  Rule 14:  assignstat --> idnonterm  ASTOK express ';'
    private void checkAssignmentStat(LexicalAnalyzer.Token assignToken) throws IOException {
        appendLMD("14\n");
        match(assignToken,1);   // IDTOK
        match(currentToken,25); //  :=
        checkExpres();
        match(currentToken,23); //  ;
    }

    /**************
    *   IFSTAT    *
    **************/
    // Rule 15: ifstat --> IFTOK express THENTOK  stats ENDTOK IFTOK  ';'
    private void checkIfStat() throws IOException {
        appendLMD("15\n");
        match(currentToken,12); //  IFTOK
        checkExpres();
        match(currentToken,19); //  THENTOK
        checkStat();
        match(currentToken,10); //  ENDTOK
        match(currentToken,12); //  IFTOK
        match(currentToken,23); //  ;
        
    }
    
    /****************
    *   READSTAT    *
    ****************/
    // Rule 16: readstat --> READTOK '(' idnonterm ')' ';'
    private void checkReadStat() throws IOException {
        appendLMD("16\n");
        match(currentToken,11); //  READTOK
        match(currentToken,21); //  (
        match(currentToken,1);  //  IDTOK
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
        
    }

    /*****************
    *   WRITESTAT    *
    *****************/
    // Rule 17: writestat --> WRITETOK '('  writeexp ')' ';'
    private void checkWriteStat() throws IOException {
        if(currentToken.type == 17){
            match(currentToken,17); //  put
        }else{
            match(currentToken,18); //  put_line
        }
        match(currentToken,21); //  (
        appendLMD("17,");
        if(currentToken.type == 26){    //  STRLITTOK
            match(currentToken,26);
            appendLMD("22\n");
        }else{
            appendLMD("23,");
            checkExpres();
        }
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
    }
    
    /****************
    *   LOOPSTAT    *
    ****************/
    // Rule 18: loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
    private void checkLoopStat() throws IOException {
        
        match(currentToken,20); //  WHILETOK
        appendLMD("18,");    // loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
        checkExpres();
        match(currentToken,14); //  LOOPTOK
        checkStat();
        match(currentToken,10); //  ENDTOK
        match(currentToken,14); //  LOOPTOK
        match(currentToken,23); //  ;
    }

    /*****************
    *   BLOCKSTAT    *
    *****************/
    // Rule 19: blockstat --> declpart   BEGINTOK   stats   ENDTOK  ';'
    private void checkBlockStat() throws IOException {
        
        match(currentToken,7);  //  BEGINTOK
        appendLMD("19,");
        checkStat();
        match(currentToken,10); //  ENDTOK
        if( currentToken.lexeme.equals(programName) ){
            match(currentToken,1); //  IDTOK
            match(currentToken,23); //  ;
            
        }
        match(currentToken,23); //  ;
    }
    
    /****************
    *   EXPRESSION  *
    ****************/
    // Rule 24: express --> term exprime
    private void checkExpres() throws IOException {
        appendLMD("24,");    // express --> term expprime
        //System.out.println(nextToken.lexeme + " is in checkExpres, type is "+nextToken.type);
        
        //System.out.println("checkExpress is calling checkTerm");
        checkTerm();
        checkExprime();
    }

    /************
    *   TERM    *
    ************/
    // Rule 27: term --> relfactor termprime
    private void checkTerm() throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkTerm, type is "+nextToken.type);
        //System.out.println("checkTerm is calling checkRelFactor");
        appendLMD("27,");
        checkRelFactor();
        checkTermPrime();
    }
    
    /****************
    *   RELFACTOR   *
    ****************/
    // Rule 30: relfactor --> factor factorprime
    private void checkRelFactor() throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkRelfactor, type is "+nextToken.type);
        //System.out.println("checkRelFactor is calling checkFactor");
        appendLMD("30,");
        checkFactor();
        checkFactorPrime();
    }

    /************
    *   FACTOR  *
    ************/
    //  Rule 33-36: factor --> NOTTOK factor  |  idnonterm   |  LITTOK  |  '('  express  ')' 
    private void checkFactor() throws IOException {
        
        //System.out.println(nextToken.lexeme + " is in checkFactor, type is "+nextToken.type);
        LexicalAnalyzer.Token t;
        switch(currentToken.type){
            case 15:    //  NOTTOK
                appendLMD("33,"); //  factor --> NOTTOK factor
                match(currentToken,15); //  NOTTOK
                checkFactor();
                break;
            case 1:     //  IDTOK
                appendLMD("34,");    // factor --> idnonterm
                appendLMD("37\n");    // idnonterm --> IDTOK
                match(currentToken,1);  //  IDTOK
                break;
            case 2:     //  LITTOK
                appendLMD("35\n");    //  factor --> LITTOK
                match(currentToken,2);  //  LITTOK
                break;
            case 21:    // '('
                match(currentToken,21); //  (
                appendLMD("36,"); //  factor --> '(' express ')'
                checkExpres();
                match(currentToken,22); //  )
                break;
            default:
                throwException(currentToken,"Invalid program, expected a FACTOR");
        }
    }
    
    /****************
    *   FACTORPRIME *
    ****************/
    // Rule 31,32: factorprime --> RELOPTOK factor |  <empty>
    private void checkFactorPrime() throws IOException {
        
        if(currentToken.type == 6){
            match(currentToken,6);  //  RELOPTOK
            appendLMD("31,");    // factorprime --> RELOPTOK factor
            //System.out.println("checkFactorPrime is calling checkFactor");
            checkFactor();
        } else {
            appendLMD("32,");    //  factorprime --> epsilon
        }
    }
    
    /****************
    *   TERMPRIME   *
    ****************/
    // Rule 28,29: termprime --> MULOPTOK relfactor termprime  |  <empty> 
    private void checkTermPrime() throws IOException {
        
        if(currentToken.type == 5){
            match(currentToken,5);  //  MULOPTOK
            appendLMD("28,");    //  termprime --> MULOPTOK term
            //System.out.println("checkTermPrime is calling checkTerm");
            checkTerm();
        } else {
            appendLMD("29\n");    //  termprime --> epsilon
        }
    }

    /****************
    *   EXPPRIME    *
    ****************/
    //  Rule 25,26: expprime --> ADDOPTOK express  |  <empty>  
    private void checkExprime() throws IOException {
        
        if(currentToken.type == 4){
            match(currentToken,4);  //  ADDOPTOK
            appendLMD("25,");    // expprime --> ADDOPTOK express
            checkExpres();
        }else{
            appendLMD("26\n");    //    exprime --> epsilon
        }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    public void getToken() throws IOException {
        this.currentToken = scanner.getToken();
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
}
