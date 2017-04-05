/*
 * SparseVector.java
 *
 * Created on den 21 augusti 2004, 12:53
 */

package gen.math.util;

import java.io.*;

/**
 *
 * @author  ggorrell
 */
public class SparseVector {
    
    public Dynamic1DIntArray indices = new Dynamic1DIntArray();
    public Dynamic1DFloatArray values = new Dynamic1DFloatArray();
    public int length = 0;
    private boolean magchanged = true;
    private boolean normchanged = true;
    private float magnitude = 0;
    private float manhattanMagnitude = 0;
    private SparseVector norm = null;
    
    /** Creates a new instance of SparseVector */
    public SparseVector() {
    }
    
    public SparseVector(Dynamic1DIntArray indices, Dynamic1DFloatArray values, int length) {
        this.indices = indices;
        this.values = values;
        this.length = length;
    }
    
    private void changed(){
        this.magchanged=true;
        this.normchanged=true;
    }
    
    public MathVector toMathVector(){
        float[] newArray = new float[this.length+2];
        int[] inds = this.indices.getArray();
        float[] vals = this.values.getArray();
        int upto = this.indices.getPublicLength();
        for(int i=0;i<upto;i++){
            int index = inds[i];
            newArray[index]=vals[i];
        }
        return new MathVector(newArray, this.length);
    }
    
    /** Adds a value to the vector. The value may overwrite an existing element and may
     * also extend the vector by an unlimited amount.
     * @param value The value to add.
     * @param index The position at which to add the new value.
     */    
    public void addValue(float value, int index){
        int upto = this.indices.getPublicLength();
        int[] inds = this.indices.getArray();
        float[] vals = this.values.getArray();
        boolean found = false;
        for(int i=0;i<upto;i++){
            if(inds[i]==index){
                vals[i]=value;
                found = true;
            }
        }
        if(!found){
            this.values.addValue(value);
            this.indices.addValue(index);
        }
        if(index>=this.length){
            this.length=index+1;
        }
        this.changed();
    }
    
    public float getValue(int index){
        int upto = this.indices.getPublicLength();
        int[] inds = this.indices.getArray();
        float[] vals = this.values.getArray();
        float returnvalue = 0;
        for(int i=0;i<upto;i++){
            if(inds[i]==index){
                returnvalue = vals[i];
            }
        }
        return returnvalue;
    }
    
    public void increment(float value, int index){
        int upto = this.indices.getPublicLength();
        int[] inds = this.indices.getArray();
        float[] vals = this.values.getArray();
        boolean found = false;
        for(int i=0;i<upto;i++){
            if(inds[i]==index){
                vals[i]=vals[i]+value;
                found = true;
            }
        }
        if(!found){
            this.values.addValue(value);
            this.indices.addValue(index);
        }
        if(index>=this.length){
            this.length=index+1;
        }
        this.changed();
    }
    
    /** Returns the magnitude (length, in the maths sense) of the vector.
     * @return The magnitude of the vector.
     */    
    public void recalculateMagnitudes(){
        if(this.magchanged==true){
            float mag = 0;
            float manmag = 0;
            float[] array = this.values.getArray();
            int len=this.values.getPublicLength();
            for(int i=0;i<len;i++){
                mag += array[i]*array[i];
                manmag += array[i];
            }
            Float returnFloat = new Float(0);
            if(manmag>0){
                this.magnitude = (float)Math.sqrt(mag);
                this.manhattanMagnitude = manmag;
            }
        }
        this.magchanged=false;
    }
    
    public float getManhattanMagnitude(){
        this.recalculateMagnitudes();
        return this.manhattanMagnitude;
    }
    
    public float getMagnitude(){
        this.recalculateMagnitudes();
        return this.magnitude;
    }
    
