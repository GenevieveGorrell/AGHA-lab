
package gen.math.util;

import java.io.*;

/** Dynamic1DIntArray is a fast dynamic integer array.
 * @author Genevieve Gorrell
 */
public class Dynamic1DIntArray {
    private int[] array = new int[2];
    private int publicLength = 0;
    private int privateLength = 2;
    
    /** Class constructor. */    
    public Dynamic1DIntArray() {
    }
    
    public Dynamic1DIntArray(int[] array, int pulen, int prlen){
        this.array = array;
        this.publicLength = pulen;
        this.privateLength = prlen;
    }
    
    /** Returns the current int array.
     * @return The current int array.
     */    
    public int[] getArray(){
        return this.array;
    }
    
    /** Returns the current apparent length.
     * @return The current apparent length.
     */    
    public int getPublicLength(){
        return this.publicLength;
    }
    
    /** Returns the current actual length.
     * @return The current actual length.
     */    
    public int getPrivateLength(){
        return this.privateLength;
    }
    
    /** Sets the array of integers.
     * @param array The new array of integers.
     */    
    public void setArray(int[] array){
        this.array = array;
        this.privateLength = array.length;
    }
    
    /** Sets a new apparent length.
     * @param publicLength The new apparent length.
     */    
    public void setPublicLength(int publicLength){
        this.publicLength = publicLength;
        this.extendArray(publicLength);
    }
    
    /** Adds a value to the dynamic integer array.
     * @param value The value to add.
     * @param index The position in the array to add it. The position need neither be vacant nor
     * directly ensue the current last element.
     */    
    public void addValue(int value, int index){
        if(this.privateLength<index+1){
            this.extendArray(index+1);
        }
        if(this.publicLength<index+1){
            this.publicLength = index+1;
        }
        this.array[index]=value;
    }
    
    /** Adds a value to the dynamic integer array. The new value is added at the end.
     * @param value The value to add.
     */    
    public void addValue(int value){
        this.publicLength++;
        if(this.privateLength<this.publicLength){
            this.extendArray(this.publicLength);
        }
        this.array[this.publicLength-1]=value;
    }
    
    /** Returns a value from the array.
     * @param index The index of the value to return.
     * @return The value if the array at that index.
     */    
    public int getValue(int index){
        if(index>this.publicLength-1){
            System.out.println("Error! Index greater than array length.");
            return 0;
        } else {
            return this.array[index];
        }
    }
    
    /** Extends the array to cover a specified length. The actual length of the array is
     * doubled if necessary and the apparent length is increased to the specified
     * minimum new length.
     * @param minNewLength The minimum new length.
     */    
    public void extendArray(int minNewLength){
        int oldPrivateLength = this.privateLength;
        while(this.privateLength<minNewLength){
            this.privateLength = this.privateLength*2;
        }
        int newArray[] = new int[this.privateLength];
        for(int i=0;i<oldPrivateLength;i++){
            newArray[i]=this.array[i];
        }
        this.array = newArray;
    }
    
    /** Prints the array. */    
    public void print(){
        for(int i=0;i<this.publicLength;i++){
            float value = this.array[i];
            System.out.print(value+" ");
        }
        System.out.print("\n\n");
    }
    
    public void writeToFile(ObjectOutputStream s){
        try {
            s.writeObject(new Integer(this.publicLength));
            s.writeObject(this.array);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static Dynamic1DIntArray readFromFile(ObjectInputStream s){
        try {
            Dynamic1DIntArray a = new Dynamic1DIntArray();
            a.setPublicLength(((Integer)s.readObject()).intValue());
            a.setArray((int[])s.readObject());
            return a;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public Dynamic1DIntArray copy(){
        return new Dynamic1DIntArray((int[])this.array.clone(), this.publicLength, this.privateLength);
    }
}
