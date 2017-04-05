
package gen.nlp.svd;

import gen.math.util.Matrix;
import gen.math.util.MathVector;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.io.*;

/** CorpusMatrix extends Matrix with elements appropriate to a matrix that is being
 * used to store natural language data. Row and column labels are added along with
 * appropriate printing methods.
 * @author Genevieve Gorrell
 */
public class CorpusMatrix extends Matrix {
    private NameSet names = new NameSet();
    
    /** Class constructor. */    
    public CorpusMatrix() {
    }
    
    /** Class constructor. */    
    public CorpusMatrix(float[][] array, int puw, int pul) {
        super(array, puw, pul);
    }
    
    /** Class constructor. */    
    public CorpusMatrix(NameSet names) {
        this.names=names;
    }
    
    /** Class constructor. */    
    public CorpusMatrix(NameSet names, float[][] array, int puw, int pul) {
        super(array, puw, pul);
        this.names=names;
    }
    
    public NameSet getNames(){
        return this.names;
    }
    
    public void setNames(NameSet names){
        this.names = names;
    }
    
    /** Prints the matrix to the command line in a visual format. */    
    public void comparisonFormat(){
        int padding = 7;
        System.out.print("\n"+generatePadding(padding*2));
        int colnum = this.names.getColumnNames().size();
        if(this.names.getColumnRILength()!=-1){
            colnum=this.names.getColumnRILength();
        }
        for(int i=0;i<colnum;i++){
            String word = "";
            if(this.names.getColumnRILength()==-1){
                word = (String)this.names.getColumnNames().elementAt(i);
            } else {
                word = new Integer(i).toString();
            }
            System.out.print(word+generatePadding(padding-word.length())+"\t");
        }
            
        System.out.print("\n");
        for(int i=0;i<this.getPublicLength();i++){
            String word;
            if(this.names.getRowNames()!=null && this.names.getRowNames().size()>i){
                if(this.names.getRowRILength()==-1){
                    word = (String)this.names.getRowNames().elementAt(i);
                } else {
                    word = new Integer(i).toString();
                }
            } else {
                word = "";
            }
            System.out.print(word+generatePadding(padding*2-word.length())+"\t");
            for(int j=0;j<this.getPublicWidth();j++){
                //System.out.print(this.getValue(j,i)+"\t");
                float val = this.getValue(j,i)*1000;
                int intval = (int)val;
                val = ((float)intval)/1000;
                System.out.print(val+"\t");
            }
            System.out.print("\n");
        }
        System.out.println();
    }
    
