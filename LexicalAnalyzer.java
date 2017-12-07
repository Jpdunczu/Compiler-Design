
package cs4110.homework.pkg1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author joshuaduncan
 */
public class LexicalAnalyzer extends FileReader {

    private String nextLine;    // next line of the file being scanned.
    private final File file;    // the program file to scan.
    
    /*
    Creates a buffering character-input stream that uses an input buffer of the 
    specified size. In this case the default size is used as we are just reading
    one line into the buffer and then reading characters from there.
    */
    private final BufferedReader br;  
    
    
    private int lineNumber; //  line number where the Token was found at.
    private int nextIndex;  //  the next index of the character to be read from the program file.
    
    /*
    This is set to 'true' when the 'EOLN' token is read in from the file. It lets
    the Lexical Analyzer know that it is time to get the next next line from the
    file into the buffer.
    */
    private boolean eoln;   
    private final PrintWriter writer;
    
    /*
    This list of strings holds a set of pre-defined tokens in babyAda, these are 
    reserved keywords which cannot be used by the programmer as idTokens. They are
    stored in this list and any idToken scanned from the program is first compared
    to this list to make sure it is not a reserved keyword. 
    */
    private static final List<String> KEYWORDS = 
            new ArrayList<>(Arrays.asList("begin", "constant", "declare", "end", 
                    "get", "if", "is", "loop", "not", "procedure", "put", 
                        "put_line", "then", "while"));
    
    // this is set to true when the 'EOF' token is read in and signifies the end
    // of file.
    private boolean eof;
    
    /************
    *   TOKEN
    */
    public static class Token {
        String lexeme;
        String strType;
        String value;
        
        int type;
        int foundAt;
        int offSet;
        
        boolean isConstant;
        
        /********
         * TOKEN
         * 
         * @param lexeme
         * @param type
         * @param lineNumber
         * 
         * This is the constructor for the Token class, all Tokens are created 
         * with the lexeme value, type and line number in the program where this 
         * token was found. The lexeme is the word scanned from the program, the 
         * type is an integer value which is determined by the Lexical Analyzer 
         * based on the lexeme. The line number is used for debugging purposes.
        */
        Token(String lexeme, int type, int lineNumber){
            this.lexeme = lexeme;
            this.type = type;
            this.foundAt = lineNumber;
        }
        
        /*********
         * OFFSET
         * 
         * @param offset
         * 
         * All Tokens which are added to the symbol table will have a corresponding 
         * location on the stack. This location is represented by an integer value 
         * which signifies the amount to offset the stack from it's original location 
         * to find this value. This is needed for the MIPS code gen in order to 
         * store the value on the stack. For integers and booleans the offset is 
         * -4 bits. For floats it is -8. This amount represents the amount of bits 
         * needed to store the value in memory. This value will be added to the 
         * global offset saved in the symbol table and used for all tokens being 
         * saved to or returned from the symbol table.
        */
        public void setOffSet(int offset){
            this.offSet = offset;
        }
        
        // used if the token was declared as a constant in the program.
        public void setIsConstant(){
            this.isConstant = true;
        }
        
        /***********
         * CHANGETYPE
         * 
         * @param type
         * 
         * Takes the parameter and matches it to a case, then it sets the strType 
         * variable as the type, and sets the offset of this Token to the 
         * corresponding value for the type it is.
        */
        public void changeType(String type){
            switch(type.toLowerCase()){
                case "integer":
                    this.strType = type;
                    setOffSet(-4);
                    break;
                case "boolean":
                    this.strType = type;
                    setOffSet(-4);
                    break;
                case "float":
                    this.strType = type;
                    setOffSet(-8);
                    break;
            }
        }
        
        /***********
         * SETVALUE
         * 
         * @param value
         * 
         * Sets the value of this token to the literal value declared in 
         * the program. This will be used by any function which later searches 
         * for this token in the Symbol table
        */
        public void setValue(String value){
            this.value = value;
        }
    }
    
