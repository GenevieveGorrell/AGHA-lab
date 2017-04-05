/*
 * NameSet.java
 *
 * Created on den 26 juli 2004, 15:59
 */

package gen.nlp.svd;

import gen.math.util.SparseVector;
import gen.math.util.MathVector;
import gen.math.util.Matrix;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Enumeration;
import java.io.*;

/**
 *
 * @author  ggorrell
 */
public class NameSet {
    //private boolean randomIndexing = false;
    private Hashtable rows = new Hashtable();
    private Hashtable columns = new Hashtable();
    private int rowRILength = -1;
    private int columnRILength = -1;
    private Random r = new Random();
    private Vector rowNames = new Vector();
    private Vector columnNames = new Vector();
    
    /*private Matrix rowMatrix = null;
    private Matrix columnMatrix = null;
    private boolean recreateExportData = true;*/
    
    /** Creates a new instance of NameSet */
    public NameSet(int rowRILength, int columnRILength) {
        //this.randomIndexing=true;
        this.rowRILength=rowRILength;
        this.columnRILength=columnRILength;
    }
    
    /** Creates a new instance of NameSet */
    public NameSet() {
        //this.randomIndexing=false;
    }
    
    public NameSet(Hashtable rows, Hashtable cols, int rril, int cril, Vector rnam, Vector cnam){
        //this.randomIndexing = ri;
        this.rows = rows;
        this.columns = cols;
        this.rowRILength = rril;
        this.columnRILength = cril;
        this.rowNames = rnam;
        this.columnNames = cnam;
    }
    
    public int getRowRILength(){
        return this.rowRILength;
    }
    
    public int getColumnRILength(){
        return this.columnRILength;
    }
    
    public void setRowRILength(int rowLength){
        this.rowRILength = rowLength;
    }
    
    public void setColumnRILength(int columnLength){
        this.columnRILength = columnLength;
    }
    
    public NameSet copy(){
        return new NameSet((Hashtable)this.rows.clone(), 
                           (Hashtable)this.columns.clone(), 
                           this.rowRILength, 
                           this.columnRILength, 
                           (Vector)this.rowNames.clone(), 
                           (Vector)this.columnNames.clone());
    }
    
    public void toRowSquare(){
        this.columnRILength = this.rowRILength;
        this.columns = this.rows;
        this.columnNames = this.rowNames;
    }
    
    public void toColumnSquare(){
        this.rowRILength = this.columnRILength;
        this.rows = this.columns;
        this.rowNames = this.columnNames;
    }
    
    /*public boolean isRandomIndexing(){
        return this.randomIndexing;
    }*/
    
    public SparseVector smartSymmetricalExtend(String name){
        SparseVector v = getRowVector(name);
        if(v==null){
            v = this.extendRows(name);
            this.columns = this.rows;
            this.columnNames = this.rowNames;
            //System.out.println(this.rows.size()+", "+name);
            //System.out.println("Columns, "+this.columns.size()+", rows, "+this.rows.size());
            //v.print();
        }
        return v;
    }
    
    public SparseVector smartRowExtend(String name){
        SparseVector v = getRowVector(name);
        if(v==null){
            return this.extendRows(name);
        } else {
            return v;
        }
    }
    
    public SparseVector smartColumnExtend(String name){
        SparseVector v = getColumnVector(name);
        if(v==null){
            //System.out.println(Runtime.getRuntime().totalMemory());
            return this.extendColumns(name);
        } else {
            return v;
        }
    }
    
    public SparseVector getRowVector(String name){
        return (SparseVector)this.rows.get(name);
    }
    
    public SparseVector getColumnVector(String name){
        return (SparseVector)this.columns.get(name);
    }
    
    private SparseVector generateVector(int len){
        SparseVector newV = new SparseVector();
        newV.length=len;
        newV.addValue(1, len-1);
        return newV;
    }
    
    private SparseVector generateRIVector(int len){
        SparseVector newV = new SparseVector();
        newV.length=len;
        for(int i=0;i<4;i++){
            int val = this.r.nextInt(2);
            if(val==0){
                val = -1;
            }
            int ind = this.r.nextInt(len);
            newV.addValue(val, ind);
        }
        return newV.euclideanNormalise();
    }
    
    private void updateRowNameVectorLengths(int len){
        Enumeration e = this.rows.elements();
        while (e.hasMoreElements()) {
          SparseVector m = (SparseVector)e.nextElement();
          m.length=len;
        }
    }
    
    private void updateColumnNameVectorLengths(int len){
        Enumeration e = this.columns.elements();
        while (e.hasMoreElements()) {
          SparseVector m = (SparseVector)e.nextElement();
          m.length=len;
        }
    }
    
