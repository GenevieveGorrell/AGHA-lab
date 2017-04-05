
package gen.math.svd;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import gen.math.util.*;
import gen.nlp.svd.*;

/** IncrementalTrainingStrategy encapsulates an approach to singular value
 * decomposition in which data items (such as would be used to create a matrix for
 * standard SVD) are instead presented serially. The strategy assumes natural
 * language data are being presented in the form of n-grams.
 * @author Genevieve Gorrell
 */
public class IncrementalTrainer{
    SvdVectorSet svd = null;
    public static int UPTO = 0;
    public static int ONEV = 1;
    private int substrategy = UPTO;
    private int n = 2;
    private int vnum = 1;
    private float convMeasure = Float.parseFloat("0.0000001");
    private SvdVectorSet ref = null;
        
    public IncrementalTrainer(int substrategy, int vnum, int n, float convMeasure, SvdVectorSet ref) {
        this.substrategy = substrategy;
        this.vnum = vnum;
        this.n = n;
        this.convMeasure = convMeasure;
        this.ref = ref;
    }
    
    public void go(Corpus corpus, Lab lab, SvdVectorSet svdVectorSet) {
        this.svd = svdVectorSet;
        Vector s1 = this.svd.getS1();
        Vector s2 = this.svd.getS2();
        boolean stop = lab.requestStop;
        System.out.println("  Calculating SVD. Hit enter to interrupt.");
        for(int i=0;i<=svd.lastToConverge;i++){
            System.out.println("\n  Reloaded vectors at "+i+", first value:"+svd.getV1().getValue(i)
                                +", second value:"+svd.getV2().getValue(i));
        }
        int index = svd.lastToConverge+1;
        int trainingExamples = 0;
        float conv = 100;
        int printIncrement=0;
        
        if(index<this.vnum){
            System.out.println("\n  Training vector at "+index+", "+ new Date());
            if(s1.size()<index+1 || s2.size()<index+1){
                s1.add(new MathVector());
                s2.add(new MathVector());
            } else {
		trainingExamples=((Integer)this.svd.getTrainingExamples().elementAt(index)).intValue();
	    }
        }
            
        corpus.startOver();
        Vector ngram = corpus.getNGram(this.n-1, 1, true);
        while(index<this.vnum && ngram!=null && stop==false){
            String first=(String)ngram.elementAt(0);
            String second=(String)ngram.elementAt(1);
            if(this.substrategy==this.UPTO){
                this.learnNgramsUpTo(first,second,s1,s2,index);
            } else {
		this.learnNgramOneLV(first,second,s1,s2,index);
            }
            trainingExamples++;
            printIncrement++;
            if(printIncrement==100){
		conv=convMeasure;
                if(lab.requestStop){
                    stop=true;
		    if(svd.getTrainingExamples().size()<=index){
			svd.getTrainingExamples().add(new Integer(trainingExamples));
		    } else {
			svd.getTrainingExamples().set(index, new Integer(trainingExamples));
		    }
                } else {
                    conv = this.printProgress(index,trainingExamples,(MathVector)s1.elementAt(index),(MathVector)s2.elementAt(index));
                }
                printIncrement=0;
            }
            if(conv>convMeasure){
                this.updateValuesAndTrainingExamples(index,trainingExamples,s1,s2);
                this.svd.lastToConverge=index;
                /*if(this.svd.getSaveFileName()!=null){
                    this.svd.save();
		    }*/
                trainingExamples=0;
                index++;
		conv=convMeasure;
                if(index<this.vnum){
                    System.out.println("\n  Training vector at "+index+", "+ new Date());
                    s1.add(new MathVector());
                    s2.add(new MathVector());
                } else {
                    System.out.println("\n");
                }
            }
            ngram = corpus.getNGram(n-1, 1, true);
        }
        if(stop==true){
            /*for(int i=this.svd.getV1().getPublicLength();i<s1.size();i++){
                s1.remove(i);
            }
            for(int i=this.svd.getV1().getPublicLength();i<s2.size();i++){
                s2.remove(i);
            }*/
            this.updateValuesAndTrainingExamples(index, trainingExamples, this.svd.getS1(), this.svd.getS2());
            System.out.println("  Exited on request. "+ new Date()+"\n");
        } else {
            System.out.println("  Done. "+ new Date()+"\n");
        }
    }
    
