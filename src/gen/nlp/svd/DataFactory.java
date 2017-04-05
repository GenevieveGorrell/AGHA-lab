
package gen.nlp.svd;

import gen.math.util.Matrix;
import gen.math.util.MathVector;
import gen.math.util.SparseVector;
import gen.math.svd.BatchTrainer;
import java.io.*;
import java.util.Vector;
import java.util.Vector;
import java.util.Hashtable;

/** DataFactory makes data from a corpus available in a variety of formats.
 * @author Genevieve Gorrell
 */
public class DataFactory {
    private Corpus corpus;
    public static int PREPREPARED = 0;
    public static int NGRAM = 1;
    public static int LINEWISE = 2;
    public static int PASSAGEWISE = 3;
    public static int COLUMNS = 4;
    public static int PREPCOLUMNS = 5;
    public static int CACHECOLUMNS = 6;
    private CorpusMatrix persistentMatrix = null;
    private int persistentColumnNumber = 0;
    private Vector cachedDataVectors = null;
    private int cachePointer = 0;
    
    /** Class constructor.
     * @param corpus A corpus.
     */    
    public DataFactory(Corpus corpus) {
        this.corpus = corpus;
        this.persistentMatrix=null;
        this.persistentColumnNumber=0;
    }
    
    /** Reads in the data from the corpus as a matrix. The format it expects to find is
     * a text file containing tab-separated column names on the first line, row names
     * on the second line and tab-separated matrix values on the subsequent lines.
     * @return The matrix.
     */    
    public CorpusMatrix getPrepreparedMatrix(NameSet names){
        CorpusMatrix m = new CorpusMatrix(names);
        this.corpus.startOver();
        
        String line = this.corpus.nextLine();
        String[] columns = null;
        String[] rows = null;
        if(line!=null){
            columns = line.split("\t");
            System.out.println("  Getting column names ...");
            for(int i=0;i<columns.length;i++){
                System.out.print("\r  Processing name at "+i+" ...    ");
                names.smartColumnExtend(columns[i]);
            }
        }
        line = this.corpus.nextLine();
        if(line!=null){
            rows = line.split("\t");
            System.out.println("\n  Getting row names ...");
            for(int i=0;i<rows.length;i++){
                System.out.print("\r  Processing name at "+i+" ...    ");
                names.smartRowExtend(rows[i]);
            }
        }
        
        line = this.corpus.nextLine();
        int j=0;
        System.out.println("\n  Getting rows ...");
        while(line!=null){
            System.out.print("\r  Processing row at "+j+" ...    ");
            //int r = names.getRowIndex(rows[j]);
            String[] row = line.split("\t");
            for(int i=0;i<row.length;i++){
                //int c = names.getColumnIndex(columns[i]);
                m.addValue(Float.parseFloat(row[i]), i, j);
            }
            line = this.corpus.nextLine();
            j++;
        }
        
        System.out.println("\n  Done!");
        return m;
    }
    
    /** A count matrix is created from the corpus in which each line takes a row and each
     * column, an unique word. The expected data format is that of text, in which each
     * line is a text passage of some kind.
     * @return The matrix created.
     */
    public CorpusMatrix getLinewiseMatrix(NameSet names, boolean norm){
        this.corpus.startOver();
        CorpusMatrix c = new CorpusMatrix(names);
        SparseVector v = this.getLineVector(names, false, false, norm);
        int col = 0;
        while(v!=null){
            //System.out.print(col+"       \r");
            //MathVector mv = v.toMathVector();
            //int vlen = mv.getPublicLength();
            //for(int i=0;i<vlen;i++){
            //    c.addValue(mv.getValue(i), col, i);
            //}
            SparseVector colvec = names.smartColumnExtend(new Integer(col).toString());
            Matrix m = colvec.outerProduct(v);
            c.flexiadd(m);
            col++;
            v = this.getLineVector(names, false, false, norm);
        }
        return c;
    }
    
    public CorpusMatrix getPassagewiseMatrix(NameSet names, boolean norm){
        this.corpus.startOver();
        CorpusMatrix c = new CorpusMatrix(names);
        SparseVector v = this.getDocumentVector(names, false, false, norm);
        int col = 0;
        while(v!=null){
            //System.out.print(col+"       \r");
            //MathVector mv = v.toMathVector();
            //int vlen = mv.getPublicLength();
            //for(int i=0;i<vlen;i++){
            //    c.addValue(mv.getValue(i), col, i);
            //}
            SparseVector colvec = names.smartColumnExtend(new Integer(col).toString());
            Matrix m = colvec.outerProduct(v);
            c.flexiadd(m);
            col++;
            v = this.getDocumentVector(names, false, false, norm);
        }
        return c;
    }
    
