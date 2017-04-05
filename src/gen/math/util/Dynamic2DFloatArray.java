
package gen.math.util;

import java.io.*;

/** Dynamic2DFloatArray is a fast 2D dynamic array of floats.
 * @author Genevieve Gorrell
 */
public class Dynamic2DFloatArray {
    private float[][] array = new float[2][2];
    private int publicWidth = 0;
    private int publicLength = 0;
    private int privateWidth = 2;
    private int privateLength = 2;
    
    /** Class constructor. */
    public Dynamic2DFloatArray() {
    }
    
    /** Class constructor. */
    public Dynamic2DFloatArray(float[][] array, int puw, int pul) {
        this.array = array;
        if(array.length>0 && array[0].length>0){
            this.privateLength = array[0].length;
            this.privateWidth = array.length;
            this.publicLength = pul;
            this.publicWidth = puw;
        } else {
            System.out.println("Error! Empty float array.");
        }
    }
    
    /** Adds a float to the array at the specified position. The position can be
     * anywhere within or outside the current array.
     * @param value The value to add.
     * @param column The column position to add the new value to.
     * @param row The row position to add the new value to.
     */    
    public void addValue(float value, int column, int row){
        extendArray(column, row);
        if(column>=this.publicWidth){
            this.publicWidth = column+1;
        }
        if(row>=this.publicLength){
            this.publicLength = row+1;
        }
        this.array[column][row]=value;
    }
    
    /** Returns a value from the array.
     * @param column The column of the value to return.
     * @param row The row of the value to return.
     * @return The value to be returned.
     */    
    public float getValue(int column, int row){
        return this.array[column][row];
    }
    
    /** Returns the apparent width of the array.
     * @return The apparent width of the array.
     */    
    public int getPublicWidth(){
        return this.publicWidth;
    }
    /** Returns the apparent length of the array.
     * @return The apparent length of the array.
     */    
    public int getPublicLength(){
        return this.publicLength;
    }
     
    /** Returns the actual width of the array.
     * @return The actual width of the array.
     */    
    public int getPrivateWidth(){
        return this.privateWidth;
    }
    /** Returns the actual length of the array.
     * @return The actual length of the array.
     */    
    public int getPrivateLength(){
        return this.privateLength;
    }
    
    /** Returns the float array.
     * @return The float array.
     */    
    public float[][] getArray(){
        return this.array;
    }
    
    /** Sets the float array.
     * @param array The float array.
     */    
    public void setArray(float[][] array){
        this.array = array;
        this.privateLength = array[0].length;
        this.privateWidth = array.length;
    }
    /** Sets the apparent width.
     * @param w The new apparent width.
     */    
    public void setPublicWidth(int w){
        this.publicWidth = w;
        this.extendArray(w, this.publicLength);
    }
    /** Sets the apparent length.
     * @param l The new apparent length.
     */    
    public void setPublicLength(int l){
        this.publicLength = l;
        this.extendArray(this.publicWidth, l);
    }
    
    /** Increments the specified array element by the specified amount.
     * @param amount The amount by which to increment the specified element.
     * @param x The column of the element to be incremented.
     * @param y The row of the element to be incremented.
     */    
    public void increment(float amount, int x, int y){
        extendArray(x, y);
        if(x>=this.publicWidth){
            this.publicWidth = x+1;
        }
        if(y>=this.publicLength){
            this.publicLength = y+1;
        }
        if(x<0 || y<0){
            System.out.println("  Error! Cannot increment negative index.");
        } else {
            this.array[x][y]=this.array[x][y]+amount;
        }
    }
    
    /** Extends the array to at least the specified dimensions. The actual array is increased
     * in size if necessary, and the apparent width and height are increased if
     * necessary to cover the given dimensions.
     * @param x The minimum width.
     * @param y The minimum length.
     */    
    public void extendArray(int x, int y){
        int oldPrivateWidth=this.privateWidth;
        int oldPrivateLength=this.privateLength;
        if(this.privateWidth<=x || this.privateLength<=y){
            while(this.privateWidth<=x){
                this.privateWidth = this.privateWidth*2;
            }
            while(this.privateLength<=y){
                this.privateLength = this.privateLength*2;
            }
            float[][] newArray = new float[this.privateWidth][this.privateLength];
            for(int i=0;i<oldPrivateWidth;i++){
                for(int j=0;j<oldPrivateLength;j++){
                    newArray[i][j]=this.array[i][j];
                }
            }
            this.array = newArray;
        }
    }
    
    /** Prints the array. */    
    public void print(){
        for(int i=0;i<this.publicLength;i++){
            for(int j=0;j<this.publicWidth-1;j++){
                System.out.print(this.array[j][i]+"\t");
            }
            System.out.print(this.array[this.publicWidth-1][i]+"\n");
        }
        System.out.print("\n");
    }
    
    /** Prints the array to file.
     * @param fw The file to which the array is printed.
     */    
    public void printToFile(FileWriter fw){
        PrintWriter file = new PrintWriter(fw);
        //file.print("");
        
        if(file!=null){
            for(int i=0;i<this.publicLength-1;i++){
                for(int j=0;j<this.publicWidth-1;j++){
                    file.print(this.array[j][i]+"\t");
                }
                file.print(this.array[this.publicWidth-1][i]+"\n");
            }
            file.print(this.array[this.publicWidth-1][this.publicLength-1]);
            file.close();
        }
    }
}