    private float printProgress(int index, int trainingExamples, MathVector thisS1, MathVector thisS2){
        float val1 = thisS1.getMagnitude()/trainingExamples;
        float val2 = thisS2.getMagnitude()/trainingExamples;
        MathVector s1Norm = thisS1.euclideanNormalise();
        MathVector s2Norm = thisS2.euclideanNormalise();
        float s1Conv = thisS1.getMagnitude();
        float s2Conv = thisS2.getMagnitude();
        float lprox = 0;
        float rprox = 0;
        
        if(this.ref!=null){
            Vector l = this.ref.getS1();
            Vector r = this.ref.getS2();
	    if(l!=null && r!=null && l.size()>index && r.size()>index){
                MathVector lref = (MathVector)l.elementAt(index);
                MathVector rref = (MathVector)r.elementAt(index);
		if(lref.getPublicLength()!=s1Norm.getPublicLength() || rref.getPublicLength()!=s2Norm.getPublicLength()){
		    System.out.print("Ref error.");
		} else {
		    lprox = s1Norm.dotVectors(lref.euclideanNormalise());
		    rprox = s2Norm.dotVectors(rref.euclideanNormalise());
		}
            }
        }
        
	float c=s2Conv;
        if(s1Conv<s2Conv){
            c=s1Conv;
	}
	
	float ca = (int)c;
	float lproxa = lprox*10000;
	float rproxa = rprox*10000;
	float val1a = val1*10000;
	float val2a = val2*10000;
	int intlprox = (int)lproxa;
	int intrprox = (int)rproxa;
	int intval1 = (int)val1a;
	int intval2 = (int)val2a;
	lproxa = ((float)intlprox)/10000;
	rproxa = ((float)intrprox)/10000;
	val1a = ((float)intval1)/10000;
	val2a = ((float)intval2)/10000;
        System.out.print("\r  Conv:"+ca+
                         "\tVal1:"+val1a+
                         "\tVal2:"+val2a+
                         "\tLProx:"+lproxa+
                         "\tRProx:"+rproxa+
                         "      ");
	return c;
    }
    
    private float updateValuesAndTrainingExamples(int index, int trainingExamples, Vector s1, Vector s2){
        float length1 = ((MathVector)s1.elementAt(index)).getMagnitude();
        float length2 = ((MathVector)s2.elementAt(index)).getMagnitude();
        svd.getV1().addValue(length1/trainingExamples, index);
        svd.getV2().addValue(length2/trainingExamples, index);
	if(svd.getTrainingExamples().size()<=index){
	    svd.getTrainingExamples().add(new Integer(trainingExamples));
        } else {
	    svd.getTrainingExamples().set(index, new Integer(trainingExamples));
	}
	if(this.substrategy==this.UPTO){
            for(int i=0;i<index;i++){
                length1 = ((MathVector)s1.elementAt(i)).getMagnitude();
                length2 = ((MathVector)s2.elementAt(i)).getMagnitude();
                int tr = ((Integer)this.svd.getTrainingExamples().elementAt(i)).intValue();
                tr = tr+trainingExamples;
                this.svd.getTrainingExamples().set(i, new Integer(tr));
                svd.getV1().addValue(length1/tr, i);
                svd.getV2().addValue(length2/tr, i);
            }
        }
        return (length1+length2)/trainingExamples*2;
    }
    
    /*private MathVector createInitialFeatureVector(int length){
        MathVector v = new MathVector();
        for(int i=0;i<length;i++){
            v.addValue(1);
        }
        return v;
    }*/
    
    private SparseVector s1ToVector(String inputString){
        SparseVector v = this.svd.getNames().smartRowExtend(inputString);
        this.extendVectorsSize(this.svd.getS1(), v.length, Float.parseFloat("0.001"));
        return v;
    }
    
    private SparseVector s2ToVector(String inputString){
        SparseVector v = this.svd.getNames().smartColumnExtend(inputString);
        this.extendVectorsSize(this.svd.getS2(), v.length, Float.parseFloat("0.001"));
        return v;
    }
    
    private void extendVectorsSize(Vector vset, int newSize, float initialValue){
        for(int i=0;i<vset.size();i++){
            MathVector v = (MathVector)vset.elementAt(i);
            for(int j=v.getPublicLength();j<newSize;j++){
                v.addValue(initialValue);
            }
        }
    }
    
