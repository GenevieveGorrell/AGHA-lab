
package gen.nlp.svd;

import java.io.*;
import java.util.Vector;
import gen.math.svd.*;
import gen.math.util.MathVector;
import gen.math.util.Matrix;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Timer;

/** Lab is a command-line environment that allows users to experiment with singular
 * value decomposition and Latent Semantic Analysis.
 * @author Genevieve Gorrell.
 */
public class Lab {
    private Hashtable vectorsets = new Hashtable();
    private Hashtable datafactories = new Hashtable();
    private Hashtable matrices = new Hashtable();
    public LabThread l = null;
    public boolean runningProcess = false;
    public boolean requestStop = false;
    
    /** Class constructor. */    
    public Lab() {
    }
     
    public void go(){
        BufferedReader br = null;
        this.runningProcess = false;
        String input = "";
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception E) {
            System.out.println("Error: " + E);
        }
        System.out.print("GHALAB> ");
        while (!input.equals("exit") && !input.equals("quit") && !input.equals("q")) {
            try {
                input = br.readLine();
                if(this.runningProcess==true){
                    this.requestStop=true;
                } else if (!input.equals("exit") && !input.equals("quit") && !input.equals("q")) {
                    this.l = new LabThread(this, input);
                    this.runningProcess = true;
                    this.l.start();
                }
            } catch (Exception E) {
                System.out.println("Error: " + E);
            }
        }
    }
    
    class LabThread extends Thread {
        Lab lab;
        String input;
        
        public LabThread(Lab lab, String input){
            this.lab = lab;
            this.input = input;
        }
        
        public void run() {
            lab.parseInput(input);
            this.lab.runningProcess = false;
            this.lab.requestStop=false;
            System.out.print("GHALAB> ");
        }
    }
    
    public void parseInput(String input){
        String outputname = null;
        String functionname = null;
        Vector arguments = new Vector();
        String[] firstdivide = input.split("=");
        int firstdividecounter=0;
        if(firstdivide.length==2){
            outputname=firstdivide[0];
            firstdividecounter++;
        }
        String[] seconddivide = firstdivide[firstdividecounter].split("\\(");
        if(seconddivide.length==2){
            functionname=seconddivide[0];
            String temp = seconddivide[1].replace(')',' ');
            temp = temp.replace('"',' ');
            String[] temparguments = temp.split(",");
            int argumentcounter=0;
            for(int i=0;i<temparguments.length;i++){
                String theargument = temparguments[i].trim();
                if(theargument.length()>0){
                    arguments.add(theargument);
                    argumentcounter++;
                }
            }
        }
        if(functionname!=null){
            if(functionname.equals("printall")){
                this.printAll();
            } else if(functionname.equals("deleteall")){
                this.deleteAll();
            } else if(functionname.equals("help") && arguments.size()==1){
                this.help(arguments.elementAt(0).toString());
            } else if(arguments.size()>0){
                String firstargument = arguments.elementAt(0).toString();
                if(!this.checkvariable(firstargument)){
                    if(this.datafactories.containsKey(firstargument) && outputname!=null){
                        this.corpusManipulation(outputname, functionname, arguments);
                    } else if(this.datafactories.containsKey(firstargument) && outputname==null){
                        this.corpusManipulationNoOutput(functionname, arguments);
                    } else if(this.matrices.containsKey(firstargument) && outputname!=null){
                        this.matrixManipulation(outputname, functionname, arguments);
                    } else if(this.matrices.containsKey(firstargument) && outputname==null){
                        this.matrixManipulationNoOutput(functionname, arguments);
                    } else if(this.vectorsets.containsKey(firstargument) && outputname!=null){
                        this.vectorsetManipulation(outputname, functionname, arguments);
                    } else if(this.vectorsets.containsKey(firstargument) && outputname==null){
                        this.vectorsetManipulationNoOutput(functionname, arguments);
                    } else {
                        this.printHelp();
                    }
                } else if(this.checkvariable(firstargument) && outputname!=null && arguments.size()==1){
                    this.loadFunctions(outputname, functionname, arguments);
                } else {
                    this.printHelp();
                }
            } else {
                this.printHelp();
            }
        } else {
            this.printHelp();
        }
    }

    private void loadFunctions(String outputname, String functionname, Vector arguments){
        if(this.checkvariable(outputname)){
            if(functionname.equals("corpus")){
                Corpus corpus = new Corpus(arguments.elementAt(0).toString());
                if(corpus.file!=null){
                    this.datafactories.put(outputname, new DataFactory(corpus));
                }
            } else if(functionname.equals("vectorset")){
                SvdVectorSet savedvectorset = new SvdVectorSet();
                if(savedvectorset.reload(arguments.elementAt(0).toString())){
                    this.vectorsets.put(outputname, savedvectorset);
                }
            } else if(functionname.equals("leftvectors")){
                SvdVectorSet newvectorset = new SvdVectorSet();
                if(newvectorset.loadLeftSideFromFile(arguments.elementAt(0).toString())){
                    this.vectorsets.put(outputname, newvectorset);
                }
            } else if(functionname.equals("rightvectors")){
                SvdVectorSet newvectorset = new SvdVectorSet();
                if(newvectorset.loadRightSideFromFile(arguments.elementAt(0).toString())){
                    this.vectorsets.put(outputname, newvectorset);
                }
            } else {
                this.printHelp();
            }
        } else {
            System.out.println("  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.");
        }
    }
    
    private void matrixManipulation(String outputname, String functionname, Vector arguments){
        if(this.checkvariable(outputname)){
            CorpusMatrix m = (CorpusMatrix)this.matrices.get(arguments.elementAt(0).toString());
            if(functionname.equals("sq1") && arguments.size()==1){
                this.matrices.put(outputname, m.transposeMatrix());
            } else if(functionname.equals("sq2") && arguments.size()==1){
                this.matrices.put(outputname, m.matrixTranspose());
            } else if(functionname.equals("preprocess") && arguments.size()==1){
                this.matrices.put(outputname, m.preprocessMatrix());
            } else if(functionname.equals("dotmatrix1") && arguments.size()==1){
                this.matrices.put(outputname, m.buildColumnComparisonMatrix());
            } else if(functionname.equals("dotmatrix2") && arguments.size()==1){
                this.matrices.put(outputname, m.buildRowComparisonMatrix());
            } else if(functionname.equals("svdbatch") && arguments.size()==3
                    && this.matrices.containsKey(arguments.elementAt(0).toString())){
                int vecnum = Integer.parseInt(arguments.elementAt(1).toString());
                float conv = Float.parseFloat(arguments.elementAt(2).toString());
                if(conv==0) conv = Float.parseFloat("0.001");
                SvdVectorSet v = new SvdVectorSet();
                this.vectorsets.put(outputname, v);
                BatchTrainer bts = new BatchTrainer(BatchTrainer.PARALLEL, vecnum, conv);
                bts.go(m, v);
            } else {
                this.printHelp();
            }
        } else {
            System.out.println("\n  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.\n");
        }
    }
    
    private void matrixManipulationNoOutput(String functionname, Vector arguments){
        CorpusMatrix m = (CorpusMatrix)this.matrices.get(arguments.elementAt(0).toString());
        if(functionname.equals("printvisual") && arguments.size()==1){
            m.comparisonFormat();
        } else if(functionname.equals("printvisual") && arguments.size()==2){
            String filename = arguments.elementAt(1).toString();
            m.comparisonFormat(filename);
        } else if(functionname.equals("printmachine") && arguments.size()==1){
            m.bqFormat();
        } else if(functionname.equals("printmachine") && arguments.size()==2){
            String filename = arguments.elementAt(1).toString();
            m.bqFormat(filename);
        } else if(functionname.equals("save")){
            System.out.println("Save not implemented yet.");
        } else if(functionname.equals("delete") && arguments.size()==1){
            String toDelete = arguments.elementAt(0).toString();
            this.matrices.remove(toDelete);
        } else if(functionname.equals("comparematrices")){
            System.out.println("comparematrices");
        } else if(functionname.equals("print") && arguments.size()==1){
            System.out.println("Matrix, "+m.getPublicLength()+" columns, "+m.getPublicWidth()+" rows.");
        } else {
            this.printHelp();
        }
    }
        
    private void corpusManipulation(String outputname, String functionname, Vector arguments){
        if(functionname.equals("linewise") && (this.checkvariable(outputname)) && arguments.size()==3){
            this.linewise(outputname, functionname, arguments);
        } else if(functionname.equals("passagewise") && (this.checkvariable(outputname)) && arguments.size()==3){
            this.passagewise(outputname, functionname, arguments);
        } else if(functionname.equals("preprepared") && (this.checkvariable(outputname)) && arguments.size()==1){
            DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
            Matrix m = df.getPrepreparedMatrix(new NameSet());
            this.matrices.put(outputname, m);
        } else if(functionname.equals("ngram") && (this.checkvariable(outputname)) && arguments.size()==4){
            this.ngram(outputname, functionname, arguments);
        } else if(functionname.equals("svdincremental") && (arguments.size()==6 || arguments.size()==7)){
            this.svdincremental(outputname, functionname, arguments);
        } else if(functionname.equals("ghapassages") && (arguments.size()==6 || arguments.size()==7)){
            this.ghacorpus(outputname, functionname, arguments, DataFactory.PASSAGEWISE);
        } else if(functionname.equals("ghalines") && (arguments.size()==6 || arguments.size()==7)){
            this.ghacorpus(outputname, functionname, arguments, DataFactory.LINEWISE);
        } else if(functionname.equals("ghaprepcolumns") && (arguments.size()==6 || arguments.size()==7)){
            this.ghacorpus(outputname, functionname, arguments, DataFactory.PREPCOLUMNS);
        } else if(functionname.equals("ghacachecolumns") && (arguments.size()==6 || arguments.size()==7)){
            this.ghacorpus(outputname, functionname, arguments, DataFactory.CACHECOLUMNS);
        } else if(functionname.equals("ghacolumns") && (arguments.size()==5 || arguments.size()==6)){
            this.ghacolumns(outputname, functionname, arguments);
        } else if((functionname.equals("linewise") ||
                   functionname.equals("passagewise") ||
                   functionname.equals("preprepared") ||
                   functionname.equals("ngram")) && !this.checkvariable(outputname)){
            System.out.println("\n  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.\n");
        } else {
            this.printHelp();
        }
    }
     
    private void corpusManipulationNoOutput(String functionname, Vector arguments){
        if(functionname.equals("print") && arguments.size()==1){
            String key = arguments.elementAt(0).toString();
            DataFactory df = (DataFactory)this.datafactories.get(key);
            System.out.println("Corpus, filename "+df.getCorpus().getFilename()+".");
        } else if(functionname.equals("save")){
            System.out.println("Save not implemented yet.");
        } else if(functionname.equals("delete") && arguments.size()==1){
            String toDelete = arguments.elementAt(0).toString();
            this.datafactories.remove(toDelete);
        } else {
            this.printHelp();
        }
    }
    
    private void vectorsetManipulation(String outputname, String functionname, Vector arguments){
        if(this.checkvariable(outputname)){
            if(functionname.equals("reconstruct") && arguments.size()==2
                    && this.vectorsets.containsKey(arguments.elementAt(0).toString())
                    && this.checkvariable(outputname)){
                SvdVectorSet v = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(0).toString());
                int vecnum = Integer.parseInt(arguments.elementAt(1).toString());
                CorpusMatrix m = v.reconstructMatrix(vecnum);
                this.matrices.put(outputname, m);
            } else {
                this.printHelp();
            }
        } else {
            System.out.println("\n  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.\n");
        }
    }
    
    private void vectorsetManipulationNoOutput(String functionname, Vector arguments){
        if(functionname.equals("print") && arguments.size()==2){
            int n = Integer.parseInt(arguments.elementAt(1).toString());
            SvdVectorSet v = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(0).toString());
            v.printTopScorers(n);
        } else if(functionname.equals("comparevectorsets")){
            System.out.println("Comparevectorsets not implemented yet.");
        } else if(functionname.equals("print") && arguments.size()==1){
            SvdVectorSet v = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(0).toString());
            System.out.println("Vectorset, "+v.getS1().size()
                              +" left vectors, "+v.getS2().size()+" right vectors.");
        } else if(functionname.equals("save") && arguments.size()==2){
            SvdVectorSet v = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(0).toString());
            v.save(arguments.elementAt(1).toString());
        } else if(functionname.equals("delete") && arguments.size()==1){
            String toDelete = arguments.elementAt(0).toString();
            this.vectorsets.remove(toDelete);
        } else {
            this.printHelp();
        }
    }
    
    private void svdincremental(String outputname, String functionname, Vector arguments){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        SvdVectorSet v;
        if(this.vectorsets.containsKey(outputname)){
            v = (SvdVectorSet)this.vectorsets.get(outputname);
        } else if (this.checkvariable(outputname)){
            v = new SvdVectorSet();
        } else {
            v = null;
        }
        if(v!=null){
            int n = Integer.parseInt(arguments.elementAt(1).toString());
            int vecnum = Integer.parseInt(arguments.elementAt(2).toString());
            int s1 = Integer.parseInt(arguments.elementAt(3).toString());
            int s2 = Integer.parseInt(arguments.elementAt(4).toString());
            float conv = Float.parseFloat(arguments.elementAt(5).toString());
            SvdVectorSet ref = null;
            if(arguments.size()==7){
                ref = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(6).toString());
            }
            if(conv==0) conv = Float.parseFloat("5000");
            if(s1!=0 && s2!=0){
                NameSet names = new NameSet(s1, s2);
                v.setNames(names);
            }
            IncrementalTrainer its = new IncrementalTrainer(IncrementalTrainer.ONEV, vecnum, n, conv, ref);
            its.go(df.getCorpus(), this, v);
            this.vectorsets.put(outputname, v);
        } else {
            System.out.println("  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.");
        }
    }   
    
    private void ghacorpus(String outputname, String functionname, Vector arguments, int sw){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        SvdVectorSet v;
        if(this.vectorsets.containsKey(outputname)){
            v = (SvdVectorSet)this.vectorsets.get(outputname);
        } else if (this.checkvariable(outputname)){
            v = new SvdVectorSet();
        } else {
            v = null;
        }
        if(v!=null){
            int vecnum = Integer.parseInt(arguments.elementAt(1).toString());
            int rand = Integer.parseInt(arguments.elementAt(2).toString());
            SvdVectorSet ref = null;
            float conv = Float.parseFloat(arguments.elementAt(3).toString());
            if(conv==0) conv = Float.parseFloat("0.001");
            boolean weighting = Boolean.parseBoolean(arguments.elementAt(4).toString());
            int epicsize = Integer.parseInt(arguments.elementAt(5).toString());
            if(arguments.size()==7){
                ref = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(6).toString());
            }
            if(rand!=0){
                NameSet names = new NameSet(rand, rand);
                v.setNames(names);
            }
            GeneralizedHebbian gha = new GeneralizedHebbian(vecnum, -1, conv, sw, 1, ref, epicsize);
            gha.go(df, v, this, true, weighting);
            this.vectorsets.put(outputname, v);
        } else {
            System.out.println("  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.");
        }
    }
  
    private void ghacolumns(String outputname, String functionname, Vector arguments){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        SvdVectorSet v;
        if(this.vectorsets.containsKey(outputname)){
            v = (SvdVectorSet)this.vectorsets.get(outputname);
        } else if (this.checkvariable(outputname)){
            v = new SvdVectorSet();
        } else {
            v = null;
        }
        if(v!=null){
            int vecnum = Integer.parseInt(arguments.elementAt(1).toString());
            SvdVectorSet ref = null;
            float conv = Float.parseFloat(arguments.elementAt(2).toString());
            if(conv==0) conv = Float.parseFloat("0.001");
            boolean weighting = Boolean.parseBoolean(arguments.elementAt(3).toString());
            int epicsize = Integer.parseInt(arguments.elementAt(4).toString());
            if(arguments.size()==6){
                ref = (SvdVectorSet)this.vectorsets.get(arguments.elementAt(5).toString());
            }
            GeneralizedHebbian gha = new GeneralizedHebbian(vecnum, -1, conv, DataFactory.COLUMNS, 1, ref, epicsize);
            gha.go(df, v, this, true, weighting);
            this.vectorsets.put(outputname, v);
        } else {
            System.out.println("  Error! Output name taken. Use \"delete(variablename)\" to free up a variable name.");
        }
    }
    
    private void linewise(String outputname, String functionname, Vector arguments){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        int rowRILength = Integer.parseInt(arguments.elementAt(1).toString());
        int columnRILength = Integer.parseInt(arguments.elementAt(2).toString());
        NameSet names = new NameSet();
        if(rowRILength!=0) names.setRowRILength(rowRILength);
        if(columnRILength!=0) names.setColumnRILength(columnRILength);
        Matrix m = df.getLinewiseMatrix(names, false);
        this.matrices.put(outputname, m);
    }
    
    private void passagewise(String outputname, String functionname, Vector arguments){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        int rowRILength = Integer.parseInt(arguments.elementAt(1).toString());
        int columnRILength = Integer.parseInt(arguments.elementAt(2).toString());
        NameSet names = new NameSet();
        if(rowRILength!=0) names.setRowRILength(rowRILength);
        if(columnRILength!=0) names.setColumnRILength(columnRILength);
        Matrix m = df.getPassagewiseMatrix(names, false);
        this.matrices.put(outputname, m);
    }
    
    private void ngram(String outputname, String functionname, Vector arguments){
        DataFactory df = (DataFactory)this.datafactories.get(arguments.elementAt(0).toString());
        int n = Integer.parseInt(arguments.elementAt(1).toString());
        int rowRILength = Integer.parseInt(arguments.elementAt(2).toString());
        int columnRILength = Integer.parseInt(arguments.elementAt(3).toString());
        NameSet names = new NameSet();
        if(rowRILength!=0) names.setRowRILength(rowRILength);
        if(columnRILength!=0) names.setColumnRILength(columnRILength);
        Matrix m = df.getNGramMatrix(names, n-1, 1);
        this.matrices.put(outputname, m);
    }
    
    private void printAll(){
        System.out.println();
        Iterator iterator = this.datafactories.keySet().iterator();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            DataFactory df = (DataFactory)this.datafactories.get(key);
            System.out.println("  "+key+", corpus, filename \""+df.getCorpus().getFilename()+"\"");
        }
        iterator = this.matrices.keySet().iterator();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            CorpusMatrix m = (CorpusMatrix)this.matrices.get(key);
            System.out.println("  "+key+", matrix, "+m.getPublicLength()+" columns, "+m.getPublicWidth()+" rows");
        }
        iterator = this.vectorsets.keySet().iterator();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            SvdVectorSet v = (SvdVectorSet)this.vectorsets.get(key);
            System.out.println("  "+key+", vectorset, "+v.getS1().size()+" left vectors, "+v.getS2().size()+" right vectors");
        }
        System.out.println();
    }
    
    private void deleteAll(){
        this.datafactories = new Hashtable();
        this.matrices = new Hashtable();
        this.vectorsets = new Hashtable();
    }
            
    private boolean checkvariable(String variablename){
        if(this.vectorsets.containsKey(variablename) 
        || this.datafactories.containsKey(variablename)
        || this.matrices.containsKey(variablename)){
            return false;
        } else {
            return true;
        }
    }
    
    private void printHelp(){
        System.out.print(
"\n"+
"	Load, save, delete and help functions:\n"+
"		Corpus=corpus(<filename>)\n"+
"		Vectorset=vectorset(<filename>)\n"+
"		Vectorset=leftvectors(<filename>)\n"+
"		Vectorset=rightvectors(<filename>)\n"+
"		save(Vectorset, <filename>)\n"+
"		delete(Variable)\n"+
"		deleteall()\n"+
"		help()\n"+
"		help(<functionname>)\n"+
"\n"+
"	Print functions:\n"+
"		printall()\n"+
"		print(Variable)\n"+
"		print(Vectorset, <number of vector pairs>)\n"+
"		printvisual(Matrix)\n"+
"		printvisual(Matrix, <filename>)\n"+
"		printmachine(Matrix)\n"+
"		printmachine(Matrix, <filename>)\n"+
"\n"+
"	Matrix creation and manipulation:\n"+
"		Matrix=linewise(Corpus, <rowlen>, <columnlen>)\n"+
"		Matrix=passagewise(Corpus, <rowlen>, <columnlen>)\n"+
"		Matrix=preprepared(Corpus)\n"+
"		Matrix=ngram(Corpus, <n>, <rowlen>, <columnlen>)\n"+
"		Matrix1=sq1(Matrix2)\n"+
"		Matrix1=sq2(Matrix2)\n"+
"		Matrix1=dotmatrix1(Matrix2)\n"+
"		Matrix1=dotmatrix2(Matrix2)\n"+
"		Matrix1=preprocess(Matrix2)\n"+
"		Matrix=reconstruct(Vectorset, <vecnum>)\n"+
"\n"+
"	Matrix decomposition:\n"+
"		Vectorset=svdbatch(Matrix, <vecnum>, <conv>)\n"+
"		Vectorset=svdincremental(Corpus, <n>, <vecnum>, <rowlen>, <columnlen>, <conv>)\n"+
"		Vectorset=svdincremental(Corpus, <n>, <vecnum>, <rowlen>, <columnlen>, <conv>, Vectorset)\n"+
"		Vectorset=ghapassages(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)\n"+
"		Vectorset=ghapassages(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)\n"+
"		Vectorset=ghalines(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)\n"+
"		Vectorset=ghalines(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)\n"+
"		Vectorset=ghaprepcolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)\n"+
"		Vectorset=ghaprepcolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)\n"+
"		Vectorset=ghacachecolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)\n"+
"		Vectorset=ghacachecolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)\n"+
"		Vectorset=ghacolumns(Corpus, <vecnum>, <conv>, <weighting>, <epochsize>)\n"+
"		Vectorset=ghacolumns(Corpus, <vecnum>, <conv>, <weighting>, <epochsize>, Vectorset)\n"+
"\n"
        );
    }
     
    private void help(String functionname){
        if(functionname.equals("corpus")){
            System.out.println("\n  Corpus=corpus(<filename>) - given a filename (quotes not necessary)"
                              +"\n  loads the file into the variablename Corpus.\n");
        } else if(functionname.equals("vectorset")){
            System.out.println("\n  Vectorset=vectorset(<filename>) - given a filename of a saved"
                              +"\n  vectorset, loads the vectorset into the variablename Vectorset.\n");
        } else if(functionname.equals("leftvectors")){
            System.out.println("\n  Vectorset=leftvectors(<filename>) - given a filename of a plain text"
                              +"\n  file of new line separated vectors, loads the left side of a"
                              +"\n  vectorset into the variable Vectorset.\n");
        } else if(functionname.equals("rightvectors")){
            System.out.println("\n  Vectorset=rightvectors(<filename>) - given a filename of a plain text"
                              +"\n  file of new line separated vectors, loads the right side of a"
                              +"\n  vectorset into the variable Vectorset.\n");
        } else if(functionname.equals("save")){
            System.out.println("\n  save(Vectorset, <filename>) - saves the vectorset Vectorset to the"
                              +"\n  given file location (quotes not necessary.)\n");
        } else if(functionname.equals("delete")){
            System.out.println("\n  delete(Variable) - deletes the given variable.\n");
        } else if(functionname.equals("deleteall")){
            System.out.println("\n  deleteall() - deletes all the variables.\n");
        } else if(functionname.equals("help")){
            System.out.println("\n  help() - summarises all functions."
                              +"\n  help(<functionname>) - prints help for the given function name.\n");
        } else if(functionname.equals("printall")){
            System.out.println("\n  printall() - summarises all the variables currently in memory.\n");
        } else if(functionname.equals("print")){
            System.out.println("\n  print(Variable) - summarises the given variable."
                              +"\n  print(Vectorset, <n>) - for each vector pair, prints the n items"
                              +"\n  most strongly represented in the vector.");
        } else if(functionname.equals("printvisual")){
            System.out.println("\n  printvisual(Matrix) - prints the given matrix to the screen in a"
                              +"\n  format designed to be readable."
                              +"\n  printvisual(Matrix, <filename>) - prints the given matrix to the"
                              +"\n  given file location in a format designed to be readable.\n");
        } else if(functionname.equals("printmachine")){
            System.out.println("\n  printmachine(Matrix) - prints the given matrix to the screen in a"
                              +"\n  format designed to be easy to read in using a computer program."
                              +"\n  printmachine(Matrix, <filename>) - prints the given matrix to the"
                              +"\n  given file location in a format designed to be easy to read in"
                              +"\n  using a computer program.\n");
        } else if(functionname.equals("linewise")){
            System.out.println("\n  Matrix=linewise(Corpus, <rowlen>, <columnlen>) - creates a matrix from"
                              +"\n  the given corpus, lines as columns and words as rows. Specifying row"
                              +"\n  and column length allows vectors to be fixed in size allowing reduced"
                              +"\n  memory footprint at the expense of some accuracy. Enter 0 to keep"
                              +"\n  rows/columns perfectly orthogonal.\n");
        } else if(functionname.equals("passagewise")){
            System.out.println("\n  Matrix=linewise(Corpus, <rowlen>, <columnlen>) - creates a matrix from"
                              +"\n  the given corpus, passages as columns and words as rows. Passages are"
                              +"\n  blank line separated. Specifying row and column length allows vectors"
                              +"\n  to be fixed in size allowing reduced memory footprint at the expense"
                              +"\n  of some accuracy. Enter 0 to keep rows/columns perfectly orthogonal.\n");
        } else if(functionname.equals("preprepared")){
            System.out.println("\n  Matrix=preprepared(Corpus) - reads a matrix from a file into a"
                              +"\n  variable. The matrix file comprises a line of tab separated column"
                              +"\n  headers, a line of tab separated row headers and one tab separated line"
                              +"\n  of numbers for each row of the matrix.\n");
        } else if(functionname.equals("ngram")){
            System.out.println("\n  Matrix=ngram(Corpus, <n>, <rowlen>, <columnlen>) - creates a matrix"
                              +"\n  from a text file. The matrix describes ngrams, with unique n-1grams as"
                              +"\n  rows and unigrams as columns. Each ngram increments a cell count by 1."
                              +"\n  n specifies the n to be used. Specifying row and column length allows"
                              +"\n  vectors to be fixed in size allowing reduced memory footprint at the"
                              +"\n  expense of some accuracy. Enter 0 to keep rows/columns perfectly"
                              +"\n  orthogonal.\n");
        } else if(functionname.equals("sq1")){
            System.out.println("\n  Matrix1=sq1(Matrix2) - multiplies Matrix1 by its transpose and returns"
                              +"\n  the result in Matrix2.\n");
        } else if(functionname.equals("sq2")){
            System.out.println("\n  Matrix1=sq2(Matrix2) - multiplies the transpose of Matrix1 by Matrix1"
                              +"\n  and returns the result in Matrix2.\n");
        } else if(functionname.equals("dotmatrix1")){
            System.out.println("\n  Matrix1=dotmatrix1(Matrix2) - Computes a matrix of dot products in which"
			      +"\n  every normalised column vector in Matrix2 is compared to every other.\n");
        } else if(functionname.equals("dotmatrix2")){
            System.out.println("\n  Matrix1=dotmatrix2(Matrix2) - Computes a matrix of dot products in which"
			      +"\n  every normalised row vector in Matrix2 is compared to every other.\n");
        } else if(functionname.equals("preprocess")){
            System.out.println("\n  Matrix1=preprocess(Matrix2) - applies 1-entropy preprocessing to"
                              +"\n  Matrix1 and returns the result in Matrix2.\n");
        } else if(functionname.equals("reconstruct")){
            System.out.println("\n  Matrix=reconstruct(Vectorset, <vecnum>) - Given a vectorset and a"
                              +"\n  number, creates a matrix from the given number of vector pairs.\n");
        } else if(functionname.equals("svdbatch")){
            System.out.println("\n  Vectorset=svdbatch(Matrix, <vecnum>, <conv>) - given a matrix, uses"
                              +"\n  a simple method to perform singular value decomposition and return"
                              +"\n  a vectorset containing left and right orthonormal bases and value"
                              +"\n  sets. Specify the number of vector pairs to calculate as the second"
                              +"\n  argument and the convergence criterion as the third argument. Enter"
                              +"\n  0 and a default of 0.001 will be used.\n");
        } else if(functionname.equals("svdincremental")){
            System.out.println("\n  Vectorset=svdincremental(Corpus, <n>, <vecnum>, <rowlen>, <columnlen>, <conv>)"
                              +"\n  - given a corpus, performs incremental SVD taking each ngram as a"
                              +"\n  datapoint. n is the number of words in the ngram, vecnum is the number"
                              +"\n  of vector pairs to calculate. Specifying row and column length allows"
                              +"\n  vectors to be fixed in size allowing reduced memory footprint at the"
                              +"\n  expense of some accuracy. Enter 0 to keep rows/columns perfectly"
                              +"\n  orthogonal. Specify a convergence criterion as the final argument, or"
                              +"\n  give 0 and a default of 5000 will be used."
                              +"\n  Vectorset=svdincremental(Corpus, <n>, <vecnum>, <rowlen>, <columnlen>, <conv>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else if(functionname.equals("ghapassages")){
            System.out.println("\n  Vectorset=ghapassages(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)"
                              +"\n  runs the Generalized Hebbian Algorithm on a corpus, treating each"
                              +"\n  blank line separated text passage as a datapoint. vecnum is the number"
                              +"\n  of vectors to calculate. Specifying a row length allows"
                              +"\n  vectors to be fixed in size allowing reduced memory footprint at the"
                              +"\n  expense of some accuracy. Enter 0 to keep rows perfectly orthogonal."
                              +"\n  Specify a convergence criterion as the next argument, or 0 to use the"
                              +"\n  default of 0.001. Set weighting to true to use LSA-style entropy"
                              +"\n  normalisation. epochsize is a heuristic measure only relevant if you"
                              +"\n  are using weighting. With moderate corpus sizes, set this to the number"
                              +"\n  of items in your corpus."
                              +"\n  Vectorset=ghapassages(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else if(functionname.equals("ghalines")){
            System.out.println("\n  Vectorset=ghalines(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>) - runs the"
                              +"\n  Generalized Hebbian Algorithm on a corpus, treating each line as a"
                              +"\n  datapoint. vecnum is the number of vectors to calculate. Specifying"
                              +"\n  a row length allows vectors to be fixed in size allowing reduced"
                              +"\n  memory footprint at the expense of some accuracy. Enter 0 to keep rows"
                              +"\n  perfectly orthogonal. Specify a convergence criterion as the next"
                              +"\n  argument, or 0 to use the default of 0.001. Set weighting to"
                              +"\n  true to use LSA-style entropy normalisation. epochsize is a heuristic"
                              +"\n  measure only relevant if you are using weighting. With moderate corpus"
                              +"\n  sizes, set this to the number of items in your corpus."
                              +"\n  Vectorset=ghalines(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else if(functionname.equals("ghacachecolumns")){
            System.out.println("\n  Vectorset=ghacachecolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)"
                              +"\n  runs the Generalized Hebbian Algorithm on a corpus, treating each"
                              +"\n  line as a matrix column presented as a sparse vector. Vector format"
                              +"\n  is a comma-separated string of numbers alternating indices with values."
                              +"\n  For example, a vector containing two non-zero items 20 and 30 at indices"
                              +"\n  2 and 3 would be presented 2,20,3,30. The algorithm doesn't need to know"
                              +"\n  the dimensionality of the vectors. The corpus is cached for efficiency."
                              +"\n  vecnum is the number of vectors to calculate. Specifying a row length"
                              +"\n  allows vectors to be fixed in size allowing reduced memory footprint"
                              +"\n  at the expense of some accuracy. Enter 0 to keep rows perfectly"
                              +"\n  orthogonal. Specify a convergence criterion as the next argument, or"
                              +"\n  0 to use the default of 0.001. Set weighting to true to use"
                              +"\n  LSA-style entropy normalisation. epochsize is a heuristic"
                              +"\n  measure only relevant if you are using weighting. With moderate corpus"
                              +"\n  sizes, set this to the number of items in your corpus."
		              +"\n  Vectorset=ghacachecolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else if(functionname.equals("ghaprepcolumns")){
            System.out.println("\n  Vectorset=ghaprepcolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>)"
                              +"\n  runs the Generalized Hebbian Algorithm on a corpus, treating each"
                              +"\n  line as a matrix column presented as a sparse vector. Vector format"
                              +"\n  is a comma-separated string of numbers alternating indices with values."
                              +"\n  For example, a vector containing two non-zero items 20 and 30 at indices"
                              +"\n  2 and 3 would be presented 2,20,3,30. The algorithm doesn't need to know"
                              +"\n  the dimensionality of the vectors. vecnum is the number of vectors to"
                              +"\n  calculate. Specifying a row length allows vectors to be fixed in size"
                              +"\n  allowing reduced memory footprint at the expense of some accuracy."
                              +"\n  Enter 0 to keep rows perfectly orthogonal. Specify a convergence criterion"
                              +"\n  as the next argument, or 0 to use the default of 0.001. Set weighting"
                              +"\n  to true to use LSA-style entropy normalisation. epochsize is a heuristic"
                              +"\n  measure only relevant if you are using weighting. With moderate corpus"
                              +"\n  sizes, set this to the number of items in your corpus."
		              +"\n  Vectorset=ghaprepcolumns(Corpus, <vecnum>, <rowlen>, <conv>, <weighting>, <epochsize>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else if(functionname.equals("ghacolumns")){
            System.out.println("\n  Vectorset=ghacolumns(Corpus, <vecnum>, <conv>, <weighting>, <epochsize>)"
                              +"\n  runs the Generalized Hebbian Algorithm on a corpus, treating each"
                              +"\n  column of a preprepared matrix as a datapoint. vecnum is the number"
                              +"\n  of vectors to calculate. Specifying a row length allows"
                              +"\n  vectors to be fixed in size allowing reduced memory footprint at the"
                              +"\n  expense of some accuracy. Enter 0 to keep rows perfectly orthogonal."
                              +"\n  Specify a convergence criterion as the next argument, or 0 to use the"
                              +"\n  default of 0.001. Set weighting to true to use LSA-style entropy"
                              +"\n  normalisation. epochsize is a heuristic measure only relevant if you"
                              +"\n  are using weighting. With moderate corpus sizes, set this to the number"
                              +"\n  of items in your corpus."
		              +"\n  Vectorset=ghacolumns(Corpus, <vecnum>, <conv>, <weighting>, <epochsize>, Vectorset)"
                              +"\n  - as above, but the final argument is a vectorset to which the vectors"
                              +"\n  being calculated will be compared as they converge.\n");
        } else {
            this.printHelp();
        }
    }
    
    private void printWelcome(){
        System.out.println(
"\n  Welcome to GHA Lab v1.0"+
"\n  G. Gorrell 2005"+
"\n"+
"\n  GHA Lab demonstrates singular value decomposition, eigen decomposition"+
"\n  and the Generalized Hebbian Algorithm. It allows textual corpora and sets"+
"\n  of vectors to be read in in various formats, manipulated and compared."+
"\n  Type \"help\" to get help\n\n");
    }
    
    /** Runs Lab.
     * @param args No arguments required.
     */    
    public static void main(String[] args) {
        Lab myLab = new Lab();
        myLab.printWelcome();
        myLab.go();
    }
}
