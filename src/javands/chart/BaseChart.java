/*
 * BaseChart.java
 *
 * Created on April 3, 2007, 10:15 PM
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

import java.util.ArrayList;
import java.util.Vector;

import ensdfparser.ensdf.Gamma;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.Normal;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.util.Str;

/**
 * Chart Base class.
 *
 * @author Roy Zywina
 * revised by Jun
 */
@SuppressWarnings("unused")
public class BaseChart {
    // no two levels can be within this distance of each other
    public static float GAP_BETWEEN_LEVELS = 0.12f;
    // distance between two gammas
    public static float GAP_BETWEEN_GAMMAS = 0.17f;//for DecayChart, 
                                                   //changed from 0.12 on 1/3/2025. It is roughly 0.18 in LevelChart.java from pad/CM_TO_POINT
    
    public static float GAP_BETWEEN_GAMMAS_IN_BAND = 0.25f;
    
    // padding to the left of a level before gammas start
    public static float LEFT_PAD = 0.3f+ensdfparser.nds.config.NDSConfig.bandPad;
    // padding to the right of a level after gammas end
    public static float RIGHT_PAD = 0.6f+ensdfparser.nds.config.NDSConfig.bandPad;

    protected float width,height;//width doesnot include those for parent level and side table in decay scheme
    
    protected float[] widths,heights;//store actual total width and height of each page including those for parent level and side table in decay scheme
    protected int nPages=0;

    //static final float SMALL_FONT = 7.2f;
    //static final float NORMAL_FONT = 9.0f;
    // the font size
    public static float FONT_SIZE = Post.pointToCm(8.25f);
    
    protected DrawingControl dc;
    
    protected boolean hasGCOIN=false;
    protected boolean useColorLine=false;
    protected boolean hasUncertainGCOIN=false;
    protected boolean hasUncertainGamma=false;

    //static final String small_font = "\\newcommand{\\labelfont}{\\tiny}\n";
    //static final String font = "\\newcommand{\\labelfont}{\\fontsize{6pt}{7pt} \\selectfont}\n";
    //static final String normal_font= "\\newcommand{\\labelfont}{\\scriptsize}\n";
    
    static String[] mpost_head = {
        "verbatimtex\n",
        "%&latex\n",
        "\\documentclass[10pt]{article}\n",
        "\\usepackage{mathptmx}\n",
        "\\usepackage{color}\n",
        "\\usepackage{amsmath}\n",
        "\\usepackage{amssymb}\n",
        "\\usepackage{scalefnt}\n",// get more accurate font sizes
        "\\usepackage{textcomp}\n",//for /textminus sign
        "\\setlength{\\fboxsep}{0.01cm}\n",
        //"\\newfont{\\labelfont}{cmr6}\n",
        //"\\newfont{\\gammalabelfont}{cmr7}\n",
        //"\\newcommand{\\labelfont}{\\fontsize{6pt}{7pt} \\selectfont}\n",
        "\\newcommand{\\levellabelfont}{\\normalsize \\scalefont{0.7}}\n",
        "\\newcommand{\\gammalabelfont}{\\normalsize \\scalefont{0.5}}\n",
        "\\newcommand{\\labelfont}{\\normalsize \\scalefont{0.7}}\n",
        "\\newcommand{\\bandlabelfont}{\\normalsize \\scalefont{"+0.7*ensdfparser.nds.config.NDSConfig.bandLabelScale+"}}\n",
        "\\newcommand{\\tinylabelfont}{\\normalsize \\scalefont{0.4}}\n",
        "\\begin{document}\n",
        "etex\n",
        "color gamcol,levcol,outcol;\n",
        "gamcol = (0.8,0.,0.);\n",
        "levcol = black;\n",
        "outcol = (0.,0.3,0.8);\n",
        "picture levdash,gamdash,levdot;\n",
        "draw dashpattern(on 0.2cm off 0.1cm);\n",
        "levdash = currentpicture;\n",
        "gamdash = levdash;\n",
        "levdot  = dashpattern(on 0.02cm off 0.05cm);\n",
        "bboxmargin := 0;\n",
        // I have to rediefine the arrowhead so it will draw for short gammas
        "vardef arrowhead expr p =\n",
        "  save q,e; path q; pair e;\n",
        "  e = point length p of p;\n",
        "  q = p shifted -e cutbefore makepath(pencircle scaled 2ahlength);\n",
        "  (q rotated .5ahangle & reverse q rotated -.5ahangle -- cycle)  shifted e\n",
        "enddef;\n",
        "\n",
        //change the output name and format
        "prologues := 3;\n",
        "outputtemplate := \"%j-%c.ps\";",
        "\n"
    };
    static final String[] mpost_tail = {
        "end;\n"
    };

