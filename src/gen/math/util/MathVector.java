
package gen.math.util;

import java.lang.reflect.Array;
import java.util.Vector;
import gen.math.util.SparseVector;

/** Extends Dynamic1DFloatArray with some methods relevant to vectors (of the maths
 * domain).
 * @author Genevieve Gorrell
 */
public class MathVector extends Dynamic1DFloatArray {
    float magnitude = 0;
    float manhattanMagnitude = 0;
    MathVector norm = null;
    boolean magchanged = true;
    boolean normchanged = true;
   
    /** Class constructor. */    
    public MathVector(){
        super();
    }
    
    /** Class constructor.
     * @param array The MathVector can be initialised with an extant float array.
     * @param length Apparent length.
     */    
    public MathVector(float[] array, int length){
        super(array, length);
    }
    
    public MathVector copy(){
        return new MathVector((float[])super.getArray().clone(),  super.getPublicLength());
    }
    
    /** Adds a value to the vector. The value may overwrite an existing element and may
     * also extend the vector by an unlimited amount.
     * @param value The value to add.
     * @param index The position at which to add the new value.
     */    
    public void addValue(float value, int index){
        super.addValue(value, index);
        this.magchanged=true;
        this.normchanged=true;
    }
    
    /** Adds a value to the end of the vector.
     * @param value The value to add.
     */    
    public void addValue(float value){
        super.addValue(value);
        this.magchanged=true;
        this.normchanged=true;
    }
    
    /** Returns the magnitude (length, in the maths sense) of the vector.
     * @return The magnitude of the vector.
     */     
    public float getMagnitude(){
        this.recalculateMagnitudes();
        return this.magnitude;
    }
    
    public float getManhattanMagnitude(){
        this.recalculateMagnitudes();
        return this.manhattanMagnitude;
    }
    
    private void recalculateMagnitudes(){
        if(this.magchanged==true){
            float mag = 0;
            float manmag = 0;
            for(int i=0;i<this.getPublicLength();i++){
                float val = this.getValue(i);
                mag += val*val;
                manmag += val;
            }
            if(mag>0){
                this.magnitude = (float)Math.sqrt(mag);
            }
            this.manhattanMagnitude = manmag;
        }
        this.magchanged=false;
    }
    
    /** Sorts the supplied vector in the order of the current vector, highest first. The
     * quicksort method is used. The result is returned as a separate vector.
     * @param indices The vector to sort.
     * @return The sorted vector.
     */    
    public Vector qSort(Vector indices){
        if(indices.size()<1){
            return new Vector();
        } else {
            Integer first = (Integer)indices.elementAt(0);
            float middle = this.getValue(first.intValue());
            Vector topHalf = new Vector();
            Vector bottomHalf = new Vector();
            for(int i=1;i<indices.size();i++){
                Integer index = (Integer)indices.elementAt(i);
                float thisValue = this.getValue(index.intValue());
                if(thisValue>middle){
                    topHalf.add(indices.elementAt(i));
                } else {
                    bottomHalf.add(indices.elementAt(i));
                }
            }
            
            Vector sortedTopHalf = this.qSort(topHalf);
            Vector sortedBottomHalf = this.qSort(bottomHalf);
            sortedTopHalf.add(indices.elementAt(0));
            for(int i=0;i<sortedBottomHalf.size();i++){
                sortedTopHalf.add(sortedBottomHalf.elementAt(i));
            }
            return sortedTopHalf;
        }
    }
    
    /** Returns the dot product of the current vector with a supplied vector.
     * @param secondVector The vector to multiply with the current vector.
     * @return The scalar result of multiplying the two vectors together.
     */    
    public float dotVectors(MathVector secondVector){
        float[] secondV = secondVector.getArray();
        float[] thisV = this.getArray();
        int upto = this.getPublicLength();
        float total = 0;
        if(upto!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to dot different length vectors.");
        } else {
            for(int i=0;i<upto;i++){
                float second = secondV[i];
                total += (thisV[i]*second);
            }
        }
        return total;
    }
    
    public float dotVectors(SparseVector secondVector){
        float[] thisV = this.getArray();
        int[] indices = secondVector.indices.getArray();
        float[] values = secondVector.values.getArray();
        int upto = secondVector.indices.getPublicLength();
        float total = 0;
        if(secondVector.length==this.getPublicLength()){
            for(int i=0;i<upto;i++){
                int index = indices[i];
                total += values[i]*thisV[index];
            }
            return total;
        } else {
            System.out.println("  Error! Attempt to dot vectors of differing length.");
            return 0;
        }
    }
    
    /** Multiplies the current vector by the supplied scalar.
     * @param scalar The scalar to multiply the current vector with.
     * @return The vector result of the multiplication.
     */    
    public MathVector multiplyVectorByScalar(float scalar){
        float[] returnV = new float[this.getPrivateLength()];
        float[] thisV = this.getArray();
        int upto = this.getPublicLength();
        for(int i=0;i<upto;i++){
            returnV[i]=(thisV[i]*scalar);
        }
        return new MathVector(returnV, upto);
    }
    