    private float learnNgramOneLV(String first, String second, Vector s1, Vector s2, int index){
        MathVector inputS1 = s1ToVector(first).toMathVector();
        MathVector inputS2 = s2ToVector(second).toMathVector();
        
        //Calculate the scaled vector to add to s1
        MathVector thisS2 = (MathVector)s2.elementAt(index);
        MathVector thisS2Norm = thisS2.euclideanNormalise();
        float s2Term = thisS2Norm.dotVectors(inputS2);
        MathVector inputs1Scaled = inputS1.multiplyVectorByScalar(s2Term);
        
        //Calculate the scaled vector to add to s2
        MathVector thisS1 = (MathVector)s1.elementAt(index);
        MathVector thisS1Norm = thisS1.euclideanNormalise();
        float s1Term = thisS1Norm.dotVectors(inputS1);
        MathVector inputs2Scaled = inputS2.multiplyVectorByScalar(s1Term);
            
	MathVector thisS1temp;
	MathVector thisS2temp;
	MathVector thisS1tempNorm;
	MathVector thisS2tempNorm;
        //Subtract previous LVs from these inputs to avoid finding the same thing twice
        for(int i=0;i<index;i++){
            thisS1temp = (MathVector)s1.elementAt(i);
            thisS2temp = (MathVector)s2.elementAt(i);
            thisS1tempNorm = thisS1temp.euclideanNormalise();
            thisS2tempNorm = thisS2temp.euclideanNormalise();
            float subtractS1Size = thisS1tempNorm.dotVectors(inputs1Scaled);
            float subtractS2Size = thisS2tempNorm.dotVectors(inputs2Scaled);
            inputs1Scaled = inputs1Scaled.subtractVectors(thisS1tempNorm.multiplyVectorByScalar(subtractS1Size));
            inputs2Scaled = inputs2Scaled.subtractVectors(thisS2tempNorm.multiplyVectorByScalar(subtractS2Size));
        }
            
	//Learning rate:
	//inputs1Scaled.multiplyVectorByScalar(Float.parseFloat("0.1"));
	//inputs2Scaled.multiplyVectorByScalar(Float.parseFloat("0.1"));

        //Add the scaled inputs to the appropriate LVs and update
        MathVector newS1 = thisS1.addVectors(inputs1Scaled);
        MathVector newS2 = thisS2.addVectors(inputs2Scaled);
        this.svd.getS1().set(index, newS1);
        this.svd.getS2().set(index, newS2);
           
        //float length1 = newS1.getMagnitude();
        //float length2 = newS2.getMagnitude();
        
        //return length1+length2;
	return 1;
    }
    
    private float learnNgramsUpTo(String first, String second, Vector s1, Vector s2, int max){
        MathVector inputS1 = s1ToVector(first).toMathVector();
        MathVector inputS2 = s2ToVector(second).toMathVector();
        
        if(max>this.svd.getS1().size()) max=this.svd.getS1().size();
        
        for(int i=0;i<=max;i++){
            MathVector thisS1 = (MathVector)s1.elementAt(i);
            MathVector thisS2 = (MathVector)s2.elementAt(i);
            
            MathVector thisS1Norm = thisS1.euclideanNormalise();
            MathVector thisS2Norm = thisS2.euclideanNormalise();
            
            float s1Term = thisS1Norm.dotVectors(inputS1);
            //if (s1Term<0) s1Term=0;
            MathVector s2Scaled = inputS2.multiplyVectorByScalar(s1Term);
            
            float s2Term = thisS2Norm.dotVectors(inputS2);
            //if (s2Term<0) s2Term=0;
            MathVector s1Scaled = inputS1.multiplyVectorByScalar(s2Term);
            
            MathVector newS1 = thisS1.addVectors(s1Scaled);
            MathVector newS2 = thisS2.addVectors(s2Scaled);
            
            this.svd.getS1().set(i, newS1);
            this.svd.getS2().set(i, newS2);
            
            //Subtract projection of new feature vectors from input vectors
            MathVector newS1Norm = newS1.euclideanNormalise();
            MathVector newS2Norm = newS2.euclideanNormalise();
            float sub1 = newS1Norm.dotVectors(inputS1);
            float sub2 = newS2Norm.dotVectors(inputS2);
            inputS1 = inputS1.subtractVectors(newS1Norm.multiplyVectorByScalar(sub1));
            inputS2 = inputS2.subtractVectors(newS2Norm.multiplyVectorByScalar(sub2));
            
        }
        
        return 0;
    }
}