    /** Creates a new instance of BaseChart */
    public BaseChart() {
        width = height = 5.0f;
        nPages=0;
        widths=new float[100];
        heights=new float[100];
    }
    
    public void reset() {
        width = height = 5.0f;
        nPages=0;
        widths=new float[100];
        heights=new float[100];
    }
    
    public void writeHead(java.io.Writer out) throws java.io.IOException{
        for (int x=0; x<mpost_head.length; x++)
            out.write(mpost_head[x]);
    }
    
    public void writeHead(ArrayList<String> out) throws java.io.IOException{
        for (int x=0; x<mpost_head.length; x++)
            out.add(mpost_head[x]);
    }
    
    public void writeTail(ArrayList<String> out) throws java.io.IOException{
        for (int x=0; x<mpost_tail.length; x++)
            out.add(mpost_tail[x]);
    }
    
    public void writeTail(java.io.Writer out) throws java.io.IOException{
        for (int x=0; x<mpost_tail.length; x++)
            out.write(mpost_tail[x]);
    }
    
    public void writeFigureHead(java.io.Writer out,int fig) throws java.io.IOException{
        out.write("beginfig("+String.valueOf(fig)+");\n");
    }
    public void writeFigureTail(java.io.Writer out) throws java.io.IOException{
        out.write("endfig;\n");
    }
    
    public void writeFigureHead(ArrayList<String> out,int fig) throws java.io.IOException{
        out.add("beginfig("+String.valueOf(fig)+");\n");
    }
    public void writeFigureTail(ArrayList<String> out) throws java.io.IOException{
        out.add("endfig;\n");
    }
    
    /// set picture size in centimeters
    public void setSize(float w,float h){
        width=w;
        height=h;
    }

    public void setSizes(float w,float h){
        for(int i=0;i<widths.length;i++){
        	widths[i]=w;
        	heights[i]=h;
        }
    }
    
    public float getWidthAt(int n){return widths[n];}
    public float getHeightAt(int n){return heights[n];}
    
    public float getWidth(){return width;}
    public float getHeight(){return height;}
    public float getNPages(){return nPages;}
    
