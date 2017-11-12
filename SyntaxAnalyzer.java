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
    CodeGen mips;
    private LexicalAnalyzer.Token currentToken;
    
    
    String programName;
    boolean runtime;  
    boolean fatalError;
    
    
    /***********************************
    *   VARIABLES FOR OUTPUT DISPLAY   *
    ************************************/
    Outputer output;
    String lmd = new String();  // Left Most Derivation 

    
    
    
    /************************
    *   EXPRESSION RECORD   *
    ************************/
    public class ExpressionRecord {
        int type;
        int location;
        
        ExpressionRecord setType(int type) {
            this.type = type;
            return this;
        }
        
        ExpressionRecord setLoc(int loc) {
            this.location = loc;
            return this;
        }
    }
    
    ExpressionRecord ex;
    
    SyntaxAnalyzer(Outputer output) { 
        this.output = output; 
        mips = new CodeGen();
        st = new SymbolTable(output);
    }
    
    public void loadFile(File file) {
        try{
            scanner = new LexicalAnalyzer(file);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    public void start() throws IOException {
        mips.WriteProlog();
        LexicalAnalyzer.Token t;
        scanner.startScanning();
        getToken();
        program(currentToken);
    }
   
    /************
    *   PROGRAM *
    ************/
    //  program --> PROCTOK IDTOK ISTOK decls BEGINTOK stats ENDTOK IDTOK ';'
    void program(LexicalAnalyzer.Token firstToken) throws IOException {
        match(firstToken,16);   //  PROCTOK
        if( (currentToken).type == 1 )
            programName = currentToken.lexeme;
        match(currentToken,1);  //  IDTOK
        match(currentToken,13); //  ISTOK
        decls();
        match(currentToken,7);  //  BEGINTOK
        stats();
        match(currentToken,10); //  ENDTOK
        if( currentToken.lexeme.equals(programName) ) {
            match(currentToken,1); //  IDTOK
            match(currentToken,23); //  ;
        }
    }
    
    /************
    *   STATS   *
    ************/
    //  Rule 1,2: stats --> statmnt stats | epsilon
    private void stats() throws IOException{
        appendLMD("1,");
        if( statmnt() )
            stats();
        else 
            appendLMD("2\n");   //  stats --> epsilon
    }
    
    /************
    *   DECLS   *
    ************/
    // Rule 3-5: decls --> decl decls | epsilon
    private void decls() throws IOException {
        if(currentToken.type == 1) {
            appendLMD("3,"); //  decls --> decl decls
            decl(currentToken);
            decls();
        } else {
            appendLMD("4\n"); //  decls --> epsilon
        }
    }
    
    /************
    *   DECL    *
    ************/
    // decl --> IDTOK ':' rest 
    private void decl(LexicalAnalyzer.Token declToken) throws IOException {
        match(declToken,1);    // match IDTOK
        match(currentToken,24);    // ';'
        appendLMD("5,"); // rule 5:  decl --> IDTOK ':' rest 
        checkRest(declToken);
    }

    /************
    *   REST    *
    ************/
    // Rule 6,7: rest --> BASTYPTOK ';' | CONSTTOK BASTYPTOK ASTOK LITTOK ';'
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
    
    /************
    *   STATMNT *
    ************/
    //  Rule 8-13: statmnt --> assignstat  |  ifstat   |  readstat   |  writestat |  blockst   |  loopst 
    private boolean statmnt() throws IOException {
        switch(currentToken.type) {
            case 1: 
                appendLMD("8,");     // statmnt --> assignstat
                assignstat(currentToken);
                return true;
            case 12:    //  IFTOK
                appendLMD("9,");     // statmnt --> ifstat
                ifstat();
                return true;
            case 11:    //  READTOK
                appendLMD("10,");    // statmnt --> readstat
                readstat();
                return true;
            case 17:
            case 18:    // WRITETOK
                appendLMD("11,");    // statmnt --> writestat
                writestat();
                return true;
            case 7: //  BEGIN
            case 9: //  DECTOK
                appendLMD("12,");    //  statmt --> blockstat
                blockstat();
                return true;
            case 20:    //  WHILETOK
                appendLMD("13,");    // statmnt --> loopstat
                loopst();
                return true;
            default:
                return false;
        }
    }
    
    /*****************
    *   ASIGNSTAT    *
    *****************/
    //  Rule 14:  assignstat --> idnonterm  ASTOK express ';'
    private void assignstat(LexicalAnalyzer.Token assignToken) throws IOException {
        appendLMD("14\n");
        if(assignToken.type == 1) {
            LexicalAnalyzer.Token idptr = st.findInAllScopes(assignToken.lexeme);
            if(idptr == null)
                throwException(assignToken,"assignstat: undeclared identifier.");
            int idType = idptr.type;
            int idLoc = idptr.offSet;
            match(assignToken,1);  //  IDTOK
            match(currentToken,25); //  :=
            ex = express();
            if(idType != ex.type){
                warningMessage("Type does not match.");
            }
            mips.codeGen("lw $t0 "+ex.location+"($sp)");    
            mips.codeGen("sw $t0 "+idLoc+"($sp)");   
            match(currentToken,23);
        }
    }

    /**************
    *   IFSTAT    *
    **************/
    // Rule 15: ifstat --> IFTOK express THENTOK stats ENDTOK IFTOK ';'
    private void ifstat() throws IOException {
        appendLMD("15\n");
        match(currentToken,12); //  IFTOK
        express();
        match(currentToken,19); //  THENTOK
        stats();
        match(currentToken,10); //  ENDTOK
        match(currentToken,12); //  IFTOK
        match(currentToken,23); //  ;
    }
    
    /****************
    *   READSTAT    *
    ****************/
    // Rule 16: readstat --> READTOK '(' idnonterm ')' ';'
    private void readstat() throws IOException {
        appendLMD("16\n");
        match(currentToken,11); //  READTOK
        match(currentToken,21); //  (
        newRecord();
        idnonterm(currentToken,ex);
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
    }

    /*****************
    *   WRITESTAT    *
    *****************/
    // Rule 17: writestat --> WRITETOK '(' writeexp ')' ';'
    private void writestat() throws IOException {
        appendLMD("17,");   //  writestat --> WRITETOK '(' writeexp ')' ';'
        if(currentToken.type == 17) {
            match(currentToken,17); //  put
        } else {
            match(currentToken,18); //  put_line
        }
        match(currentToken,21); //  (
        writeexp();
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
    }
    
    /*************
    *   LOOPST   *
    *************/
    // Rule 18: loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
    private void loopst() throws IOException {
        appendLMD("18,");    // loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
        match(currentToken,20); //  WHILETOK
        express();
        match(currentToken,14); //  LOOPTOK
        stats();
        match(currentToken,10); //  ENDTOK
        match(currentToken,14); //  LOOPTOK
        match(currentToken,23); //  ;
    }

    /*****************
    *   BLOCKSTAT    *
    *****************/
    // Rule 19: blockstat --> declpart   BEGINTOK   stats   ENDTOK  ';'
    private void blockstat() throws IOException {
        appendLMD("19,");
        declpart();
        match(currentToken,7);  //  BEGINTOK
        stats();
        match(currentToken,10); //  ENDTOK
        match(currentToken,23); //  ;
    }
    
    /****************
    *   DECLPART    *
    ****************/
    // Rule 20,21: declpart --> DECTOK decl decls   |   epsilon
    private void declpart() throws IOException {
        switch(currentToken.type){
            case 9:
                appendLMD("20,");
                match(currentToken,9);  //  DECTOK
                decl(currentToken);   //  declpart --> DECLTOK decl decls
                decls();
                break;
            default:
                appendLMD("21,");    //  Rule 21: declpart --> empty
        }
    }
    
    /****************
    *   WRITEEXP    *
    ****************/
    //  Rule: 22,23 writeexp --> STRLITTOK | express
    private void writeexp() throws IOException {
        if(currentToken.type == 26) {    //  STRLITTOK
            appendLMD("22\n");  //  writeexp --> STRLITTOK
            match(currentToken,26);
        } else {
            appendLMD("23,");   //  writeexp --> express
            express();
        }
    }
    
    /****************
    *   EXPRESSION  *
    ****************/
    // Rule 24: express --> term exprime
    private ExpressionRecord express() throws IOException {
        appendLMD("24,");    // express --> term expprime
        newRecord();
        term();
        exprime();
        return ex;
    }
    
    /****************
    *   EXPPRIME    *
    ****************/
    //  Rule 25,26: expprime --> ADDOPTOK term termprime |  <empty>  
    private void exprime() throws IOException {
        if(currentToken.type == 4) {
            appendLMD("25,");    // expprime --> ADDOPTOK term termprime
            match(currentToken,4);  //  ADDOPTOK
            term();
            exprime();
        } else {
            appendLMD("26\n");    //    exprime --> epsilon
        }
    }

    /************
    *   TERM    *
    ************/
    // Rule 27: term --> relfactor termprime
    private void term() throws IOException {
        appendLMD("27,");   //  term --> relfactor termprime
        relfactor();
        termprime();
    }
    
    /****************
    *   TERMPRIME   *
    ****************/
    // Rule 28,29: termprime --> MULOPTOK relfactor termprime  |  <empty> 
    private void termprime() throws IOException {
        if(currentToken.type == 5) {
            appendLMD("28,");    //  termprime --> MULOPTOK relfactor termprime
            match(currentToken,5);  //  MULOPTOK
            relfactor();
            termprime();
        } else {
            appendLMD("29\n");    //  termprime --> <empty>
        }
    }
    
    /****************
    *   RELFACTOR   *
    ****************/
    // Rule 30: relfactor --> factor factorprime
    private void relfactor() throws IOException {
        appendLMD("30,");   //  relfactor --> factor factorprime
        factor();
        factorprime();
    }
    
    /****************
    *   FACTORPRIME *
    ****************/
    // Rule 31,32: factorprime --> RELOPTOK factor |  <empty>
    private void factorprime() throws IOException {
        if(currentToken.type == 6) {
            appendLMD("31,");    // factorprime --> RELOPTOK factor
            match(currentToken,6);  //  RELOPTOK
            factor();
        } else {
            appendLMD("32,");    //  factorprime --> epsilon
        }
    }
    
    /************
    *   FACTOR  *
    ************/
    //  Rule 33-36: factor --> NOTTOK factor  |  idnonterm   |  LITTOK  |  '('  express  ')' 
    private void factor() throws IOException {
        switch(currentToken.type) {
            case 15:    //  NOTTOK
                appendLMD("33,"); //  factor --> NOTTOK factor
                match(currentToken,15); //  NOTTOK
                factor();
                break;
            case 1:     //  IDTOK
                appendLMD("34,");    // factor --> idnonterm
                idnonterm(currentToken,ex);
                break;
            case 2:     //  LITTOK
                /*
                    generate code to put the literal in a register
                    allocate space for a temporary on the stack
                    generate code to move the register to this offset
                    set expression type to integer/float/boolean
                    set expression location to the temp space allocated

                */
                appendLMD("35\n");    //  factor --> LITTOK
                char type = getType(currentToken.lexeme);
                switch(type){
                    case 'b':
                    case 'i':
                        ex = ex.setLoc(st.offSet);
                        st.changeOffSet(-4);
                        ex = ex.setType(currentToken.type);
                        mips.codeGen("li $t0 "+currentToken.lexeme);
                        mips.codeGen("sw $t0 "+ex.location+"($sp)");
                        break;
                    case 'f':
                        ex = ex.setLoc(st.offSet);
                        st.changeOffSet(-8);
                        ex = ex.setType(currentToken.type);
                        mips.codeGen("li $t0 "+currentToken.lexeme);
                        mips.codeGen("sw $t0 "+ex.location+"($sp)");
                        break;
                }
                match(currentToken,2);  //  LITTOK
                break;
            case 21:    // '('
                appendLMD("36,"); //  factor --> '(' express ')'
                match(currentToken,21); //  (
                express();
                match(currentToken,22); //  )
                break;
            default:
                throwException(currentToken,"Invalid program, expected a FACTOR");
        }
    }
    
    /****************
    *   IDNONTERM   *
    ****************/
    //  Rule 37: idnonterm --> IDTOK
    private void idnonterm(LexicalAnalyzer.Token token, ExpressionRecord ex) throws IOException{
        appendLMD("37\n");    // idnonterm --> IDTOK
        //System.out.println("idnonterm: token.lexeme = "+token.lexeme);
        LexicalAnalyzer.Token result = st.findInAllScopes(token.lexeme);
        //System.out.println("idnonterm: result.lexeme = "+result.lexeme);
        if(result == null) {
            throwException(token,"idnonterm: undeclared indentifier.");
        } else {
            ex = ex.setLoc(result.offSet);
            ex = ex.setType(result.type);
            match(token,1);
        }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    void match(LexicalAnalyzer.Token matchToken, int type) throws IOException {
        if(matchToken.type == type) {
            System.out.println("matched: "+matchToken.lexeme+" with type "+type);
            if(matchToken.type == 7 || matchToken.type == 10){
                addToTable(matchToken);
            }
            getToken();
        } else { throwException(matchToken," Error:"+matchToken.lexeme+" does not match type "+type+" "); }
    }
    
    public void getToken() throws IOException {
        this.currentToken = scanner.getToken();
    }
    
    public void newRecord(){
        this.ex = new ExpressionRecord();
    }
    
    private void throwException(LexicalAnalyzer.Token token, String message) {
        output.setErrors(" --> line: "+token.foundAt+" on Token: "+token.lexeme+",  "+message );
        quit();
    }
    
    private void addToTable(LexicalAnalyzer.Token entry) {
        st.insertEntry(entry);
        //appendLMD(lmd);
    }
    
    private void appendLMD(String str) {
        output.setLMD(str);
    }
    
    public void printOutput() {
        output.print();
    }
    
    private void warningMessage(String message) {
        System.out.println(message);
    }
    
    private void quit(){
        printOutput();
        mips.WritePostLog();
        mips.printMipsCode();
        st.display();
        System.exit(0);
    }
    
    char getType(String str){
        char type;
        str = str.toLowerCase();
        if( str.charAt(0) == 't' || str.charAt(0) == 'f' ){
            type = 'b';
        } else if ( str.contains(".") ){
            type = 'r';
        } else {
            type = 'i';
        }
        return type;
    }
}
