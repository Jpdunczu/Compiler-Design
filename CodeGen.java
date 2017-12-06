package cs4110.homework.pkg1;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CodeGen {
    
    PrintWriter intermediateCode;   //  for printing the output and saving it in a file.
    int labelCount; //  int used to generate unique labels.
    String strings;  //  list to keep track of the strings seen.
    String expression;  //  string to print out the expression which is being translated into mips.
    String words;   //  string used for declarations.
    boolean trashCode;  //  if true the code generation is not printed to a file. Only true if the SA detects an error.
    String outputFile;
    
    Date date;
    SimpleDateFormat dateFormat;
    
    //  String vs. StringBuilder: 
    //  from stack overflow: StringBuilder is optimized for building a String in a loop, 
    //  that way the compiler does not have to rebuild the String over and over again.
    StringBuilder mips; 
    
    public CodeGen(){
        try{
            String out = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.asm'").format(new Date());
            intermediateCode = new PrintWriter("babyADA."+out, "UTF-8");
            
        } catch (IOException e) {
            System.out.println("Error in PrintWriter");
        }
        mips = new StringBuilder();
        labelCount = 0;
        strings = new String();
        expression = new String();
        words = new String();
    }
    
    public void codeGenFile(String file){
        this.outputFile = file;
    }
    
    public void WriteProlog(){
        codeGen("#Prolog:\n" +
                ".text\n" +
                ".globl main\n"+
                "main:\n" +
                "move $sp\n" +
                "la $a0 ProgBegin\n" +
                "li $v0 4\n" +
                "syscall\n" +
                "#End of Prolog\n" +
                "\t#all code will go below here...\n");
    }
    
    public void codeGen(String str){
        mips.append(str).append("\n");
    }
    
    public void WritePostLog(){
        codeGen("#Postlog:\n" +
                "la $a0 ProgEnd\n" +
                "li $v0 4\n" +
                "syscall\n" +
                "li $v0 10\n" +
                "syscall\n\n" +
                ".data\n"+strings+words+
                "newLine:\t.asciiz\t\"\\n\"\n\n"+
                "ProgBegin:\t.asciiz\t\"Program Begin\\n\"\n" +
                "ProgEnd:\t.asciiz\t\"\\nProgram End\\n\"\n");
    }
    
    public void printMipsCode(){
        if( !trashCode ){
            intermediateCode.println(mips);
            intermediateCode.close();
        }
    }
    
    public String genStringLabel(String strlit){
        strings = strings.concat("String"+labelCount+":\t.asciiz\t"+strlit+"\n");
        return "String"+(labelCount++)+":";
    }
    
    public String getLabel(String str){
        return str+(labelCount++)+":";
    }
    
    public void expression(String str){
        this.expression = expression.concat(str+" ");
    }
    
    public void addExpressionToMips(){
        codeGen("#"+expression);
        expression = "";
    }
    
    public void addWord(String str, String type, String value){
        if( type.equals("integer") ){
            type = "word";
        }
        words = words.concat(str+":\t."+type+"\t"+value+"\n");
    }
    
    public void trashCode(){
        this.trashCode = true;
    }
}