    //get the intensity to be printed in the level scheme
    protected SDS2XDX RI(Gamma g, Normal norm){
    	int PN;
    	
        if(!norm.OS().isEmpty())
        	PN=Integer.valueOf(norm.OS());
        else if(norm.implicitPN()>=0)
        	PN=norm.implicitPN();
        else
        	PN=0;      
    	
	     String NRBRS=norm.NRBRS();
	     String DNRBRS=norm.DNRBRS();
	     String NTBRS=norm.NTBRS();
	     String DNTBRS=norm.DNTBRS();
	        
	     String BRS=norm.BRS();
	     String DBRS=norm.DBRS();       
	     String NRS=norm.NRS();
	     String DNRS=norm.DNRS();
			
	     String NTS=norm.NTS();
	     String DNTS=norm.DNTS();

	     
	     if(DNRBRS.isEmpty() && norm.isGRenUnity())				
	    	 DNRBRS="0";
	     if(DNTBRS.isEmpty() && norm.isTIRenUnity())
			DNTBRS="0";		
	     if(DBRS.isEmpty() && norm.isBRUnity())
			DBRS="0";
		 if(DNRS.isEmpty() && norm.isNRUnity())
			DNRS="0";
		 if(DNTS.isEmpty() && norm.isNTUnity())
			DNTS="0";
		 
	     if(NTS.isEmpty()) {
	    	 NTS=NRS;
	    	 DNTS=DNRS;
	     }
	     if(NTBRS.isEmpty()) {
	    	 NTBRS=NRBRS;
	    	 DNTBRS=DNRBRS;
	     }
	     
    	SDS2XDX temp=new SDS2XDX();
    	SDS2XDX OCC=new SDS2XDX("1+cc");//1+cc
    	SDS2XDX RI=new SDS2XDX("RI");
    	SDS2XDX TI=new SDS2XDX("TI");

    	if(PN>7 || PN<=0){
    		temp.setValues(g.RIS(), g.DRIS());
    		return temp;
    	}
    	
    	RI.setValues(g.RIS(), g.DRIS());
    	
    	OCC.setValues("1", "0");
    	if(g.CCS().trim().length()>0){
    	    temp.setValues(g.CCS(), g.DCCS());
    		OCC=OCC.add(temp,true);
    	}
    	
    	if(g.TIS().length()>0)
    		TI.setValues(g.TIS(), g.DTIS());
    	else if(g.RIS().length()>0){
        	TI=RI.multiply(OCC); 
    	}else
    		TI.setValues(g.RIS(), g.DRIS());
    	
    	if(g.RIS().isEmpty() && g.TIS().length()>0 && g.CCS().length()>=0) {
    		RI=TI.divided(OCC);
    	}
  	
    	
  	    /*
    	//debug
    	System.out.println("In BaseChart: EG="+g.ES()+" "+g.DES());
    	System.out.println("              RI="+RI.S()+" "+RI.DS());
    	System.out.println("             OCC="+OCC.S()+" "+OCC.DS());
    	System.out.println("              TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X()+" "+TI.DX());
    	System.out.println("              NR="+norm.NRS()+" "+norm.DNRS());
    	System.out.println("              BR="+norm.BRS()+" "+norm.DBRS());
    	System.out.println("              NT="+norm.NTS()+" "+norm.DNTS());
    	System.out.println("            NRBR="+norm.NRBRS()+" "+norm.DNRBRS());
    	System.out.println("            NTBR="+norm.NTBRS()+" "+norm.DNTBRS());	
    	System.out.println("              PN="+PN);
    	*/
    	
        switch(PN){      
        case 1:
        	return TI;
        case 2:
        	if(g.TIS().length()>0 && NTS.length()>0)
        		return TI.multiply(NTS, DNTS);
        	else if(g.RIS().length()>0 && NRS.length()>0){
        		TI=RI.multiply(OCC);
        		return TI.multiply(NRS, DNRS);
        	}
        	else
        		return TI;
        case 3:
        	
        	//System.out.println(" line 296          TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X()+" "+TI.DX()+" NTBRS="+NTBRS+" "+DNTBRS);
        	
        	//PN records override N record if given
        	if(g.TIS().length()>0 && NTBRS.length()>0)
        		return TI.multiply(NTBRS, DNTBRS);
        	else if(g.RIS().length()>0 && NRBRS.length()>0 ){
        		TI=RI.multiply(OCC);
        		return TI.multiply(NRBRS,DNRBRS);
        	}

        	if(g.TIS().length()>0 && NTS.length()>0){
        		//System.out.println(" line 307          TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X()+" "+TI.DX());
        		//System.out.println("              NT="+NTS+" "+DNTS);
        		
        		TI=TI.multiply(NTS, DNTS);    
        		
        		//System.out.println(" line 265          TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X()+" "+TI.DX());
        	}
        	else if(g.RIS().length()>0 && NRS.length()>0 ){
        		TI=RI.multiply(OCC);
        		TI=TI.multiply(NRS, DNRS);      
        		
        		//System.out.println(" line 318         TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X()+" "+TI.DX()+" RI="+RI.S());
        	}

        	if(BRS.length()>0 )
        		TI=TI.multiply(BRS, DBRS);
      	
        	//System.out.println(" line 324 EG="+g.ES()+"    TI="+TI.S()+" "+TI.DS()+" TI.X="+TI.X());
        	
        	return TI;
        case 4:
        	
        	//PN records override N record if given
        	if(NRBRS.length()>0 )
        		return RI.multiply(NRBRS,DNRBRS);
        	else if(NTBRS.length()>0)
        		return RI.multiply(NTBRS, DNTBRS);
          	
        	if(NRS.length()>0 ) {
        		RI=RI.multiply(NRS, DNRS);
        	}else if(NTS.length()>0) {
        		RI=RI.multiply(NTS, DNTS);
        	}

        	if(BRS.length()>0 ) {
        		RI=RI.multiply(BRS, DBRS);
        	}       	

        	return RI;
        default:
        	break;
        }
        
		return RI;
    }
    
