
package gen.math.svd;

import java.util.Vector;
import java.io.*;
import gen.math.util.*;
import gen.nlp.svd.*;

/** SvdVectorSet is a data object containing vector pairs and singular values.
 * @author Genevieve Gorrell
 */
public class SvdVectorSet {    
    private Vector s1 = new Vector(); //arrays of DynamicVectors
    private Vector s2 = new Vector(); //arrays of DynamicVectors
    
    private MathVector v1 = new MathVector(); //floats, the eigenvalues    
    private MathVector v2 = new MathVector(); //floats, the eigenvalues
    
    private NameSet names = new NameSet();
    
    private Vector trainingExamples = new Vector(); //Value divisor
    private String saveFileName = "C:\\lab_save_file.svd";
    
    public static int RIGHT = 1;
    public static int LEFT = 2;
    
    public int lastToConverge = -1;
    
    /** Class constructor. */    
    public SvdVectorSet() {
    }
    
    /** Class constructor.
     * @param saveFileName The file location to save the vector set to.
     */    
    public SvdVectorSet(String saveFileName) {
        this.saveFileName = saveFileName;
    }
    
    /** Class constructor.
     * @param saveFileName The file location to save the vector set to.
     * @param reloadFileName The file location to reload the file from.
     */    
    public SvdVectorSet(String saveFileName, String reloadFileName) {
        this.reload(reloadFileName);
        this.saveFileName = saveFileName;
    }
    
    public int getVectorCount(){
        return this.s1.size();
    }
    
    /** Returns the left vector set.
     * @return Vector of MathVectors.
     */    
    public Vector getS1(){
        return this.s1;
    }
    /** Returns the right vector set.
     * @return Vector of MathVectors.
     */    
    public Vector getS2(){
        return this.s2;
    }
    
    public MathVector getEigenvector(int index){
        if(index<this.s1.size()){
            return (MathVector)this.s1.elementAt(index);
        } else {
            return null;
        }
    }
    
    /** Returns the left value set.
     * @return MathVector of floats.
     */    
    public MathVector getV1(){
        return this.v1;
    }
    /** Returns the right value set.
     * @return MathVector of floats.
     */    
    public MathVector getV2(){
        return this.v2;
    }
    
    public float getEigenvalue(int index){
        if(index<this.v1.getPublicLength()){
            return this.v1.getValue(index);
        } else {
            return 0;
        }
    }
    
    public NameSet getNames(){
        return this.names;
    }
    
    /** Returns the save file name.
     * @return String containing the save file location.
     */    
    public String getSaveFileName(){
        return this.saveFileName;
    }
    /** Returns the vector of counts of training examples, relevant to vector sets
     * trained using the incremental approach.
     * @return Vector of ints.
     */    
    public Vector getTrainingExamples(){
        return this.trainingExamples;
    }
    
    /** Sets the right vector set.
     * @param s1 Vector containing MathVectors.
     */    
    public void setS1(Vector s1){
        this.s1 = s1;
    }
    /** Sets the left vector set.
     * @param s2 Vector containing MathVectors.
     */    
    public void setS2(Vector s2){
        this.s2 = s2;
    }
    /** Sets the left value set.
     * @param v1 MathVector containing floats.
     */    
    
    public void setEigenvector(MathVector evec, int index){
        if(this.s1.size()>index){
            this.s1.set(index, evec);
            this.s2.set(index, evec);
        } else {
            this.s1.add(index, evec);
            this.s2.add(index, evec);
        }
    }
    
    public void setV1(MathVector v1){
        this.v1 = v1;
    }
    /** Sets the right value set.
     * @param v2 MathVector containing floats.
     */    
    public void setV2(MathVector v2){
        this.v2 = v2;
    }
    
    public void setEigenvalue(float eval, int index){
        this.v1.addValue(eval, index);
        this.v2.addValue(eval, index);
    }
    
    public void setNames(NameSet names){
        this.names = names;
    }
    
    /** Sets the save file name.
     * @param saveFileName String containing the save file location.
     */    
    public void setSaveFileName(String saveFileName){
        this.saveFileName = saveFileName;
    }
    
    /** Saves the vector set to the current save location. */    
    public void save(){
        if(this.saveFileName!=null){
            this.save(this.saveFileName);
        }
    }
    