    /** A matrix is created in which the data is treated as a collection of n-grams.
     * Take for example the trigram case. The corpus is treated as a series of three word
     * sets. Unique two-word trigram starting sequences form rows, and unique final
     * words form columns. A matrix of counts is formed and returned.
     * @return The matrix created.
     * @param n The number of words in the first part of the n-gram. In the trigram case this
     * would be 2.
     * @param m The number of words in the second part of the n-gram, typically 1.
     */    
    public CorpusMatrix getNGramMatrix(NameSet names, int n, int m){
        CorpusMatrix ngramMatrix = new CorpusMatrix(names);
        this.corpus.startOver();
        int dataItems = 0;
        boolean isnotRI = (names.getColumnRILength()==-1 && names.getRowRILength()==-1);
        
        Vector ngram = this.corpus.getNGram(n, m, false);
        while(ngram!=null){
            String row = (String)ngram.elementAt(0);
            String column = (String)ngram.elementAt(1);
            SparseVector rowV = names.smartRowExtend(row);
            SparseVector columnV = names.smartColumnExtend(column);
            if(isnotRI){
                int columnPos=ngramMatrix.getNames().getColumnIndex(column);
                int rowPos=ngramMatrix.getNames().getRowIndex(row);
                ngramMatrix.increment(1, columnPos, rowPos);
            } else {
                Matrix bigram = columnV.outerProduct(rowV);
                ngramMatrix.flexiadd(bigram);
            }
            dataItems++;
            ngram = this.corpus.getNGram(n, m, false);
        }
        
        this.wackyNormalise(ngramMatrix, dataItems);
        return ngramMatrix;
    }
    
    public CorpusMatrix getRow(String word, int n, NameSet names){
        CorpusMatrix rowMatrix = new CorpusMatrix(names);
        rowMatrix.setPublicLength(1);
        rowMatrix.setPublicWidth(names.getColumnNames().size());
        this.corpus.startOver();
        Vector ngram = this.corpus.getNGram(n-1, 1, false);
        
        while(ngram!=null){
            String row = (String)ngram.elementAt(0);
            String column = (String)ngram.elementAt(1);
            if(row.equals(word)){
                int columnPos=rowMatrix.getNames().getColumnIndex(column);
                rowMatrix.increment(1, columnPos, 0);
            }

            ngram = this.corpus.getNGram(n-1, 1, false);
        }
        
        return rowMatrix;
    }
    
    public int getTrainingExamples(int n){
        int trainingExamples = 0;
        this.corpus.startOver();
        Vector ngram = this.corpus.getNGram(n-1, 1, false);
        
        while(ngram!=null){
            trainingExamples++;
            ngram = this.corpus.getNGram(n-1, 1, false);
        }
        
        return trainingExamples;
    }
    
    public SparseVector getLineVector(NameSet names, boolean loop, boolean symm, boolean norm){
        SparseVector v = new SparseVector();
        String[] words = null;
        String s = null;
        int exit = 0;

        while(words==null && exit!=2){
            s = this.corpus.nextLine();
            if(s==null){
                if(loop && exit==0){
                    corpus.startOver();
                    exit = 1;
                } else {
                    exit = 2;
                }
            } else {
                words = s.replaceAll("[^a-zA-Z \t]","").split("[ \t]+");
                if(words==null || words.length<1 || (words.length==1 && words[0].equals(""))){
                    words = null;
                } else {
                    exit = 0;
                }
            }
        }
        if(words!=null){
            for(int i=0;i<words.length;i++){
                String word = words[i].toLowerCase().trim();
                //System.out.println(">"+word+"<");
                SparseVector w = null;
                if(symm){
                    w = names.smartSymmetricalExtend(word);
                } else {
                    w = names.smartRowExtend(word);
                }
                if(w.length>v.length) v.length = w.length;
                v=v.addVectors(w);
            }
            if(!symm){
                String colname = Integer.toString(names.getColumnNames().size());
                names.smartColumnExtend(colname);
            }
            if(norm){
                return v.multiplyVectorByScalar(1/v.getManhattanMagnitude());
            } else {
                return v;
            }
        } else {
            return null;
        }
    }
    