    String scaledFontSize(float scale){
    	return "\\normalsize \\scalefont{"+scale+"} ";
    }
    
    //LaTex normalsize=10 pt
    String bandLabelSize(){
    	if(dc!=null)
    		return scaledFontSize(dc.bandLabelSize()/10f);
    	
    	return "\\bandlabelfont ";
    }
    
    String levelLabelSize(){
    	if(dc!=null)
    		return scaledFontSize(dc.levelLabelSize()/10f);
    	
    	return "\\levellabelfont ";
    }
    
    String gammaLabelSize(){
    	if(dc!=null)
    		return scaledFontSize(dc.gammaLabelSize()/10f);
    	
    	return "\\gammalabelfont ";
    }
    
    String otherLabelSize(){
    	if(dc!=null)
    		return scaledFontSize(dc.otherLabelSize()/10f);
    	
    	return "\\labelfont ";
    }
    
    public void setDrawingControl(DrawingControl drw){
    	dc=drw;
    }
    
    /**determins the optimal level layout and spacing. If levels are too clumpy, it tries to spread them out*/
    @SuppressWarnings("rawtypes")
    //Currently, this function is used in DecayChart and LevelCart to find the Y position
    //of level lines, and it serves the same purpose as LevelLayout which is used in BandChart.
    //In LevelLayout, LevelY never goes beyond height and all levels are squeezed to fit within the given height
    //and mindy is modified accordingly, while here mindy is fixed and LevelY is not limited within height which
    //could result in the top levels having LevelY>height.
    //So, here LevelY[LevelY.length-1]>height is equivalent to minLevelGap<GIVEN_mindy in LevelLayout
	protected float[] findLevelY(Vector Levels, float height, float mindy){
        float[] LevelY = new float[Levels.size()];
        float[] LevelYt = new float[Levels.size()];
        Level lx;
        float low;
        float high;
        float x;
        
        if(Levels.size()==0)
        	return new float[0];
        
        Level li=(Level)Levels.elementAt(0);   
        low=li.EF();//for level energy like 1234.5+X, the revised energy has been set in ENSDF.java
        //if(low<0) low=li.ERF();
        
        Level lf=(Level)Levels.lastElement();
        high=lf.EF();
        
        //debug
        //System.out.println("In BaseChart line 292: low="+low+" high="+high);
        //if(high<0) high=lf.ERF();
        
        /* old
        if(Str.isNumeric(li.ES().trim()))
        	low=li.EF();
        else
        	low=li.ERF();
        
        Level lf=(Level)Levels.lastElement();
        if(Str.isNumeric(lf.ES().trim()))
        	high=lf.EF();
        else
        	high=lf.ERF();
        */
        
        /* very old
        if(li.ES().contains("+"))
        	low=li.ERF();
        else 
        	low=Float.valueOf(li.ES());


        Level lf=(Level)Levels.lastElement();
        if(lf.ES().contains("+"))
        	high=Float.valueOf(lf.ES().substring(0,lf.ES().indexOf("+")));
        else 
        	high=Float.valueOf(lf.ES());
        
        */
        
        if(Levels.size()==1){
        	LevelY[0]=high;
        	return LevelY;
        }else if(high<=low){//should not happen
        		high=low+10;
        }
        
        
        for(int i=0;i<Levels.size();i++){
            lx=(Level)Levels.elementAt(i);
            
            x=lx.EF();
            //if(x<0) x=lx.ERF();
            
            /* old
            if(Str.isNumeric(lx.ES().trim()))
            	x=lx.EF();
            else
            	x=lx.ERF();
            */
            
            /* very old
            if(lx.ES().contains("+"))
            	x=Float.valueOf(lx.ES().substring(0,lx.ES().indexOf("+")));
            else 
            	x=Float.valueOf(lx.ES());
            */
            
            LevelY[i]=((x-low)/(high-low))*height;
            if(i>0&&LevelY[i]-LevelY[i-1]<mindy)
            	LevelY[i]=LevelY[i-1]+mindy;
        }
        
        
        
        float ave=0;
        float sqrave=0;
        for(int i=0;i<LevelY.length-1;i++){
            ave+=LevelY[i+1]-LevelY[i];
        }
        ave=ave/(LevelY.length-1);
        for (int i=0;i<LevelY.length-1;i++){
            sqrave+=((LevelY[i+1]-LevelY[i])/ave)*((LevelY[i+1]-LevelY[i])/ave);
        }
        sqrave=sqrave/(LevelY.length-1);


        while(sqrave>2.0){
            for(int i=0;i<LevelY.length-1;i++){
                LevelYt[i+1]=LevelYt[i]+((float)Math.pow((double)(LevelY[i+1]-LevelY[i]),0.95));
            }

            for(int i=0;i<LevelY.length;i++){
                LevelY[i]=(LevelYt[i]/(LevelYt[LevelYt.length-1]-LevelYt[0]))*height;
            }

            for(int i=0;i<LevelY.length-1;i++){
                ave+=LevelY[i+1]-LevelY[i];
            }

            ave=ave/(LevelY.length-1);
            for(int i=0;i<LevelY.length-1;i++){
                sqrave+=((LevelY[i+1]-LevelY[i])/ave)*((LevelY[i+1]-LevelY[i])/ave);
            }

            sqrave=sqrave/(LevelY.length-1);
        }
        
        //debug
        //for(int i=0;i<LevelY.length;i++) System.out.println("In BaseChart line 392: levelY"+i+"="+LevelY[i]);
        
        return LevelY;
    }

    
    /**Determines the layout for the labels. Goes up first, putting them as low as it can, 
     * then goes back down, trying to put them flush with the last one
     * */
    //Currently, this function is used in DecayChart and LevelCart to find the Y positions
    //of level labels, JPI and energy, and it serves the same purpose as LevelLabelLayout
    //which is used in BandChart.
    protected float[] findLevelLabelY(float[] LevelY, float miny){
        float[] LevelL = new float[LevelY.length];
        LevelL[0]=0;
        //debug
        //System.out.println("miny="+miny+" dc.levelLabelsize="+dc.levelLabelSize());
        
        if(miny<=(dc.levelLabelSize()+1.5f))
        	miny=dc.levelLabelSize()+1.5f;
        
        for(int i=1;i<LevelY.length;i++){
            if(LevelY[i]-LevelL[i-1]>4*miny)
            	LevelL[i]=LevelY[i]-3*miny;
            else
            	LevelL[i]=LevelL[i-1]+miny;
        }
        
        for(int i=LevelY.length-1;i>0;i--){
            if(LevelL[i]<LevelY[i]){
                if(i<LevelY.length-1&&LevelY[i]>LevelL[i+1]-miny){
                    LevelL[i]=LevelL[i+1]-miny;
                }else{
                    LevelL[i]=LevelY[i];
                }
            }
        }
        return LevelL;
    }
    