    public void save(String filename){
        try {
            FileOutputStream out = new FileOutputStream(filename);
            ObjectOutputStream s = new ObjectOutputStream(out);

            //s.writeObject(new Integer(n));

            s.writeObject(trainingExamples);
            this.names.writeToFile(s);

            s.writeObject(new Integer(s1.size()));
            for(int i=0;i<s1.size();i++){
                MathVector thisS1 = (MathVector)s1.elementAt(i);
                s.writeObject(new Integer(thisS1.getPublicLength()));
                s.writeObject(thisS1.getArray());
            }

            s.writeObject(new Integer(v1.getPublicLength()));
            s.writeObject(v1.getArray());
            //s.writeObject(names1);

            s.writeObject(new Integer(s2.size()));
            for(int i=0;i<s2.size();i++){
                MathVector thisS2 = (MathVector)s2.elementAt(i);
                s.writeObject(new Integer(thisS2.getPublicLength()));
                s.writeObject(thisS2.getArray());
            }

            s.writeObject(new Integer(v2.getPublicLength()));
            s.writeObject(v2.getArray());
            //s.writeObject(names2);

            s.flush();
            s.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
        
    /** Reloads the file from the specified location.
     * @param reloadFileName String containing the file location to reload the file from.
     * @return Returns true if the reload was successful.
     */    
    public boolean reload(String reloadFileName){
        try {
            FileInputStream in = new FileInputStream(reloadFileName);
            ObjectInputStream s = new ObjectInputStream(in);
        
            //Integer nInteger = (Integer)s.readObject();
            //n = nInteger.intValue();
            
            trainingExamples = (Vector)s.readObject();
            NameSet n = NameSet.readFromFile(s);
            this.setNames(n);
            
            Integer s1num = (Integer)s.readObject();
            for(int i=0;i<s1num.intValue();i++){
                Integer length = (Integer)s.readObject();
                float[] array = (float[])s.readObject();
                MathVector da = new MathVector(array, length.intValue());
                if(s1.size()>i){
                    s1.set(i,da);
                } else {
                    s1.add(da);
                }
            }
            
            Integer length = (Integer)s.readObject();
            float[] v1Array = (float[])s.readObject();
            v1 = new MathVector(v1Array, length.intValue());
            //names1 = (Vector)s.readObject();

            Integer s2number = (Integer)s.readObject();
            for(int i=0;i<s2number.intValue();i++){
                length = (Integer)s.readObject();
                float[] array = (float[])s.readObject();
                MathVector da = new MathVector(array, length.intValue());
                if(s2.size()>i){
                    s2.set(i,da);
                } else {
                    s2.add(da);
                }
            }
            
            length = (Integer)s.readObject();
            float[] v2Array = (float[])s.readObject();
            v2 = new MathVector(v2Array, length.intValue());
            //names2 = (Vector)s.readObject();
            s.close();
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean loadRightSideFromFile(String loadFileName){
        return loadVectorsFromFile(this.RIGHT, loadFileName);
    }
    
    public boolean loadLeftSideFromFile(String loadFileName){
        return loadVectorsFromFile(this.LEFT, loadFileName);
    }
    
    private boolean loadVectorsFromFile(int side, String loadFileName){
        Vector s = this.s1;
        MathVector v = null;
        if(side==this.RIGHT){
            s = this.s2;
        }
        try {
            FileReader in = new FileReader(loadFileName);
            BufferedReader br = new BufferedReader(in);
            String line = br.readLine();
            while(line!=null){
                v = new MathVector();
                String[] elements = line.split("[\t ]+");
                for(int i=0;i<elements.length;i++){
                    if(elements[i]!=null && elements[i].length()>0){
                        v.addValue(Float.parseFloat(elements[i]));
                    }
                }
                s.add(v);
                line = br.readLine();
            }
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    public void loadValuesFromFile(String loadFileName){
        try {
            FileReader in = new FileReader(loadFileName);
            BufferedReader br = new BufferedReader(in);
            String line = br.readLine();
            while(line!=null){
                String valuestring = line.trim();
                this.v1.addValue(Float.parseFloat(valuestring));
                this.v2.addValue(Float.parseFloat(valuestring));
                line = br.readLine();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public CorpusMatrix reconstructMatrix(){
        return this.reconstructMatrix(this.v1.getPublicLength());
    }
    
    /** Uses the vector set to construct a matrix. Each pair is outer-producted
     * together and multiplied by the associated value, and the resulting matrices are added.
     * @param useFeatures The number of vector pairs to use in the reconstruction.
     * @return The reconstructed matrix.
     */    
    public CorpusMatrix reconstructMatrix(int useFeatures){   
        int height = this.getNames().getRowNames().size();
        int width = this.getNames().getColumnNames().size();
        CorpusMatrix m = new CorpusMatrix(this.names, new float[width+2][height+2], width, height);

        int max = useFeatures;
        if(max>this.getS1().size()){
            max=this.getS1().size();
        }
        for(int vectorPairIndex=0;vectorPairIndex<max;vectorPairIndex++){
            MathVector thisS1 = (MathVector)this.getS1().elementAt(vectorPairIndex);
            MathVector thisS2 = (MathVector)this.getS2().elementAt(vectorPairIndex);

            MathVector s1Name = this.names.toRowNameSpace(thisS1);
            MathVector s2Name = this.names.toColumnNameSpace(thisS2);
            MathVector s1Norm = s1Name.euclideanNormalise();
            MathVector s2Norm = s2Name.euclideanNormalise();
            
            float s1EValue = this.getV1().getValue(vectorPairIndex);
            float s2EValue = this.getV2().getValue(vectorPairIndex);
            if(!Float.isNaN(s1EValue) && !Float.isNaN(s2EValue)){
                float eValue = (s1EValue+s2EValue)/2;
                Matrix mToAdd = s2Norm.outerProduct(s1Norm);
                mToAdd.multiplyMatrixByScalar(eValue);
                m.add(mToAdd);
            } else {
                System.out.println("Singular value not a number.");
            }
        }
        return m;
    }
    
    /** Prints to the command line the names of the elements in the vector pairs that
     * have the highest values associated with them. This print method allows the user
     * to get an intuitive overview of what part of the data a vector pair is
     * describing.
     * @param numberToPrint The number of top scorers to print.
     */    
    public void printTopScorers(int numberToPrint){
        if(this.getV1().getPublicLength()>this.getS1().size() ||
           this.getV1().getPublicLength()>this.getS2().size()){
            System.out.println("Error! More values than vectors.");
        } else {
            for(int i=0;i<this.getV2().getPublicLength();i++){
                MathVector thisS1 = (MathVector)this.getS1().elementAt(i);
                MathVector thisS2 = (MathVector)this.getS2().elementAt(i);
                MathVector s1WordSpace = this.names.toRowNameSpace(thisS1);
                MathVector s2WordSpace = this.names.toColumnNameSpace(thisS2);
                s1WordSpace = s1WordSpace.euclideanNormalise();
                s2WordSpace = s2WordSpace.euclideanNormalise();
                System.out.println("\nVector number "+i+", first value:"+this.getV1().getValue(i)+", second value:"+
                                                                       this.getV2().getValue(i));

                
                /*thisS1.print();
                s1WordSpace.print();
                thisS2.print();
                s2WordSpace.print();*/
                
                Vector s1Indices = new Vector();
                for(int j=0;j<s1WordSpace.getPublicLength();j++){
                    Integer index = new Integer(j);
                    s1Indices.add(index);
                }
                s1Indices = s1WordSpace.qSort(s1Indices);
                
                Vector s2Indices = new Vector();
                for(int j=0;j<s2WordSpace.getPublicLength();j++){
                    Integer index = new Integer(j);
                    s2Indices.add(index);
                }
                s2Indices = s2WordSpace.qSort(s2Indices);
                
                int j=0;
                while(j<numberToPrint){
                    String string1 = null;
                    String string2 = null;
                    if(j<s1Indices.size()){
                        Integer index = (Integer)s1Indices.elementAt(j);
                        int ind = index.intValue();
                        string1 = this.names.getRowNames().elementAt(ind)+"\t"+s1WordSpace.getValue(ind)+"\t\t";
                    } else {
                        string1 = "None remaining ****\t\t";
                    }
                    if(j<s2Indices.size()){
                        Integer index = (Integer)s2Indices.elementAt(j);
                        int ind = index.intValue();
                        string2 = this.names.getColumnNames().elementAt(ind)+"\t"+s2WordSpace.getValue(ind);
                    } else {
                        string2 = "None remaining ****\t\t";
                    }
                    System.out.println(string1+string2);
                    j++;
                }
                System.out.print("\n");
            }
        }
    }
    
    public void complete(CorpusMatrix m){
        this.s2.setSize(this.s1.size());
        for(int i=0;i<this.s1.size();i++){
            MathVector s1elem = (MathVector)this.s1.elementAt(i);
            if(s1elem.getPublicLength()==m.getPublicLength()){
                MathVector s2elem = m.multiplyMatrixByVector(s1elem.euclideanNormalise());
                float oneovervalue = 1;
                if(this.v1.getPublicLength()<=i){
                    System.out.println("  Error! Too few values.");
                } else {
                    oneovervalue = 1/this.v1.getValue(i);
                }
                s2elem = s2elem.multiplyVectorByScalar(oneovervalue);
                this.s2.setElementAt(s2elem, i);
            } else {
                System.out.println("  Error! Completion matrix has inappropriate dimensionality.");
            }
        }
        this.names = m.getNames();
    }
    
}
