/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

import java.util.ArrayList;
import java.util.Scanner;
import cs4110.homework.pkg1.SymbolTable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 *
 * @author joshuaduncan
 */
public class CS4110Homework1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        // TODO code application logic here
        
        File file = new File("/Users/joshuaduncan/NetBeansProjects/CS3120/assignmentOneOutput.txt");
        

        //SymbolTable st = new SymbolTable(myList);
        //st.display();
        

        //BabyADAGenerator badaG = new BabyADAGenerator();
        //LexicalAnalyzer la = new LexicalAnalyzer(file);
        Outputer output = new Outputer();
        SyntaxAnalyzer parser = new SyntaxAnalyzer(output);
        parser.loadFile(file);
        parser.start();
        parser.printOutput();
    }
    
}