    //return max number of consecutive level gaps <=mindy
    int checkLevelGap(float[] LevelY,float minGap){
    	int nbad=0;
    	int maxNbad=0;
    	float gap=0;
    	if(LevelY.length<=1 || minGap<=0)
    		return 0;
    	
    	for(int i=0;i<LevelY.length-1;i++){
    		gap=LevelY[i+1]-LevelY[i];
    		
    		//debug
    		//System.out.println("In BaseChart Line424: gap="+gap+" LevelYi="+LevelY[i+1]+" LevelYf="+LevelY[i]+" minGap="+minGap+" nbad="+nbad+" maxNbad="+maxNbad);
 
    		if(gap<=minGap)
    			nbad++;
    		else{
    			if(nbad>maxNbad)
    				maxNbad=nbad;    			
    				
    			nbad=0;
    		 						
    		}
    	}
    	
    	if(nbad>maxNbad)
    		maxNbad=nbad;
    	
    	return maxNbad;
    }
    
    /**Distribues the gammas horizontally*/
    //Currently, this function is used in DecayChart, DelayChart and LevelCart to find the X positions
    //of gamma lines, and it serves the same purpose as BandGammaLayout which is used in BandChart.
    protected float[] findGammaX(int nGammas, float width, float pad){
        float[] GammaX = new float[nGammas];
        
        if(nGammas>1){
            for(int i=0;i<nGammas;i++){
                GammaX[i]=(((float)i/(nGammas-1))*(width)+pad);      
            }
        }
        else if(nGammas==1){
        	//GammaX[0]=pad;
        	GammaX[0]=width/2+pad;
        	//GammaX[0]=width/2;
        }
        
        return GammaX;
    }
    