    /** Returns the dot product of the current vector with a supplied vector.
     * @param secondVector The vector to multiply with the current vector.
     * @return The scalar result of multiplying the two vectors together.
     */    
    public float dotVectors(MathVector secondVector){
        if(this.length!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to dot different length vectors.");
            return -1;
        } else {
            int upto = this.values.getPublicLength();
            float[] vals = this.values.getArray();
            int[] inds = this.indices.getArray();
            float[] secondV = secondVector.getArray();
            float total = 0;
            for(int i=0;i<upto;i++){
                int index = inds[i];
                total += (vals[i]*secondV[index]);
            }
            return total;
        }
    }
    
    public float dotVectors(SparseVector secondVector){
        if(this.length!=secondVector.length){
            System.out.println("Error! Attempt to dot different length vectors.");
            return -1;
        } else {
            int upto = this.values.getPublicLength();
            int supto = secondVector.values.getPublicLength();
            float[] vals = this.values.getArray();
            float[] svals = secondVector.values.getArray();
            int[] inds = this.indices.getArray();
            int[] sinds = secondVector.indices.getArray();
            float total = 0;
            for(int i=0;i<upto;i++){
                for(int j=0;j<supto;j++){
                    if(inds[i]==sinds[j]){
                        total += vals[i]*svals[j];
                    }
                }
            }
            return total;
        }
    }
    
    /** Multiplies the current vector by the supplied scalar.
     * @param scalar The scalar to multiply the current vector with.
     * @return The vector result of the multiplication.
     */    
    public SparseVector multiplyVectorByScalar(float scalar){
        Dynamic1DFloatArray rvals = new Dynamic1DFloatArray();
        Dynamic1DIntArray rinds = new Dynamic1DIntArray();
        float[] vals = this.values.getArray();
        int[] inds = this.indices.getArray();
        int upto = this.values.getPublicLength();
        for(int i=0;i<upto;i++){
            rvals.addValue(vals[i]*scalar);
            rinds.addValue(inds[i]);
        }
        return new SparseVector(rinds, rvals, this.length);
    }
    
    /** Reverses the direction of a vector by multiplying all its elements by -1. The
     * current vector is updated by this method.
     */    
    public void flip(){
        int upto = this.values.getPublicLength();
        float[] thisV = this.values.getArray();
        for(int i=0;i<upto;i++){
            thisV[i]=thisV[i]*(-1);
        }
        this.changed();
    }
    
    /** Adds the current vector to the supplied vector and returns the result.
     * @param secondVector Vector to add.
     * @return The result of the addition.
     */    
    public SparseVector addVectors(SparseVector secondVector){
        if(this.length!=secondVector.length){
            System.out.println("Error! Attempt to add different length vectors.");
            return null;
        } else {
            SparseVector returnV = new SparseVector();
            returnV.length = this.length;
            int upto = this.indices.getPublicLength();
            int supto = secondVector.indices.getPublicLength();
            int[] inds = this.indices.getArray();
            int[] sinds = secondVector.indices.getArray();
            float[] vals = this.values.getArray();
            float[] svals = secondVector.values.getArray();
            for(int i=0;i<upto;i++){
                returnV.increment(vals[i], inds[i]);
            }
            for(int i=0;i<supto;i++){
                returnV.increment(svals[i], sinds[i]);
            }
            return returnV;
        }
    }
       
    public MathVector addVectors(MathVector secondVector){
        if(this.length!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to add different length vectors.");
            return null;
        } else {
            float[] newArray = new float[this.length+2];
            int upto = secondVector.getPublicLength();
            float[] array = secondVector.getArray();
            for(int i=0;i<upto;i++){
                newArray[i]=array[i];
            }
            upto = this.indices.getPublicLength();
            int[] inds = this.indices.getArray();
            float[] vals = this.values.getArray();
            for(int i=0;i<upto;i++){
                int index = inds[i];
                newArray[index]+=vals[i];
            }
            return new MathVector(newArray, this.length);
        }
    }
    