    /*********************
    *   LEXICAL ANALYZER
    *   
    *   @param File file
    *   @throws FileNotFoundException
    *   @throws IOException
    *  
    */
    public LexicalAnalyzer(File file) throws FileNotFoundException, IOException {
        super(file);
        this.file = file;
        br = new BufferedReader( new FileReader(this.file));
        writer = new PrintWriter("CS4110.Scanner.txt", "UTF-8");
    }
    
    /****************
    *   FILEBUFFER
    *   
    *   @throws FileNotFoundException
    *   @throws IOException
    * 
    * Reads the next line from the program file and stores it in a buffer so that
    * the characters can be read from it. 
    */
    public void fileBuffer() throws FileNotFoundException, IOException {
        nextLine = br.readLine().trim();
        setLineNumber();
        if( nextLine.isEmpty() ){
            fileBuffer();
        } else {
            clearAll();
        }
    }

    /**********
     * GETCHAR
     * 
     * @return char
     * @throws java.io.IOException
     * 
     * This function first checks if the current index in the buffer is the end 
     * of a line in the program, if true, it returns the '$' character so the 
     * calling function knows this and can call the fileBuffer function to get the
     * next line from the program file. Otherwise it gets the next char from the 
     * input buffer and returns it's value and increments the value of the nextIndex.
    */
    public char getChar() throws IOException {
        return ( nextLine.length() < nextIndex+1 ) ? '$' : nextLine.charAt(nextIndex++);
    }
    