    /** Reverses the direction of a vector by multiplying all its elements by -1. The
     * current vector is updated by this method.
     */    
    public void flip(){
        int upto = this.getPublicLength();
        float[] thisV = this.getArray();
        for(int i=0;i<upto;i++){
            thisV[i]=thisV[i]*(-1);
        }
        this.normchanged=true;
    }
    
    /** Adds the current vector to the supplied vector and returns the result.
     * @param secondVector Vector to add.
     * @return The result of the addition.
     */    
    public MathVector addVectors(MathVector secondVector){
        int upto = this.getPublicLength();
        float[] returnV = new float[this.getPrivateLength()];
        float[] thisV = this.getArray();
        float[] secondV = secondVector.getArray();
        MathVector returnVector = new MathVector();
        if(upto!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to add vectors of differing length.");
        } else {
            for(int i=0;i<upto;i++){
                returnV[i]=thisV[i]+secondV[i];
            }
        }
        return new MathVector(returnV, upto);
    }
    
    /** Adds the supplied vector to the current vector.
     * @param secondVector Vector to add.
     */    
    public void addVectorsFlexible(SparseVector secondVector){
        this.extendArray(secondVector.length);
        this.setPublicLength(secondVector.length);
        int len = secondVector.indices.getPublicLength();
        float magsq = this.magnitude*this.magnitude;
        for(int i=0;i<len;i++){
            int sparseIndex = secondVector.indices.getValue(i);
            float sparseValue = secondVector.values.getValue(i);
            float oldValue = 0;
            if(sparseIndex<this.getPublicLength()){
                oldValue = this.getValue(sparseIndex);
            }
            magsq -= oldValue*oldValue;
            this.manhattanMagnitude -= oldValue;
            float newValue = oldValue+sparseValue;
            magsq += newValue*newValue;
            this.manhattanMagnitude += newValue;
            this.addValue(newValue, sparseIndex);
        }
        this.magnitude = (float)Math.sqrt(magsq);
        this.magchanged = false;
    }
    
    /** Subtracts the supplied vector from the current vector.
     * @param secondVector The vector to subtract.
     * @return The result of the subtraction.
     */    
    public MathVector subtractVectors(MathVector secondVector){
        int upto = this.getPublicLength();
        float[] returnV = new float[this.getPrivateLength()];
        float[] thisV = this.getArray();
        float[] secondV = secondVector.getArray();
        if(upto!=secondVector.getPublicLength()){
            System.out.println("Error! Attempt to subtract vectors of differing length.");
        } else {
            for(int i=0;i<upto;i++){
                returnV[i]=(thisV[i]-secondV[i]);
            }
        }
        return new MathVector(returnV, upto);
    }
    
    /** Euclidean-normalises the vector (square root of the sum of squares.)
     * @return The normalised vector.
     */    
    public MathVector euclideanNormalise(){
        if(this.normchanged==true){
            float[] newV = new float[this.getPrivateLength()];
            float[] thisV = this.getArray();
            float mag = this.getMagnitude();
            float invMag = 1;
            int upto = this.getPublicLength();
            if(mag!=0){
                invMag = 1/mag;
            }
            for(int i=0;i<upto;i++){
                newV[i]=thisV[i]*invMag;
            }
            this.norm = new MathVector(newV, upto);
            this.normchanged=false;
        }
        return this.norm;
    }
    
    /** Calculates the distance between the endpoint of the current vector and the
     * supplied vector.
     * @return The distance between the endpoints of the two vectors.
     * @param secondVector The vector to compare endpoints with.
     */    
    public float euclideanDistance(MathVector secondVector){
        int upto = this.getPublicLength();
        float[] thisV = this.getArray();
        float[] secondV = secondVector.getArray();
        if(secondVector.getPublicLength()!=upto){
            System.out.println("Error! Vectors are of differing dimensionality.");
            return -1;
        } else {
            //Euclidean distance between two points is the distances between the points in each dimension
            //squared, added up and square rooted.
            float total = 0;
            for(int i=0;i<upto;i++){
                float difference = thisV[i]-secondV[i];
                total += difference*difference;
            }
            if(total!=0){
                Float returnFloat = new Float(Math.sqrt(total));
                return returnFloat.floatValue();
            } else {
                return 0;
            }
        }
    }
    
    /** Calculates the outer product of the current vector with the supplied vector.
     * @param secondVector The vector with which to outer-product the current vector.
     * @return The matrix result of the outer-product.
     */    
    public Matrix outerProduct(MathVector secondVector){
        float[] thisV = this.getArray();
        float[] secondV = secondVector.getArray();
        int puw = this.getPublicLength();
        int pul = secondVector.getPublicLength();
        int prw = this.getPrivateLength();
        int prl = secondVector.getPrivateLength();
        float[][] matrix = new float[prw][prl];
        for(int i=0;i<puw;i++){
            for(int j=0;j<pul;j++){
                matrix[i][j] = thisV[i]*secondV[j];
            }
        }
        return new Matrix(matrix, puw, pul);
    }
}
