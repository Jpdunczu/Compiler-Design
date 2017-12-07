package cs4110.homework.pkg1;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author joshuaduncan
 */
public class SyntaxAnalyzer {
    
    SymbolTable st;             //  Symbol table used for storing IDTOK's and their attributes
    LexicalAnalyzer scanner;    //  Scanning the file and getting the next token.
    CodeGen mips;               // Generating intermediate code
    LexicalAnalyzer.Token currentToken; //  the last token which was scanned
    String programName;         //  Store the program name to compare at the end of the program
    boolean doesCR = false;     //  boolean for write_stat if it reads a put_line
    
    /***********************************
    *   VARIABLES FOR OUTPUT DISPLAY   *
    ************************************/
    Outputer output;            //  class for printing output
    String lmd = new String();  // Left Most Derivation 

    /************************
    *   EXPRESSION RECORD   *
    ************************/
    // store type and location of tokens
    public class ExpressionRecord {
        
        //  the register which was used to store the result of the expression in MIPS.
        int reg; 
        /*
        The boolean isConstant is set when a token is found in the symbol table 
        and the token's isConstant value is set to true.
        This means that the idToken was declraed as constant in the main program.
        */
        boolean isConstant; 
        
        //  this is set if a 'not' token is read from the expression. It lets the function
        boolean isNot;       
        
        // for 'and' & 'or'
        int regLHS;         //  register used to store the rhs result.
        int regRHS;         //  "                    " the lhs result.
        boolean isOR;       //  Expression is a short circuit OR
        boolean isAND;      //  "                           " AND
        boolean isLitteral; //  the Expression is a litteral value, for MIPS codegen.
        
        // for cases where the expressions result was stored at an offset from the stack.
        int offSet;         
        String type;        //  the type of expression, boolean, float or integer.
        
        
        /*
        if the expression has a specified value, such as 'true' or 'false' for a 
        boolean, or a literal value for an integer and float. that will be saved 
        to this variable and then used later, rather then storing and loading 
        from the stack, since this value will have been declared in the MIPS .data 
        section of the code.
        */
        String value;
        // same as 'value' but these are used in 'AND' 'OR' operations for the lhs and rhs
        String valueLHS;    
        String valueRHS;  
        /*
        
        */
        String branch = "beq"; 
        
        // used for literal values to store the type of the Expression.
        ExpressionRecord setType(String type) {
            this.type = type;
            return this;
        }
        
        
        /*
        This saves the register which was used to store the result of the expression. 
        This is an attempt to reduce code redundancy by eliminating the need to store 
        the result on the stack and then immediately load it back into a register 
        to do something else with it.
        */
        ExpressionRecord setReg(int reg) {
            this.reg = reg;
            return this;
        }
        
        /* 
        This is not currently being used, but it's for future cases where an 
        IDTOKEN was declared constant in the main program, the token was saved 
        with the isContant boolean set to true, this is preserving that information 
        so that later on when it is pulled out of the symbol table to be used the 
        program can warn the user if they try to change it's pre-assigned value.
        */
        ExpressionRecord setConstant(){
            this.isConstant = true;
            return this;
        }
        
        /*
        When an expressions result is store on the stack this saves the offset 
        from the stack pointer so that it can be loaded later to use in ASSIGNSTAT, 
        IFSTAT and LOOPSTAT
        */
        ExpressionRecord setOffset(int offSet){
            this.offSet = offSet;
            return this;
        }
        
        /*
        Save the value of the expression, if it's a boolean we want to change the 
        value to something that can be put into MIPS code, if it's a literal value 
        we just want to store it. If it's null then it was not set correctly in 
        the expression and I just give it the $zero register so a null pointer 
        exception is not thrown and I can deal with it later.
        */
        ExpressionRecord setValue(String value){
            if( value != null ){
                switch(value){
                    case "":
                        break;
                    case "false":
                        value = "0";
                        break;
                    case "true":
                        value = "1";
                        break;
                    default:
                }
                this.value = value;
            } else {
                this.value = "$zero";
            }
            return this;
        }
        
        /*
        The expression was a 'NOT' statement, this is set when the 'NOT' token is 
        read from the scanner, it changes the branch to be negated and sets the 
        'isNOT' boolean to the opposite of what it was. The branch is just printed 
        no matter what in the IFSTAT or LOOPSTAT, this ensures the proper branch 
        is stored, I was trying to keep the code in those 2 statements less cluttered 
        so I decided to deal with it here. In the case of multiple NOT statements, 
        this will ensure the most recent holds precedence. This may not have been 
        the best choice of implementation, but for now this works. The isNot boolean 
        is not currently being used, but i added it just in case i might have a 
        need for it later.
        */
        ExpressionRecord setNot(){
            if( this.branch.equals("beq"))
                this.branch = "bne";
            else 
                this.branch = "beq";
            
            if( this.isNot )
                this.isNot = false;
            else
                this.isNot = true;
            return this;
        }
        
        /*
        Saves expression state as an 'OR' expression, the lhs and rhs registers 
        used to store the lhs and rhs values are saved in the expression record, 
        then it saves the value 'true' or 'false' needs to be changed to '1' or 
        '0', in this case we just use the register $zero.
        */
        ExpressionRecord setOR(int lhsReg, int rhsReg, String lhsValue, String rhsValue){
            this.regLHS = lhsReg;
            this.regRHS = rhsReg;
            if(lhsValue.equals("true")){
                lhsValue = "1";
            } else {
                lhsValue = "$zero";
            }
            this.valueLHS = lhsValue;
            if(rhsValue.equals("true")){
                rhsValue = "1";
            } else {
                rhsValue ="$zero";
            }
            this.valueRHS = rhsValue;
            this.isOR = true;
            return this;
        }
        
        /*
        This function is used for 'and' tokens, it saves which resgisters the 
        result of the lhs and rhs expressions were stored in Then depending on 
        the value of the token, it saves either a 1 or a $zero for MIPS code gen, 
        it then sets the value for isAND. All of these variables will be used by 
        the calling function for the MIPS code gen.
        */
        ExpressionRecord setAND(int lhsReg, int rhsReg, String lhsValue, String rhsValue){
            this.regLHS = lhsReg;
            this.regRHS = rhsReg;
            if(lhsValue.equals("true")){
                lhsValue = "1";
            } else {
                lhsValue = "$zero";
            }
            this.valueLHS = lhsValue;
            if(rhsValue.equals("true")){
                rhsValue = "1";
            } else {
                rhsValue ="$zero";
            }
            
            //  we want to branch if the case is NOT equal.
            if( !this.isNot ){   
                this.branch = "bne";
            } else { 
            //  but if there was a 'NOT' in the statement then that is negated, 
            //  and then we need to branch if it IS equal.
                this.branch = "beq";
            }
            
            this.valueRHS = rhsValue;
            this.isAND = true;
            return this;
        }
        