    /************
     * GETTOKEN
     * 
     * @return Token
     * @throws java.io.IOException
     * 
     * This function checks if the EOLN boolean is false, then if calls getChar 
     * to get the next character in the program. It then compares this to it's 
     * list, and creates a Token accordingly.
    */
    public Token getToken() throws IOException {
        
        while( eoln == false ) {
            switch( getChar() ) {
                
                case '$':   // end of line token.
                    clearAll(); 
                    endOfLine();
                    break;
                    
                case ' ':
                    break;
                    
                case ';': return (new Token(";",23,lineNumber));
                    
                case '-':
                    if( getChar() == '-' ){
                        endOfLine();
                        break;
                    } else {
                        backUp();
                        return (new Token("-",4,lineNumber));
                    }
                    
                case ':':
                    if( getChar() == '=' ){
                        return (new Token(":=",25,lineNumber));
                    }
                    else {
                        backUp();
                        return (new Token(":",24,lineNumber));
                    }
                    
                case '(': return (new Token("(",21,lineNumber));
                    
                case ')': return (new Token(")",22,lineNumber));
                    
                case '<':
                    if( getChar() == '>' ){
                        return (new Token("<>",6,lineNumber));
                    } else {
                        backUp();
                        return (new Token("<",6,lineNumber));
                    }
                    
                case '>': return (new Token(">",6,lineNumber));
                    
                case '+': return (new Token("+",4,lineNumber));
                    
                case '/': return (new Token("/",5,lineNumber));
                    
                case '*': return (new Token("*",5,lineNumber));
                    
                case '"':
                    int firstQuote = nextIndex-1;
                    boolean quotedText = true;
                    while( quotedText ){
                        switch( getChar() ){
                            case '"':
                                quotedText = false;
                                break;
                        }
                    }
                    String literal = nextLine.substring(firstQuote, nextIndex).trim();
                    System.out.println("got: "+literal+"from lexAnalyzer");
                    return (new Token(literal,26,lineNumber));
                    
                case '=': return (new Token("=",6,lineNumber));
                    
                default:
                    backUp();
                    int start = nextIndex;
                    boolean check = true;
                    while( check ) {
                        switch( getChar() ) {
                            case ':':
                                check = false;
                                backUp();
                                break;
                            case ';':
                                check = false;
                                backUp();
                                break;
                            case '(':
                                check = false;
                                backUp();
                                break;
                            case ')':
                                check = false;
                                backUp();
                                break;
                            case '>':
                                check = false;
                                backUp();
                                break;
                            case '<':
                                check = false;
                                backUp();
                                break;
                            case '+':
                                check = false;
                                backUp();
                                break;
                            case '-':
                                check = false;
                                backUp();
                                break;
                            case '/':
                                check = false;
                                backUp();
                                break;
                            case '*':
                                check = false;
                                backUp();
                                break;
                            case '"':
                                check = false;
                                backUp();
                                break;
                            case '=':
                                check = false;
                                backUp();
                                break;
                            case ' ':
                                check = false;
                                backUp();
                                break;
                            case '$':
                                check = false;
                                break;
                        }
                    }
                    String word = nextLine.substring(start, nextIndex).trim();
                    switch( word.toLowerCase() ){
                        case "or": return (new Token(word,4,lineNumber));
                                    
                        case "and": return (new Token(word,5,lineNumber));
                            
                        case "mod": return (new Token(word,5,lineNumber));
                            
                        case "integer": return (new Token(word,3,lineNumber));
                            
                        case "float": return (new Token(word,3,lineNumber));
                            
                        case "boolean": return (new Token(word,3,lineNumber));
                            
                        case "true": return (new Token(word,2,lineNumber));
                            
                        case "false": return (new Token(word,2,lineNumber));
                            
                        case "eof":
                            endOfFile();
                            break;
                        default:
                            return checkWord(word);
                    }
                    break;
            }
        }
        if( eof !=  true ) { 
            fileBuffer();
            return getToken();
        } else { return (new Token("eof",-1,-1)); }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    
    
    /***************
     * CHECKWORD
     * @param word
     * 
     * The parameter passed in is a word that needs to be checked against the list
     * of reserved keywords. This function does a little extra work, it also checks
     * for literal tokens, it first compares the word to a few regular expressions
     * to see if its a string of digits, or if it's the literal value "true" or "false".
     * Otherwise it passes the word on to the isKeyword function to check against
     * the reserved keyword list.
     */
    private Token checkWord(String word) throws IOException {
        if( Pattern.matches("\\d\\d*", word ) ||                    //REG EXP "0..9"
                Pattern.matches("\\d\\d*.\\d\\d*", word) ||     //REG EXP "0..9(0..9)* . 0..9(0..9)*"
                    word.toLowerCase().equals("false") ||
                        word.toLowerCase().equals("true")) {
            System.out.println("LexAnalyzer.checkWord.pattern: "+word);
            return (new Token(word,2,lineNumber));
        } else {
            int tokenNumber = isKeyword(word.toLowerCase());
            if( tokenNumber != -1 ) {
                return (new Token(word,tokenNumber+7,lineNumber));
            }
            else
                return (new Token(word,1,lineNumber));
        }
    }
    
    /*************
     * ISKEYWORD
     * @param word
     * 
     * used to check the word against the list of reserved keywords.
    */
    public static int isKeyword(String word) {
        for( String keyword : KEYWORDS ){
            if( keyword.equals(word) )
                return KEYWORDS.indexOf(word);
        } 
        return -1;
    }
    
    // call fileBuffer to get the next line from the program.
    // this is just my attempt to start the scanner elegantly.
    public void startScanning() throws IOException{ fileBuffer(); }
    
    /*
    Important function to step back and focus on the previous character. In cases 
    like '>=' if we see the '>' character we have to check to see if there is 
    also a '=' character following it. If there is then great we found '>=' but 
    if there is not then we need to back up because all we have is the '>' character.
    */
    private void backUp() { if(nextIndex > 0) nextIndex--; }
    
    
    private void endOfFile() { this.eof = true; }
    
    private void endOfLine() { this.eoln = true; }
    
    private void setLineNumber() { lineNumber++; }
    
    /*
    Resets everything back to the starting position. the index of the next character
    to read is set back to 0 and the EOLN value is set back to false. This is called
    when it's time to start reading characters from a new line. This is like a reset button.
    */
    private void clearAll() {
        this.nextIndex = 0;
        this.eoln = false;
    }
}
