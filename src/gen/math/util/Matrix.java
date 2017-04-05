
package gen.math.util;

import java.io.*;

/**
 * Matrix extends Dynamic2DFloatArray with some methods appropriate to
 * matrices, in the math sense of the word.
 * 
 * @author Genevieve Gorrell
 */
public class Matrix extends Dynamic2DFloatArray{
    
    /** Class constructor. */    
    public Matrix(){
        super();
    }
    
    /** Class constructor. */    
    public Matrix(float[][] array, int puw, int pul){
        super(array, puw, pul);
    }
    
    /** Returns a clone of the current Matrix object.
     * @return Copy of the Matrix.
     */
    public Matrix copy(){
        float[][] a = new float[this.getPrivateWidth()][this.getPrivateLength()];
        for(int i=this.getPublicWidth()-1;i>=0;i--){
            for(int j=this.getPublicLength()-1;j>=0;j--){
                a[i][j]=this.getArray()[i][j];
            }
        }
        Matrix m = new Matrix(a, this.getPublicWidth(), this.getPublicLength());
        return m;
    }
    
    /** Updates the current matrix to be itself squared. */
    public void square(){
        if(this.getPublicLength()!=this.getPublicWidth()){
            System.out.println("Error! Attempt to square an non-square matrix.");
        } else {
            float[][] newMatrix = new float[this.getPrivateWidth()][this.getPrivateLength()];
            int w = this.getPublicWidth();
            int l = this.getPublicLength();
            float[][] m = this.getArray();
            for(int x=0;x<w;x++){
                for(int y=0;y<l;y++){
                    float value=0;
                    for(int i=0;i<w;i++){ //Could just as well have been length
                        value += m[x][i] * m[i][y];
                    }
                    newMatrix[x][y]=value;
                }
            }
            this.setArray(newMatrix);
        }
    }
    
    /** Multiplies the matrix by its transpose.
     * @return A new Matrix object that is the current matrix multiplied by its transpose.
     */    
    public Matrix multiplyMatrixTranspose(){
        Matrix sq = new Matrix();
        int w = this.getPublicWidth();
        int l = this.getPublicLength();
        float[][] temp = new float[l][l];
        float[][] m = this.getArray();
        for(int x=0;x<l;x++){
            for(int y=0;y<l;y++){
                float value=0;
                for(int i=0;i<w;i++){
                    value += m[i][x] * m[i][y];
                }
                temp[x][y]=value;
            }
        }
        
        return new Matrix(temp, l, l);
    }
    
    /** Multiplies the transpose of the current Matrix by itself.
     * @return A new Matrix object that is the result of multiplying the transpose of the
     * current matrix by the current matrix.
     */    
    public Matrix multiplyTransposeMatrix(){
        Matrix sq = new Matrix();
        int w = this.getPublicWidth();
        int l = this.getPublicLength();
        float[][] temp = new float[w][w];
        float[][] m = this.getArray();
        for(int x=0;x<w;x++){
            for(int y=0;y<w;y++){
                float value=0;
                for(int i=0;i<l;i++){
                    value += m[x][i] * m[y][i];
                }
                temp[x][y]=value;
            }
        }
        
        return new Matrix(temp, w, w);
    }
    
    /** Performs a Euclidean (root of sum of squares) over the entire matrix. The
     * current matrix is updated.
     */    
    public void normalise(){
        //first get sum of squares
        double ssq = 0;
        for(int x=0;x<this.getPublicWidth();x++){
            for(int y=0;y<this.getPublicLength();y++){
                ssq += this.getValue(x,y) * this.getValue(x,y);
            }
        }
        float sqrssq = (float)Math.sqrt(ssq);
        //then divide each element by the square root of the sum of squares
        for(int x=0;x<this.getPublicWidth();x++){
            for(int y=0;y<this.getPublicLength();y++){
                if(sqrssq!=0){
                    this.addValue(this.getValue(x,y)/sqrssq,x,y);
                } else {
                    System.out.println("Error! Normalise cannot divide by zero. Value unchanged.");
                }
            }
        }
    }
    
    public float total(){
        float total = 0;
        for(int x=0;x<this.getPublicWidth();x++){
            for(int y=0;y<this.getPublicLength();y++){
                total += this.getValue(x,y);
            }
        }
        return total;
    }
    
    /** Performs a Manhattan normalise (add everything up and divide by the total number
     * of elements) over the matrix. The current matrix is updated.
     */    
    public void manhattanNormalise(){
        //first add everything up
        float total = 0;
        for(int x=0;x<this.getPublicWidth();x++){
            for(int y=0;y<this.getPublicLength();y++){
                total += this.getValue(x,y);
            }
        }
        //then divide each element by the total
        for(int x=0;x<this.getPublicWidth();x++){
            for(int y=0;y<this.getPublicLength();y++){
                if(total!=0){
                    this.addValue(this.getValue(x,y)/total,x,y);
                } else {
                    this.addValue(0,x,y);
                }
            }
        }
    }
    
