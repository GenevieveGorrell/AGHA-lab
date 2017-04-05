
package gen.math.svd;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import gen.math.util.*;
import gen.nlp.svd.*;

/** GeneralizedHebbian implements the generalised Hebbian algorithm.
 * @author Genevieve Gorrell
 */
public class GeneralizedHebbian{
    SvdVectorSet svd = null;
    private int n = 2;
    private int vnum = 1;
    private float convMeasure = Float.parseFloat("0.001");
    private int dataType = DataFactory.LINEWISE;
    private boolean columns = false;
    private float init = 1;
    private SvdVectorSet referenceset = null;
    private MathVector cumulativeAdjuster = null;
    private MathVector currentFeature;
    private int lastlen = 0;
    private PrintWriter graphfile = null;
    private Dynamic1DIntArray garray = new Dynamic1DIntArray();
    private Dynamic1DFloatArray tarray = new Dynamic1DFloatArray();
    private int epicsize = 0;
    private float logEpicsize;
    private boolean useref = false;
    private MathVector thisRa = null;
    
    /** Class constructor.
     * @param substrategy The substrategy to be used in this decomposition.
     * @param vnum The number of vectors to train.
     * @param n The number of words to be taken from the corpus to form each training item. For
     * example, 2 would mean that the vector set is being trained on bigram data.
     * @param settledLength Terminate criterion. The vector pairs grow large in length as they settle on
     * their appropriate directions and the data begin to increasingly reinforce them.
     * As a terminate criterion, at which the vector pair is considered sufficiently
     * stable, the next vector pair begins to be trained.
     */    
    public GeneralizedHebbian(int vnum, int n, float convMeasure, int dataType, float init, SvdVectorSet referenceset, int epicsize) {
        this.vnum = vnum;
        this.n = n;
        this.convMeasure = convMeasure;
        this.dataType = dataType;
        this.init = init;
        this.referenceset = referenceset;
        this.epicsize = epicsize;
        this.logEpicsize = (float)Math.log(this.epicsize);
        
        try {
            graphfile = new PrintWriter(new FileWriter("out.txt"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    /** Executes the training of the vector set. If the vector set has a save location
     * specified, saving takes place automatically each time a new vector pair begins
     * to be trained.
     * @param dataFactory The data factory from which training data (n-grams) are drawn.
     * @param svdVectorSet The vector set that the method is training.
     */    
    public void go(DataFactory df, SvdVectorSet svdVectorSet, Lab lab, boolean useref, boolean weighting) {
	this.useref=useref;
        if (this.referenceset==null || this.referenceset.getEigenvector(0)==null){
            useref=false;
        } else {
            thisRa = this.referenceset.getEigenvector(0);
        }
        this.svd = svdVectorSet;
        NameSet names = this.svd.getNames();
        SparseVector vec = null;
        int incrementTarget = 500;
        int increment = 0;
        MathVector previousVec = null;
        this.cumulativeAdjuster = null;
        df.getCorpus().startOver();
        int index = this.initialiseAndPrintStartupMessage();
        int items=0;
        int totalitems=0;
        boolean stop = lab.requestStop;
        
        vec = this.selectInitialisationVector(df, names, 25, index);
        while(index<this.vnum && vec!=null && stop==false){
            items++;
            totalitems++;
            if(weighting){
                vec = this.imposeWeightings(vec, totalitems);
            }
            this.learnSparse(vec, index);
            increment++;
            float conv = convMeasure;
            if(increment>=incrementTarget){
                if(lab.requestStop){
                    stop=true;
                } else {
                    conv = this.statusReport(previousVec, useref, thisRa, convMeasure, items);
                }
                previousVec = currentFeature.copy();
                increment=0;
            }
            if(conv<convMeasure){
                this.orthogonaliseFeature(index, cumulativeAdjuster);
                float val = this.updateValuesAndTrainingExamples(index, items);
                float prox = -100;
                if(thisRa!=null){
                    prox = thisRa.dotVectors(this.svd.getEigenvector(index).euclideanNormalise());
                }
                items = 0;
                if(this.svd.getSaveFileName()!=null) this.svd.save();
                index++;
                this.cumulativeAdjuster = null;
                if (this.referenceset==null || this.referenceset.getEigenvector(index)==null){
                    useref=false;
                } else {
                    thisRa = this.referenceset.getEigenvector(index);
                }
                this.printConvergenceMessage(index, prox, val);
                vec = this.selectInitialisationVector(df, names, 100, index);
            } else {
                vec = this.getTrainingVector(this.svd.getNames(), df);
            }
        }
        if(stop==true){
            System.out.println("  Exited on request. "+ new Date()+"\n");
        } else {
            System.out.println("  Done. "+ new Date()+"\n");
        }
        try {
            this.graphfile.close();
        } catch (Exception e){
            System.out.println(e);
        }
    }
    
    private SparseVector imposeWeightings(SparseVector v, int n){
        if(this.garray.getPublicLength()!=v.length){
            this.garray.setPublicLength(v.length);
            this.tarray.extendArray(v.length);
            this.garray.extendArray(v.length);
        }
        int[] inds = v.indices.getArray();
        float[] vals = v.values.getArray();
        float logn = (float)Math.log(n);
        int[] g = garray.getArray();
        float[] t = tarray.getArray();
        SparseVector toReturn = new SparseVector();
        toReturn.length = v.length;
        for(int i=0;i<v.indices.getPublicLength();i++){
            int sparseIndex = inds[i];
            g[sparseIndex]+=vals[i];
            t[sparseIndex]+=vals[i]*(float)Math.log(vals[i]);
            if(n>1){
                float G = g[sparseIndex];
                float T = t[sparseIndex];
                float gw = 0;
                if(this.epicsize>0){
                    gw = 1 + ((((1/G)*T)-(float)Math.log(G)+logn-this.logEpicsize)/this.logEpicsize);
                } else {
                    gw = 1 + ((T - (G * (float)Math.log(G)))/(G * logn));
                }
                float value = gw*(float)Math.log(vals[i]+1);
                /*if(sparseIndex==3){
                    System.out.print("\n"+G+"\t"+T+"\t"+this.logEpicsize+"\t"+gw+"\t"+value+"\t"+v.getValue(sparseIndex));
                }*/
                toReturn.addValue(value, sparseIndex);
            } else {
                toReturn = v;
            }
        }
        this.garray.setArray(g);
        this.tarray.setArray(t);
        return toReturn;
    }
    
    private int initialiseAndPrintStartupMessage(){
        System.out.println("  Calculating eigen decomposition. Hit enter to interrupt.");
        for(int i=0;i<svd.getVectorCount();i++){
            System.out.println("\n  Reloaded vectors at "+i+", value:"+svd.getV1().getValue(i));
        }
        int index=svd.getVectorCount(); //Skip over any already previously calculated
	if (this.referenceset==null || this.referenceset.getEigenvector(index)==null){
	    useref=false;
	} else {
	    thisRa = this.referenceset.getEigenvector(index);
	}
        if(index<this.vnum){
            System.out.println("\n  Training vector at "+index+", "+ new Date());
        }
        return index;
    }
    
    private void printConvergenceMessage(int index, float prox, float val){
        if(prox!=-100){
            System.out.print("\r  Value: "+val+", Proximity to reference: "+prox);
        } else {
            System.out.print("\r  Value: "+val);
        }
        if(index<vnum){
            System.out.println("\n\n  Training vector at "+index+", "+ new Date());
        } else {
            System.out.println("\n");
        }
    }
    
    private float statusReport(MathVector previousVec, boolean useref, MathVector thisRa, 
            float convMeasure, int items){
        float raprox = -1;
        float conv = convMeasure;
        if(this.currentFeature!=null && previousVec!=null && this.currentFeature.getPublicLength()==previousVec.getPublicLength()){
            MathVector vecNorm = this.currentFeature.euclideanNormalise();
            MathVector previousVecNorm = previousVec.euclideanNormalise();
            //previousVecNorm.print(false);
            conv = vecNorm.euclideanDistance(previousVecNorm);
            //REFERENCE NOT RELEVANT DURING SPARSE TRAINING!!
	    /*if(useref){
                raprox = vecNorm.euclideanDistance(thisRa);
                if(raprox>1){
                    raprox=2-raprox;
                }
                //raprox = 1 - vecNorm.dotVectors(previousVecNorm);
		}*/
        }
	Float conva=new Float(conv*1000000);
	int intconva=conva.intValue();
	float newconv=((float)intconva)/1000000;
        System.out.print("\r  Convergence:"+newconv);
	/*if(useref){
	    Float raproxa=new Float(raprox*10000);
	    int intraproxa=raproxa.intValue();
	    float newraprox=((float)intraproxa)/10000;
	    System.out.print("\tRaprox:"+newraprox);
	    }*/
	//System.out.print("\tLen:"+this.currentFeature.getMagnitude()+"                               ");
        
        /*try {
            //System.out.print(epics+"\t"+raprox+"\t"+conv+"\t"+currentVal+"\n");
            this.graphfile.println(items+"\t"+raprox+"\t"+conv+"\t"+currentVal);
            this.graphfile.flush();
        } catch (Exception e){
            System.out.println(e);
        }*/
        return conv;
    }
    
    private SparseVector getTrainingVector(NameSet names, DataFactory df){
        SparseVector vec = null;
        if(this.dataType==DataFactory.LINEWISE){
            vec = df.getLineVector(names, true, true, false);
        } else if(this.dataType==DataFactory.PASSAGEWISE){
            vec = df.getDocumentVector(names, true, true, false);
        } else if(this.dataType==DataFactory.COLUMNS){
            vec = df.getColumnVector(names, true);
        } else if(this.dataType==DataFactory.PREPCOLUMNS){
            vec = df.getPrepreparedColumn(true);
        } else if(this.dataType==DataFactory.CACHECOLUMNS){
            vec = df.getPrepColumnSparseCache(true);
        }
        //vec.print();
        //System.out.println(vec.length);
        return vec;
    }
    
    private SparseVector selectInitialisationVector(DataFactory df, NameSet names, int samplesize, int index){
        float[] dotsquares = new float[samplesize];
        Vector mathvectors = new Vector();
        Vector sparsevectors = new Vector();
        for(int i=0;i<samplesize;i++){
            SparseVector spvec = this.getTrainingVector(this.svd.getNames(), df);
            MathVector mvec = spvec.toMathVector();
            sparsevectors.add(spvec);
            mathvectors.add(mvec);
            for(int k=0;k<index;k++){
                MathVector fvec = this.svd.getEigenvector(k);
                if(fvec.getPublicLength()!=mvec.getPublicLength()){
                    fvec = fvec.copy();
                    fvec.setPublicLength(mvec.getPublicLength());
                }
                float dotval = fvec.dotVectors(mvec);
                MathVector dotvec = fvec.multiplyVectorByScalar(dotval);
                mvec = mvec.subtractVectors(dotvec);
            }
            for(int j=i;j>=0;j--){
                MathVector prevvec = (MathVector)mathvectors.elementAt(j);
                if(prevvec.getPublicLength()<mvec.getPublicLength()){
                    prevvec.setPublicLength(mvec.getPublicLength());
                } else if(prevvec.getPublicLength()>mvec.getPublicLength()){
                    System.out.println("Error! New data vector shorter than previous.");
                }
                float newdot = mvec.dotVectors(prevvec);
                dotsquares[i]+=newdot*newdot;
                dotsquares[j]+=newdot*newdot;
            }
        }
        int bestind = 0;
        float bestval = 0;
        for(int i=0;i<samplesize;i++){
            if(dotsquares[i]>bestval){
                bestval = dotsquares[i];
                bestind = i;
            }
        }
        return (SparseVector)sparsevectors.elementAt(bestind);
    }
    
    private void orthogonaliseFeature(int index, MathVector cumulativeAdjuster){
        if(cumulativeAdjuster!=null && cumulativeAdjuster.getPublicLength()>0){
            MathVector nonorthogonalPart = null;
            float[] harray = cumulativeAdjuster.getArray();
            for(int i=0;i<index;i++){
                MathVector thisF = this.svd.getEigenvector(i);
                MathVector thisFnorm = thisF.euclideanNormalise();
                if(nonorthogonalPart==null){
                    nonorthogonalPart = thisFnorm.multiplyVectorByScalar(harray[i]);
                } else {
                    nonorthogonalPart = nonorthogonalPart.addVectors(thisFnorm.multiplyVectorByScalar(harray[i]));
                }
            }
            MathVector newFeature = this.currentFeature.subtractVectors(nonorthogonalPart);
            this.svd.setEigenvector(newFeature, index);
        } else {
            this.svd.setEigenvector(currentFeature, index);
        }
        this.currentFeature = null;
    }
    
    private float updateValuesAndTrainingExamples(int index, int trainingExamples){
        int n = trainingExamples;
        Vector trVec = svd.getTrainingExamples();
        if(index<trVec.size()){
            n += ((Integer)trVec.elementAt(index)).intValue();
            trVec.set(index, new Integer(n));
        } else {
            trVec.add(index, new Integer(n));
        }
        float length = this.svd.getEigenvector(index).getMagnitude();
        float value = length/n;
        this.svd.setEigenvalue(value, index);
        //if(this.substrategy==this.UPTO){
        if(false){
            for(int i=0;i<index;i++){
                length = this.svd.getEigenvector(i).getMagnitude();
                int tr = ((Integer)this.svd.getTrainingExamples().elementAt(i)).intValue();
                tr+=trainingExamples;
                this.svd.getTrainingExamples().set(i, new Integer(tr));
                float val = length/tr;
                this.svd.setEigenvalue(val, i);
            }
        }
        return value;
    }
    
    private void extendVectorsSize(int newSize, float initialValue){
        if (this.lastlen<newSize){
            for(int i=0;i<this.svd.getVectorCount();i++){
                MathVector v = this.svd.getEigenvector(i);
                for(int j=v.getPublicLength();j<newSize;j++){
                    v.addValue(initialValue);
                }
                this.svd.setEigenvector(v, i);
            }
            for(int j=this.currentFeature.getPublicLength();j<newSize;j++){
                this.currentFeature.addValue(initialValue);
            }
            this.lastlen = newSize;
        }
    }
    
    private void learnSparse(SparseVector vec, int index){
        //MathVector currentFeature = this.svd.getEigenvector(index);
        
        MathVector prevFDots = new MathVector();
        for(int i=0;i<index;i++){
            MathVector thisS1 = this.svd.getEigenvector(i);
            MathVector thisS1norm = thisS1.euclideanNormalise();
            prevFDots.addValue(thisS1norm.dotVectors(vec));
        }

        if(currentFeature==null || currentFeature.getPublicLength()<1){
            //this.svd.setEigenvector(vec.toMathVector(), index);
            currentFeature = vec.toMathVector();
            this.cumulativeAdjuster = prevFDots;
        } else {
            this.extendVectorsSize(vec.length, 0);

            float numerator = currentFeature.dotVectors(vec);
            float hmag = 0;
            if(this.cumulativeAdjuster!=null && this.cumulativeAdjuster.getPublicLength()>0){
                hmag = this.cumulativeAdjuster.getMagnitude();
                numerator -= this.cumulativeAdjuster.dotVectors(prevFDots);
            }
            float gmag = currentFeature.getMagnitude();
            float denominator = (float)Math.sqrt((gmag*gmag)-(hmag*hmag));
            float sc = numerator/denominator;

            SparseVector scaledvec = vec.multiplyVectorByScalar(sc);
            currentFeature.addVectorsFlexible(scaledvec);
            //this.svd.setEigenvector(currentFeature, index);
            if(this.cumulativeAdjuster!=null && this.cumulativeAdjuster.getPublicLength()>0){
                this.cumulativeAdjuster = this.cumulativeAdjuster.addVectors(prevFDots.multiplyVectorByScalar(sc));
            }
        }
        //return currentFeature;
    }
}
    
    /*
    private float learnTrigramOneLV(MathVector vec, int index, float lr){
        this.extendVectorsSize(this.svd.getS1(), vec.getPublicLength(), this.init);
        this.extendVectorsSize(this.svd.getS2(), vec.getPublicLength(), this.init);
        
        for(int i=0;i<index;i++){
            MathVector thisS1 = (MathVector)this.svd.getS1().elementAt(i);
            MathVector thisS1Norm = thisS1.euclideanNormalise();
            float subtractS1Size = thisS1Norm.dotVectors(vec);
            vec = vec.subtractVectors(thisS1Norm.multiplyVectorByScalar(subtractS1Size));
        }
        //add vec at this point to a running total
        
        //Calculate the scaled vector to add to s1
        MathVector thisS1 = (MathVector)this.svd.getS1().elementAt(index);
        MathVector thisS1Norm = thisS1.euclideanNormalise();
        float s1Term = thisS1Norm.dotVectors(vec);
        MathVector s1Scaled = vec.multiplyVectorByScalar(s1Term*lr);
            
        //Add the scaled inputs to the appropriate LVs and update
        MathVector newS1 = thisS1.addVectors(s1Scaled);
        this.svd.getS1().set(index, newS1);
        this.svd.getS2().set(index, newS1);
           
        float length1 = newS1.getMagnitude();
        return length1;
    }
    
    private float learnTrigramsUpTo(MathVector vec, int max, float lr){
        this.extendVectorsSize(this.svd.getS1(), vec.getPublicLength(), this.init);
        this.extendVectorsSize(this.svd.getS2(), vec.getPublicLength(), this.init);
        
        if(max>this.svd.getS1().size()) max=this.svd.getS1().size();
        
        for(int i=0;i<=max;i++){
            MathVector thisS1 = (MathVector)this.svd.getS1().elementAt(i);
            MathVector thisS1Norm = thisS1.euclideanNormalise();
            float s1Term = thisS1Norm.dotVectors(vec);
            
            MathVector s1Scaled = vec.multiplyVectorByScalar(s1Term*lr);
            MathVector newS1 = thisS1.addVectors(s1Scaled);
            this.svd.getS1().set(i, newS1);
            this.svd.getS2().set(i, newS1);
            
            //Subtract projection of new feature vectors from input vectors
            MathVector newS1Norm = newS1.euclideanNormalise();
            float sub1 = newS1Norm.dotVectors(vec);
            vec = vec.subtractVectors(newS1Norm.multiplyVectorByScalar(sub1));
        }
        
        return 0;
    }*/
    
    
