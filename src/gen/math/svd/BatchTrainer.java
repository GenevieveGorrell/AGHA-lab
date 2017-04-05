
package gen.math.svd;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import gen.math.util.*;
import gen.nlp.svd.*;

/** BatchTrainingStrategy encapsulates a traditional approach to singular value
 * decomposition, in which data are taken in the form of a count matrix, and
 * matrices are formed comprising the data matrix multiplied by its transpose, and
 * the transpose of the data matrix multiplied by the data matrix. Each of these
 * matrices is then eigen decomposed by repeatedly squaring it until the rows
 * converge on the strongest eigen vector. The part of the data explained by this
 * vector/vector pair is removed, and the process repeated to find the second
 * strongest vector pair and so on.
 * @author Genevieve Gorrell
 */
public class BatchTrainer{
    private int substrategy = PARALLEL;
    /** Refers to the substrategy in which the two square matrices are decomposed
     * entirely separately, and the resulting vector sets then used to determine the
     * singular values.
     */    
    public static int SERIAL = 0;
    /** Refers to the substrategy in which subsequent vector pairs are derived from
     * recalculated square matrices, after the preceding vector pair has been used to
     * remove from the *original data matrix* the part explained by it.
     */    
    public static int PARALLEL = 1;
    private int vnum = 1;
    private int s1Length = 10;
    private int s2Length = 10;
    private float distCutoff = Float.parseFloat("0.0000001");
    
    /** Class constructor.
     * @param substrategy The substrategy to be used.
     * @param matrixtype The format that the data will take.
     * @param vnum The number of vector pairs to train.
     */    
    public BatchTrainer(int substrategy, int vnum, float distCutoff) {
        this.substrategy = substrategy;
        //this.matrixtype = matrixtype;
        this.vnum = vnum;
        this.distCutoff = distCutoff;
    }
    
    /** Class constructor.
     * @param substrategy The substrategy to be used.
     * @param matrixtype The format that the data will take.
     * @param vnum The number of vector pairs to train.
     * @param n In the n-gram case, n.
     */    
    /*public BatchTrainer(int substrategy, int vnum, float distCutoff, int n) {
        this.substrategy = substrategy;
        //this.matrixtype = matrixtype;
        this.vnum = vnum;
        this.distCutoff = distCutoff;
        //this.n = n;
    }*/
      
    /*public BatchTrainer(int substrategy, int vnum, float distCutoff, int n, int s1len, int s2len) {
        this.substrategy = substrategy;
        //this.matrixtype = matrixtype;
        this.vnum = vnum;
        this.distCutoff = distCutoff;
        //this.n = n;
        //this.s1Length=s1len;
        this.s2Length=s2len;
    }*/
    
    /** Trains the vector set.
     * @param dataFactory The data factory from which to draw the data.
     * @param svdVectorSet The vector set to train.
     */    
    public void go(CorpusMatrix m, SvdVectorSet svdVectorSet) {
        svdVectorSet.setNames(m.getNames());
        System.out.println("  Start time: "+new Date());
        if(this.substrategy==this.PARALLEL){
            goParallel(m, svdVectorSet);
        } else {
            goSerial(m, svdVectorSet);
        }
        System.out.println("  End time: "+new Date()+"\n");
    }
    
    private void goSerial(Matrix data, SvdVectorSet svdVectorSet){
        Matrix m1 = data.multiplyMatrixTranspose();
        Matrix m2 = data.multiplyTransposeMatrix();
        
        float[] eigenValues1 = eigenDecompose(m1, this.vnum, distCutoff, svdVectorSet.getS1());
        float[] eigenValues2 = eigenDecompose(m2, this.vnum, distCutoff, svdVectorSet.getS2());
        
        System.out.println("\n  Calculating singular value decomposition. Values are:\n");
        
        //Now fill in the values, discard previous since sign is ambiguous
        for(int i=0;i<this.vnum;i++){
            MathVector firstVector = (MathVector)svdVectorSet.getS1().elementAt(i);
            MathVector secondVector = (MathVector)svdVectorSet.getS2().elementAt(i);
            float value = this.getSingularValue(data, firstVector, secondVector);
            if(value<0){
                value = value*(-1);
                firstVector.flip();
            }
            svdVectorSet.getV1().addValue(value);
            svdVectorSet.getV2().addValue(value);
            
            System.out.println("    "+value+"\n");
        }
        System.out.println();
    }
    
    private float[] eigenDecompose(Matrix m, int vectorNumber, float distCutoff, Vector s){
        float[] eigenValues = new float[vectorNumber];
        
        for(int i=0;i<vectorNumber;i++){
            MathVector eigenVector = getEigenVector(m.copy(),distCutoff);
            s.addElement(eigenVector);

            float eigenValue = getEigenValue(m, eigenVector);
            eigenValues[i] = eigenValue;
            
            Matrix mrem = eigenVector.outerProduct(eigenVector);
            mrem.multiplyMatrixByScalar(eigenValue);
            m.subtract(mrem);
        }
        
        return eigenValues;
    }
    
    private void goParallel(Matrix data, SvdVectorSet svdVectorSet){
        Matrix currentData = data.copy();
        
        System.out.println("\n  Calculating singular value decomposition. Values are:\n");
        
        for(int i=0;i<this.vnum;i++){
            Matrix m1 = currentData.multiplyMatrixTranspose();
            Matrix m2 = currentData.multiplyTransposeMatrix();

            MathVector firstVector = getEigenVector(m1.copy(),distCutoff);
            MathVector secondVector = getEigenVector(m2.copy(),distCutoff);

            float value = getSingularValue(currentData, firstVector, secondVector);
            if(value<0){
                value = value*(-1);
                firstVector.flip();
            }
            svdVectorSet.getS1().addElement(firstVector);
            svdVectorSet.getS2().addElement(secondVector);
            svdVectorSet.getV1().addValue(value);
            svdVectorSet.getV2().addValue(value);

            System.out.println("    "+value+"\n");

            Matrix toRemove = secondVector.outerProduct(firstVector);
            toRemove.multiplyMatrixByScalar(value);
            currentData.subtract(toRemove);
            if(currentData.total()<0.0000000001){
                i=this.vnum;
            }
        }
        System.out.println();
    }
    
    private float getSingularValue(Matrix originalm, MathVector singularv1, MathVector singularv2){
        MathVector scaledv2 = originalm.multiplyMatrixByVector(singularv1);
        return singularv2.dotVectors(scaledv2);
    }
    
    private float getEigenValue(Matrix originalm, MathVector singularv){
        MathVector scaledv = originalm.multiplyMatrixByVector(singularv);
        return singularv.dotVectors(scaledv);
    }
    
    private MathVector getEigenVector(Matrix current, float distCutoff){
        MathVector eigenVector = matrixToVector(current);
        MathVector newEigenVector = null;
        float dist = 100;
        while(dist>distCutoff){
            current.square();
            current.normalise();
            newEigenVector = matrixToVector(current);
            dist = newEigenVector.euclideanDistance(eigenVector);
            eigenVector = newEigenVector;
            //System.out.print("\r  Current eigenvector convergence measure: "+dist);
        }
        //System.out.println("\n");
        return eigenVector.euclideanNormalise();
    }
    
    private MathVector matrixToVector(Matrix matrix){
        MathVector v = new MathVector();
        for(int i=0;i<matrix.getPublicLength();i++){
            v.addValue(1);
        }
        return matrix.multiplyMatrixByVector(v);
    }
}