    /** Prints the matrix to file in a visual format.
     * @param outputFile The location of the file to output the matrix to.
     */    
    public void comparisonFormat(String outputFile){
        try {
            FileWriter fw = new FileWriter(outputFile);
            int padding = 7;
            fw.write("\n"+generatePadding(padding*2));
            int colnum = this.names.getColumnNames().size();
            if(this.names.getColumnRILength()!=-1){
                colnum=this.names.getColumnRILength();
            }
            for(int i=0;i<colnum;i++){
                String word = "";
                if(this.names.getColumnRILength()==-1){
                    word = (String)this.names.getColumnNames().elementAt(i);
                } else {
                    word = new Integer(i).toString();
                }
                fw.write(word+generatePadding(padding-word.length())+"\t");
            }
            fw.write("\n");
            for(int i=0;i<this.getPublicLength();i++){
                String word;
                if(this.names.getRowNames()!=null && this.names.getRowNames().size()>i){
                    if(this.names.getRowRILength()==-1){
                        word = (String)this.names.getRowNames().elementAt(i);
                    } else {
                        word = new Integer(i).toString();
                    }
                } else {
                    word = "";
                }
                fw.write(word+generatePadding(padding*2-word.length())+"\t");
                for(int j=0;j<this.getPublicWidth();j++){
                    float val = this.getValue(j,i)*1000;
                    int intval = (int)val;
                    val = ((float)intval)/1000;
                    fw.write(val+"\t");
                }
                fw.write("\n");
            }
            fw.write("\n");
            fw.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }
    
    String generatePadding(int n){
        String returnString = "";
        for(int i=0;i<n;i++){
            returnString = returnString+" ";
        }
        return returnString;
    }
    
    /** Prints the matrix to the command line in an easily machine-readable format. */    
    public void bqFormat(){
        Vector c = this.names.getColumnNames();
        int colnum = c.size();
        if(this.names.getColumnRILength()!=-1){
            colnum=this.names.getColumnRILength();
        }
        for(int i=0;i<colnum-1;i++){
            if(this.names.getColumnRILength()==-1){
                System.out.print((String)c.elementAt(i)+"\t");
            } else {
                System.out.print(i+"\t");
            }
        }
        if(colnum>1){
            if(this.names.getColumnRILength()==-1){
                System.out.print((String)c.elementAt(colnum-1)+"\n");
            } else {
                System.out.print((colnum-1)+"\n");
            }
        }
        Vector r = this.names.getRowNames();
        int rownum = r.size();
        if(this.names.getRowRILength()!=-1){
            rownum=this.names.getRowRILength();
        }
        for(int i=0;i<rownum-1;i++){
            if(this.names.getRowRILength()==-1){
                System.out.print((String)r.elementAt(i)+"\t");
            } else {
                System.out.print(i+"\t");
            }
        }
        if(rownum>1){
            if(this.names.getRowRILength()==-1){
                System.out.print((String)r.elementAt(rownum-1)+"\n");
            } else {
                System.out.print((rownum-1)+"\n");
            }
        }
        this.print();
    }
    
    /** Prints the matrix to file in an easily machine-readable format.
     * @param outputFile The location of the file to print the matrix to.
     */    
    public void bqFormat(String outputFile){
        try {
            FileWriter fw = new FileWriter(outputFile);
            Vector c = this.names.getColumnNames();
            int colnum = c.size();
            if(this.names.getColumnRILength()!=-1){
                colnum=this.names.getColumnRILength();
            }
            for(int i=0;i<colnum-1;i++){
                if(this.names.getColumnRILength()==-1){
                    fw.write((String)c.elementAt(i)+"\t");
                } else {
                    fw.write(i+"\t");
                }
            }
            if(colnum>1){
                if(this.names.getColumnRILength()==-1){
                    fw.write((String)c.elementAt(colnum-1)+"\n");
                } else {
                    fw.write((colnum-1)+"\n");
                }
            }
            Vector r = this.names.getRowNames();
            int rownum = r.size();
            if(this.names.getRowRILength()!=-1){
                rownum=this.names.getRowRILength();
            }
            for(int i=0;i<rownum-1;i++){
                if(this.names.getRowRILength()==-1){
                    fw.write((String)r.elementAt(i)+"\t");
                } else {
                    fw.write(i+"\t");
                }
            }
            if(rownum>1){
                if(this.names.getRowRILength()==-1){
                    fw.write((String)r.elementAt(rownum-1)+"\n");
                } else {
                    fw.write((rownum-1)+"\n");
                }
            }
            this.printToFile(fw);
        } catch(Exception e){
            System.out.println(e);
        }
    }
    
    public CorpusMatrix preprocessMatrix(){
        CorpusMatrix processedm = new CorpusMatrix(this.getNames());
        
        for(int i=0;i<this.getPublicLength();i++){
            //System.out.print("  Preprocessing line "+(i+1)+"\r");
            float gf = globalFrequency(this, i);
            float gw = globalWeighting(this, i, gf);
            //System.out.println(this.getNames().getRowNames().elementAt(i)+", "+gf+", "+gw);
            for(int j=0;j<this.getPublicWidth();j++){
                float value = gw*(float)Math.log((double)this.getValue(j, i)+1);
                processedm.addValue(value, j,  i);
            }
        }
        //System.out.print("\n");
        return processedm;
    }
    
    private float globalWeighting(Matrix m, int word, float gf){
        double n = m.getPublicWidth();
        double logn = Math.log(n);
        float total = 0;
        for(int j=0;j<m.getPublicWidth();j++){
            double p = m.getValue(j, word);
            if(p!=0){
                p = p/gf;
                double value = (p*Math.log(p))/logn;
                total = total + (float)value;
            }
        }
        return 1+total;
    }
    
    private float globalFrequency(Matrix m, int word){
        float total = 0;
        for(int j=0;j<m.getPublicWidth();j++){
            total = total + m.getValue(j, word);
        }
        return total;
    }
    
    public CorpusMatrix matrixTranspose(){
        NameSet rownames = this.getNames().copy();
        rownames.toRowSquare();
        Matrix sq1 = this.multiplyMatrixTranspose();
        CorpusMatrix cm = new CorpusMatrix(rownames, 
                                           sq1.getArray(),
                                           sq1.getPublicWidth(), 
                                           sq1.getPublicLength());
        return cm;
    }
    
    public CorpusMatrix transposeMatrix(){
        NameSet columnnames = this.getNames().copy();
        columnnames.toColumnSquare();
        Matrix sq2 = this.multiplyTransposeMatrix();
        CorpusMatrix cm = new CorpusMatrix(columnnames, 
                                           sq2.getArray(),
                                           sq2.getPublicWidth(), 
                                           sq2.getPublicLength());
        return cm;
    }
    
    public CorpusMatrix buildColumnComparisonMatrix(){
        CorpusMatrix m = new CorpusMatrix();
        Vector columnVectors = new Vector();
        
        for(int i=0;i<this.getPublicWidth();i++){
            MathVector newVec = new MathVector();
            for(int j=0;j<this.getPublicLength();j++){
                newVec.addValue(this.getValue(i,j));
            }
            columnVectors.add(newVec);
        }
        
        for(int i=0;i<columnVectors.size();i++){
            for(int j=i;j<columnVectors.size();j++){
                MathVector v1 = (MathVector)columnVectors.elementAt(i);
                MathVector v2 = (MathVector)columnVectors.elementAt(j);
                v1 = v1.euclideanNormalise();
                v2 = v2.euclideanNormalise();
                float dotproduct = v1.dotVectors(v2);
                m.addValue(dotproduct, i, j);
                m.addValue(dotproduct, j, i);
                //System.out.println(dotproduct+", "+i+", "+j);
            }
        }
	m.names=this.names.copy();
	m.names.toColumnSquare();
        return m;
    }

    public CorpusMatrix buildRowComparisonMatrix(){
        CorpusMatrix m = new CorpusMatrix();
        Vector rowVectors = new Vector();
        
        for(int i=0;i<this.getPublicLength();i++){
            MathVector newVec = new MathVector();
            for(int j=0;j<this.getPublicWidth();j++){
                newVec.addValue(this.getValue(i,j));
            }
            rowVectors.add(newVec);
        }
        
        for(int i=0;i<rowVectors.size();i++){
            for(int j=i;j<rowVectors.size();j++){
                MathVector v1 = (MathVector)rowVectors.elementAt(i);
                MathVector v2 = (MathVector)rowVectors.elementAt(j);
                v1 = v1.euclideanNormalise();
                v2 = v2.euclideanNormalise();
                float dotproduct = v1.dotVectors(v2);
                m.addValue(dotproduct, i, j);
                m.addValue(dotproduct, j, i);
                //System.out.println(dotproduct+", "+i+", "+j);
            }
        }
	m.names=this.names.copy();
	m.names.toRowSquare();
        return m;
    }
}