    /** Multiplies the matrix by a MathVector of appropriate length.
     * @param vector The MathVector to multiply the matrix by.
     * @return The MathVector resulting from multiplying the matrix by a vector.
     */    
    public MathVector multiplyMatrixByVector(MathVector vector){
        if(vector.getPublicLength()!=this.getPublicLength()){
            System.out.println("  Error! Attempt to multiply matrix by vector of unequal length.");
            return null;
        } else {
            MathVector rvec = new MathVector();
            for(int x=0;x<this.getPublicWidth();x++){
                float newvelem = 0;
                for(int y=0;y<this.getPublicLength();y++){
                    float velem = vector.getValue(y);
                    newvelem += (velem * this.getValue(x, y));
                }
                rvec.addValue(newvelem);
            }
            return rvec;
        }
    }
    
    /** Subtracts a similarly dimensioned matrix from the current matrix. The matrix is
     * updated.
     * @param toSubtract The Matrix to subtract.
     */    
    public void subtract(Matrix toSubtract){
        if(this.getPublicLength()!=toSubtract.getPublicLength()
            || this.getPublicWidth()!=toSubtract.getPublicWidth()){
            System.out.println("Error! Attempt to subtract matrix of unequal size.");
        } else {
            for(int  i=0;i<this.getPublicWidth();i++){
                for(int j=0;j<this.getPublicLength();j++){
                    this.addValue(this.getValue(i,j)-toSubtract.getValue(i,j), i, j);
                }
            }
        }
    }
    
    public void add(Matrix toAdd){
        if(this.getPublicLength()!=toAdd.getPublicLength()
            || this.getPublicWidth()!=toAdd.getPublicWidth()){
            System.out.println("Error! Attempt to add matrix of unequal size.");
        } else {
            for(int  i=0;i<this.getPublicWidth();i++){
                for(int j=0;j<this.getPublicLength();j++){
                    this.addValue(this.getValue(i,j)+toAdd.getValue(i,j), i, j);
                }
            }
        }
    }
        
    public void flexiadd(Matrix toAdd){
        if(this.getPublicLength()<toAdd.getPublicLength()){
            this.setPublicLength(toAdd.getPublicLength());
        }
        if(this.getPublicWidth()<toAdd.getPublicWidth()){
            this.setPublicWidth(toAdd.getPublicWidth());
        }
        for(int  i=0;i<this.getPublicWidth();i++){
            for(int j=0;j<this.getPublicLength();j++){
                this.addValue(this.getValue(i,j)+toAdd.getValue(i,j), i, j);
            }
        }
    }
    /** Multiplies the matrix by a scalar. The current matrix is updated.
     * @param sc The Float to multiply the current matrix by.
     */    
    public void multiplyMatrixByScalar(float sc){
        for(int  i=0;i<this.getPublicWidth();i++){
            for(int j=0;j<this.getPublicLength();j++){
                this.addValue(this.getValue(i,j)*sc,i,j);
            }
        }
    }
    
    /** Returns the transpose of the current matrix.
     * @return A Matrix object that is the transpose of the current matrix.
     */    
    public Matrix transpose(){
        Matrix t = new Matrix();
        for(int  i=0;i<this.getPublicWidth();i++){
            for(int j=0;j<this.getPublicLength();j++){
                t.addValue(this.getValue(i,j), j, i);
            }
        }
        return t;
    }
    
    /** Prints the current matrix to the command line. */    
    public void print(){
        for(int i=0;i<this.getPublicLength();i++){
            for(int j=0;j<this.getPublicWidth()-1;j++){
                System.out.print(this.getValue(j,i)+"\t");
            }
            System.out.print(this.getValue(this.getPublicWidth()-1,i)+"\n");
        }
        System.out.print("\n");
    }
    
    /** Prints the current matrix to the specified file.
     * @param fw The FileWriter to print the matrix to.
     */    
    public void printToFile(FileWriter fw){
        PrintWriter file = new PrintWriter(fw);
        //file.print("");
        
        if(file!=null){
            for(int i=0;i<this.getPublicLength();i++){
                for(int j=0;j<this.getPublicWidth()-1;j++){
                    file.print(this.getValue(j,i)+"\t");
                }
                file.print(this.getValue(this.getPublicWidth()-1,i)+"\n");
            }
            file.close();
        }
    }
    
    /** Divides each row by the first to facilitate visual assesment of convergence in
     * singular value decomposition.
     */    
    public void rowCompPrint(){
        for(int j=0;j<this.getPublicWidth();j++){
            System.out.print(this.getValue(j,0)+"\t");
        }
        System.out.print("\n");
        for(int i=1;i<this.getPublicLength();i++){
            for(int j=0;j<this.getPublicWidth();j++){
                System.out.print(this.getValue(j,i)/this.getValue(j,0)+"\t");
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }
}
