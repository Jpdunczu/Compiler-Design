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
    
    /************
    *   TOKENS  *
    *************/
    private static class Token {
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
        st = new SymbolTable();
        br = new BufferedReader( new FileReader(this.file));
        writer = new PrintWriter("CS4110.Scanner.txt", "UTF-8");
        fileBuffer();
        writer.println(output);
        writer.close();
    }
    
    /************
    *   START   *
    ************/
    private void fileBuffer() throws FileNotFoundException, IOException {
        nextLine = br.readLine().trim();
        
        System.out.println(nextLine);
        setLineNumber();
        
        if( nextLine.isEmpty() ){
            fileBuffer();
        } else {
            clearAll();
            getToken();
        }
    }

    public char getChar() throws IOException {
        return ( nextLine.length() < nextIndex+1 ) ? '$' : nextLine.charAt(nextIndex++);
    }
    
    private void getToken() throws IOException {
        
        while( eoln == false ) {
            switch( getChar() ) {
                
                case '$':
                    System.out.println("end of Line");
                    clearAll();
                    endOfLine();
                    break;
                    
                case ' ':
                    break;
                    
                case ';': printOutput(new Token(";",23,lineNumber));
                    break;
                    
                case '-':
                    if( getChar() == '-' ){
                        endOfLine();
                        break;
                    } else {
                        backUp();
                        printOutput(new Token("-",4,lineNumber));
                    }
                    break;
                    
                case ':':
                    if( getChar() == '=' ){
                        printOutput(new Token(":=",25,lineNumber));
                    }
                    else {
                        backUp();
                        printOutput(new Token(":",24,lineNumber));
                    }
                    break;
                    
                case '(': printOutput(new Token("(",21,lineNumber));
                    break;
                    
                case ')': printOutput(new Token(")",22,lineNumber));
                    break;
                    
                case '<':
                    if( getChar() == '>' ){
                        printOutput(new Token("<>",6,lineNumber));
                    } else {
                        backUp();
                        printOutput(new Token("<",6,lineNumber));
                    }
                    break;
                    
                case '+': printOutput(new Token("+",4,lineNumber));
                    break;
                    
                case '/': printOutput(new Token("/",5,lineNumber));
                    break;
                    
                case '*': printOutput(new Token("*",5,lineNumber));
                    break;
                    
                case '"':
                    backUp();
                    int firstQuote = nextIndex;
                    boolean quotedText = true;
                    while( quotedText ){
                        switch( getChar() ){
                            case '"':
                                quotedText = false;
                                break;
                            default:
                        }
                    }
                    String literal = nextLine.substring(firstQuote, nextIndex);
                    printOutput(new Token(literal,1,lineNumber));
                    break;
                    
                case '=': printOutput(new Token("=",26,lineNumber));
                    break;
                    
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
                                backUp();
                                break;
                            default:
                        }
                    }
                    String word = nextLine.substring(start, nextIndex).trim();
                    if( word.equals("EOF") ){
                        endOfFile();
                        break;
                    } else { checkWord(word); }
                    System.out.println("word: " + word);
                    break;
            }
        }
        if( eof !=  true ) { fileBuffer(); }
    }
    
    /****************
    *   UTILITIES   *
    ****************/
    private void checkWord(String word) throws IOException {
        if( Pattern.matches("\\d\\d*", word ) ||                    //REG EXP "0..9"
                Pattern.matches("\\d\\d*.\\d\\d*", word) ||     //REG EXP "0..9(0..9)* . 0..9(0..9)*"
                    word.toLowerCase().equals("false") ||
                        word.toLowerCase().equals("true")) {
            printOutput(new Token(word,2,lineNumber));
        } else {
            int tokenNumber = st.isKeyword(word.toLowerCase());
            if( tokenNumber != -1 ) {
                printOutput(new Token(word,tokenNumber+7,lineNumber));
            }
            else
                printOutput(new Token(word,1,lineNumber));
        }
    }
    
    private void printOutput(Token token) throws IOException {
        output.append("Token: " + token.lexeme + " Type: " + token.type + " @Line#: " + token.foundAt+ "\n");
    }
    
    private void backUp() { nextIndex--; }
    
    private void endOfFile() { this.eof = true; }
    
    private void endOfLine() { this.eoln = true; }
    
    private void setLineNumber() { lineNumber++; }
    
    private void clearAll() {
        this.nextIndex = 0;
        this.eoln = false;
    }
    
    
    
    
}
