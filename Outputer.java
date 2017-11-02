/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4110.homework.pkg1;

/**
 *
 * @author joshuaduncan
 */
public class Outputer {
    private String rules = 
"S→	program    :  PROCTOK IDTOK ISTOK decls BEGINTOK stats  ENDTOK IDTOK ';'\n" +
"1,2 	stats :   statmt   stats  |    <empty>\n" +
"3,4	decls :   decl  decls     |    <empty>\n" +
"5	decl  :   IDTOK ':' rest \n" +
"6,7	rest  :   BASTYPTOK  ';'  |   CONSTTOK BASTYPTOK ASTOK LITTOK ';'\n" +
"8-13	statmt :  assignstat  |  ifstat   |  readstat   |  writestat	|  blockst   |  loopst  \n" +
"14	assignstat :  idnonterm  ASTOK express ';'\n" +
"15	ifstat     :  IFTOK express THENTOK  stats ENDTOK IFTOK  ';'\n" +
"16	readstat   :  READTOK '(' idnonterm ')' ';'\n" +
"17	writestat  :  WRITETOK '('  writeexp ')' ';'\n" +
"18	loopst     :  WHILETOK express LOOPTOK stats  ENDTOK LOOPTOK ';'\n" +
"19	blockst    :  declpart   BEGINTOK   stats   ENDTOK  ';'\n" +
"20,21	declpart   :  DECTOK  decl  decls  |  <empty>\n" +
"22,23	writeexp   :  STRLITTOK  |  express\n" +
"24	express    :  term expprime       \n" +
"25,26	expprime   :  ADDOPTOK  term expprime   |  <empty>  \n" +
"27	term       :  relfactor termprime\n" +
"28,29	termprime  :  MULOPTOK  relfactor termprime  |  <empty> \n" +
"30	relfactor  :  factor factorprime\n" +
"31,32	factorprime :  RELOPTOK  factor |  <empty>\n" +
"33-36	factor      :  NOTTOK   factor  |  idnonterm  |  LITTOK |  '('  express  ')' \n" +
"37	idnonterm   :  IDTOK";
    
    private String LMD;
    private String parseTree;
    private String symbols;
    
    private String errors;
    
    private String rule;
    
    Outputer(){
        this.errors = new String();
        this.LMD = new String();
    }
    
    public String getRule(String lmd){
        switch(lmd){
            case "1":
                rule = "";
        }
        return rule;
    }
    public void print(){
        System.out.println(
        rules+"\n"+
        "************************"        +
        "* Left Most Derivation *"+ LMD+"\n"+
        "************************"+
                        "Errors found: " + errors
        );
    }
    
    public void setLMD(String lmd){
        this.LMD = lmd;
    }
    
    public void setErrors(String errors){
        this.errors = this.errors.concat(errors);
    }
   
}
