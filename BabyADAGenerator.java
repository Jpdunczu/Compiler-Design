/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Math.random;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author joshuaduncan
 */
public class BabyADAGenerator {
    
    public StringBuilder babyADA = new StringBuilder("procedure "); 
    private String programName;
    
    public BabyADAGenerator() throws InstantiationException, IllegalAccessException{
        programName = getName();
        babyADA.append(programName);
        babyADA.append(statList());
        babyADA.append("\nend "+programName+";");
        try{
            PrintWriter writer = new PrintWriter("babyADAGeneratorOutput.txt", "UTF-8");
            writer.println(babyADA);
            writer.close();
            } catch (IOException e) {
                System.out.println("Error in PrintWriter");
            }
        System.out.println(babyADA);
    }
    
    private double rand() {  // GENERATE A RANDOM NUMBER BETWEEN 0.0 AND 1.0
        double result = random();   
        return result;
    }
    
    private String getName() {
        int len = 1+ (int)(Math.random() * 32);
        //System.out.println("len = " + len);
        int letter;
        char[] newName = new char[len];
        String name;
        for( int i = 0; i < len; i++ ) {
            double result = rand();
            if( result > .5 ) {
                letter = 65+ (int)(result*26);
                //System.out.println("letter >.5= " + letter);
            } else {
                letter = 97+ (int)(result*26);
                //System.out.println("letter <.5= " + letter);
            }
            Character ascii = (char)letter;
            newName[i] = ascii;
            //System.out.println("ascii= " + ascii);
        }
        return name = new String(newName);
    }
    
    private String statList() {
        String statList;
        
        if( rand() > .3 ) { 
            statList = statList() + stat();
        } else {
            statList = stat();
        }
        
        return "\nbegin" + statList + "\nend;";
    }
    
    private String stat() {
        String stat;
        double result = rand();
        
        if( result < .3 ) {
            stat = simple_stat();
        }else {
            stat = compound_stat();
        }
        return stat;
    }
    
    private String compound_stat() {
        String stat;
        double result = rand();
        
        if( result < .15 ) {
            stat = ifStat();    //      IF STATEMENT
        } else if ( result >= .15 && result < .3 ) {
            stat = iterStat();  //      ITERATIVE STATEMENT
        } else if ( result >= .3 && result < .4 ) {
            stat = empdStat();  //      EMPTY STATEMENT
        } else if ( result >= .4 && result < .7 ) {
            stat = declStat();  //      DELCARATION STATEMENT
        } else {
            stat = assignStat();    //  ASSIGNMENT STATEMENT
        }     
        return stat;  
    }
    
    private String assignStat() {
        return "\n\t" + getName() + " := " + exp() + ";\n";
    }
    
    private String simple_stat() {
        return "\n\t" + getName() + " : " + type() + ";\n";
    }
    
    private String ifStat() {    
        String ifStat;
        double result = rand();
        
        if( result <= .2 ) {
            ifStat = stat(); //                                   IF ( <EXP> )  <STAT>
        } else if ( result > .2 && result <= .6 ) {
            ifStat = compound_stat(); //                               IF ( <EXP> )  <CMPD_STAT>
        } else if ( result > .6 && result <= .7 ) {
            ifStat = stat() + "\n\telseif " + stat(); //         IF ( <EXP> )  <STAT> "ELSEIF" ANOTHER <STAT>
        } else if ( result > .7 && result <= .8 ) {
            ifStat = compound_stat() + "\n\telse " + stat(); //     IF ( <EXP> ) <CMPD_STAT> ELSE <STAT>
        } else if ( result > .8 && result <= .9 ) {
            ifStat = stat() + "\n\telse " + compound_stat(); //     IF ( <EXP> ) <STAT> ELSE <CMPD_STAT>
        } else {
            ifStat = compound_stat() + "\n\telse " + compound_stat(); // IF ( <EXP< ) <CMPD_STAT> ELSE <CMPD_STAT>
        }
        
        return "\n\tif " + exp() + " then put" + ifStat + " endif;\n";
    }
    
    
    private String iterStat() {
        /*
            [ ( "while" condition ) |  
            "loop" 
                sequence_of_statements 
            "end" "loop" ";" 
        */
        double result = rand();
        
        if( result < .5 ){
            return "\n\twhile not ( " + exp() + " ) loop\n\t" + statList() + "\n\t end loop;";
        } else {
            return "\n\twhile ( " + exp() + " ) loop\n\t" + statList() + "\n\t end loop;";
        }
    }