    /**Draw a color-scheme legend with lower-left corner at given position at (xl,yl) 
     * 
     * @param xul
     * @param yul
     */
    protected void drawLegend(ArrayList<String> out,float xl,float yl)throws java.io.IOException{
    	float x,y;
    	x=xl;
    	y=yl;
    	
    	//write order: from bottom to top
    	boolean toDraw=false;
    	
        if(hasUncertainGCOIN){
            out.add("draw"+P(x+20,y)+" withpen pencircle scaled 4;\n");
            out.add("draw"+P(x+20,y)+" withpen pencircle scaled 2.5 withcolor white;\n");
            out.add("label.urt(btex "+otherLabelSize()+"Coincidence (Uncertain) etex,"+P(x+45,y-5)+");\n");
            y=y+10;
            toDraw=true;
        }
        
        if(hasGCOIN){
            out.add("draw"+P(x+20,y)+" withpen pencircle scaled 4;\n");
            out.add("label.urt(btex "+otherLabelSize()+"Coincidence etex,"+P(x+45,y-5)+");\n");      
            y=y+10;
            toDraw=true;
        }
    	
        if(hasUncertainGamma){
            out.add("drawarrow "+P(x,y)+"--"+P(x+40,y)+" dashed evenly;\n");   
            out.add("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay (Uncertain) etex,"+P(x+45,y-5)+");\n"); 
            y=y+10;
            toDraw=true;
        }
             
        if(useColorLine){
            out.add("drawarrow "+P(x,y)+"--"+P(x+40,y)+" withcolor red;\n");       
            out.add("label.urt(btex "+otherLabelSize()+"I$_{\\gamma}$ $>$ 10\\%$\\times$I$_{\\gamma}^{max}$ etex,"+P(x+45,y-8)+");\n");  
            out.add("drawarrow "+P(x,y+10)+"--"+P(x+40,y+10)+" withcolor blue;\n");       
            out.add("label.urt(btex "+otherLabelSize()+"I$_{\\gamma}$ $<$ 10\\%$\\times$I$_{\\gamma}^{max}$ etex,"+P(x+45,y+2)+");\n");       
            out.add("drawarrow "+P(x,y+20)+"--"+P(x+40,y+20)+";\n");
            out.add("label.urt(btex "+otherLabelSize()+"I$_{\\gamma}$ $<$ \\phantom{0}2\\%$\\times$I$_{\\gamma}^{max}$ etex,"+P(x+45,y+12)+");\n");
            toDraw=true;
        }

 
        y=y+30;
        if(toDraw)
        	out.add("label.urt(btex "+otherLabelSize()+"Legend etex,"+P(x+30,y+3)+");\n");
        

    }
    