    /** Subtracts the supplied vector from the current vector.
     * @param secondVector The vector to subtract.
     * @return The result of the subtraction.
     */    
    public SparseVector subtractVectors(SparseVector secondVector){
        if(this.length!=secondVector.length){
            System.out.println("Error! Attempt to subtract different length vectors.");
            return null;
        } else {
            SparseVector returnV = new SparseVector();
            int upto = this.indices.getPublicLength();
            int supto = secondVector.indices.getPublicLength();
            int[] inds = this.indices.getArray();
            int[] sinds = secondVector.indices.getArray();
            float[] vals = this.values.getArray();
            float[] svals = secondVector.values.getArray();
            for(int i=0;i<upto;i++){
                returnV.increment(vals[i], inds[i]);
            }
            for(int i=0;i<supto;i++){
                returnV.increment(svals[i]*(-1), sinds[i]);
            }
            return returnV;
        }
    }
       
    public MathVector subtractVectors(MathVector secondVector){
        if(this.length!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to subtract different length vectors.");
            return null;
        } else {
            float[] newArray = new float[this.length+2];
            int upto = secondVector.getPublicLength();
            float[] array = secondVector.getArray();
            for(int i=0;i<upto;i++){
                newArray[i]=array[i]*(-1);
            }
            upto = this.indices.getPublicLength();
            int[] inds = this.indices.getArray();
            float[] vals = this.values.getArray();
            for(int i=0;i<upto;i++){
                int index = inds[i];
                newArray[index]+=vals[i];
            }
            return new MathVector(newArray, this.length);
        }
    }
    
    /** Euclidean-normalises the vector (square root of the sum of squares.)
     * @return The normalised vector.
     */    
    public SparseVector euclideanNormalise(){
        if(this.normchanged==true){
            SparseVector normV = new SparseVector();
            normV.length=this.length;
            float mag = this.getMagnitude();
            float invMag = 1;
            if(mag!=0){
                invMag = 1/mag;
            }
            int upto = this.values.getPublicLength();
            int[] inds = this.indices.getArray();
            float[] vals = this.values.getArray();
            for(int i=0;i<upto;i++){
                int ind = inds[i];
                float newVal = vals[i]*invMag;
                normV.addValue(newVal, ind);
            }
            this.norm = normV;
            this.normchanged=false;
        }
        return this.norm;
    }
    
    /** Calculates the outer product of the current vector with the supplied vector.
     * @param secondVector The vector with which to outer-product the current vector.
     * @return The matrix result of the outer-product.
     */    
    public Matrix outerProduct(SparseVector secondVector){
        int upto = this.values.getPublicLength();
        int supto = secondVector.values.getPublicLength();
        int[] inds = this.indices.getArray();
        int[] sinds = secondVector.indices.getArray();
        float[] vals = this.values.getArray();
        float[] svals = secondVector.values.getArray();
        int wid = this.length;
        int len = secondVector.length;
        float[][] matrix = new float[wid+1][len+1];
        for(int i=0;i<upto;i++){
            for(int j=0;j<supto;j++){
                int ind = inds[i];
                int sind = sinds[j];
                matrix[ind][sind] = vals[i]*svals[j];
            }
        }
        return new Matrix(matrix, wid, len);
    }
    
    public void writeToFile(ObjectOutputStream s){
        this.indices.writeToFile(s);
        this.values.writeToFile(s);
        try {
            s.writeObject(new Integer(this.length));
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static SparseVector readFromFile(ObjectInputStream s){
        try {
            Dynamic1DIntArray i = Dynamic1DIntArray.readFromFile(s);
            Dynamic1DFloatArray v = Dynamic1DFloatArray.readFromFile(s);
            int len = ((Integer)s.readObject()).intValue();
            return new SparseVector(i, v, len);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public void print(){
        System.out.print("Sparse vector, length "+this.length+"--");
        for(int i=0;i<this.indices.getPublicLength()-1;i++){
            System.out.print(this.indices.getValue(i)+":"+this.values.getValue(i)+", ");
        }
        if(this.indices.getPublicLength()>0){
            System.out.print(this.indices.getValue(this.indices.getPublicLength()-1)
                        +":"+this.values.getValue(this.indices.getPublicLength()-1));
        }
        System.out.print("--end\n");
    }

    public SparseVector copy(){
        return new SparseVector(this.indices.copy(), this.values.copyFloatArray(), this.length);
    }
}