    private String declStat() {
        double result = rand();
        String declStat = new String();
        /*
            ( defining_identifier_list ":" [ "aliased" ] [ "constant" ] 
            subtype_indication [ ":=" expression ] ";" ) 
        */
        if( result < .33 ) {
            declStat = getName() + " : constant " + type() + " := " + exp();
        } else if ( result >= .33 && result < .66 ) {
            declStat = getName() + " : aliased " + type() + " := " + exp();
        } else {
            declStat = getName() + " : " + type() + " := " + exp();
        }
        return "\n\t" + declStat;
    }

    private String exp() {
        double result = rand();
        /*
        relation { "and" relation } | relation { "and" "then" relation } 
      | relation { "or" relation } | relation { "or" "else" relation } 
      | relation { "xor" relation } 
        */
        if( result < .1 ){
            return relation() + " and " + relation(); 
        } else if ( result >= .1 && result < .2 ) {
            return relation() + " and then " + relation();
        } else if ( result >= .2 && result < .3 ) {
            return relation() + " or " + relation(); 
        } else if ( result >= .3 && result < .4 ) {
            return relation() + " or else " + relation();
        } else if ( result >= .4 && result < .5 ) {
            return "\"This is a String literal !@#$%^&*() 1234567890  ?><:;'|\\?/}{[]|+_-\";";
        } else {
            return relation();
        }
    }
    
    private String op() {
        double result = rand();
        
        // opperators
        
            if( result < .125 ){ return " + "; } 
            else if ( result >= .125 && result < .25 ) { return " - "; } 
            else if ( result >= .25 && result < .375 ){ return " or "; } 
            else if ( result >= .5 && result < .625 ){ return " * "; } 
            else if ( result >= .625 && result < .750 ) { return " / "; } 
            else if ( result >= .750 && result < .875 ) { return " mod "; }
            return " and ";
    }
    
    
    private String type() {
        double result = rand();
        
        if( result < .33 ) {
            return "integer";
        } else if ( result >= .33 && result < .66 ) {
            return "real";
        }
        return "boolean";
    }
    
    private String real() {
        return digit() + digitSeq() + "." + digit() + digitSeq();
    }
    
    private String character() {
        double result = rand();
        int i = (int)(result *100) + 65;
        Character ascii = (char)i;
        return ascii.toString();
    }
    
    private String digit() { 
        Integer n = (int) (rand()*10);
        String digit = n.toString();
        return digit;
    }
    
    private String charDigitSeq() {
        double result = rand();
        
        if( result < .25 ) { return character() + charDigitSeq(); } 
        else if ( result >= .25 && result < .5 ) { return character() + charDigitSeq() + digitSeq(); } 
        else if ( result >= .5 && result < .75 ) { return character() + charDigitSeq() + digitSeq() + "_"+ charDigitSeq(); } 
        else { return character(); }
    }
    
    private String digitSeq() {
        double result = rand();
        
        if( result < .5 ) { return digit() + digitSeq(); } 
        else { return digit(); }
    }

    private String empdStat() {
        return "\t-- This is a comment in Baby ADA that produces no Tokens !@#$%^&*()_+-=|}{\\/?><';:";
    }

    private String relation() {
        double result = rand();
        /*
            ( simple_expression [ ( "=" | "/=" | "<" | "<=" | ">" | ">=" ) simple_expression ] ) 
            | ( simple_expression [ "not" ] "in" ( range | subtype_mark ) ) 
        */
        if( result < .125 ) { return simpleExpression() + " <= " + simpleExpression(); } 
        else if ( result >= .125 && result < .25 ) { return simpleExpression() + " = " + simpleExpression(); } 
        else if ( result >= .25 && result < .375 ) { return simpleExpression() + " <> " + simpleExpression(); } 
        else if ( result >= .5 && result < .675 ) { return simpleExpression() + " > " + simpleExpression(); } 
        else if ( result >= .675 && result < .9 ) { return simpleExpression() + " < " + simpleExpression(); } 
        else { return simpleExpression() + " >= " + simpleExpression(); }
    }

    private String simpleExpression() {
        double result = rand();
        /*
            simple_expression ::= [ ( "+" | "-" ) ] term { ( "+" | "-" | "&" ) term } 
        */
        if( result < .5 ) { return term() + op() + term(); } 
        else { return term() + op() + term(); }
    }

    private String term() {
        double result = rand();
        
        if( result < .5 ){ return real(); } 
        else { return digitSeq(); }
    }
}
