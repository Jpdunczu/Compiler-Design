
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
        this.parseTree = new String();
    }
    
    public String getRule(String lmd){
        switch(lmd){
            case "1":
                rule = "Rule 1: stats --> statmt stats";
                break;
            case "2":
                rule = "Rule 2: stats --> <empty>";
                break;
            case "3":
                rule = "Rule 3: decls --> decl decls";
                break;
            case "4":
                rule = "Rule 4: decls --> <empty>";
                break;
            case "5":
                rule = "Rule 5: decl --> IDTOK ':' rest";
                break;
            case "6":
                rule = "Rule 6: rest --> BASTYPTOK ';'";
                break;
            case "7":
                rule = "Rule 7: rest --> CONSTTOK BASTYPTOK ASTOK LITTOK ';'";
                break;
            case "8":
                rule = "Rule 8: statmt --> assignstat";
                break;
            case "9":
                rule = "Rule 9: statmt --> ifstat";
                break;
            case "10":
                rule = "Rule 10: statmt --> readstat";
                break;
            case "11":
                rule = "Rule 11: statmt --> writestat";
                break;
            case "12":
                rule = "Rule 12: statmt --> blockstat";
                break;
            case "13":
                rule = "Rule 13: statmt --> loopstat";
                break;
            case "14":
                rule = "Rule 14: assignstat --> idnonterm ASTOK express ';'";
                break;
            case "15":
                rule = "Rule 15: ifstat --> IFTOK express THENTOK stats ENDTOK IFTOK ';'";
                break;
            case "16":
                rule = "Rule 16: readstat --> READTOK '(' idnonterm ')' ';'";
                break;
            case "17":
                rule = "Rule 17: writestat --> WRITETOK '(' writeexp ')' ';'";
                break;
            case "18":
                rule = "Rule 18: loopst --> WHILETOK express LOOPTOK stats ENDTOK LOOPTOK ';'";
                break;
            case "19":
                rule = "Rule 19: blockst --> declpart BEGINTOK stats ENDTOK ';'";
                break;
            case "20":
                rule = "Rule 20: declpart --> DECTOK decl decls";
                break;
            case "21":
                rule = "Rule 21: declpart --> <empty>";
                break;
            case "22":
                rule = "Rule 22: writeexp --> STRLITTOK";
                break;
            case "23":
                rule = "Rule 23: writeexp --> express";
                break;
            case "24":
                rule = "Rule 24: express --> term expprime";
                break;
            case "25":
                rule = "Rule 25: expprime --> ADDOPTOK term expprime";
                break;
            case "26":
                rule = "Rule 26: exprime --> <empty>";
                break;
            case "27":
                rule = "Rule 27: term --> relfactor termprime";
                break;
            case "28":
                rule = "Rule 28: termprime --> MULOPTOK relfactor termprime";
                break;
            case "29":
                rule = "Rule 29: termprime --> <empty>";
                break;
            case "30":
                rule = "Rule 30: relfactor --> factor factorprime";
                break;
            case "31":
                rule = "Rule 31: factorprime --> RELOPTOK factor";
                break;
            case "32":
                rule = "Rule 32: factorprime --> <empty>";
                break;
            case "33":
                rule = "Rule 33: factor --> NOTTOK factor";
                break;
            case "34":
                rule = "Rule 34: factor --> idnonterm";
                break;
            case "35":
                rule = "Rule 35: factor --> LITTOK";
                break;
            case "36":
                rule = "Rule 36: factor --> '(' express ')'";
                break;
            case "37":
                rule = "Rule 37: idnonterm --> IDTOK";
                break;
        }
        return rule;
    }
    public void print(){
        System.out.println(
            rules+"\n"+
            "************************\n"+
            "* Left Most Derivation *\n"+
            "************************\n"+
            LMD+"\n"+
            parseTree+"\n"+
            "Errors found: " + errors
        );
    }
    
    public void setLMD(String str){
        this.LMD = LMD.concat(str+", ");
        dTree(getRule(str));
    }
    
    public void setErrors(String errors){
        this.errors = this.errors.concat(errors);
    }
    
    public void dTree(String str){
        this.parseTree = this.parseTree.concat("\n"+str);
    }
   
}
