/*
 * LabelLayout.java
 *
 * Created on May 5, 2007, 1:03 AM
 *
 * Copyright (c) 2007 Roy Zywina
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package javands.chart;

import java.util.*;

/**
 * Controls the positioning of mid gamma labels in charts.
 * Ensures that labels will not overwrite each other or level
 * labels and will simply remove labels that can't be fitted in.
 *
 * @author Roy Zywina
 */
public class BandGammaLabelLayout {
    protected class LabelWrap{
        boolean fixed=false; // can this one move?
        boolean vertical=false;//put it vertical if always overlap with others
        float xpos=0.0f; // stored for convenience
        String label;    // stored for convenience
        float pos=0.0f; // position
        float min=0.0f; // can't go lower than...
        float max=0.0f; // can't go higher than...
        boolean show=true; // show this label?
        int col=-1;
        public boolean equals(LabelWrap l){
            return xpos==l.xpos && pos==l.pos && fixed==l.fixed &&
                show==l.show && min==l.min && max==l.max && col==l.col;
        }
    }
    Vector<LabelWrap> labelWraps;
    Vector<LabelWrap> newLabelWraps;
    float SIZE=Post.pointToCm(7.0f); // height of label text (a guess)
    
    boolean supGammaLabels=false;
    boolean showAllGammaLabels=false;
    
    
    /** Creates a new instance of LabelLayout */
    public BandGammaLabelLayout() {
        labelWraps = new Vector<LabelWrap>();
        //newLabelWraps=new Vector<LabelWrap>();
        newLabelWraps=labelWraps;
    }
    /** Creates a new instance of LabelLayout */
    public BandGammaLabelLayout(float fontsize) {
        labelWraps = new Vector<LabelWrap>();
        //newLabelWraps=new Vector<LabelWrap>();
        newLabelWraps=labelWraps;
        SIZE=fontsize;
    }
    /** Make a better gues aout text size */
    public void setTextSize(float sz){
        SIZE=sz;
    }
    /// add a label that can't move
    public void addFixedLabel(float ypos){
        LabelWrap l = new LabelWrap();
        l.pos = ypos + SIZE*0.5f;
        l.fixed=true;
        if (!labelWraps.contains(l))
            labelWraps.add(l);
    }
 
    /// add a label that can't move, level energy and JPI labels
    public void addFixedLabel(float xpos,float ypos){
        LabelWrap l = new LabelWrap();
        l.pos = ypos + SIZE*0.5f;
        l.xpos =xpos;
        l.fixed=true;
        if (!labelWraps.contains(l))
            labelWraps.add(l);
    }
    
    /// add a label that can't move
    public void addFixedLabel(float xpos,float ypos,int col){
        LabelWrap l = new LabelWrap();
        l.pos = ypos + SIZE*0.5f;
        l.xpos =xpos;
        l.col=col;
        l.fixed=true;
        if (!labelWraps.contains(l))
            labelWraps.add(l);
    }
    