    public SparseVector extendRows(String name){
        SparseVector newRow = null;
        if(this.rowRILength==-1){
            int rowlen = 0;
            if(!this.rows.isEmpty()){
                rowlen = ((SparseVector)this.rows.elements().nextElement()).length;
            }
            newRow = this.generateVector(rowlen+1);
            this.updateRowNameVectorLengths(rowlen+1);
        } else {
            newRow = this.generateRIVector(this.rowRILength);
        }
        this.rows.put(name, newRow);
        this.rowNames.add(name);
        //this.recreateExportData=true;
        return newRow;
    }
    
    public SparseVector extendColumns(String name){
        SparseVector newCol = null;
        if(this.columnRILength==-1){
            int collen = 0;
            if(!this.columns.isEmpty()){
                collen = ((SparseVector)this.columns.elements().nextElement()).length;
            }
            newCol = this.generateVector(collen+1);
            this.updateColumnNameVectorLengths(collen+1);
        } else {
            newCol = this.generateRIVector(this.columnRILength);
        }
        this.columns.put(name, newCol);
        this.columnNames.add(name);
        //this.recreateExportData=true;
        return newCol;
    }
    
    public Vector getRowNames(){
        return this.rowNames;
    }
    
    public Vector getColumnNames(){
        return this.columnNames;
    }
    
    public MathVector toRowNameSpace(MathVector rISpaceV){
        MathVector r = new MathVector();
        for(int i=0;i<this.rowNames.size();i++) {
            SparseVector rVec = (SparseVector)this.rows.get((String)this.rowNames.elementAt(i));
            r.addValue(rVec.dotVectors(rISpaceV));
        }
        return r;
    }
    
    public MathVector toColumnNameSpace(MathVector rISpaceV){
        MathVector c = new MathVector();
        for(int i=0;i<this.columnNames.size();i++) {
            SparseVector cVec = (SparseVector)this.columns.get((String)this.columnNames.elementAt(i));
            c.addValue(cVec.dotVectors(rISpaceV));
        }
        return c;
    }
    
    public int getRowIndex(String name){
        return this.getIndex(name, this.getRowNames());
    }
    
    public int getColumnIndex(String name){
        return this.getIndex(name, this.getColumnNames());
    }
    
    private int getIndex(String name, Vector v){
        for(int i=0;i<v.size();i++){
            String tests = (String)v.elementAt(i);
            if(tests.compareTo(name)==0){
                return i;
            }
        }
        return -1;
    }
    
    public void writeToFile(ObjectOutputStream s){
        try {
            /*if(randomIndexing==true){
                s.writeObject(new String("true"));
            } else {
                s.writeObject(new String("false"));
            }*/
            
            s.writeObject(new Integer(rows.size()));
            //Integer rLen = new Integer(((SparseVector)rows.elements().nextElement()).length);
            Enumeration e = rows.keys();
            while(e.hasMoreElements()){
                String name = (String)e.nextElement();
                s.writeObject(name);
                SparseVector m = (SparseVector)rows.get(name);
                m.writeToFile(s);
            }
            
            s.writeObject(new Integer(columns.size()));
            //Integer cLen = new Integer(((SparseVector)columns.elements().nextElement()).length);
            e = columns.keys();
            while(e.hasMoreElements()){
                String name = (String)e.nextElement();
                s.writeObject(name);
                SparseVector m = (SparseVector)columns.get(name);
                m.writeToFile(s);
            }
            s.writeObject(Integer.toString(rowRILength));
            s.writeObject(Integer.toString(columnRILength));
            s.writeObject(rowNames);
            s.writeObject(columnNames);

            //s.writeObject(rowMatrix);
            //s.writeObject(columnMatrix);
            s.flush();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static NameSet readFromFile(ObjectInputStream s){
        try {
            NameSet n = new NameSet();
            /*if(((String)s.readObject()).equals("true")){
                n.randomIndexing=true;
            } else {
                n.randomIndexing=false;
            }*/
            
            int rownum = ((Integer)s.readObject()).intValue();
            Hashtable r = new Hashtable();
            for(int i=0;i<rownum;i++){
                String name = (String)s.readObject();
                SparseVector v = SparseVector.readFromFile(s);
                r.put(name, v);
            }
            n.rows = r;
            
            int colnum = ((Integer)s.readObject()).intValue();
            Hashtable c = new Hashtable();
            for(int i=0;i<colnum;i++){
                String name = (String)s.readObject();
                SparseVector v = SparseVector.readFromFile(s);
                c.put(name, v);
            }
            n.columns = c;
            
            n.rowRILength = Integer.parseInt((String)s.readObject());
            n.columnRILength = Integer.parseInt((String)s.readObject());
            n.rowNames = (Vector)s.readObject();
            n.columnNames = (Vector)s.readObject();

            //n.rowMatrix = (Matrix)s.readObject();
            //n.columnMatrix = (Matrix)s.readObject();
            return n;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
