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
/**
 *
 * @author joshuaduncan
 */
public class CS4110Homework1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        File file = new File("/Users/joshuaduncan/NetBeansProjects/CS3120/assignmentOneOutput.txt");
        try{
        Scanner scan = new Scanner(file);
        
        ArrayList<String> myList = new <String>ArrayList();
        while(scan.hasNext()) {
            myList.add(scan.next());
        }
        SymbolTable st = new SymbolTable(myList);
        st.display();
        
        }catch(FileNotFoundException e){
            System.out.println("Expected use from CL or Terminal: java AssignmentTwo filename.txt");
        }
        
    }
    
}
