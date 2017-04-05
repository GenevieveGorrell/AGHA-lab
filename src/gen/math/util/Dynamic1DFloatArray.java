
package gen.math.util;

import java.io.*;

/** Dynamic1DFloatArray is a fast dynamic float array.
 * @author Genevieve Gorrell
 */
public class Dynamic1DFloatArray {
    private float[] array = new float[2];
    private int publicLength = 0;
    private int privateLength = 2;
    
    /** Class constructor. */    
    public Dynamic1DFloatArray() {
    }
    
    /** Class constructor.
     * @param array A new dynamic float array can be initialised with an extant array of floats.
     * @param publicLength Public length is the length that the dynamic array will appear to have.
     */    
    public Dynamic1DFloatArray(float[] array, int publicLength) {
        this.array = array;
        this.privateLength = array.length;
        this.publicLength = publicLength;
    }
    
    /** Returns the current array of floats.
     * @return The current array of floats.
     */    
    public float[] getArray(){
        return this.array;
    }
    
    /** Returns the current apparent length.
     * @return The current apparent length.
     */    
    public int getPublicLength(){
        return this.publicLength;
    }
    
    /** Returns the current actual array length (potentially longer than the public
     * length since when the array is enlarged it is enlarged by more than is
     * necessary, to speed it up.)
     * @return The actual length of the array.
     */    
    public int getPrivateLength(){
        return this.privateLength;
    }
    
    /** Sets the current float array.
     * @param array The array to set.
     */    
    public void setArray(float[] array){
        this.array = array;
        this.privateLength = array.length;
    }
    
    /** Sets the apparent array length.
     * @param publicLength The new length.
     */    
    public void setPublicLength(int publicLength){
        this.publicLength = publicLength;
        this.extendArray(publicLength);
    }
    
    /** Adds a value to the float array.
     * @param value The float to add.
     * @param index The position to add the new value in. The position does not need to be vacant,
     * nor need it directly follow the last element in the float array.
     */    
    public void addValue(float value, int index){
        if(this.privateLength<index+1){
            this.extendArray(index+1);
        }
        if(this.publicLength<index+1){
            this.publicLength = index+1;
        }
        this.array[index]=value;
    }
    
    /** Adds a value to the float array.
     * @param value The value to add. The new value is added to the end of the dynamic float array.
     */    
    public void addValue(float value){
        this.publicLength++;
        if(this.privateLength<this.publicLength){
            this.extendArray(this.publicLength);
        }
        this.array[this.publicLength-1]=value;
    }
    
    /** Returns a value from the dynamic float array.
     * @param index The index of the value to return.
     * @return The element at the given index.
     */    
    public float getValue(int index){
        if(index>this.publicLength-1){
            System.out.println("Error! Index greater than array length.");
            return 0;
        } else {
            return this.array[index];
        }
    }
    
    /** Extends the array to the given length. The actual length is doubled if
     * necessary and the apparent length is increased to the new length.
     * @param minNewLength The minimum new length.
     */    
    public void extendArray(int minNewLength){
        int oldPrivateLength = this.privateLength;
        while(this.privateLength<minNewLength){
            this.privateLength = this.privateLength*2;
        }
        float newArray[] = new float[this.privateLength];
        for(int i=0;i<oldPrivateLength;i++){
            newArray[i]=this.array[i];
        }
        this.array = newArray;
    }
    
    /** Prints the dynamic float array. */    
    public void print(boolean overwrite){
        if(overwrite){
            for(int i=0;i<this.publicLength;i++){
                float value = this.array[i];
                value = value*100000;
                value = (int)value;
                value = value/100000;
                System.out.print(value+"\t");
            }
            System.out.print("\r");
        } else {
            for(int i=0;i<this.publicLength;i++){
                float value = this.array[i];
                System.out.print(value+" ");
            }
            System.out.print("\n\n");
        }
    }
    
    public void writeToFile(ObjectOutputStream s){
        try {
            s.writeObject(new Integer(this.publicLength));
            s.writeObject(this.array);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static Dynamic1DFloatArray readFromFile(ObjectInputStream s){
        try {
            Dynamic1DFloatArray a = new Dynamic1DFloatArray();
            a.setPublicLength(((Integer)s.readObject()).intValue());
            a.setArray((float[])s.readObject());
            return a;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public Dynamic1DFloatArray copyFloatArray(){
        return new Dynamic1DFloatArray((float[])this.array.clone(), this.publicLength);
    }
}