    public SparseVector getDocumentVector(NameSet names, boolean loop, boolean symm, boolean norm){
        SparseVector v = new SparseVector();
        String s = this.corpus.nextLine();
        boolean exit = false;
        boolean vectorIsPopulated = false;

        while(exit != true){
            if(s==null && loop==false){
		exit = true;
            } else if(s==null && loop==true){
                corpus.startOver();
                if(vectorIsPopulated){
                    exit = true;
                } else {
                    s = this.corpus.nextLine();
                }
            } else {
                String[] words = s.replaceAll("[^a-zA-Z \t]","").split("[ \t]+");
                if(s.equals("") || (words.length==1 && words[0].equals(""))){
                    if(vectorIsPopulated){
                        exit = true;
                    } else {
                        s = this.corpus.nextLine();
                    }
                } else {
                    for(int i=0;i<words.length;i++){
                        String word = words[i].toLowerCase().trim();
                        SparseVector w = null;
                        if(symm){
                            w = names.smartSymmetricalExtend(word);
                        } else {
                            w = names.smartRowExtend(word);

                        }
                        if(w.length>v.length) v.length = w.length;
                        v=v.addVectors(w);
                    }
                    vectorIsPopulated = true;
                    s = this.corpus.nextLine();
                }
            }
        }

        if(vectorIsPopulated){
            if(!symm){
                String colname = Integer.toString(names.getColumnNames().size());
                names.smartColumnExtend(colname);
            }
            if(norm){
                return v.multiplyVectorByScalar(1/v.getManhattanMagnitude());
            } else {
                return v;
            }
        } else {
            return null;
        }
    }
    
    public SparseVector getColumnVector(NameSet names, boolean loop){
        if(this.persistentMatrix==null){
            this.persistentMatrix = this.getPrepreparedMatrix(names);
            //we want each column to average a length of one ...
            this.persistentMatrix.manhattanNormalise();
            this.persistentMatrix.multiplyMatrixByScalar(this.persistentMatrix.getPublicWidth());
            this.persistentColumnNumber = 0;
            //this.persistentMatrix.print();
        }
        if(this.persistentColumnNumber>=this.persistentMatrix.getPublicWidth()&&loop==true){
            this.persistentColumnNumber=0;
        }
        if(this.persistentColumnNumber<this.persistentMatrix.getPublicWidth()){
            SparseVector v = new SparseVector();
            int rownum = this.persistentMatrix.getPublicLength();
            for(int i=0;i<rownum;i++){
                v.addValue(this.persistentMatrix.getValue(this.persistentColumnNumber,i),i);
            }
            this.persistentColumnNumber++;
            return v;
        } else {
            return null;
        }
    }
    