        /*
        a boolean value if the expression was a litteral token then this is set 
        to be true, because if it is a litteral value then it does not need to be 
        loaded from the stack pointer to a register, we can just use the litteral 
        value in the MIPS code. Not shown here is the int reg variable in 
        Expression record that is also assigned a value of where the immediate 
        value was stored.
        */
        ExpressionRecord setLitteral(){
            isLitteral = true;
            return this;
        }
    }
    
    ExpressionRecord ex;    //expression record
    ExpressionRecord lhs;   //storing the left hand side of an expression.
    ExpressionRecord rhs;   //storing the right hand side of an expession.
    
    // arrays to keep track of available registers for MIPS codegen
    int[] sRegisters = new int[8];  
    int[] tRegisters = new int[8];
    int[] fRegisters = new int[8];
    int[] rRegisters = new int[8];
    
    SyntaxAnalyzer(Outputer output) { 
        this.output = output;   // used for debugging the code if an exception is thrown.
        mips = new CodeGen();   // the class which generates the MIPS .asm output file.
        st = new SymbolTable(output);   //  
        ex = new ExpressionRecord();
    }
    
    public void loadFile(File file) {
        try{
            scanner = new LexicalAnalyzer(file);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    /************
    *   START
    * 
    *   @throws java.io.IOException
    * 
    *   This function initializes a few Global variables, then it gets the first 
    *   token and passes it to Program to start the Grammar check. When program 
    *   returns, it means the end of the program has been reached and 'start' 
    *   calls quit(), which finalizes the MIPS code gen.
    */
    public void start() throws IOException {
        mips.WriteProlog();
        scanner.startScanning();
        getToken();
        program(currentToken);
        quit();
    }
   
    /************
    *   PROGRAM
    *   program --> PROCTOK IDTOK ISTOK <decls> BEGINTOK <stats> ENDTOK IDTOK ';'
    * 
    *   @param firstToken
    *   @throws java.io.IOException
    * 
    *   This function makes sure the Grammar is followed correctly, it serves as 
    *   a check to ensure the very base conditions of a Baby ADA program are met. 
    *   It first matches the token 'procedure', then verifies the next token is 
    *   an idToken, it saves this tokens lexeme as the name of the program to be 
    *   used at the end of the program to make sure the program is ended correctly 
    *   according to the Grammar. If at any point in the function the next Token 
    *   does not follow the Grammar, this function passes the currentToken to the 
    *   throw exception function and the scanning stops as well as the code 
    *   generation is thrown out.
    */
    public void program(LexicalAnalyzer.Token firstToken) throws IOException {
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
        } else {
            throwException(currentToken,"Illegal program ending.");
        }
    }
    
    /************
    *   STATS  
    *   Rule 1,2: stats --> <statmnt> <stats> | <epsilon>
    * 
    *   This function checks if there is a statement in the code by calling the 
    *   <statmnt> function. The base case according to the Grammar is that <stats> 
    *   is empty, in which case this function appends the rule to the LMD and then 
    *   takes no further action.
    */
    private void stats() throws IOException{
        appendLMD("1,");
        if( statmnt() )
            stats();
        else 
            appendLMD("2\n");   //  stats --> epsilon
    }
    
    /************
    *   DECLS 
    *   Rule 3,4: <decls> --> <decl> <decls> | <epsilon>
    * 
    *   This function verifies that the currentToken is an idToken, then according 
    *   to the Grammar it passes this token to <decl>, after <decl> returns, 
    *   <decls> calls itself to repeat the process. <decls> can be empty, in which 
    *   the LMD is updated with the epsilon rule and <decls> takes no further action.
    */
    private void decls() throws IOException {
        if(currentToken.type == 1) {
            appendLMD("3,"); //  RULE #3: decls --> decl decls
            decl(currentToken);
            decls();
        } else {
            appendLMD("4\n"); //  RULE #4: decls --> epsilon
        }
    }
    
    /************
    *   DECL 
    *   Rule 5: decl --> IDTOK ':' <rest> 
    * 
    *   @param declToken
    * 
    *   <decl> is called by <decls> and a token is passed in as a parameter, this 
    *   token is the currentToken, no check is made against it as the type check 
    *   was done by <decl>. The idToken is then matched and <decl> verifies the 
    *   next token follows the Grammar for a correct declaration statement. It 
    *   then passes the token which was passed to it on to <rest> to determine 
    *   which type the idToken is.
    */
    private void decl(LexicalAnalyzer.Token declToken) throws IOException {
        appendLMD("5,"); // rule 5:  decl --> IDTOK ':' rest 
        match(declToken,1);    // match IDTOK
        match(currentToken,24);    // ':'
        rest(declToken);
    }

    /************
    *   REST   
    *   Rule 6,7: rest --> BASTYPTOK ';' | CONSTTOK BASTYPTOK ASTOK LITTOK ';'
    * 
    *   @param idToken
    * 
    *   <rest> takes a token as a parameter, this is the idToken which the type 
    *   will be assigned to. It then checks the currentToken against 2 possible 
    *   cases. If the token is a basic type token, it begins the process to 
    *   determine what type it is. Since no value is assigned to the idToken in 
    *   this case, the MIPS code will be initialized to 0 or 0.0 in the case of 
    *   a float. This will ensure that the MIPS code is able to be saved under 
    *   the .data section which a new value can be assigned to it in the case 
    *   that the variable it used later in the code.
    */
    private void rest(LexicalAnalyzer.Token idToken) throws IOException {
        
        switch(currentToken.type) {
            case 3:     // BASTYPTOK
                
                appendLMD("6\n");     //  rest --> BASTYPTOK  ';'
                idToken.changeType(currentToken.lexeme);
                match(currentToken,3);  //  BASTYPTOK
                match(currentToken,23); //  ;
                
                String temp;    //   string to hold a temporary value for an unassigned variable.
                switch(idToken.strType){
                    case "boolean":
                    case "integer":
                        temp = "0";
                        break;
                    default:
                        temp = "0.0";
                        break;  
                }
                mips.addWord(idToken.lexeme, idToken.strType, temp);
                addToTable(idToken);
                break;
            case 8:     
                // CONSTTOK
                appendLMD("7\n");         // rest --> CONSTTOK BASTYPTOK ASTOK LITTOK ';'
                match(currentToken,8);
                idToken.setIsConstant();
                idToken.changeType(currentToken.lexeme);
                match(currentToken,3);  // BASTYPTOK
                match(currentToken,25); // ASTOK
                String value = currentToken.lexeme;
                match(currentToken,2);  // LITTOK
                match(currentToken,23); // ;
                mips.addWord(idToken.lexeme, idToken.strType, value);
                idToken.setValue(value);
                addToTable(idToken);
                break;
            default:
                throwException(currentToken, "Invalid rest.");
                break;
        }
    }
    
    /************
    *   STATMNT
    *   Rule 8-13: statmnt --> <assignstat> | <ifstat> | <readstat> | <writestat> | <blockst> | <loopst> 
    *
    *   This function consists of only the switch statement to check if the 
    *   currentToken is the start of a legal statement in Baby ADA. If a case is 
    *   found, the currentToken is passed to the corresponding function, and once 
    *   control is returned, it returns true.
    */
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
    *   ASIGNSTAT 
    *   Rule 14:  assignstat --> <idnonterm> ASTOK <express> ';'
    * 
    *   @param assignToken
    *   
    *   This function receives a token as a parameter and  creates 2 new Expressions 
    *   records. It saves the tokens lexeme to a local variable. This will be used 
    *   in the MIPS code gen, according to the Grammar, the token is an idToken, 
    *   and therefore to be legal in Baby ADA it must have been declared at some 
    *   point before this. The token is passed to <idnonterm> and then it verifies 
    *   the next token matches the Grammar. It then calls <express> and passes it 
    *   an expression record, when this returns it will have the information it 
    *   needs to complete the MIPS code gen and assign a value to the idToken 
    *   variable which it has saved at the start of the function.
    */
    private void assignstat(LexicalAnalyzer.Token assignToken) throws IOException {
        lhs = new ExpressionRecord();
        rhs = new ExpressionRecord();
        mips.codeGen("# starting assignment: "+assignToken.lexeme);
        
        String id = assignToken.lexeme;     // save the identifier name for MIPS code;
        
        appendLMD("14\n");
        idnonterm(assignToken,lhs);  //  IDTOK
        match(currentToken,25); //  :=
        express(rhs);
        match(currentToken,23); //  ;
        
        
        if(!lhs.type.equals(rhs.type)){
            System.out.println("assignStat.lhs.type= "+ lhs.type);
            System.out.println("assignStat.rhs.type= "+ rhs.type);
            warningMessage("assignStat.Type does not match.");
        }
        System.out.println("rhs.reg= "+rhs.reg);
        System.out.println("ex.reg= "+ex.reg);
        
        String register;
        if(lhs.type.equals("integer")){
            register = "t";
            mips.codeGen("sw\t$"+register+ex.reg+","+id+"\t #assignstat: integer");
        } else if ( lhs.type.equals("boolean") ){
            register = "r";
            mips.codeGen("sw\t$"+register+ex.reg+","+id+"\t #assignstat: boolean");
        } else {
            register = "f";
            mips.codeGen("swc1\t$"+register+ex.reg+","+id+"\t #assignstat: float");
        }
        clearReg(register);
        mips.codeGen("\n");
    }

    /**************
    *   IFSTAT 
    *   Rule 15: ifstat --> IFTOK <express> THENTOK <stats> ENDTOK IFTOK ';'
    * 
    *   This function has no parameters, once it is called it creates an Expression 
    *   Record, and a label for MIPS code. It then verifies that the currentToken 
    *   matches the Grammar, then calls <express> with the ER it created. Once 
    *   this returns it verifies that the expression was a boolean type, as this 
    *   is required per the language specifications. If it's not a boolean value, 
    *   it will warn the user of the faulty code. It makes a check for the 'and' 
    *   or 'or' cases, in which case an additional set of MIPS instructions must 
    *   be generated. Afterwards it verifies the rest of the tokens matches the 
    *   grammar correctly and then ends.
    */
    private void ifstat() throws IOException {
        ex = new ExpressionRecord();
        mips.codeGen("# ifstat");
        String label = mips.getLabel("if");
        
        appendLMD("15\n");
        match(currentToken,12); //  IFTOK
        express(ex);
        if( !ex.type.equals("boolean") ) {
            warningMessage("IFSTAT.express: Expression must be a boolean");
        }
        
        if( ex.isOR || ex.isAND ){
            mips.codeGen(ex.branch+"\t$r"+ex.regLHS+","+ex.valueLHS+","+label);
            mips.codeGen(ex.branch+"\t$r"+ex.regRHS+","+ex.valueRHS+","+label);
        } else {
            int reg = ex.reg;
            mips.codeGen(ex.branch+"\t$r"+reg+","+ex.value+","+label);
            freeReg(reg,"r");
        }
        
        match(currentToken,19); //  THENTOK
        stats();
        mips.codeGen(label);
        matchEnds(currentToken,10); //  ENDTOK  //  special match for ifstat and loopstat end tokens
        match(currentToken,12); //  IFTOK   
        match(currentToken,23); //  ;
    }
    
    /****************
    *   READSTAT 
    *   Rule 16: readstat --> READTOK '(' <idnonterm> ')' ';'
    * 
    *   <readstat> takes input from the user and saves it to a temporary location, 
    *   in this case a register depending on which type of input it receives. For 
    *   now only 2 cases are handled, integer and float.
    */
    private void readstat() throws IOException {
        ex = new ExpressionRecord();
        mips.codeGen("# readstat");
        int tempReg;
        
        appendLMD("16\n");
        match(currentToken,11); //  READTOK
        match(currentToken,21); //  (
        idnonterm(currentToken,ex);
        
        if( ex.isConstant ){
            // TODO
        }
        if( ex.type.equals("float") ){
            mips.codeGen("li\t$v0,6\t#"+ex.value);
            mips.codeGen("syscall");
            tempReg = getReg("f");
            mips.codeGen("mov.s\t$v0,$f"+tempReg);
        } else if ( ex.type.equals("integer") ) { 
            mips.codeGen("li\t$v0,5]\t#"+ex.value);
            mips.codeGen("syscall");
            tempReg = getReg("t");
            mips.codeGen("move\t$v0,$t"+tempReg);
        } else {
            // TODO code for boolean
        }
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
    }

    /*****************
    *   WRITESTAT 
    *   Rule 17: writestat --> WRITETOK '(' <writeexp> ')' ';'
    * 
    *   <writstat> deals with 2 cases, the first is a normal 'put' token in which 
    *   case it just matches and moves on, but a 'put_line' means that the user 
    *   wants a new line to be generated after the output, so the global variable 
    *   'doesCR' is used to handle this. If there is a 'put_line' then doesCR is 
    *   set to true. This value will be checked later on during <writeexp>.
    */
    private void writestat() throws IOException {
        mips.codeGen("# writestat");
        
        appendLMD("17,");   //  writestat --> WRITETOK '(' writeexp ')' ';'
        if(currentToken.type == 17) {
            match(currentToken,17); //  put
        } else {
            match(currentToken,18); //  put_line
            doesCR = true; // there is a Carriage Return
        }
        match(currentToken,21); //  (
        writeexp();
        match(currentToken,22); //  )
        match(currentToken,23); //  ;
    }
    
    /*************
    *   LOOPST 
    *   Rule 18: <loopst> --> WHILETOK <express> LOOPTOK <stats>  ENDTOK LOOPTOK ';'
    * 
    *   <loopst> generates three labels for the pseudo code, it lays the first 
    *   one down immediately to signify the beginning of the loop.
    *   Then it gets the expression to be evaluated, <express> must return as a 
    *   boolean value, or else an error is generated. In the special case that 
    *   there is an 'OR' or 'AND' statement.
    */
    private void loopst() throws IOException {
        ex = new ExpressionRecord();
        mips.codeGen("# loopst");
        String label1 = mips.getLabel("while");     //  while#1:
        String endLabel = mips.getLabel("while");
        mips.codeGen(label1);   // set down the first while label.
        
        appendLMD("18,");    // loopst --> WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'
        match(currentToken,20); // match the WHILETOK
        express(ex);    //  get the expression
        
        if( !ex.type.equals("boolean" ) ){ // the Expression of a loopst has to be a boolean
            warningMessage("loopst: expression is not a boolean value, invalid while loop.");
            quit();
        }
        
        // check if the expression was an 'AND' or an 'OR' statement, if either 
        // is true there needs to be some additional pseudo code.
        if( ex.isOR || ex.isAND ){
            mips.codeGen(ex.branch+"\t$r"+ex.regLHS+","+ex.valueLHS+","+endLabel);
            mips.codeGen(ex.branch+"\t$r"+ex.regRHS+","+ex.valueRHS+","+endLabel);
        } 
        //  if not we can just procede as normal.
        else {
            int reg = ex.reg;   // get the register number where the expression value was stored.
            mips.codeGen(ex.branch+"\t$r"+reg+","+ex.value+","+endLabel);
            freeReg(reg,"r");    //   free up the register we were using in the expression.
        }
        
        // complete the grammar
        match(currentToken,14); //  LOOPTOK
        stats();
        mips.codeGen("j\t"+label1); //
        mips.codeGen(endLabel);
        matchEnds(currentToken,10); //  ENDTOK
        match(currentToken,14); //  LOOPTOK
        match(currentToken,23); //  ;
        
    }

    /*****************
    *   BLOCKSTAT  
    *   Rule 19: <blockstat> --> <declpart>   BEGINTOK   <stats>   ENDTOK  ';'
    * 
    *   This function verifies that the grammar is being correctly followed. 
    *   There must be a begin token and there must be an end token.
    *   Both <declpart> and <stats> can be empty, all that is required by the 
    *   grammar are the three tokens.
    */
    private void blockstat() throws IOException {
        appendLMD("19,");
        declpart();
        match(currentToken,7);  //  BEGINTOK
        stats();
        match(currentToken,10); //  ENDTOK
        match(currentToken,23); //  ;
    }
    
    /****************
    *   DECLPART 
    *   Rule 20,21: <declpart> --> DECTOK <decl> <decls> | <epsilon>
    * 
    *   This function checks if the current token is 'declare' then verifies 
    *   that there are declarations. Otherwise <declpart> can be empty in which 
    *   case the function does nothing except append the epsilon rule to the LMD.
    */
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
    *   WRITEEXP  
    *   Rule: 22,23 <writeexp> --> STRLITTOK | <express>
    * 
    * This function generates a label for the current String literal. The current 
    * token has to be a String literal if we have reached this point in the Grammar 
    * so it is not checked here as this would have already been verified before 
    * <writeexp> was called. The String literal is passed to the MIPS code gen 
    * class where it adds the appropriate code to the .data section of the code, 
    * this in turn returns the label that it created for the string value. This 
    * label is assigned to the label variable and used to generate the MIPS code. 
    * The function then checks if doesCr has been set to true, if so then there 
    * was a 'put_line' and MIPS code is generated to call the 'newLine' variable 
    * pre-set in the .data of the MIPS code gen class which creates a new line.
    * 
    * If the current token is not a string literal then an Expression record is 
    * created and passed to expression.
    */
    private void writeexp() throws IOException {
        if(currentToken.type == 26) {    //  STRLITTOK
            
            // adds the string to the .data section and returns the label used.
            String label = mips.genStringLabel(currentToken.lexeme);    
            
            mips.codeGen("li\t$v0,4");
            mips.codeGen("la\t$a0,"+label);
            mips.codeGen("syscall");
            if( doesCR ){   
                // check for a Carriage return. only set to true if WRITETOK was 'put_line'
                mips.codeGen("li\t$v0,4");
                mips.codeGen("la\t$a0,newLine");
                mips.codeGen("syscall");
            }
            mips.expression(currentToken.lexeme);   //  for output.
            
            appendLMD("22\n");  //  writeexp --> STRLITTOK
            match(currentToken,26);
        } else {
            appendLMD("23,");   //  writeexp --> express
            ex = new ExpressionRecord();
            express(ex);
        }
    }
    
    /****************
    *   EXPRESSION 
    *   Rule 24: <express> --> <term> <exprime>
    * 
    *   @param er
    * 
    * This function controls the flow of grammar, it takes as a parameter an 
    * Expression record which it passes first to <term> and then on to <exprime>.
    */
    private void express(ExpressionRecord er) throws IOException {
        appendLMD("24,");    // express --> term expprime
        term(er);
        exprime(er);
    }
    
    /****************
    *   EXPPRIME
    *   Rule 25,26: <expprime> --> ADDOPTOK <term> <termprime> | <empty> 
    * 
    *   @param er
    * 
    * This function checks if the expression is an addition, subtraction or an 
    * 'OR' expression. It creates two expression records, a LHS and a RHS, and 
    * saves the parameter as the LHS. The block of local variables are used for 
    * MIPS code gen and some are used to save values into the global expression 
    * record to be used in MIPS code gen by the calling function.
    */ 
    private void exprime(ExpressionRecord er) throws IOException {
        if(currentToken.type == 4) {
            lhs = new ExpressionRecord();
            lhs = er;
            rhs = new ExpressionRecord();
            
            String oper;
            String load;
            String store;
            String lhsLoc;
            String rhsLoc;
            String reg;
            int tempReg1;
            int tempReg2;
            
            
            mips.expression(currentToken.lexeme);   //  for output.
            char op = currentToken.lexeme.charAt(0);
            
            appendLMD("25,");    // expprime --> ADDOPTOK term termprime
            match(currentToken,4);  //  ADDOPTOK
            term(rhs);  // call <term> and save it's value to the rhs expression record.
            
            // check that neither side is a boolean, otherwise we skip this block, 
            // and make sure the both sides are of the same type.
            if( !(lhs.type.equals("boolean") && rhs.type.equals("boolean")) 
                    && (lhs.type.equals(rhs.type)) ) {
                
                /*
                This switch statement checks which type of operation is to be performed. 
                It then assigns a MIPS pseudo code value to the oper String. 
                */
                switch(op){
                    case '+':
                        if( lhs.type.equals("integer") ){
                            oper = "add";
                        } else {
                            oper = "add.s";
                        }
                        break;
                    default:    // -
                        if( lhs.type.equals("integer") )
                            oper = "sub";
                        else
                            oper = "sub.s";
                        break;
                }
                
                /*
                Next we determine which load and store pseudo code and which 
                registers to use in the code gen. This is needed because the code 
                for integers and floats is different.
                */
                if( lhs.type.equals("integer") ){    // its an integer
                    load = "lw";
                    store = "sw";
                    reg = "t";
                    tempReg1 = getReg("t");
                    tempReg2 = getReg("t");
                } else {                // its a float
                    load = "lwc1";
                    store = "swc1";
                    reg = "f";
                    tempReg1 = getReg("f");
                    tempReg2 = getReg("f");
                }
                
                /*
                This check determines if either side was a literal value, if it 
                is then we have already generated code to load an immediate value 
                and stored it in some register which would have been saved inside 
                the expression record. This code checks if the boolean value for 
                this has been set to true, if so then the location to load the 
                value from was also stored in the expression record and we now 
                want to access this value. Otherwise we need to load the value 
                from the stack pointer offset because it is a declared variable 
                and will have been stored in the symbol table.
                */
                if( lhs.value != null ){
                    lhsLoc = lhs.value;
                } else {
                    lhsLoc = lhs.offSet+"($sp)";
                }
                
                if( rhs.value != null ){
                    rhsLoc = rhs.value;
                } else {
                    rhsLoc = rhs.offSet+"($sp)";
                }
                
                /*
                The next 2 test code blocks check to see if the lhs and/or rhs 
                were litteral values, if they were then we do not need to load 
                the value from a memory location as we have already loaded an 
                immediate value to a register, so it checks if the Expression 
                record boolean value: 'isLitteral' is true, if it is then we skip 
                the load from memory statement and instead update the register 
                value we are using with the one we loaded the immediate value into.
                */
                if( !lhs.isLitteral ){
                    mips.codeGen(load+"\t$"+reg+tempReg1+","+lhsLoc+"\t#exprime --> ADDOPTOK");
                } else {
                    //freeReg(tempReg1,reg);
                    tempReg1 = lhs.reg;
                }
                
                if( !rhs.isLitteral ){
                    mips.codeGen(load+"\t$"+reg+tempReg2+","+rhsLoc);
                } else {
                    //freeReg(tempReg2,reg);
                    tempReg2 = rhs.reg;
                }
                
                //int currOff = st.getOffSet();
                
                // generate the code using the variables 
                mips.codeGen(oper+"\t$"+reg+tempReg1+",$"+reg+tempReg2+","
                        + reg+tempReg1);
                mips.codeGen("#\t:= "+lhsLoc+" "+op+" "+rhsLoc);
                
                //er.setOffset(currOff);   
                er = er.setReg(tempReg1);
                ex = er;
                freeReg(tempReg1,reg);
                freeReg(tempReg2,reg);
                freeReg(lhs.reg,reg);
                freeReg(rhs.reg,reg);
                /*
                if(er.type.equals("integer")){
                    st.changeOffSet(-4);
                } else { st.changeOffSet(-8); }
                */
            } else if(op == 'o' && lhs.type.equals("boolean")) { //  its an 'or'
                tempReg1 = getReg("r");
                tempReg2 = getReg("r");
                /*
                for now boolean values are stored on the stack, this code retrieves 
                them from their location and stores them in registers, then saves 
                all the values in the isOR function of the Expression record.
                */
                mips.codeGen("lw\t$r"+tempReg1+","+lhs.offSet+"($sp)");
                mips.codeGen("lw\t$r"+tempReg2+","+rhs.offSet+"($sp)");
                
                // for MIPS code generation to be used in the calling function 
                // with branch and labels.
                // saves both registers and the values of both sides.
                ex = er.setOR(tempReg1,tempReg2, lhs.value, rhs.value); 
            } else { throwException(currentToken,"EXPRIME: wrong types."); }
            exprime(er);    //   complete the Grammar.
        } else {
            appendLMD("26\n");    //    exprime --> epsilon
        }
    }

    /************
    *   TERM  
    *   Rule 27: term --> <relfactor> <termprime>
    * 
    *   @param er
    * 
    * This function serves as a Grammar check, it takes the expression record 
    * and passes it to <relfactor> and then on to <termprime>
    */
    private void term(ExpressionRecord er) throws IOException {
        appendLMD("27,");   //  term --> relfactor termprime
        relfactor(er);
        termprime(er);
    }
    
    /****************
    *   TERMPRIME 
    *   Rule 28,29: <termprime> --> MULOPTOK <relfactor> <termprime>  |  <empty> 
    *   
    *   @param er
    * 
    * This function verifies the current token is a MULOPTOK, then copies the 
    * Expression record passed to it as a parameter to the LHS, and calls <relfactor> 
    * with a new expression record 'rhs'. When this returns it sets up the variables 
    * for the MIPS code generation then determines which types it is dealing with. 
    * After generating the appropriate MIPS code it frees the registers and finally 
    * calls itself with the updated Expression record to complete the grammar.
    */
    private void termprime(ExpressionRecord er) throws IOException {
        if(currentToken.type == 5) {
            lhs = new ExpressionRecord();
            lhs = er;
            rhs = new ExpressionRecord();
            String oper;    // operator, either add or add.s depending on type
            String load;    // either lw or lwc1 depending on type 
            String store;   // either sw or swc1 depending on type  
            String lhsLoc;  // if ER has a value that will be used in the MIPS 
                            //  code, otherwise a offset on the SP will be used.
                            
            String rhsLoc;  //  ""
            int tempReg1;   // int to hold the temporary register #
            int tempReg2;   // "                                  "
            String reg;     // will be added to the Expression Record to keep 
                            //  track of which store and which register was used.
                            
            char op = currentToken.lexeme.charAt(0);
            
            appendLMD("28,");    //  termprime --> MULOPTOK relfactor termprime
            match(currentToken,5);  //  MULOPTOK
            relfactor(rhs);
            
            //CODEGEN
            // first check to make sure neither side of the expression is a boolean type, 
            // and that both sides are the same type INTEGER or FLOAT.
            if( !(lhs.type.equals("boolean") && rhs.type.equals("boolean")) 
                    && (lhs.type.equals(rhs.type)) ) {
                
                switch(op){
                    case '*':
                        if( lhs.type.equals("integer") ){
                            oper = "mul";
                        } else {
                            oper = "mul.s";
                        }
                        break;
                    default:    //  '/' division or 'mod'
                        if( lhs.type.equals("integer") ){
                            oper = "div";
                        } else {
                            oper = "div.s";
                        }
                        break;
                }
                if( lhs.type.equals("integer") ){    //   integer
                    load = "lw";
                    store = "sw";
                    reg = "t";
                    
                    // need to add code to check for a lhs.reg and rhs.reg before 
                    // getting a new register.
                    tempReg1 = getReg("t");
                    tempReg2 = getReg("t");
                } else {                // float
                    load = "lwc1";
                    store = "swc1";
                    reg = "f";
                    tempReg1 = getReg("f");
                    tempReg2 = getReg("f");
                }
                
                if( lhs.value != null ){
                    lhsLoc = lhs.value;
                } else {
                    lhsLoc = lhs.offSet+"($sp)";
                }
                
                if( rhs.value != null ){
                    rhsLoc = rhs.value;
                } else {
                    rhsLoc = rhs.offSet+"($sp)";
                }
                
                /*
                The next 2 test code blocks check to see if the lhs and/or rhs 
                were litteral values, if they were then we do not need to load 
                the value from a memory location as we have already loaded an 
                immediate value to a register, so it checks the if the Expression 
                record boolean isLitteral is true, if it is then we skip the load 
                from memory statement and instead update the register value we are 
                using with the one we loaded the immediate value into.
                */
                if( !lhs.isLitteral ){
                    mips.codeGen(load+"\t$"+reg+tempReg1+","+lhsLoc+"\t#exprime --> MULOPTOK");
                } else {
                    //freeReg(tempReg1,reg);
                    tempReg1 = lhs.reg;
                }
                
                if( !rhs.isLitteral ){
                    mips.codeGen(load+"\t$"+reg+tempReg2+","+rhsLoc);
                } else {
                    //freeReg(tempReg2,reg);
                    tempReg2 = rhs.reg;
                }
                
                if( lhs.type.equals("integer") ){
                    // if we are dealing with integers we do the operation
                    mips.codeGen(oper+"\t$"+reg+tempReg1+",$"+reg+tempReg2
                            + "\t#exprime --> MULOPTOK integer");
                    
                    // then we have to deal with hi and lo
                    if( op != 'm' )
                    { mips.codeGen("mflo\t$"+reg+tempReg1); }   // quotient for division
                    else
                    { mips.codeGen("mfhi\t$"+reg+tempReg1); }   //  remainder for mod
                    
                } else {
                    // for floating point values we do not need to deal with the 
                    // hi unless we are doing the 'mod' operation
                    mips.codeGen(oper+"\t$"+reg+tempReg1+",$"+reg+tempReg2+","
                            + reg+tempReg1+"\t#exprime --> MULOPTOK float");
                    
                    if( op == 'm' ){
                        mips.codeGen("mfhi\t$"+reg+tempReg1);
                    }
                }
                
                // generate a comment in MIPS to note which values and which operation we did.
                mips.codeGen("#\t:= "+lhsLoc+" "+op+" "+rhsLoc);
                
                //int currOff = st.getOffSet();
                //er.setOffset(currOff);
                er = er.setReg(tempReg1);
                
                // copy this information to the Global Expression Record to be 
                // used by the calling function.
                ex = er;    
                freeReg(tempReg1,reg);   //  free the register we were using.
                freeReg(tempReg2,reg);   //  "                              "
                freeReg(lhs.reg,reg);
                freeReg(rhs.reg,reg);
                /*
                if(er.type.equals("integer")){   //  if the type is integer
                    st.changeOffSet(-4);
                } else { st.changeOffSet(-8); } // or else it is a float.
                */
            } // ADOPTOK is 'and' & both sides are boolean values
            else if(op == 'a' && (lhs.type.equals("boolean") && rhs.type.equals("boolean"))){  
                tempReg1 = getReg("r");
                tempReg2 = getReg("r");
                mips.codeGen("lw\t$r"+tempReg1+","+lhs.offSet+"($sp)");
                mips.codeGen("lw\t$r"+tempReg2+","+rhs.offSet+"($sp)");
                ex = er.setAND(tempReg1,tempReg2, lhs.value, rhs.value);
                freeReg(tempReg1,"r");   //  free the register we were using.
                freeReg(tempReg2,"r");   //  "                              "
            } else { throwException(currentToken,"Termprime: wrong types."); }
            termprime(er);  //  complete the Grammar.
        } else {
            appendLMD("29\n");    //  termprime --> <empty>
        }
    }
    
    /****************
    *   RELFACTOR
    *   Rule 30: <relfactor> --> <factor> <factorprime>
    *   
    *   @param er
    * 
    * Takes the expression records passed to it as a parameter and calls factor, 
    * when this returns the expression record now contains the information from 
    * factor which is the left hand side of the expression. The Expression record 
    * is then used to call <factorprime> to complete the grammar.
    */
    private void relfactor(ExpressionRecord er) throws IOException {
        appendLMD("30,");   //  relfactor --> factor factorprime
        factor(er);
        factorprime(er);
    }
    
    /****************
    *   FACTORPRIME
    *   Rule 31,32: <factorprime> --> RELOPTOK <factor> |  <empty>
    * 
    *   @param er
    * 
    * Takes the ExpressionRecord passed in as a parameter and saves it as the 
    * left hand side of the Expression, then creates a new RHS Expression record 
    * and passes it to Factor, when this returns it then determines the operation 
    * to be performed, generates the appropriate MIPS code based on the type of 
    * the LHS and RHS factors. 
    * 
    *   TODO: implement '<>' and '='
    */
    private void factorprime(ExpressionRecord er) throws IOException {
        if(currentToken.type == 6) {
            appendLMD("31,");    // factorprime --> RELOPTOK factor
            lhs = new ExpressionRecord();
            lhs = er;
            rhs = new ExpressionRecord();
            String relOp = currentToken.lexeme;
            match(currentToken,6);  //  RELOPTOK    <,>,<>,=
            factor(rhs);
            
            /*
                This group of variables is used in MIPS pseudo code generation. In order to simplify the MIPS I decided to use
                String variables and just save the string values depending on different situations. This way I would not need to 
                write the MIPS code for each individual case.
            */
            int reg = getReg("r");
            int reg1 = getReg("r");
            int reg2 = getReg("r");
            String load = "$r"+reg;
            String load1;
            String load2;
            String call;    
            String lhsLoc;
            String rhsLoc;
            
            switch(relOp){
                case ">":   // if r1 > r2 then r2 < r1 == true
                    load1 = "$r"+reg2;  // r2
                    load2 = "$r"+reg1;  // r1
                    break;
                default:    //  <
                    load1 = "$r"+reg1;
                    load2 = "$r"+reg2;
                    //  = and <>
            }
            
            /* 
            These next 2 if statements check whether the value of either expression 
            record (lhs or rhs) is null. This is used if the location in the pseudo 
            code is a location off the stack pointer or if it's the actual variable 
            name declared in .data
            */
            if( lhs.value != null ){
                lhsLoc = lhs.value;
            } else {
                lhsLoc = lhs.offSet+"($sp)";
            }
                
            if( rhs.value != null ){
                rhsLoc = rhs.value;
            } else {
                rhsLoc = rhs.offSet+"($sp)";
            }
                
            // check if the type of the expression is integer or float because the 
            // lw is different for both, and then save it to call.
            if(lhs.type.equals("integer")){
                call = "lw";
            } else {
                call = "lwc1";
            }
            
            //generate the MIPS pseudo code with the variables.
            mips.codeGen("#factorprime");
            mips.codeGen(call+"\t"+load1+","+lhsLoc+"\t# lhs");
            mips.codeGen(call+"\t"+load2+","+rhsLoc+"\t# rhs");
            
            if( relOp.equals(">") || relOp.equals("<") ){
                mips.codeGen("slt\t"+load+","+load1+","+load2);
                mips.codeGen("#\t--- RELOPTOK "+relOp+" processed. ---");
            }
            else {
                // setNot can also handle the case of '=' and '<>' because it
                // changes the branch statement to be 'bne', which is what we 
                // want in both cases.
                er = er.setNot();
                mips.codeGen("#\t--- RELOPTOK "+relOp+" processed. ---");
            }
            
            freeReg(reg1,"r");   // free up the register
            freeReg(reg2,"r");   // free up the register
            er = er.setReg(reg); // save the register where we stored the result to the record.
            er = er.setType("boolean");
        } else {
            appendLMD("32,");    //  factorprime --> epsilon
        }
    }
    
    /************
    *   FACTOR 
    *   Rule 33-36: factor --> NOTTOK <factor>  |  <idnonterm>   |  LITTOK  |  '('  <express>  ')' 
    *   
    *   @param er
    * 
    * matches the current token type to one of 4 cases, if none are matched the 
    * function throws an error exception and terminates the program.
    */
    private void factor(ExpressionRecord er) throws IOException {
        switch(currentToken.type) {
            case 15:    //  NOTTOK
                appendLMD("33,"); //  factor --> NOTTOK factor
                match(currentToken,15); //  NOTTOK
                er = er.setNot();
                factor(er);
                break;
            case 1:     //  IDTOK
                appendLMD("34,");    // factor --> idnonterm
                idnonterm(currentToken,er);
                
                // this is the case that the IDTOK was declared as a boolean value.
                if( er.type.equals("boolean") ){ 
                    int reg = getReg("r");  // get a free register
                    er = er.setOffset(st.offSet);   
                    er = er.setReg(reg);
                    if( er.value.equals("true") ){
                        mips.codeGen("li\t$r"+reg+",1");
                    } else {
                        mips.codeGen("li\t$r"+reg+",0");
                    }
                    mips.codeGen("sw\t$r"+reg+","+er.offSet+"($sp)");
                    mips.expression(currentToken.lexeme);   //  for output.
                    st.changeOffSet(-4);
                    freeReg(reg,"r");    //  free the register
                }
                break;
            case 2:     //  LITTOK
                /*
                 for float and int:
                1. get an available register to hold our floating point value.
                2. get the current offset and save it to our record.
                3. save the type of the token.
                4. save this register in our record.
                5. add a comment to the MIPS code to show where we are.
                6. MIPS code to load the float/int into the register.
                7. add the lexeme to our MIPS comment.
                8. change the offset to reflect a float/int being added.
                */
                appendLMD("35\n");    //  factor --> LITTOK
                char type = getType(currentToken.lexeme);
                switch(type){
                    case '1':
                    case '0':
                        int reg = getReg("r");
                        er = er.setOffset(st.offSet);
                        er = er.setValue(currentToken.lexeme);
                        er = er.setType("boolean");
                        er = er.setReg(reg);
                        ex = er;
                        mips.codeGen("# LITTOK: boolean");
                        if( type == '1' ){
                            mips.codeGen("li\t$r0,1");
                        } else {
                            mips.codeGen("li\t$r0,0");
                        }
                        mips.codeGen("sw\t$r0,"+er.offSet+"($sp)");
                        mips.expression(currentToken.lexeme);   //  for output.
                        st.changeOffSet(-4);
                        freeReg(reg,"r");
                        break;
                    case 'i':
                        int reg1 = getReg("t");
                        er = er.setOffset(st.offSet);
                        er = er.setValue(currentToken.lexeme);
                        er = er.setType("integer");
                        er = er.setReg(reg1);
                        mips.codeGen("# LITTOK: integer");
                        mips.codeGen("li\t$t"+reg1+","+currentToken.lexeme);
                        er = er.setLitteral();
                        mips.expression(currentToken.lexeme);   //  for output.
                        st.changeOffSet(-4);
                        ex = er;
                        break;
                    case 'r':
                        int reg2 = getReg("f");                         
                        er = er.setOffset(st.offSet);                   
                        er = er.setValue(currentToken.lexeme);
                        er = er.setType("float");          
                        er = er.setReg(reg2); 
                        mips.codeGen("# LITTOK: float");         
                        mips.codeGen("li.s\t$f"+reg2+","+currentToken.lexeme);
                        er = er.setLitteral();
                        ex = er;
                        st.changeOffSet(-8);
                        break;
                }
                match(currentToken,2);  //  LITTOK
                break;
            case 21:    // '('
                appendLMD("36,"); //  factor --> '(' express ')'
                match(currentToken,21); //  (
                express(er);
                match(currentToken,22); //  )
                break;
            default:
                throwException(currentToken,"Invalid program, expected a FACTOR");
        }
    }
    
    /****************
    *   IDNONTERM
    *   Rule 37: idnonterm --> IDTOK
    * 
    *   @param token
    *   @param ex
    *   
    * This function first checks if the token has been declared, if not it throws 
    * an exceptions and terminates the program. otherwise, it copies the type, 
    * location and value of the token from the ST in the expression record and 
    * then adds some information to the MIPS codegen which adds comments to our 
    * MIPS code.
    */
    private void idnonterm(LexicalAnalyzer.Token token, ExpressionRecord ex) throws IOException {
        appendLMD("37\n");
        LexicalAnalyzer.Token result = st.findInAllScopes(token.lexeme);
        if(result == null) { 
            throwException(token,"idnonterm: undeclared indentifier or not " 
                    + "accessible from the current scope.");
        } else {
            ex = ex.setOffset(result.offSet);
            ex = ex.setValue(result.lexeme);
            ex = ex.setType(result.strType);
            
            if( result.isConstant ){
                ex = ex.setConstant();
            }
            token = result;
            match(token,token.type);
        }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    
    /***************
    *     MATCH
    * 
    *  @Param token
    *  @param type
    *  
    * This function takes the token passed to it and matches it's type to the type 
    * which was passed into the function. If the type does not match, it throws 
    * an error and the program terminates. If it matches the type, then it checks 
    * if the token is a 'BEGIN' or an 'END', either one will be added to the Symbol 
    * Table via the addToTable function. ( to keep track of scopes ).
    * 
    * Finally, if the token is a ';', then we have reached the end of some code 
    * in the program, and we take this as the cue to add the expression to the MIPS
    * pseudo code generation.
    */
    void match(LexicalAnalyzer.Token matchToken, int type) throws IOException {
        if(matchToken.type == type) {
            System.out.println("matched: "+matchToken.lexeme+" with type "+type);
            
            if(matchToken.type == 7 || matchToken.type == 10){
                addToTable(matchToken);
            }
            if(matchToken.type == 23){
                // add a declared variable to the mips code .data section
                mips.addExpressionToMips();    
            }
            getToken();
        } else { throwException(matchToken," Error: "+matchToken.lexeme
                + " does not match type "+type+" "); }
    }
    
    /**************
    *   MATCHENDS
    *   
    *   @Param token
    *   @param type
    *
    * Special match function used only in the <ifstat> and <loopstat>, this fixes 
    * an issue i was having with the 'end' tokens of IF and LOOP statements affecting 
    * the scope count in the Symbol Table.
    */
    void matchEnds(LexicalAnalyzer.Token token, int type) throws IOException{
        if( token.type == type ){
            // great.
            getToken();
        } else { throwException(token," Error: "+token.lexeme+" does not match type "
                + type+" "); }
        
    }
    
    public void getToken() throws IOException {
        this.currentToken = scanner.getToken();
    }
    
    /***********
     * THROWEXCEPTION
     * @param token
     * @param message
     * 
     * Used for debugging purposed but also will let the programmer know that there
     * is a sever error in the code that completely shuts down the Syntax Analyzer.
     * Some information about the error is printer, such as the line number and the 
     * lexeme of the Token which caused the exception to be thrown. Unrecoverable 
     * errors in the Syntax Analyzer can be things like incorrect grammar, using
     * reserved keywords as idTokens etc.
    */
    private void throwException(LexicalAnalyzer.Token token, String message) {
        output.setErrors(" --> line: "+token.foundAt+" on Token: "+token.lexeme
                + ",  "+message );
        mips.trashCode();
        quit();
    }
    
    /***********
     * ADDTOTABLE
     * @param entry
     * 
     * Takes a token as a parameter and passes it to the Symbol Table to store.
    */
    private void addToTable(LexicalAnalyzer.Token entry) {
        st.insertEntry(entry);
    }
    
    /************
    *   APPENDLMD
    *   @param str
    * 
    *   Function which builds a LMD string of the rules used.
    */
    private void appendLMD(String str) {
        output.setLMD(str);
    }
    
    public void printOutput() {
        output.print();
    }
    
    /*
    Recoverable errors generate a warning message and keep going, however, they
    signl to the code generator to throw out the code as it is faulty.
    */
    private void warningMessage(String message) {
        System.out.println("**WARNING** : "+message);
        mips.trashCode();
    }
    
    // elegant way to end the program.
    private void quit(){
        printOutput();
        mips.WritePostLog();
        mips.printMipsCode();
        st.display();
        System.exit(0);
    }
    
    /****************
    *   GETTYPE 
    *   
    *   @param String str
    *   @return char type
    * 
    *   This function is used to determine the type of a token.
    *   It takes the String value which is equal to the lexeme of the current Token.
    *   It then looks at the first letter of this string to determine which type 
    *   the current token is.
    *   In Baby ADA there are only 2 choices: 
    *   a Boolean which can be either (t)rue or (f)alse, an (i)nteger or a (f)loat.
    *   
    *   getType sets the char 'type' to the value which was determined and returns 
    *   this char back to the calling routine.
    */
    char getType(String str){
        char type;
        str = str.toLowerCase();
        if( str.charAt(0) == 't' ){
            type = '1';
        } else if ( str.charAt(0) == 'f' ) {
            type = '0';
        }else if ( str.contains(".") ){
            type = 'r';
        } else {
            type = 'i';
        }
        return type;
    }
    
    /****************
    *   GETREG
    *   @param reg
    *   @return i
    *   @return -1
    * 
    *   Keeps track of the registers being used in our MIPS intermediate code
    * 
    *   - takes a string argument which it matches to the corresponding array and 
    *       then returns the first available "empty" index.
    *   - index which are equal to 0 are considered empty, and if the index == 1, 
    *       the register number is still being used.
    * 
    *   After an empty register is found this function returns the index number 
    *   to the calling function to be used as a corresponding register number in 
    *   the MIPS code generation.
    * 
    *   If all of the registers are currently being used this function will 
    *   return -1, and the calling function will need to deal with this case. It 
    *   will need to save the value off the stack pointer. Or try to load the value
    * into another register.
    */
    int getReg(String reg){
        switch(reg){
            case "a":
                for( int i = 0; i < 8; i ++ ) {
                    if( sRegisters[i] == 0 ){
                        sRegisters[i] = 1;
                        return i;
                    }
                }
                break;
            case "t":
                for( int i = 0; i < 8; i ++ ) {
                    if( tRegisters[i] == 0 ){
                        tRegisters[i] = 1;
                        return i;
                    }
                }
                break;
            case "f":
                for( int i = 0; i < 8; i ++ ) {
                    if( fRegisters[i] == 0 ){
                        fRegisters[i] = 1;
                        return i;
                    }
                }
                break;
            case "r":
                for( int i = 0; i < 8; i ++ ) {
                    if( rRegisters[i] == 0 ){
                        rRegisters[i] = 1;
                        return i;
                    }
                }
                break;
        }
        return -1;
    }
    
    
    /*************
    *   FREEREG
    * 
    *   @param i
    *   @param reg
    * 
    * This function is designed to help keep track of the registers which are 
    * being used while generating MIPS code.
    * i is the index to change, essentially freeing the register to be used again.
    * reg is the string which is evaluated in the switch statement to determine 
    * which array the int i came from.
    */
    void freeReg(int i, String reg){
        switch(reg){
            case "a":
                sRegisters[i] = 0;
                break;
            case "t":
                tRegisters[i] = 0;
                break;
            case "f":
                fRegisters[i] = 0;
                break;
            case "r":
                rRegisters[i] = 0;
                break;
        }
        System.out.println(reg+i+" Register was freed");
    }
    
    void clearReg(String reg){
        switch(reg){
            case "a":
                sRegisters = new int[8];
                break;
            case "t":
                tRegisters = new int[8];
                break;
            case "f":
                fRegisters = new int[8];
                break;
            case "r":
                rRegisters = new int[8];
                break;
        }
    }
}
