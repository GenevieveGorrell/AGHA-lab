
package gen.nlp.svd;

import java.io.*;
import java.util.Vector;

/** Corpus encapsulates a textual natural language corpus and makes available a
 * number of methods for accessing it.
 * @author Genevieve Gorrell
 */
public class Corpus{
    /** The text file to be read. */    
    public BufferedReader file = null;
    private String fileName = null;
    private Vector sentence = new Vector();
    private int sentence_pointer = 0;
    private boolean file_ended = true;
    
    /** Class constructor.
     * @param fileName The location of the file to be read.
     */    
    public Corpus(String fileName) {
        this.fileName = fileName;
        this.startOver();
    }
    
    public String getFilename(){
        return this.fileName;
    }
    
    /** Reopens the file such that the file pointer is returned to the beginning. */    
    public void startOver(){
        try {
            if(file!=null){
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(fileName!=null){
            try {
                file = new BufferedReader(new FileReader(fileName));
                this.file_ended=false;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        this.sentence = new Vector();
    }
    
    /** Reads in a line from the text file.
     * @return The line that has been read.
     */    
    public String nextLine(){
        String line = null;
        try {
            line = file.readLine();
        } catch (Exception e) {
            System.out.println(e);
        }
        return line;
    }
    
    /** Gets the next sentence from the text file and stores it. */    
    public void nextSentence(){
        String word = new String();
        char[] character = new char[1];
        int eof = 0;
        int wordState = -1;
        this.sentence = new Vector();
        this.sentence_pointer = 0;
        
        try {
            eof = file.read(character);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        //Read characters into word until next non-character     
        while(eof!=-1 
                && character[0]!='.' 
                && character[0]!='?' 
                && character[0]!='!' 
                && character[0]!=':'){
            if (character[0]==' '
                       || character[0]=='\t'
                       || character[0]=='\r'
                       || character[0]=='\n'
                       || character[0]==','){
                if(word.length()>0){
                    sentence.add(word.toLowerCase());
                    word = new String();
                }
            } else if (character[0]=='-'
                       || character[0]=='\"'
                       || character[0]=='('
                       || character[0]==')'){
                    //Do nothing
            } else {
                word = word+character[0];
            }
            try {
                eof = file.read(character);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        if(word.length()>0){
            sentence.add(word.toLowerCase());
            word = new String();
        }
        if(eof==-1){
            this.file_ended=true;
        }
    }
    
    /** Returns the next n-gram of specified dimension. This is accomplished by taking
     * the next set of the required size from the sentence variable. N-grams are taken
     * from sentences rather than lines because an n-gram does not cross a sentence
     * boundary. If the sentence is used up, then nextSentence() is used to get another
     * one. If the file is used up, then if the parameter restart is set to true, then
     * the file pointer is returned to the beginning and the first sentence is used.
     * @param n Number of words in the first part of the pair (in a trigram, typically 2).
     * @param m Number of words in the second part of the pair.
     * @param restart Should the file be started over when the end is reached?
     * @return A pair of strings containing the required n-gram.
     */    
    public Vector getNGram(int n, int m, boolean restart){
        if(this.sentence!=null && this.sentence.size()>=n+m+this.sentence_pointer+1){
            this.sentence_pointer++;
            return this.formulateNGram(n, m);
        } else {
            boolean to_restart = restart;
            this.nextSentence();
            while(this.sentence==null || sentence.size()<n+m){
                if(this.file_ended==true){
                    if(to_restart==true){
                        this.startOver();
                        to_restart = false;
                    } else {
                        return null;
                    }
                }
                this.nextSentence();
            }
            return this.formulateNGram(n, m);
        }
    }
    
    /** Utility function that prepares the string pair to be returned by getNGram().
     * @param n Number of words in the first part of the string pair.
     * @param m Number of words in the second part of the string pair.
     * @return Pair of strings containing the n-gram.
     */    
    public Vector formulateNGram(int n, int m){
        String nstring = new String("");
        String mstring = new String("");
        int start_point = this.sentence_pointer;
        for(int x=start_point;x<start_point+n-1;x++){
            nstring = nstring + sentence.elementAt(x) + "-";
        }
        if(n>0){
            nstring = nstring + sentence.elementAt(start_point+n-1);
        }
        start_point = this.sentence_pointer+n;
        for(int x=start_point;x<start_point+m-1;x++){
            mstring = mstring + sentence.elementAt(x) + "-";
        }
        if(m>0){
            mstring = mstring + sentence.elementAt(start_point+m-1);
        }
        Vector v = new Vector();
        v.add(nstring);
        v.add(mstring);
        return v;
    }
    
    
}