    protected void drawLegend(java.io.Writer out,float xl,float yl)throws java.io.IOException{
    	ArrayList<String> list=new ArrayList<String>();
    	drawLegend(list,xl,yl);
    	for(int i=0;i<list.size();i++)
    		out.write(list.get(i));
    }
    protected void breakLevelAtCross(ArrayList<String> out,float xpos,float[] LevelY,float yi,float yf)throws java.io.IOException{
    	
    	for(int i=0;i<LevelY.length;i++){
    	    float ypos=LevelY[i];
    		if(ypos>yf && ypos<yi){
    	        out.add("fill ("+(xpos-0.5)+","+(ypos-0.2)+")--("+(xpos+0.5)+","+(ypos-0.2)+")--("
    	                +(xpos+0.5)+","+(ypos+0.2)+")--("+(xpos-0.5)+","+(ypos+0.2)+")--cycle withcolor white;\n");
    	        
    		}
    			
    	}
    	
    }
    
    protected void breakLevelAtCross(java.io.Writer out,float xpos,float[] LevelY,float yi,float yf)throws java.io.IOException{
    	ArrayList<String> list=new ArrayList<String>();
    	breakLevelAtCross(list,xpos,LevelY,yi,yf);
    	for(int i=0;i<list.size();i++)
    		out.write(list.get(i));
    }
    //NOTE: leftmar and rightmar in units of cm
    public static float[] recalculateMargins(Vector<Level> Levels,float LEFTMAR,float RIGHTMAR){
    	
    	float[] margins=new float[2];//[0]-left margin, [1]-right margin
    	
        //reset leftmar and rightmar based on the maxLengthJPI and maxLengthES
        int maxLengthES=0;
        int maxLengthJPI=0;
        float leftmar=LEFTMAR;
        float rightmar=RIGHTMAR;
        
        for(int i=0;i<Levels.size();i++){
        	Level l=Levels.get(i);    
        	
        	if(l.ES().length()>maxLengthES)            	
        		maxLengthES=l.ES().length();           
        	if(l.JPiS().length()>maxLengthJPI)         	
        		maxLengthJPI=l.JPiS().length();
        }
        if(maxLengthJPI*4*NDSConfig.POINT2CM>leftmar)//assue 1 char= 4 point
        	leftmar=maxLengthJPI*4*NDSConfig.POINT2CM;
        if(maxLengthES*4*NDSConfig.POINT2CM>rightmar)
        	rightmar=maxLengthES*4*NDSConfig.POINT2CM;
        
    	margins[0]=leftmar;
    	margins[1]=rightmar;
    	
        return margins;
    }
    
    
    protected String P(float x,float y){
    	return "("+x+","+y+")";
    }
    
    
    //write a MetaPost line to output file, replacing exponential notation E to **
    //NOTE: metapost does not understand exponential notation such as 1.23E4.
    //It would interpret this as the real number 1.23, followed by the symbol
    //E, followed by the number 4.
    protected void write(java.io.Writer out,String line)throws java.io.IOException{
    	int n=line.indexOf("E");
    	if(n<=0){
    		out.write(line);
    		return;
    	}
    	
    	String newLine=line;
    	String s=line.trim();
    	if(s.indexOf("fill")==0 || s.indexOf("draw")==0)
    		newLine=line.replace("E", "**");
    	else if(s.indexOf("label")==0){
    		int n1=line.indexOf("etex");
    		if(n1>0){
        		String s1=line.substring(0, n1);
        		String s2=line.substring(n1);
        		newLine=s1+s2.replace("E", "**");
    		}   		
    	}
    	
    	out.write(newLine);
    	return;
    		
    }
}