    /// add a label that can be moved
    public void addLabel(String lbl,int col,float xpos,float ypos,float min,float max){
        LabelWrap l = new LabelWrap();
        l.xpos = xpos;
        l.pos = ypos;
        l.min=min;
        l.max=max;
        l.label=lbl;
        l.col=col;
        labelWraps.add(l);
    }
    /*
     * The label placement algorithm is really simple.  I test if it fits
     * nicely, if it doesn't I move it randomly.  This repeats until it 
     * either fits or the algorithm gives up and hides it.
     */
    protected void fit(LabelWrap[] LWs,int index){
        LabelWrap lw = LWs[index];
        float pad = SIZE*0.5f;
        boolean fits = false;
        int tries=0;
        int maxNTries=20;

        //debug
        //System.out.println("                         label="+lw.label);
        //System.out.println("In LabelLayout line 124: index="+index+" fits="+fits+" lw.pos="+lw.pos+" lw.max="+lw.max+" lw.min="+lw.min+" SIZE="+SIZE+" ntries="+tries);

        float step=(float)(lw.max-lw.min-SIZE)/maxNTries;
        
        for (tries=0; tries<maxNTries && !fits; tries++){
            fits=true;
            for (int x=0; x<LWs.length; x++){
                if (x==index) continue;
                
                // check for overlap
                
                //debug
                //if(lw.label.contains("126")){
                //System.out.println("In BandGammaLabelLayout line 125: label="+lw.label+" pos="+lw.pos+" LWs["+x+"]="+LWs[x].pos+" label="+LWs[x].label+" SIZE="+SIZE);
                //System.out.println("           lw.xpos="+lw.xpos+" LWs.xpos="+LWs[x].xpos+" lw.col="+lw.col+" LWs.col="+LWs[x].col+" fixed="+LWs[x].fixed+" LWs.size="+LWs.length);
                //}
                
                int col_diff=Math.abs(lw.col-LWs[x].col);
                float pos_diff=Math.abs(lw.pos-LWs[x].pos);
                //if (Math.abs(lw.pos-LWs[x].pos)<SIZE*0.5f && Math.abs(lw.xpos-LWs[x].xpos)<2*SIZE){
                
                if(col_diff==0){
                	if (pos_diff<SIZE*0.8f){
                        fits=false;
                        break;
                    }
                }else if(col_diff==1){
                    if (pos_diff<SIZE*0.5f && Math.abs(lw.xpos-LWs[x].xpos)<2*SIZE){
                        fits=false;
                        break;
                    }
                }

            }
            if (!fits){
                //lw.pos = lw.min+pad+(float)Math.random()*(lw.max-lw.min-SIZE);
            	int n=maxNTries/2+(tries/2-1)*(2*(tries%2)-1);
            	lw.pos = lw.min+pad+n*step;
                continue;
            }else
            	break;
        }
        
        //see if it can be put vertical
        //if(!fits){
        //	if((lw.max-lw.min)>SIZE){
        		//lw.vertical=true;
        		//fits=true;
        //	}
        //}
        
        //debug
        //System.out.println("In LabelLayout line 124: index="+index+" fits="+fits+" lw.pos="+lw.pos+" lw.max="+lw.max+" lw.min="+lw.min+" SIZE="+SIZE+" ntries="+tries);
        
        if (supGammaLabels || (!fits && !showAllGammaLabels))
            lw.show=false;
    }
    /* perform fitting calculation */
    public void calc(){
        LabelWrap[] LWs = new LabelWrap[labelWraps.size()];
        labelWraps.copyInto(LWs); 
        // sort by position
        for (int x=LWs.length; x>0; x--){
            for (int y=0; y<x-1; y++){
                if (LWs[y].pos > LWs[y+1].pos){
                    LabelWrap tmp = LWs[y];
                    LWs[y]=LWs[y+1];
                    LWs[y+1]=tmp;

                }
            }
        }
        for (int x=0; x<LWs.length; x++){
            if (!LWs[x].fixed)
                fit(LWs,x);
        }
        newLabelWraps = new Vector<LabelWrap>();
        for (int x=0; x<LWs.length; x++){
            if (!LWs[x].fixed && LWs[x].show){
                newLabelWraps.add(LWs[x]);
                //System.out.printf("%% %2s: %s\n",String.valueOf(x),
                //        String.valueOf(l[x].label));
            }
        }
        
    }
    /** number of fitted labels contained */
    public int sizeOfLabels(){
        return newLabelWraps.size();
    }
    public String getLabelAt(int x){
        return ((LabelWrap)newLabelWraps.get(x)).label;
    }
    public float getXPosAt(int x){
        return ((LabelWrap)newLabelWraps.get(x)).xpos;
    }
    public float getYPosAt(int x){
        return ((LabelWrap)newLabelWraps.get(x)).pos;
    }
    
    public void setShowGammaLabels(boolean supAll,boolean showAll){
    	supGammaLabels=supAll;
    	showAllGammaLabels=showAll;
    }
    
    public boolean isVerticalAt(int x){
        return ((LabelWrap)newLabelWraps.get(x)).vertical;
    }
    public Vector<LabelWrap> oldLabels(){return labelWraps;}
}