    /**
     * getPrepreparedColumn will read in a *row* from the corpus
     * that describes a column of an LSA matrix (one row per word).
     */
    public SparseVector getPrepreparedColumn(boolean loop){
        String line = null;
        int exit = 0;
        while(line==null && exit!=2){
            line = this.corpus.nextLine();
            if(line==null){
                if(loop && exit==0){
                    corpus.startOver();
                    exit = 1;
                } else {
                    exit = 2;
                }
            } else {
                exit = 0;
            }
        }
        if(line!=null){
            String[] contents = line.split(",");
            SparseVector returnv = new SparseVector();
            if(contents.length>=2){
                returnv.length=Integer.parseInt(contents[1]);
                int i = 2;
                while (i<contents.length-1){
                    int index = Integer.parseInt(contents[i]);
                    float value = Float.parseFloat(contents[i+1]);
                    returnv.addValue(value, index);
                    i+=2;
                }
                //returnv.print();
                //System.out.println();
                return returnv;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public SparseVector getPrepColumnSparseCache(boolean loop){
        SparseVector retv = null;
        if(this.cachedDataVectors==null){
            this.cachedDataVectors = new Vector();
            String line = this.corpus.nextLine();
            while(line!=null){
                if(line.length()>0){
                    this.cachedDataVectors.add(dataToSparse(line));
                }
                line = this.corpus.nextLine();
            }
        }
        if(this.cachePointer<this.cachedDataVectors.size() && this.cachePointer>=0){
            retv = (SparseVector)this.cachedDataVectors.elementAt(this.cachePointer);
            this.cachePointer++;
        } else {
            this.cachePointer = 0;
            retv = (SparseVector)this.cachedDataVectors.elementAt(0);
        }
        return retv;
    }
    
    public SparseVector getPrepreparedColumnSparse(boolean loop){
        String line = null;
        int exit = 0;
        while(line==null && exit!=2){
            line = this.corpus.nextLine();
            if(line==null){
                if(loop && exit==0){
                    corpus.startOver();
                    exit = 1;
                } else {
                    exit = 2;
                }
            } else {
                exit = 0;
            }
        }
        if(line!=null){
            return dataToSparse(line);
        } else {
            return null;
        }
    }
    
    private SparseVector dataToSparse(String line){
        String[] contents = line.split(",");
        SparseVector returnv = new SparseVector();
        if(contents.length>=2){
            returnv.length=Integer.parseInt(contents[1]);
            int i = 2;
            while (i<contents.length-1){
                int index = Integer.parseInt(contents[i]);
                float value = Float.parseFloat(contents[i+1]);
                returnv.addValue(value, index);
                i+=2;
            }
            //returnv.print();
            //System.out.println();
            return returnv;
        } else {
            return null;
        }
    }
        
    private CorpusMatrix wackyNormalise(CorpusMatrix m, int n){
        for(int x=0;x<m.getPublicWidth();x++){
            for(int y=0;y<m.getPublicLength();y++){
                if(n!=0){
                    m.addValue(m.getValue(x,y)/n,x,y);
                } else {
                    m.addValue(0,x,y);
                }
            }
        }
        return m;
    }
    
    public int countLines(){
        int count = 0;
        this.corpus.startOver();
        String s = this.corpus.nextLine();
        String[] words = null;
        
        while(s!=null){
            words = s.replaceAll("[^a-zA-Z \t]","").split("[ \t]+");
            if (!(words==null || words.length<1 || (words.length==1 && words[0].equals("")))){
                count++;
            }
            s = this.corpus.nextLine();
        }
        return count;
    }
    
    public int countPassages(){
        int count = 0;
        this.corpus.startOver();
        String s = this.corpus.nextLine();
        boolean exit = false;
        boolean vectorIsPopulated = false;

        while(s!=null){
            String[] words = s.replaceAll("[^a-zA-Z \t]","").split("[ \t]+");
            if(s.equals("") || (words.length==1 && words[0].equals(""))){
                if(vectorIsPopulated){
                    count++;
                    vectorIsPopulated = false;
                }
            } else {
                vectorIsPopulated = true;
            }
            s = this.corpus.nextLine();
        }
        return count;
    }
    
    /** Gets a single n-gram from the corpus.
     * @param n The number of words in the first part of the n-gram.
     * @param m The number of words in the second part of the n-gram.
     * @param restart Should the corpus file be reopened when the end is reached?
     * @return The n-gram.
     */    
    /*public Vector getNGram(int n, int m, boolean restart){
        return corpus.getNGram(n, m, restart);
    }*/
    
    public Corpus getCorpus(){
        return this.corpus;
    }
    
    /** Prints word count and unique word count information about the current corpus. */    
    public void printCorpusStatistics(){
        Hashtable words = new Hashtable();
        int totalWords = 0;
        int arrayPosition = 0;
        
        this.corpus.startOver();
        Vector ngram = this.corpus.getNGram(1, 0, false);
        
        while(ngram!=null){
            totalWords++;
            String word = (String)ngram.elementAt(0);
            Integer integer = (Integer)words.get(word);
            if(integer==null){
                integer = new Integer(arrayPosition);
                words.put(word,integer);
                arrayPosition++;
            }
            ngram = this.corpus.getNGram(1, 0, false);
        }
        System.out.println("\nThere are "+words.size()+" unique words in the corpus.");
        System.out.println("There are "+totalWords+" words in total.\n");
        
        NameSet names = new NameSet();
        int totallines = 0;
        float totalmag = 0;
        this.corpus.startOver();
        SparseVector lv = this.getPrepreparedColumn(false);
        while(lv!=null){
            totallines++;
            totalmag+=lv.getManhattanMagnitude();
            lv = this.getPrepreparedColumn(false);
        }
        System.out.println("\nThere are "+totallines+" lines in the corpus.");
        System.out.println("The lines average "+totalmag/totallines+" in manhattan magnitude.\n");
    }
}
