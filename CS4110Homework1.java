
package cs4110.homework.pkg1;

import java.io.File;
import java.io.IOException;
/**
 *
 * @author joshuaduncan
 */
public class CS4110Homework1 {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        // TODO code application logic here
        File file = new File("/Users/joshuaduncan/NetBeansProjects/CS4110.homework.1/src/cs4110/homework/pkg1/test_mulops.txt");
        //String path = args[0];
        //File file = new File(path);
        Outputer output = new Outputer();
        SyntaxAnalyzer parser = new SyntaxAnalyzer(output);
        parser.loadFile(file);
        parser.start();
        parser.printOutput();
        
    }
    
}
