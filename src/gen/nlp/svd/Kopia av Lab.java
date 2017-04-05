
package gen.nlp.svd;

import java.io.*;
import java.util.Vector;
import gen.math.svd.*;
import gen.math.util.MathVector;
import gen.math.util.Matrix;
import java.util.Hashtable;

/** Lab is a command-line environment that allows users to experiment with singular
 * value decomposition and Latent Semantic Analysis.
 * @author Genevieve Gorrell.
 */
public class Lab {
    private DataFactory df = null;
    private DataFactory df1 = null;
    private SvdVectorSet vectorset = new SvdVectorSet();
    private SvdVectorSet referenceset = new SvdVectorSet();
    private Hashtable variables = new Hashtable();
    
    /** Class constructor. */    
    public Lab() {
    }
    
    private Vector parseArguments(String[] words){
        Vector parsedArguments = new Vector();
        for(int i=0;i<words.length;i++){
            String[] pair = new String[2];
            if(words[i].matches(".*=.*")){
                pair = words[i].split("=");
            } else {
                pair = new String[2];
                pair[0]=words[i];
                pair[1]="true";
            }
            parsedArguments.add(pair);
        }
        return parsedArguments;
    }
    
    private void parseInput(String input){
        String[] words = input.split("[ \t]");
        String[] args = new String[words.length-1];
        for(int i=0;i<words.length-1;i++){
            args[i]=words[i+1];
        }
        Vector arguments = this.parseArguments(args);
        try {
            if(input.startsWith("help")){
                this.printHelp();
            } else if(input.startsWith("load")){
                this.load(arguments);
            } else if(input.startsWith("print")){
                this.print(arguments);
            } else if(input.startsWith("svd")){
                this.svd(arguments);
            } else if(input.startsWith("gha")){
                this.gha(arguments);
            } else if(input.startsWith("clear vectorset")){
                this.clearVectorset();
            } else if(input.startsWith("save vectorset")){
                this.saveVectorset(arguments);
            } else if(input.startsWith("accuracy")){
                this.calculateAccuracy(arguments);
            } else if(input.startsWith("compare vectorset")){
                this.compareVectorset(arguments);
            } else {
                this.printHelp();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void printHelp(){
        System.out.println();
        System.out.println("  load (corpus=<filename>/vectorset=<filename>)");
        System.out.println("  print corpus statistics");
        System.out.println("  print corpus [(bqformat/def=visual)] (linewise/passagewise/preprepared) [(sq1/sq2/preprocess)] [outputfile=<output file>]");
        System.out.println("  print corpus [(bqformat/def=visual)] ngram <n> [(sq1/sq2/preprocess)] [outputfile=<output file>]");
        System.out.println("  print vectorset topn=<example number>");
        System.out.println("  print vectorset reconstruct=<vector number> [(bqformat/def=visual)] [<output file>]");
        System.out.println("  svd batch [(serial/def=parallel)] (linewise/passagewise/preprepared) [preprocess] vectornumber=<int> [convergence=<float, def=0.0000001>]");
        System.out.println("  svd batch [(serial/def=parallel)] ngram [preprocess] vectornumber=<int> n=<int> [convergence=<float, def=0.0000001>] [s1Length=<int> s2Length=<int>]");
        System.out.println("  svd incremental [(def=onev/upto)] vectornumber=<int> n=<int> [convergence=<int, def=5000>]  [s1Length=<int> s2Length=<int>]");
        System.out.println("  gha [(def=onev/upto)] vectornumber=<int> [convergence=<float, def=0.0000001>] [sLength=<int>] (linewise/passagewise/columns/prepcolumns) [initialiser=<float, def=1>]");
        System.out.println("  clear vectorset - initialises the vectorset");
        System.out.println("  accuracy n=<int>");
        System.out.println("  save vectorset=<string> - saves the vectorset");
        System.out.println("  help - prints this message");
        System.out.println("  exit - exits Lab");
        System.out.println();
    }
     
    private void load(Vector arguments){
        if(arguments.size()>0){
            String[] arg=(String[])arguments.elementAt(0);
            String[] arg1=null;
            if(arguments.size()>1){
                arg1=(String[])arguments.elementAt(1);
            }
            if(arg[0].equals("corpus") && !arg[1].equals("true")){
                Corpus corpus = new Corpus(arg[1]);
                this.df = new DataFactory(corpus);
            } else if(arg[0].equals("vectorset")){
                this.vectorset.reload(arg[1]);
            } else if(arg[0].equals("referenceset") && arg1!=null){
                if(arg1[0].equals("leftside")){
                    this.referenceset.loadLeftSideFromFile(arg1[1]);
                } else if(arg1[0].equals("rightside")){
                    this.referenceset.loadRightSideFromFile(arg1[1]);
                } else {
                    System.out.println("  Error! Invalid argument to load referenceset.");
                }
            } else if(arg[0].equals("referencevals") && arg1!=null){
                this.referenceset.loadValuesFromFile(arg1[1]);
            } else {
                this.printHelp();
            }
        } else {
            this.printHelp();
        }
    }
    
    private void print(Vector arguments){
        NameSet names = new NameSet();
        int CORPUS = 0;
        int VECTORSET = 1;
        int STATISTICS = 2;
        int VECTORS = 3;
        int VISUAL = 0;
        int BQ = 1;
        int SQ1 = 12;
        int SQ2 = 13;
        int PREPROCESS = 14;
        int toPrint = -1;
        int format = VISUAL;
        int matrixtype = -1;
        int preprocessed = -1;
        int n = 2;
        int topn = -1;
        int reconstruct = -1;
        String outputfile = null;
        boolean badarg = false;
        for(int i=0;i<arguments.size();i++){
            String[] pair = (String[])arguments.elementAt(i);
            if(pair[0].equals("corpus")){
                toPrint=CORPUS;
            } else if(pair[0].equals("vectorset")){
                toPrint=VECTORSET;
            } else if(pair[0].equals("statistics")){
                toPrint=STATISTICS;
            } else if (pair[0].equals("bqformat")){
                format=BQ;
            } else if (pair[0].equals("visual")){
                format=VISUAL;
            } else if (pair[0].equals("sq1")){
                preprocessed=SQ1;
            } else if (pair[0].equals("sq2")){
                preprocessed=SQ2;
            } else if (pair[0].equals("preprocess")){
                preprocessed=PREPROCESS;
            } else if (pair[0].equals("ngram")){
                matrixtype=DataFactory.NGRAM;
            } else if (pair[0].equals("linewise")){
                matrixtype=DataFactory.LINEWISE;
            } else if (pair[0].equals("passagewise")){
                matrixtype=DataFactory.PASSAGEWISE;
            } else if (pair[0].equals("preprepared")){
                matrixtype=DataFactory.PREPREPARED;
            } else if (pair[0].equals("s1Length")){
                names.setRowRILength(Integer.parseInt(pair[1]));
            } else if (pair[0].equals("s2Length")){
                names.setColumnRILength(Integer.parseInt(pair[1]));
            } else if (pair[0].equals("n")){
                n=Integer.parseInt(pair[1]);
            } else if (pair[0].equals("topn")){
                topn=Integer.parseInt(pair[1]);
            } else if (pair[0].equals("reconstruct")){
                reconstruct=Integer.parseInt(pair[1]);
            } else if (pair[0].equals("outputfile")){
                outputfile=pair[1];
            } else if (pair[0].equals("vectors")){
                toPrint=VECTORS;
            } else {
                badarg = true;
            }
        }
        if(badarg==true){
            this.printHelp();
        } else if(toPrint==CORPUS){
            this.printCorpus(names, format, matrixtype, n, outputfile, preprocessed);
        } else if(toPrint==STATISTICS){
            if(this.df==null){
                System.out.println("\n  No corpus loaded.\n");
            } else {
                this.df.printCorpusStatistics();
            }
        } else if(toPrint==VECTORSET){
            this.printVectorSet(format, matrixtype, topn, reconstruct, outputfile);
        } else if(toPrint==VECTORS){
            if(this.vectorset!=null){
                Vector s1 = this.vectorset.getS1();
                Vector s2 = this.vectorset.getS2();
                int upto = s1.size();
                if(s2.size()!=upto){
                    System.out.println("Error! Flawed vectorset.");
                } else {
                    for(int i=0;i<upto;i++){
                       ((MathVector)s1.elementAt(i)).print(false);
                       ((MathVector)s2.elementAt(i)).print(false);
                       System.out.println();
                    }
                }
            }
        } else {
            this.printHelp();
        }
    }
    
    private void printCorpus(NameSet names, int format, int matrixtype, int n, String outputfile, int preprocessed){
        int VISUAL = 0;
        int BQ = 1;
        int SQ1 = 12;
        int SQ2 = 13;
        int PREPROCESS = 14;
        if(this.df==null){
            System.out.println("\n  No corpus loaded.\n");
        } else if(matrixtype==DataFactory.NGRAM
                    || matrixtype==DataFactory.LINEWISE
                    || matrixtype==DataFactory.PASSAGEWISE
                    || matrixtype==DataFactory.PREPREPARED){
            CorpusMatrix m = null;
            System.out.print("  Reading in the corpus ... ");
            if(matrixtype==DataFactory.NGRAM){
                m = this.df.getNGramMatrix(names, n-1, 1);
            } else if(matrixtype==DataFactory.PREPREPARED){
                m = this.df.getPrepreparedMatrix(names);
            } else if(matrixtype==DataFactory.LINEWISE){
                m = this.df.getLinewiseMatrix(names, false);
            } else if(matrixtype==DataFactory.PASSAGEWISE){
                m = this.df.getPassagewiseMatrix(names, false);
            }
            System.out.println("Corpus in memory.");
            if(preprocessed==SQ1){
                m = m.matrixTranspose();
                if(matrixtype==DataFactory.LINEWISE){
                    m.multiplyMatrixByScalar((float)1/this.df.countLines());
                } else if(matrixtype==DataFactory.PASSAGEWISE){
                    m.multiplyMatrixByScalar((float)1/this.df.countPassages());
                }
            } else if(preprocessed==SQ2){
                m = m.transposeMatrix();
                if(matrixtype==DataFactory.LINEWISE){
                    m.multiplyMatrixByScalar((float)1/this.df.countLines());
                } else if(matrixtype==DataFactory.PASSAGEWISE){
                    m.multiplyMatrixByScalar((float)1/this.df.countPassages());
                }
            } else if(preprocessed==PREPROCESS){
                m = m.preprocessMatrix();
            }
            System.out.println("  Printing ...");
            if(format==VISUAL){
                if(outputfile==null){
                    m.comparisonFormat();
                } else {
                    m.comparisonFormat(outputfile);
                }
            } else if(format==BQ){
                if(outputfile==null){
                    m.bqFormat();
                } else {
                    m.bqFormat(outputfile);
                }
            }
        } else {
            this.printHelp();
        }
    }
    
    private void printVectorSet(int format, int matrixformat, int topn, int reconstruct, String outputfile){
        int VISUAL = 0;
        int BQ = 1;
        if(topn!=-1){
            this.vectorset.printTopScorers(topn);
        } else if(reconstruct!=-1){
            CorpusMatrix m = this.vectorset.reconstructMatrix(reconstruct);
            if(format==BQ){
                if(outputfile==null){
                    m.bqFormat();
                } else {
                    m.bqFormat(outputfile);
                }
            } else {
                if(outputfile==null){
                    m.comparisonFormat();
                } else {
                    m.comparisonFormat(outputfile);
                }
            }
        } else {
            this.printHelp();
        }
    }
    
    private void gha(Vector arguments){
        int substrategy = GeneralizedHebbian.ONEV;
        int s = -1;
        int vectornumber = 1;
        int n = 2;
        float settleddistance = Float.parseFloat("0.00001");
        int matrixtype = DataFactory.LINEWISE;
        float initialValue = 1;
        boolean columns = false;
        for(int i=0;i<arguments.size();i++){
            String[] pair = (String[])arguments.elementAt(i);
            if(pair[0].equals("upto")){
                substrategy=GeneralizedHebbian.UPTO;
            } else if(pair[0].equals("onev")){
                substrategy=GeneralizedHebbian.ONEV;
            } else if(pair[0].equals("vectornumber")){
                vectornumber = Integer.parseInt(pair[1]);
            } else if(pair[0].equals("convergence")){
                settleddistance = Float.parseFloat(pair[1]);
            } else if(pair[0].equals("sLength")){
                s = Integer.parseInt(pair[1]);
            } else if(pair[0].equals("passagewise")){
                matrixtype = DataFactory.PASSAGEWISE;
            } else if(pair[0].equals("linewise")){
                matrixtype = DataFactory.LINEWISE;
            } else if(pair[0].equals("columns")){
                matrixtype = DataFactory.COLUMNS;
            } else if(pair[0].equals("prepcolumns")){
                matrixtype = DataFactory.PREPCOLUMNS;
            } else if(pair[0].equals("initialiser")){
                initialValue = Float.parseFloat(pair[1]);
            } else {
                System.out.println("\n  Unknown argument \""+pair[0]+"\"\n");
            }
        }
        if(vectornumber!=0 && this.df!=null && this.saveDialogue()==true){
            if(s!=-1){
                NameSet names = new NameSet(s, s);
                this.vectorset.setNames(names);
            }
            GeneralizedHebbian gha = new GeneralizedHebbian(substrategy, vectornumber, n, settleddistance, matrixtype, initialValue, referenceset);
            gha.go(this.df, this.vectorset, true);
            this.vectorset.save();
        } else if(this.df==null){
            System.out.println("\n  No corpus.\n");
        }
    }
    
    private void svd(Vector arguments){
        NameSet names = new NameSet();
        int BATCH = 0;
        int INCREMENTAL = 1;
        int method = BATCH;
        int substrategy = BatchTrainer.PARALLEL;
        int matrixtype = DataFactory.NGRAM;
        boolean preprocessed = false;
        int vectornumber = 1;
        int n = 2;
        float settleddistance = 0;
        for(int i=0;i<arguments.size();i++){
            String[] pair = (String[])arguments.elementAt(i);
            if(pair[0].equals("batch")){
                method=BATCH;
                if(settleddistance==0){
                    settleddistance=Float.parseFloat("0.0000001");
                }
            } else if(pair[0].equals("incremental")){
                method=INCREMENTAL;
                substrategy=IncrementalTrainer.UPTO;
                if(settleddistance==0){
                    settleddistance=5000;
                }
            } else if(pair[0].equals("parallel")){
                if(method==BATCH){
                    substrategy=BatchTrainer.PARALLEL;
                } else {
                    System.out.println("  Warning! Parallel substrategy not applicable in incremental case.");
                }
            } else if(pair[0].equals("serial")){
                if(method==BATCH){
                    substrategy=BatchTrainer.SERIAL;
                } else {
                    System.out.println("  Warning! Serial substrategy not applicable in incremental case.");
                }
            } else if(pair[0].equals("upto")){
                if(method==INCREMENTAL){
                    substrategy=IncrementalTrainer.UPTO;
                } else {
                    System.out.println("  Warning! \"Up to\" substrategy not applicable in batch case.");
                }
            } else if(pair[0].equals("onev")){
                if(method==INCREMENTAL){
                    substrategy=IncrementalTrainer.ONEV;
                } else {
                    System.out.println("  Warning! \"One vector\" substrategy not applicable in batch case.");
                }
            } else if(pair[0].equals("ngram")){
                matrixtype=DataFactory.NGRAM;
            } else if(pair[0].equals("linewise")){
                matrixtype=DataFactory.LINEWISE;
            } else if(pair[0].equals("passagewise")){
                matrixtype=DataFactory.PASSAGEWISE;
            } else if(pair[0].equals("preprepared")){
                matrixtype=DataFactory.PREPREPARED;
            } else if(pair[0].equals("vectornumber")){
                vectornumber = Integer.parseInt(pair[1]);
            } else if(pair[0].equals("n")){
                n = Integer.parseInt(pair[1]);
            } else if(pair[0].equals("convergence")){
                settleddistance = Float.parseFloat(pair[1]);
            } else if(pair[0].equals("s1Length")){
                names.setRowRILength(Integer.parseInt(pair[1]));
            } else if(pair[0].equals("s2Length")){
                names.setColumnRILength(Integer.parseInt(pair[1]));
            } else if(pair[0].equals("preprocess")){
                preprocessed=true;
            } else {
                System.out.println("\n  Unknown argument \""+pair[0]+"\"\n");
            }
        }
        if(vectornumber!=0 && this.df!=null && this.saveDialogue()==true){
            if(method==BATCH){
                SvdVectorSet v = new SvdVectorSet();
                v.setSaveFileName(this.vectorset.getSaveFileName());
                this.vectorset=v;
                CorpusMatrix m = null;
                if(matrixtype==DataFactory.NGRAM){
                    m = this.df.getNGramMatrix(names, n-1, 1);
                } else if(matrixtype==DataFactory.PREPREPARED){
                    m = this.df.getPrepreparedMatrix(names);
                } else if(matrixtype==DataFactory.LINEWISE){
                    m = this.df.getLinewiseMatrix(names, false);
                } else if(matrixtype==DataFactory.PASSAGEWISE){
                    m = this.df.getPassagewiseMatrix(names, false);
                }
                if(preprocessed){
                    m = m.preprocessMatrix();
                }
                BatchTrainer bts = null;
                bts = new BatchTrainer(substrategy, vectornumber, settleddistance);
                if(bts!=null){
                    bts.go(m, this.vectorset);
                }
            } else {
                IncrementalTrainer its = new IncrementalTrainer(substrategy, vectornumber, n, settleddistance);
                its.go(this.df.getCorpus(), this.vectorset);
            }
            this.vectorset.save();
        } else if(this.df==null){
            System.out.println("\n  No corpus.\n");
        }
    }
    
    private void compareVectorset(Vector arguments){
        //System.out.println("Comparing vectorset ...");
        int matrixtype = -1;
        SvdVectorSet vset = this.vectorset;
        for(int i=0;i<arguments.size();i++){
            String[] pair = (String[])arguments.elementAt(i);
            if(pair[0].equals("linewise")){
                matrixtype=DataFactory.LINEWISE;
            } else if(pair[0].equals("passagewise")){
                matrixtype=DataFactory.PASSAGEWISE;
            } else if(pair[0].equals("preprepared")){
                matrixtype=DataFactory.PREPREPARED;
            } else if(pair[0].equals("file")){
                Corpus corpus = new Corpus(pair[1]);
                //System.out.println("The file is "+pair[1]+" ...");
                this.df1 = new DataFactory(corpus);
            } else if(pair[0].equals("referenceset")){
                System.out.println("Using the referenceset ...");
                vset = this.referenceset;
            }
        }
        if(matrixtype==-1 || this.df1==null){
            System.out.println("Error! Matrix format not specified or no data provided.");
        } else {
            CorpusMatrix m = null;
            if(matrixtype==DataFactory.LINEWISE){
                m = this.df1.getLinewiseMatrix(new NameSet(), false);
                //System.out.println("It's linewise ...");
            } else if(matrixtype==DataFactory.PASSAGEWISE){
                //System.out.println("It's passagewise ...");
                m = this.df1.getPassagewiseMatrix(new NameSet(), false);
            } else if(matrixtype==DataFactory.PREPREPARED){
                //System.out.println("It's preprepared ...");
                m = this.df1.getPrepreparedMatrix(new NameSet());
            }
            if(m!=null && this.vectorset!=null){
                //System.out.println("Completing ...");
                vset.complete(m);
                //System.out.println("Building the comparison matrix ...");
                CorpusMatrix testMatrix = vset.reconstructMatrix();
                Matrix compm = testMatrix.buildColumnComparisonMatrix();
                Matrix refm = m.buildColumnComparisonMatrix();
                compm.print();
                refm.print();
            } else {
                System.out.println("  Error! Either no matrix or no vectorset available.");
            }
        }
    }
    
    private void clearVectorset(){
        System.out.print("\n  Save first? (y/n) ");
        BufferedReader br = null;
        String input = new String();
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception E) {
            System.out.println("Error: " + E);
        }
        try {
            input = br.readLine();
        } catch (Exception e){
            System.out.println(e);
        }
        System.out.println();
        if(input.equals("y") && this.saveDialogue()){
            this.vectorset.save();
            this.vectorset = new SvdVectorSet();
        } else {
            this.vectorset = new SvdVectorSet();
        }
    }
    
    private void saveVectorset(Vector arguments){
        if(arguments.size()==1){
        String[] arg = (String[])arguments.elementAt(0);
            if(arg[0].equals("vectorset") && !arg[1].equals("true")){
                this.vectorset.setSaveFileName(arg[1]);
                if(this.saveDialogue()==true){
                    this.vectorset.save();   
                }
            } else {
                this.printHelp();
            }
        } else {
            this.printHelp();
        }
    }
    
    private boolean saveDialogue(){
        String saveFileName = this.vectorset.getSaveFileName();
        if(saveFileName!=null){
            if((new File(saveFileName)).exists()){
                System.out.print("\n  Overwrite "+saveFileName+"? (abort/<new filename>/continue) ");
            } else {
                System.out.print("\n  Save to "+saveFileName+"? (abort/<new filename>/continue) ");
            }
        } else {
            System.out.print("\n  Specify save file? (abort/<new filename>/continue) ");
        }
        BufferedReader br = null;
        String input = new String();
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception E) {
            System.out.println("Error: " + E);
        }
        try {
            input = br.readLine();
        } catch (Exception e){
            System.out.println(e);
        }
        System.out.println();
        if(input==null || input.length()==0){
            return false;
        } else if(input.equals("abort")){
            return false;
        } else if(input.equals("continue")){
            return true;
        } else {
            this.vectorset.setSaveFileName(input);
            return true;
        }
    }
    
    private void calculateAccuracy(Vector arguments){
        String[] arg = (String[])arguments.elementAt(0);
        int vectornumber = this.vectorset.getS1().size();
        Vector s1 = this.vectorset.getS1();
        Vector s2 = this.vectorset.getS2();
        float[] v1 = this.vectorset.getV1().getArray();
        float[] v2 = this.vectorset.getV2().getArray();
        int n = Integer.parseInt(arg[1]);
        int trainingExamples = this.df.getTrainingExamples(n);
        float squarederror = 0;
        if(this.df!=null && this.vectorset!=null){
            NameSet names = this.vectorset.getNames();
            Vector rownames = names.getRowNames();
            int rownum = rownames.size();
            System.out.println("\n");
            for(int i=0;i<rownum;i++){
                String rowname = (String)rownames.elementAt(i);
                CorpusMatrix row = this.df.getRow(rowname, n, names);
                int rowlen = row.getPublicWidth();
                float[][] array = row.getArray();
                for(int j=0;j<rowlen;j++){
                    float rightanswer = array[j][0]/trainingExamples;
                    float testanswer = 0;
                    for(int k=0;k<vectornumber;k++){
                        MathVector s1k = (MathVector)s1.elementAt(k);
                        MathVector s1kname = names.toRowNameSpace(s1k);
                        MathVector s1knorm = s1kname.euclideanNormalise();
                        float[] array1 = s1knorm.getArray();
                        MathVector s2k = (MathVector)s2.elementAt(k);
                        MathVector s2kname = names.toColumnNameSpace(s2k);
                        MathVector s2knorm = s2kname.euclideanNormalise();
                        float[] array2 = s2knorm.getArray();
                        float val = (v1[k]+v2[k])/2;
                        testanswer = testanswer+(array1[i]*array2[j]*val);
                    }
                    //System.out.println("Right answer: "+rightanswer+", test answer: "+testanswer);
                    squarederror = squarederror + ((rightanswer-testanswer)*(rightanswer-testanswer));
                    System.out.print("\r  Rows processed: "+i+"                    ");
                }
            }
            System.out.println("\n  The squared error is "+squarederror+"\n");
        } else {
            System.out.println("  Error! Both corpus and trained vectorset are required to be able to calculate accuracy.");
        }
    }
    
    private void printWelcome(){
        System.out.println();
        System.out.println("        Welcome to Lab");
        System.out.println("        G. Gorrell 2004");
        System.out.println("   Type \"help\" to get help");
        System.out.println();
        System.out.println();
    }
    
    /** Runs Lab.
     * @param args No arguments required.
     */    
    public static void main(String[] args) {
        BufferedReader br = null;
        Lab myLab = new Lab();
        String input = "";
        myLab.printWelcome();
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception E) {
            System.out.println("Error: " + E);
        }
        while (!input.equals("exit") && !input.equals("quit") && !input.equals("q")) {
            System.out.print("LAB> ");
            try {
                input = br.readLine();
            } catch (Exception E) {
                System.out.println("Error: " + E);
            }
            if (!input.equals("exit") && !input.equals("quit") && !input.equals("q")) {
                myLab.parseInput(input);
            }
        }
    }
}
