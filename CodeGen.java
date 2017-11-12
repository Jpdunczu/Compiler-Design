package cs4110.homework.pkg1;


import java.io.IOException;
import java.io.PrintWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joshuaduncan
 */
public class CodeGen {
    
    PrintWriter intermediateCode;
    
    //  String vs. StringBuilder: 
    //  from stack overflow: StringBuilder is optimized for building a String in a loop, 
    //  that way the compiler does not have to rebuild the String over and over again.
    StringBuilder mips; 
    
    public CodeGen(){
        try{
            intermediateCode = new PrintWriter("babyADA.intermediateCode.txt", "UTF-8");
        } catch (IOException e) {
            System.out.println("Error in PrintWriter");
        }
        mips = new StringBuilder();
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
                "\t#all code will go below here...");
    }
    
    public void codeGen(String str){
        mips.append(str+"\n");
    }
    
    public void WritePostLog(){
        codeGen("#Postlog:\n" +
                "la $a0 ProgEnd\n" +
                "li $v0 4\n" +
                "syscall\n" +
                "li $v0 10\n" +
                "syscall\n" +
                ".data\n"+
                "ProgBegin:\t.asciiz\t\"Program Begin\\n\"\n" +
                "ProgEnd:\t.asciiz\t\"\\nProgram End\\n\"\n");
    }
    
    public void printMipsCode(){
        intermediateCode.println(mips);
        intermediateCode.close();
    }
}
