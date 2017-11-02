/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 *
 * @author joshuaduncan
 */
public class LexicalAnalyzer extends FileReader {

    private String nextLine;
    private final File file;
    private final BufferedReader br;
    private int lineNumber;
    private int nextIndex;
    private boolean eoln;
    private final PrintWriter writer;
    private final SymbolTable st;
    private boolean eof;
    private StringBuilder output;
    private SyntaxAnalyzer parser;
    
    /************
    *   TOKENS  *
    *************/
    public static class Token {
        String lexeme;
        int type;
        int foundAt;
        
        Token(String lexeme, int type, int lineNumber){
            this.lexeme = lexeme;
            this.type = type;
            this.foundAt = lineNumber;
        }
    }
    
    /******************
    *   CONSTRUCTOR   *
    *******************/
    public LexicalAnalyzer(File file) throws FileNotFoundException, IOException {
        super(file);
        this.file = file;
        output = new StringBuilder();
        tokenKey();
        st = new SymbolTable();
        br = new BufferedReader( new FileReader(this.file));
        writer = new PrintWriter("CS4110.Scanner.txt", "UTF-8");
        //startScanning();
        //writer.println(output);
        //closeFile();
    }
    
    /************
    *   START   *
    ************/
    public void fileBuffer() throws FileNotFoundException, IOException {
        nextLine = br.readLine().trim();
        
        //System.out.println(nextLine);
        setLineNumber();
        
        if( nextLine.isEmpty() ){
            fileBuffer();
        } else {
            clearAll();
        }
    }

    public char getChar() throws IOException {
        return ( nextLine.length() < nextIndex+1 ) ? '$' : nextLine.charAt(nextIndex++);
    }
    
    public Token getToken() throws IOException {
        
        while( eoln == false ) {
            switch( getChar() ) {
                
                case '$':
                    //System.out.println("end of Line");
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
                        //System.out.print(nextLine.substring(start, nextIndex));
                    }
                    String word = nextLine.substring(start, nextIndex).trim();
                    //System.out.println("start=" + start + "  nextIndex=" + nextIndex);
                    switch( word.toLowerCase() ){
                        case "or": return (new Token(word,4,lineNumber));
                                    
                        case "and": return (new Token(word,5,lineNumber));
                            
                        case "mod": return (new Token(word,5,lineNumber));
                            
                        case "integer": return (new Token(word,3,lineNumber));
                            
                        case "real": return (new Token(word,3,lineNumber));
                            
                        case "boolean": return (new Token(word,3,lineNumber));
                            
                        case "true": return (new Token(word,2,lineNumber));
                            
                        case "false": return (new Token(word,2,lineNumber));
                            
                        case "eof":
                            endOfFile();
                            break;
                        default:
                            return checkWord(word);
                    }
                    //System.out.println("word: " + word);
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
    private Token checkWord(String word) throws IOException {
        if( Pattern.matches("\\d\\d*", word ) ||                    //REG EXP "0..9"
                Pattern.matches("\\d\\d*.\\d\\d*", word) ||     //REG EXP "0..9(0..9)* . 0..9(0..9)*"
                    word.toLowerCase().equals("false") ||
                        word.toLowerCase().equals("true")) {
            return (new Token(word,2,lineNumber));
        } else {
            int tokenNumber = st.isKeyword(word.toLowerCase());
            if( tokenNumber != -1 ) {
                return (new Token(word,tokenNumber+7,lineNumber));
            }
            else
                return (new Token(word,1,lineNumber));
        }
    }
    /*
    private void printOutput(Token token) throws IOException {
        output.append("Token: " + token.lexeme + " Type: " + token.type + " @Line#: " + token.foundAt+ "\n");
    }
    */
    
    public void startScanning() throws IOException{ fileBuffer(); }
    
    //private void printOutput(Token token) throws IOException { parser.parse(token); }
    
    private void backUp() { if(nextIndex > 0) nextIndex--; }
    
    private void endOfFile() { this.eof = true; }
    
    private void endOfLine() { this.eoln = true; }
    
    private void setLineNumber() { lineNumber++; }
    
    private void clearAll() {
        this.nextIndex = 0;
        this.eoln = false;
    }
    
    public void closeFile(){ writer.close(); }
    
    private void tokenKey() {
        this.output.append("  1            identifier\n" +
"  2            any literal\n" +
"  3            types\n" +
"  4            addition operators\n" +
"  5            multiplication operators\n" +
"  6            relational operators\n" +
"  7-20         Keywords\n" +
"  21-25        Punctuation\n" +
"  26           String Literals\n\n");
    } 
}
