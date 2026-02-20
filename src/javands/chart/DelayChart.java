/*
 * Sorry about this one, it's a bit of a Kludge. It was hacked together
 * by editing the DecayChart code to save time and effort.
 */

package javands.chart;
import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.ensdf.EnsdfTableData;
import ensdfparser.nds.latex.LatexWriter;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
/**
 *
 * @author Jeremie Choquette
 * revised by Jun
 */

public class DelayChart extends BaseChart{
    private float defaultChartWidth;//include arrow width but not include widths of the parent level and side table
    private float defaultChartHeight;
    private float aShrink;
    private float CM_TO_POINT=28.3464567f;
    float z=0;
    /**draws a decay chart*/
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public int drawChart(java.io.Writer out,EnsdfTableData etd,DrawingControl drw)throws Exception{
    	ENSDF ens=etd.getENSDF();
    	dc=drw;
    	
        float pageWidth;
        float pageHeight;
        float moveQ=0;
        if(drw.isPortrait()){pageWidth=16.5f;pageHeight=20.0f;}
        else{pageWidth=22.0f;pageHeight=16.5f;}
        
        //override default width and height
        //height and width in DrawingControl is set by user, -1 if not set
        if(drw.getWidth()>0 && drw.getHeight()>0){
        	pageWidth=drw.getWidth();
        	pageHeight=drw.getHeight();
        }
        
        moveQ=drw.moveQ()*CM_TO_POINT;

        /*********diagram parameters**********/
        /**/defaultChartWidth=pageWidth*CM_TO_POINT-120;       /**/
        /**/defaultChartHeight=pageHeight*CM_TO_POINT-140;      /**/
        /**/float miny=7;                  /**/
        /**/float LEFTMAR=30;              /**/
        /**/float RIGHTMAR=35;             /**/
        /**/float slopemar=5;              /**/
        /**/float minlevd=3;               /**/
        /**/float pad=5;                   /**/
        /**/float eigap=30;                /**/
        /**/int ngamperpage;               /**/
        /**/if(pageHeight<pageWidth)ngamperpage=20;         /**/
        /**/else ngamperpage=10;           /**/
        /**/int ndpperpage=22;             /**/
        /**/float arrowH=20;               /**/
        /**/float arrowW=10;               /**/
        /*************************************/

        int maxLengthJPI=0;
        int maxLengthES=0;
        
        int nMaxgamperpage=ngamperpage;
        
        boolean atflag=false;
        boolean andflag=false;
        float rescaleW=drw.rescaleW();
        float rescaleH=drw.rescaleH();
        Vector<String> SubTitle=new Vector<String>();
        SubTitle=drw.subtitle();
        boolean supFlag=drw.supFlag();
        boolean supPN=drw.supPN();

        Vector<Gamma> GammaT = new Vector<Gamma>();
        Vector<DParticle>DPT = new Vector<DParticle>();
        Gamma g;
        Level l;
        String delaytype,ptype;
        int PN;
        
        if(!ens.norm().OS().isEmpty())
        	PN=Integer.valueOf(ens.norm().OS());
        else if(ens.norm().implicitPN()>=0)
        	PN=ens.norm().implicitPN();
        else
        	PN=0;

        ensdfparser.ensdf.Nucleus midNuc=new ensdfparser.ensdf.Nucleus();

        //Load up levels

        int reorder=1;//by default, reorder=0, gamma energies of lines increase from left to right in level scheme, as index increases
        //reorder=1, print order is opposite, for this case, gammas of each level need to be sorted with energy decreases as index increases
        
        for(int i=0;i<ens.nLevels();i++){
        	int n=ens.levelAt(i).nGammas();
            for(int j=0;j<n;j++){
            	
            	int index=(1-reorder)*j+reorder*(n-1-j);
            	
            	Gamma gam=ens.levelAt(i).gammaAt(index);
                if(gam.FLI()>-1 || (gam.ES().length()>0 && !Str.isNumeric(gam.ES())))
                	GammaT.addElement(gam);
            }

        }
        
        
        //DP order
        //1. increasing level energy
        //2. increasing dp energy from the same level
        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nDParticles();j++){
            	DParticle dp=ens.levelAt(i).DParticleAt(j);
                if(dp.EIF()>=0 || !dp.isDelayed())//NOTE (12/08/2020): no intermediate level for prompt particle decay, therefore no drawing
                	DPT.addElement(ens.levelAt(i).DParticleAt(j));
            }
        }
        
        /*
        //DP order: increasing energy of the intermediate levels
        DParticle[] tempDPs=new DParticle[DPT.size()];
        for(int i=0;i<DPT.size();i++)
        	tempDPs[i]=DPT.elementAt(i);

        Arrays.sort(tempDPs,new DelayComparatorByEI());       
        DPT=new Vector<DParticle>();
        for(int i=0;i<tempDPs.length;i++)
        	DPT.addElement(tempDPs[i]);
        */
        
        //delayed type, type=' ' means particle decay with no delay
        delaytype=ens.delayType();

    	String ts=ens.decayTypeInDSID();
    	Nucleus pNuc=ens.parentAt(0).nucleus();
    	int pZ=pNuc.Z();
    	int pA=pNuc.A();
    	if(ts.startsWith("B+")||ts.startsWith("EC"))
    		midNuc.setZA(pZ-1,pA);
    	else if(ts.startsWith("B-"))
    		midNuc.setZA(pZ+1,pA); 
    	else if(ts.startsWith("A"))
    		midNuc.setZA(pZ-2,pA-4);
    	
        if(ens.DPType().equalsIgnoreCase("P")){
        	ptype="p";           	        	
        }else if(ens.DPType().equalsIgnoreCase("N")){
        	ptype="n";   	
        }else if(ens.DPType().equalsIgnoreCase("A")){
        	ptype="$\\alpha$";    	
        }else if(ens.DPType().equalsIgnoreCase("T")){
        	ptype="t";    	
        }else if(ens.DPType().equalsIgnoreCase("D")){
        	ptype="d";    	
        }else
        	ptype=ens.DPType().toLowerCase();
        
        //System.out.println("DelayType="+delaytype);
        
        Gamma[] TEMP=new Gamma[GammaT.size()];
        for(int i=0;i<GammaT.size();i++)
        	TEMP[i]=GammaT.elementAt(i);
        
        Arrays.sort(TEMP,new GammaComparator());
        
        GammaT=new Vector<Gamma>();
        for(int i=0;i<TEMP.length;i++)
        	GammaT.addElement(TEMP[i]);



        float maxI=0;
        for(int i=0;i<GammaT.size();i++){
            g=(Gamma)GammaT.elementAt(i);
            if(!g.RIS().isEmpty()&&Str.isNumeric(g.RIS())&&Float.valueOf(g.RIS())>maxI){
                maxI=Float.valueOf(g.RIS());
            }
        }

        while(DPT.size()>5&&DPT.size()%ndpperpage<5&&DPT.size()%ndpperpage>0)
        	ndpperpage++;

        writeHead(out);
        
        //System.out.println("In DelayChart: line 161: DPT.size="+DPT.size()+" GammaT.size="+GammaT.size()+" ngaperpage="+ngamperpage+" ndpperpage="+ndpperpage);
        
        //Writes each figure
        
        int start=0;
        int end=0;
        boolean moveToNextPage=false;
        boolean moveToThisPage=false;
        boolean readNewGammaLevel=true;
        int ngcurrent=-1;//number of gammas of current level in current page
        int ngrest=-1;//number of the rest gammas of current level after current page
        int npage=0;
        
        for(int sec=0; sec<DPT.size()&&DPT.size()>=1;sec+=ndpperpage){
            Vector<Gamma> Gammas = new Vector<Gamma>();
            Vector<Level> Levels = new Vector<Level>();
            Vector<DParticle> DPs = new Vector<DParticle>();
            
            start=end;
            end=start+ngamperpage;
            
            //debug
            //System.out.println("In DelayChart: line 173: start="+start+" end="+end+" ngammaperpage="+ngamperpage);
            
            //check if the level of last transition has other transitions that are in next page
            //if number of such transitions in next page is greater half of total transitions of this level
            //then also move transitions of this level in this page to next page, otherwise move transitions
            //of this level in next page to this page.
            Gamma lastGamma;
            
            if(GammaT.size()>0){
                if(end<=GammaT.size())
                	lastGamma=GammaT.elementAt(end-1);
                else
                	lastGamma=GammaT.lastElement();
                
                Level lastGammaLevel=ens.levelAt(lastGamma.ILI());
                int index=lastGammaLevel.GammasV().indexOf(lastGamma);
                int ntotal=lastGammaLevel.GammasV().size();
                
                index=(ntotal-1-index)*reorder+index*(1-reorder);
                
                ////////////
                if(readNewGammaLevel){
                    ngcurrent=index+1;//number of gammas of current level in current page
                    ngrest=ntotal-ngcurrent;//number of the rest gammas of current level after current page
                }
                
                int nvacancy=nMaxgamperpage-ngamperpage;
                if(nvacancy<=0)
                	nvacancy=1;
                
                if(ngcurrent>0 && ngrest>0){
                    if(ngcurrent<=(ngamperpage/4)){
                    	if(ngcurrent>(ntotal/2) && ngrest<=nvacancy){
                    		moveToThisPage=true;
                    		moveToNextPage=false;
                    	}
                    	else{
                    		moveToNextPage=true;
                    		moveToThisPage=false;
                    	}
                    }
                    else if(ngcurrent<=(ngamperpage/2)){
                    	if(ngrest<=nvacancy){
                    		moveToThisPage=true;
                    		moveToNextPage=false;
                    	}
                    	else{
                    		moveToNextPage=true;
                    		moveToThisPage=false;
                    	}
                    }
                    else{
                    	if(ngrest<=nvacancy){
                    		moveToThisPage=true;
                    		moveToNextPage=false;
                    	}
                    	else{
                    		moveToNextPage=false;
                    		moveToThisPage=false;
                    	}
                    }
                }
                
                if(moveToNextPage){
                	end=end-ngcurrent;
                	ngcurrent=0;
                	ngrest=ntotal;
                }else if(moveToThisPage){
                	end=end+ngrest;
                	ngcurrent+=ngrest;
                	ngrest=0;
                	
                }
                
                
                {//no moving should be done, just split lines at end=start+ngamperpage
                	
                	//for next page
                	if(ngrest<ngamperpage)
                		readNewGammaLevel=true;
                	else{//all lines in next page still from the same level
                		readNewGammaLevel=false;
                		ngcurrent=ngamperpage;//for next page
                		ngrest=ngrest-ngamperpage;//for next page
                	}
                }
                ////////////////
            }else
            	end=0;
             
            for(int i=start;i<end&&i<GammaT.size();i++){
                Gammas.addElement(GammaT.elementAt(i));
            }
            
            int nRI=0;//count the gammas with intensities
            for(int i=0;i<Gammas.size();i++){
                Gamma addgam=new Gamma();
                addgam = (Gamma)Gammas.elementAt(i);
                
                if(!Levels.contains(ens.levelAt(addgam.ILI())))
                	Levels.addElement(ens.levelAt(addgam.ILI()));
                if(addgam.FLI()>=0 && !Levels.contains(ens.levelAt(addgam.FLI())))
                	Levels.addElement(ens.levelAt(addgam.FLI()));
                
                if(addgam.RIS().length()>0)
                	nRI++;
            }

            for(int i=sec;i<sec+ndpperpage&&i<DPT.size();i++){
                DPs.addElement(DPT.elementAt(i));
                if(!Levels.contains(ens.levelAt(DPT.elementAt(i).getLEVI())))
                	Levels.addElement(ens.levelAt(DPT.elementAt(i).getLEVI()));
            }

            
            Level[] TEMPL=new Level[Levels.size()];
            for(int i=0;i<Levels.size();i++)
            	TEMPL[i]=Levels.elementAt(i);
            
            Arrays.sort(TEMPL,new LevelComparator());
            
            Levels=new Vector<Level>();
            for(int i=0;i<TEMPL.length;i++)
            	Levels.addElement(TEMPL[i]);

            
            if(rescaleH==-1){
                height=defaultChartHeight;
                if((float)DPs.size()/(float)ndpperpage<0.5)height=height*0.5f;
                if((float)DPs.size()/(float)ndpperpage<0.25)height=height*0.5f;
                if((float)DPs.size()/(float)ndpperpage<0.13)height=height*0.5f;
            }
            else height=defaultChartHeight*rescaleH;
            if(rescaleW==-1){
                width=defaultChartWidth;
                if((float)Gammas.size()/(float)ngamperpage<0.5)width=width*0.9f;
                if((float)Gammas.size()/(float)ngamperpage<0.15)width=width*0.8f;
            }
            else {width=defaultChartWidth*rescaleW;}
            aShrink=1;

            if(delaytype.equals("B-"))
            	z=eigap*aShrink+70;
            else 
            	width-=70+eigap*aShrink;

            if(height>defaultChartHeight) height=defaultChartHeight;
            if(width>defaultChartWidth) width=defaultChartWidth;
            
            float[] allLevelE = new float[Levels.size()+DPs.size()];//energies for final daughter levels+intermediate daughter levels
            float[] allLevelY = new float[Levels.size()+DPs.size()];//y pos
            float[] middleLevelLabelY = new float[DPs.size()];//could be duplicate middle level since one level could have multiple delayed particle
            float[] finalLevelLabelY = new float[Levels.size()];//final daughter level label y pos
            float[] GammaX = new float[Gammas.size()];

            
            for(int i=0;i<Levels.size();i++)
            	allLevelE[i]=Levels.elementAt(i).EF();
            
            //find the dp that has the lowest and highest intermediate level energy
            int minDPIndex=0;
            int maxDPIndex=0;
            float minEIF=0;
            float dpEIF=0;
            float dpEF=0;
            for(int i=0;i<DPs.size();i++){
            	dpEIF=DPs.elementAt(i).EIF();
            	if(dpEIF<0)
            		continue;
            	
            	if(dpEIF<DPs.elementAt(minDPIndex).EIF())
            		minDPIndex=i;
            	if(dpEIF>DPs.elementAt(maxDPIndex).EIF())
            		maxDPIndex=i;           	
            }
            
            
            //reset the lowest intermediate level energy for drawing purpose
            DParticle minDP=DPs.elementAt(minDPIndex);
            float dpE=minDP.EF();
            if(minDP.ES().trim().length()==0){//if dp has empty energy record, use ERPS() fake energy set in Level.java
            	dpE=minDP.ERPF();
            }
            
            float offsetE=dpE;//offset of the lowest intermediate level relative to its corresponding final level
            float maxEIGap=DPs.elementAt(maxDPIndex).EIF()-DPs.elementAt(minDPIndex).EIF();
            
            float minOffsetE=maxEIGap/15;
            if(offsetE<minOffsetE)
            	offsetE=minOffsetE;
            
            minEIF=ens.levelAt(DPs.elementAt(minDPIndex).getLEVI()).EF()+offsetE;           
          
            if(!delaytype.trim().isEmpty()){
                for(int i=0;i<DPs.size();i++)
                	allLevelE[i+Levels.size()]=minEIF+(DPs.elementAt(i).EIF()-DPs.elementAt(minDPIndex).EIF());
            }


            //Format the Y coordinates of the levels
            allLevelY=findLevelY(allLevelE, height);

            
            for(int i=0;i<finalLevelLabelY.length;i++){
            	finalLevelLabelY[i]=allLevelY[i];
            	//debug
            	//System.out.println("finalLevelY"+i+"="+allLevelY[i]);
            }
            
            for(int i=0;i<middleLevelLabelY.length;i++){
            	middleLevelLabelY[i]=allLevelY[i+finalLevelLabelY.length];
            	//debug
            	//System.out.println("midlleLevelY"+(i+finalLevelLabelY.length)+"="+allLevelY[i+finalLevelLabelY.length]);
            }
            
            //Format the Y coordinates of the level labels
            finalLevelLabelY=findLevelLabelY(finalLevelLabelY, miny,false);
            middleLevelLabelY=findLevelLabelY(middleLevelLabelY, miny,true);
            
            float minMidLevelLabelY=1000;
            for(int i=0;i<middleLevelLabelY.length;i++){
                for(int j=i+1;j<middleLevelLabelY.length;j++){
                    if(DPs.elementAt(i).EIS().equals(DPs.elementAt(j).EIS()))
                    	middleLevelLabelY[j]=middleLevelLabelY[i];
                }
                
                if(middleLevelLabelY[i]<minMidLevelLabelY)
                	minMidLevelLabelY=middleLevelLabelY[i];
            }

            
            //reset leftmar and rightmar based on the maxLengthJPI and maxLengthES
            maxLengthES=0;
            maxLengthJPI=0;
            float leftmar=LEFTMAR;
            float rightmar=RIGHTMAR;
            
            for(int i=0;i<Levels.size();i++){
            	l=Levels.get(i);           
            	if(l.ES().length()>maxLengthES)            	
            		maxLengthES=l.ES().length();           
            	if(l.JPiS().length()>maxLengthJPI)         	
            		maxLengthJPI=l.JPiS().length();
            }
            if(maxLengthJPI*4>leftmar)//assue 1 char=4 point
            	leftmar=maxLengthJPI*4;
            if(maxLengthES*4>rightmar)
            	rightmar=maxLengthES*4;
        
            
            float minWidth=rightmar+leftmar+2*slopemar+2*pad+20+z+(Gammas.size()-1)*pad;
            if(width<minWidth)
            	width=minWidth;
            
            //Format the X coordinates of the gammas
            GammaX=findGammaX(Gammas.size(), (width-rightmar-leftmar-2*slopemar-2*pad-20-z), pad);


            Vector Tics = new Vector();
            Gamma t;
            for(int k=0;k<GammaX.length;k++){
                t=(Gamma)Gammas.elementAt(k);
                for(int i=0;i<allLevelY.length-1;i++){
                    for(int j=i;j<allLevelY.length-1&&allLevelY[j+1]-allLevelY[j]<=minlevd&&j-i<3;j++){
                        
                    	if(j+1<Levels.size() && i<Levels.size()) 
                    		l=(Level)Levels.elementAt(j+1);
                    	else
                    		break;
                    	
                        if(l.getIndex()>=t.ILI())break;
                        if(ens.levelAt(t.FLI())==Levels.elementAt(i)){
                            Tics.addElement(allLevelY[j+1]);
                            Tics.addElement(GammaX[k]);
                            Tics.addElement("UP");
                            Tics.addElement((3-(j-i)));
                        }
                    }
                }

                for(int i=allLevelY.length-1;i>0;i--){
                    for(int j=i;j>1&&allLevelY[j]-allLevelY[j-1]<=minlevd&&j-i<2;j--){
                    	if(j-1<Levels.size() && i<Levels.size())
                    		l=(Level)Levels.elementAt(j-1);
                    	else
                    		break;
                        
                        if(l.getIndex()<=t.FLI())break;
                        if(ens.levelAt(t.ILI())==Levels.elementAt(i)){
                            Tics.addElement(allLevelY[j-1]);
                            Tics.addElement(GammaX[k]);
                            Tics.addElement("DOWN");
                            Tics.addElement((2-(i-j)));
                        }
                    }
                }
            }
            writeFigureHead(out,(sec/ndpperpage));
            out.write("ahlength:=4;\nahangle:=22;\n");

            
            float originalScale=0.5f;
            float scale=0.5f;
            boolean isIsomer=false;
            
            for(int i=0;i<Levels.size();i++){
                l=(Level)Levels.elementAt(i);
                
                isIsomer=false;
                if(l.EF()==0){ 
                	scale=2f;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                else if(l.EF()>0 && l.T12D()>1E-9 && (l.DT12S().length()==0 || l.DT12S().charAt(0)!='L')){//isomer
                	scale=1.2f;
                	isIsomer=true;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                
                out.write("draw ("+z+","+finalLevelLabelY[i]+")--("+(z+leftmar)+","+finalLevelLabelY[i]+")--("+(z+leftmar+slopemar)+","+allLevelY[i]+")-");
                for(int j=0;j<Tics.size();j+=4){
                    if(Float.valueOf(Tics.elementAt(j).toString())==allLevelY[i]){
                        if(Tics.elementAt(j+2)=="UP"){
                            out.write("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                        else if(Tics.elementAt(j+2)=="DOWN"){
                            out.write("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar)+z)+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                    }
                }
                out.write("-("+(width-(rightmar+slopemar))+","+allLevelY[i]+")--("+(width-rightmar)+","+finalLevelLabelY[i]+")--("+width+","+finalLevelLabelY[i]+")");
                if(l.q().equals("?")||l.q().equals("S"))out.write(" dashed evenly");
                out.write(";\n");
                
                if(scale>originalScale){
                	scale=originalScale;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                
                if(l.EF()==0){
                    out.write("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+(z)+","+(finalLevelLabelY[i])+"));\n");
                    out.write("label.ulft(btex "+levelLabelSize()+Translator.value(l.ES(),l.DES())+" etex,("+width+","+(finalLevelLabelY[i])+"));\n");
                }
                else{
                	float yoff=0;
                	if(isIsomer) yoff=1f;
                	
                    out.write("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+(z)+","+(finalLevelLabelY[i]-1+yoff)+"));\n");
                    out.write("label.ulft(btex "+levelLabelSize()+Translator.plainValue(l.ES(),l.DES())+" etex,("+width+","+(finalLevelLabelY[i]-1+yoff)+"));\n");
                }

                
            }
            String label;
            String colour="";
            String temp="";
            
            float len2x_digi=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for digit. 2.2f was set originally for gammaLabelSize=6pt
            float len2x_lett=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for letter and others
            
            boolean useColor=true;
            if(PN==6 || PN==7 || nRI<2)
            	useColor=false;
            
            for(int i=0;i<GammaX.length;i++){
                g=(Gamma)Gammas.elementAt(i);
                
                boolean toDraw=false;
                boolean unknown=false;
                float yi=0,yf=0;

                if(g.ILI()>=0){
                    yi=allLevelY[Levels.indexOf(ens.levelAt(g.ILI()))];
                    yf=yi-10;
                    if(g.FLI()>=0){
                    	toDraw=true;
                    	yf=allLevelY[Levels.indexOf(ens.levelAt(g.FLI()))];
                    }else if(g.ES().length()>0){//EG="X"
                    	toDraw=true;
                    	unknown=true;
                    }	
                }
                
                if(toDraw){
                    colour="";
                    /*
                    if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.1){
                        colour="withcolor red";
                    }else if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.02){
                        colour="withcolor blue";
                    }
            	    */
                    
                    if(useColor&&!g.RIS().isEmpty()&&Str.isNumeric(g.RIS())){
                    	Float value=Float.valueOf(g.RIS());
                    	if(value>maxI*.2)
                            colour="withcolor red";
                        else if(value>maxI*.1)
                            colour="withcolor blue";
                        else
                        	colour="withcolor black";
                    }
                    else
                    	colour="withcolor black";
                    
                   
                    //break level line by adding a tiny white box at gamma and level line crossing
                    breakLevelAtCross(out,GammaX[i]+leftmar+slopemar+z,allLevelY,yi,yf);
                    
                    out.write("drawarrow ("+(GammaX[i]+leftmar+slopemar+z)+","+yi+")--("+(GammaX[i]+leftmar+slopemar+z)+","+yf+")");
                    
                    if(g.q().equals("?") || g.q().equals("S")){
                    	out.write(" dashed evenly ");
                    	hasUncertainGamma=true;
                    }
                    out.write(colour+";\n");
                    
                    if(unknown || g.FLRecord().equals("?")){//if FL=?, it means there are multiple candidates for final level,choose the cloest one for drawing
                    	out.write("label.bot(btex "+gammaLabelSize()+"? etex, ("+(GammaX[i]+leftmar+slopemar)+","+yf+"));\n");
                    }
                    
                    float labelLength=(g.ES().trim().length()+1)*len2x_digi;                   
                    label=Translator.plainValue(g.ES(),g.DES());
                    if(label.length()>g.ES().length())//for uncertainty=AP,LT,LE,GT,GE, or value is exponential
                    	labelLength+=1.5*len2x_lett;
                    
                    //label+=" "+g.MS()+" "+g.IS();
                    SDS2XDX s2x=RI(g,ens.norm());
                    temp=s2x.s();
                    
                    if(label.trim().length()>0 && (g.MS().trim().length()>0 || temp.trim().length()>0)){
                    	label+=" ";
                    	labelLength+=2*len2x_digi;
                    	
                        if(g.MS().trim().length()>0){
                        	label+="{\\ }"+g.MS();
                        	labelLength+=1*len2x_digi+(g.MS().trim().length()+1)*len2x_lett;
                        	
                            if(g.MS().trim().length()>=5)//for long string of MUL, e.g., (M1+E2), more space needed
                            	labelLength+=len2x_lett*(g.MS().trim().length()/5+1);
                        }

                        
                        if(temp.trim().length()>0){
                            String str=Translator.plainValue(s2x.s(), s2x.ds());
                        	label+="{\\ }"+str;
                        	
                        	if(str.length()>s2x.s().length())
                        		labelLength+=1.5*len2x_lett;
                        	
                        	if(temp.trim().length()<=3)
                        		labelLength+=1*len2x_digi+temp.trim().length()*len2x_digi;
                        	else
                        		labelLength+=1*len2x_digi+(temp.trim().length()+1.5)*len2x_digi;
                        	
                            if(g.flag().contains("@")){
                            	label+="@";
                            	atflag=true;
                            	labelLength+=1.5*len2x_lett;                           	
                            }else if(g.flag().contains("&")){
                            	label+="\\&";
                            	andflag=true;
                            	labelLength+=1.5*len2x_lett;                          	
                            }
                        }
                    }
                    
                    
                    label=label.trim();
                    
                    //these calculations rotate and shift a white box that goes behind the labels
                    float xpos=(GammaX[i]+leftmar+slopemar+z);
                    float ypos=allLevelY[Levels.indexOf(ens.levelAt(g.ILI()))]+0.5f;
                    String s="("+(xpos*.5+ypos*0.866)+","+(-xpos*0.866+ypos*.5)+")--("
                            +((xpos*.5+(ypos)*0.866)+labelLength)+","+(-xpos*0.866+ypos*.5)+")--("
                            +((xpos*.5+(ypos)*0.866)+labelLength)+","+(-xpos*0.866+ypos*.5+6)+")--("
                            +(xpos*.5+(ypos)*0.866)+","+(-xpos*0.866+ypos*.5+6)+")";
                    
                    s=s.replace("E", "**");//metapost does not understand exponential notation, like 1.234E5
                    
                    out.write("fill "+s+"--cycle rotated 60 withcolor white;\n");
                    
                    out.write("label.urt(btex "+gammaLabelSize()+label+" etex rotated 60,("+(xpos-6)+","+(ypos-1)+"));\n");
                    if(g.coinS().contains("C")||g.coinS().contains("?")){
                    	out.write("draw ("+xpos+","+(ypos)+") withpen pencircle scaled 4;\n");
                    	hasGCOIN=true;
                    }
                    if(g.coinS().contains("?")){
                    	out.write("draw ("+xpos+","+(ypos)+") withpen pencircle scaled 2.5 withcolor white;\n");
                    	hasUncertainGCOIN=true;
                    }
                                       
                }
            }

            //position of left-bottom corner of the table
            float tby=0;
            float tbx=0;
            int colWidth=30;//this is good for a len=8 string
            int riColWidth=colWidth;//for intensity column
            int maxLen=0;
            //find out the maximum width of intensity column
            
            
            for(int i=Levels.size()-1;i>=0;i--){
                for(int j=Levels.elementAt(i).nDParticles()-1;j>=0;j--){
                    if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                    	int n=DPs.indexOf(Levels.elementAt(i).DParticleAt(j));
                    	DParticle dp=DPs.elementAt(n);
                    	int len=0;
                        if(dp.AIS().length()>0)
                        	len=Translator.plainValueLen(dp.AIS(),dp.DAIS());
                        else
                        	len=Translator.plainValueLen(dp.RIS(),dp.DRIS());
                        
                        //debug
                        //System.out.println("In delaychart line 688 len="+len+" AIS="+dp.AIS()+"  IS="+dp.IS());
                        
                        if(len>maxLen)
                        	maxLen=len;
                    }
                }
            }
            
            if(maxLen>=8)
            	riColWidth=40;
            
            
            if(delaytype.trim().isEmpty()){
            	
                
                int p=0;
                int n=0;
                tby=0;
                tbx=0;
                
                if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+140)
                	tby=finalLevelLabelY[Levels.size()-1]+80;
                else if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+60)
                	tby=finalLevelLabelY[Levels.size()-1]+40;
                else {
                	tby=20;
                	tbx=width+eigap*aShrink+60+slopemar+rightmar+arrowW;             	
                }
                
                float slope=0;
                for(int i=0;i<Levels.size();i++){
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                        	slope=(allLevelY[Levels.size()+DPs.indexOf(Levels.elementAt(i).DParticleAt(j))]-finalLevelLabelY[i])/(eigap*aShrink);
                        	break;   
                        }
                    }
                    if(slope!=0)
                    	break;
                }
                
                if(slope>2)
                	slope=2;
                else if(slope<1)
                	slope=1;

                for(int i=Levels.size()-1;i>=0;i--){
                    int k=0;
                    float x=0,y=0;
                    for(int j=Levels.elementAt(i).nDParticles()-1;j>=0;j--){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                        	
                            n=DPs.indexOf(Levels.elementAt(i).DParticleAt(j));
                        	DParticle dp=DPs.elementAt(n);
                            Level lf=ens.levelAt(dp.getLEVI());
                        	
                            x=width+eigap*aShrink-arrowW;
                            y=allLevelY[Levels.size()+n]-arrowW*slope;
                                                       
                            //out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1)+" etex,("+tbx+","+(tby+10*(p))+"));\n");
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.ES(),dp.DES(),8)+" etex,("+(tbx+10)+","+(tby+10*(p))+"));\n");
                     
                            if(dp.AIS().length()>0)
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.AIS(),dp.DAIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            else
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.RIS(),dp.DRIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            
                            //out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.EIS(),"",8).toLowerCase()+" etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(p))+"));\n");
                            
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(lf.ES(), lf.DES(),8).toLowerCase()+" etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(p))+"));\n");
                            k++;
                            p++;
                            
                        }
                    }
                    
                    //if(k>0){
                        //out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1));
                        //if(k>1)out.write("-"+(DPT.indexOf(DPs.elementAt(n))+k));
                        //out.write(" etex,("+(width-2)+","+(LevelL[i]-5)+"));\n");
                    //}
                    
                    boolean drawarrow=false;
                    boolean isdashed=true;
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){if(DPs.contains(Levels.elementAt(i).DParticleAt(j)))drawarrow=true;if(!Levels.elementAt(i).DParticleAt(j).q().equals("?"))isdashed=false;}
                    if(drawarrow){
                        out.write("drawarrow ("+(width+arrowW)+","+(finalLevelLabelY[i]+slope*arrowW)+")--("+width+","+finalLevelLabelY[i]+")");
                        if(isdashed)out.write(" dashed evenly");
                        out.write(";\n");
                    }
                }
                
                //write inset table head and border
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E("+ptype+") etex,("+(tbx+10)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf I("+ptype+") etex,("+(tbx+10+colWidth)+","+(tby+10*(DPs.size()))+"));\n");
                
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+EI.A()+"}_{"+EI.ZS()+"}$"+EI.En()+"$_{"+EI.N()+"}^{~}$) etex,("+(tbx+70)+","+(tby+10*(DPs.size()))+"));\n");
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$) etex,("+(tbx+100)+","+(tby+10*(DPs.size()))+"));\n");
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+EI.A()+"}_{"+EI.ZS()+"}$"+EI.En()+") etex,("+(tbx+colWidth+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+") etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");

                
                //out.write("draw ("+(tbx-5)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+130)+","+(tby+10*(DPs.size()-0.5))+");\n");
                //out.write("draw ("+(tbx-8)+","+(tby-8)+")--("+(tbx+133)+","+(tby-8)+")--("+(tbx+133)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby-8)+");\n");
                
                //draw table border
                out.write("draw ("+(tbx+10)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+10+colWidth*2+riColWidth)+","+(tby+10*(DPs.size()-0.5))+");\n");
                out.write("draw ("+(tbx+8)+","+(tby-8)+")--("+(tbx+10+colWidth*2+riColWidth+3)+","+(tby-8)+")--("+(tbx+10+colWidth*2+riColWidth+3)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby-8)+");\n");

                //write nucleus name
                //out.write("label(btex $^{"+EI.A()+"}_{"+EI.ZS()+"}$"+EI.En()+"$_{"+EI.N()+"}^{~}$ etex,("+(width+eigap*aShrink+40)+","+(minMidLevelLabelY-40)+"));\n");
                out.write("label(btex $^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$ etex,("+(width/2+5)+",-20));\n");
  
                //System.out.println("In delayChart line 738: middleLevelLabelY[0]="+middleLevelLabelY[0]);            	
            }else if(!delaytype.equals("B-")){
            	
            	//draw intermediate levels
            	Vector<String> EISV=new Vector<String>();
                for(int i=0;i<DPs.size();i++){
                	if(EISV.contains(DPs.elementAt(i).EIS()))
                		continue;
                	else
                		EISV.addElement(DPs.elementAt(i).EIS());
                	
                    out.write("draw ("+(width+eigap*aShrink)+","+allLevelY[i+Levels.size()]+")--("+(width+40+eigap*aShrink)+","+allLevelY[i+Levels.size()]+")--("+(width+slopemar+eigap*aShrink+40)+","+middleLevelLabelY[i]+")--("+(width+slopemar+40+eigap*aShrink+rightmar)+","+middleLevelLabelY[i]+")");
                    if(DPs.elementAt(i).EIS().contains("("))out.write(" dashed evenly");
                    out.write(";\n");
                    out.write("label.ulft(btex "+levelLabelSize()+DPs.elementAt(i).EIS()+" etex,("+(width+40+eigap*aShrink+slopemar+rightmar)+","+(middleLevelLabelY[i]-1)+"));\n");
                    if(delaytype.equals("A")){
                        out.write("drawarrow ("+(width+eigap*aShrink+40+rightmar+slopemar+arrowW)+","+(middleLevelLabelY[i]+arrowH)+")--("+(width+40+eigap*aShrink+rightmar+slopemar)+","+(middleLevelLabelY[i])+") withpen pencircle scaled 2;\n");
                        out.write("drawarrow ("+(width+40+eigap*aShrink+rightmar+slopemar+arrowW)+","+(middleLevelLabelY[i]+arrowH)+")--("+(width+40+eigap*aShrink+rightmar+slopemar)+","+(middleLevelLabelY[i])+") withcolor white;\n");
                    }else{
                        out.write("drawarrow ("+(width+40+eigap*aShrink+rightmar+slopemar+arrowW)+","+(middleLevelLabelY[i]+arrowH)+")--("+(width+40+eigap*aShrink+rightmar+slopemar)+","+(middleLevelLabelY[i])+");\n");
                    }
                    
                    //debug
                    //System.out.println("In DelayChart line 592: sec="+sec+" i="+i+" ES="+DPs.elementAt(i).EIS());
                    //System.out.println("In delayChart line 644: middleLevelLabelY[0]="+middleLevelLabelY[0]+" "+DPs.elementAt(0).EIS());
                }
                
                int p=0;
                int n=0;
                tby=0;
                tbx=0;
                
                if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+140)
                	tby=finalLevelLabelY[Levels.size()-1]+80;
                else if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+60)
                	tby=finalLevelLabelY[Levels.size()-1]+40;
                else {
                	tby=20;
                	tbx=width+eigap*aShrink+60+slopemar+rightmar+arrowW;             	
                }
                
                float slope=0;
                for(int i=0;i<Levels.size();i++){
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                        	slope=(allLevelY[Levels.size()+DPs.indexOf(Levels.elementAt(i).DParticleAt(j))]-finalLevelLabelY[i])/(eigap*aShrink);
                        	break;   
                        }
                    }
                    if(slope!=0)
                    	break;
                }
                
                if(slope>2)
                	slope=2;
                else if(slope<1)
                	slope=1;
                
                EISV.clear();
                
                for(int i=Levels.size()-1;i>=0;i--){
                    int k=0;
                    float x=0,y=0;
                    for(int j=Levels.elementAt(i).nDParticles()-1;j>=0;j--){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                        	
                            n=DPs.indexOf(Levels.elementAt(i).DParticleAt(j));
                        	DParticle dp=DPs.elementAt(n);
                            Level lf=ens.levelAt(dp.getLEVI());
                        	
                            x=width+eigap*aShrink-arrowW;
                            y=allLevelY[Levels.size()+n]-arrowW*slope;
                            
                            if(!EISV.contains(dp.EIS())){//one intermediate level could have multiple dp                       	
                            	out.write("drawarrow ("+(width+eigap*aShrink)+","+allLevelY[n+Levels.size()]+")--("+x+","+y+")");
                            	if(DPs.elementAt(n).q().equals("?"))out.write(" dashed evenly");
                                if(ptype.equals("$\\alpha$"))out.write(" withpen pencircle scaled 2;\ndrawarrow ("+(width+eigap*aShrink)+","+allLevelY[n+Levels.size()]+")--("+x+","+y+") withcolor white");
                                out.write(";\n");
                            	EISV.addElement(dp.EIS());
                            }
                            
                            
                            
                            //out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1)+" etex,("+tbx+","+(tby+10*(p))+"));\n");
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.ES(),dp.DES(),8)+" etex,("+(tbx+10)+","+(tby+10*(p))+"));\n");
                     
                            if(dp.AIS().length()>0)
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.AIS(),dp.DAIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            else
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.RIS(),dp.DRIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.EIS(),"",8).toLowerCase()+" etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(p))+"));\n");
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(lf.ES(), lf.DES(),8).toLowerCase()+" etex,("+(tbx+10+colWidth*2+riColWidth)+","+(tby+10*(p))+"));\n");
                            k++;
                            p++;
                            
                        }
                    }
                    
                    //if(k>0){
                        //out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1));
                        //if(k>1)out.write("-"+(DPT.indexOf(DPs.elementAt(n))+k));
                        //out.write(" etex,("+(width-2)+","+(LevelL[i]-5)+"));\n");
                    //}
                    
                    boolean drawarrow=false;
                    boolean isdashed=true;
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){if(DPs.contains(Levels.elementAt(i).DParticleAt(j)))drawarrow=true;if(!Levels.elementAt(i).DParticleAt(j).q().equals("?"))isdashed=false;}
                    if(drawarrow){
                        out.write("drawarrow ("+(width+arrowW)+","+(finalLevelLabelY[i]+slope*arrowW)+")--("+width+","+finalLevelLabelY[i]+")");
                        if(isdashed)out.write(" dashed evenly");
                        if(delaytype.equals("A"))out.write("withpen pencircle scaled 2.0;\ndrawarrow ("+(width+arrowW)+","+(finalLevelLabelY[i]+slope*arrowW)+")--("+width+","+finalLevelLabelY[i]+") withcolor white");
                        out.write(";\n");
                    }
                }
                
                //write inset table head and border
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E("+ptype+") etex,("+(tbx+10)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf I("+ptype+") etex,("+(tbx+10+colWidth)+","+(tby+10*(DPs.size()))+"));\n");
                
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+EI.A()+"}_{"+EI.ZS()+"}$"+EI.En()+"$_{"+EI.N()+"}^{~}$) etex,("+(tbx+70)+","+(tby+10*(DPs.size()))+"));\n");
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$) etex,("+(tbx+100)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+midNuc.A()+"}_{"+midNuc.ZS()+"}$"+midNuc.En()+") etex,("+(tbx+colWidth+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+") etex,("+(tbx+10+colWidth*2+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");

                
                //out.write("draw ("+(tbx-5)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+130)+","+(tby+10*(DPs.size()-0.5))+");\n");
                //out.write("draw ("+(tbx-8)+","+(tby-8)+")--("+(tbx+133)+","+(tby-8)+")--("+(tbx+133)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby-8)+");\n");
                
                //draw table border
                out.write("draw ("+(tbx+10)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+10+colWidth*3+riColWidth)+","+(tby+10*(DPs.size()-0.5))+");\n");
                out.write("draw ("+(tbx+8)+","+(tby-8)+")--("+(tbx+10+colWidth*3+riColWidth+3)+","+(tby-8)+")--("+(tbx+10+colWidth*3+riColWidth+3)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby-8)+");\n");

                //write nucleus name
                out.write("label(btex $^{"+midNuc.A()+"}_{"+midNuc.ZS()+"}$"+midNuc.En()+"$_{"+midNuc.N()+"}^{~}$ etex,("+(width+eigap*aShrink+40)+","+(minMidLevelLabelY-40)+"));\n");
                out.write("label(btex $^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$ etex,("+(width/2+5)+",-20));\n");
  
                //System.out.println("In delayChart line 738: middleLevelLabelY[0]="+middleLevelLabelY[0]);
            }else{
            	//draw intermediate levels
            	Vector<String> EISV=new Vector<String>();
                for(int i=0;i<DPs.size();i++){
                	if(EISV.contains(DPs.elementAt(i).EIS()))
                		continue;
                	else
                		EISV.addElement(DPs.elementAt(i).EIS());
                	
                    out.write("draw ("+(z-40-eigap*aShrink-rightmar-slopemar)+","+allLevelY[i+Levels.size()]+")--("+(z-eigap*aShrink-slopemar-rightmar)+","+allLevelY[i+Levels.size()]+")--("+(z-rightmar-eigap*aShrink)+","+middleLevelLabelY[i]+")--("+(z-eigap*aShrink)+","+middleLevelLabelY[i]+")");
                    if(DPs.elementAt(i).EIS().contains("("))out.write(" dashed evenly");
                    out.write(";\n");
                    out.write("label.ulft(btex "+levelLabelSize()+DPs.elementAt(i).EIS()+" etex,("+(z-eigap)+","+(middleLevelLabelY[i]-1)+"));\n");
                    out.write("drawarrow ("+(z-40-eigap*aShrink-rightmar-slopemar-arrowW)+","+(allLevelY[i+Levels.size()]+arrowH)+")--("+(z-40-eigap*aShrink-rightmar-slopemar)+","+(allLevelY[i+Levels.size()])+");\n");
                    
                }
                
                
                int p=0;
                int n=0;
                tby=0;
                tbx=-160;
                if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+140){
                	tby=finalLevelLabelY[Levels.size()-1]+80;
                	tbx=z+30;                	
                }
                else if((height+60)-finalLevelLabelY[Levels.size()-1]>10*DPs.size()+60){
                	tby=finalLevelLabelY[Levels.size()-1]+40;
                	tbx=z+30;             	
                }              
                else 
                	tby=height-10*DPs.size();
                
                float slope=0;
                for(int i=0;i<Levels.size();i++){
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){slope=(finalLevelLabelY[i]-middleLevelLabelY[DPs.indexOf(Levels.elementAt(i).DParticleAt(j))])/(eigap*aShrink);break;}
                    }
                    if(slope!=0)break;
                }
                if(slope<-2)
                	slope=-2;
                else if(slope>-1)
                	slope=-1;
                
                EISV.clear();
                
                for(int i=Levels.size()-1;i>=0;i--){
                    int k=0;
                    float x=0,y=0;
                    for(int j=Levels.elementAt(i).nDParticles()-1;j>=0;j--){
                        if(DPs.contains(Levels.elementAt(i).DParticleAt(j))){
                            n=DPs.indexOf(Levels.elementAt(i).DParticleAt(j));
                            
                        	DParticle dp=DPs.elementAt(n);
                            Level lf=ens.levelAt(dp.getLEVI());
                            
                            x=z-eigap*aShrink+arrowW;
                            y=middleLevelLabelY[n]+arrowW*slope;
                            if(!EISV.contains(dp.EIS())){
                            	out.write("drawarrow ("+(z-eigap*aShrink)+","+middleLevelLabelY[n]+")--("+x+","+y+")");
                            	if(DPs.elementAt(n).q().equals("?"))out.write(" dashed evenly");
                                if(ptype.equals("$\\alpha$"))out.write(" withpen pencircle scaled 2;\ndrawarrow ("+(z-eigap*aShrink)+","+middleLevelLabelY[n]+")--("+x+","+y+") withcolor white");
                                out.write(";\n");
                            	EISV.addElement(dp.EIS());
                            }
                            
                            
                            //out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1)+" etex,("+tbx+","+(tby+10*(p))+"));\n");
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.ES(),dp.DES(),8)+" etex,("+(tbx+10)+","+(tby+10*(p))+"));\n");
                            
                            if(dp.AIS().length()>0)
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.AIS(),dp.DAIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            else
                            	out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.RIS(),dp.DRIS(),8)+" etex,("+(tbx+10+colWidth)+","+(tby+10*(p))+"));\n");
                            
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(dp.EIS(),"",8).toLowerCase()+" etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(p))+"));\n");
                            
                            out.write("label.rt(btex "+levelLabelSize()+Translator.plainValue(lf.ES(),lf.DES(),8).toLowerCase()+" etex,("+(tbx+10+colWidth*2+riColWidth)+","+(tby+10*(p))+"));\n");
                            k++;
                            p++;
                        }
                    }
                    
                    //if(k>0){
                    //    out.write("label(btex "+levelLabelSize()+ptype+"\\normalsize\\scalefont{0.4}"+(DPT.indexOf(DPs.elementAt(n))+1));
                    //    if(k>1)out.write("-"+(DPT.indexOf(DPs.elementAt(n))+k));
                    //    out.write(" etex,("+(z+2)+","+(LevelL[i]-5)+"));\n");
                    //}
                    
                    boolean drawarrow=false;
                    boolean isdashed=true;
                    for(int j=0;j<Levels.elementAt(i).nDParticles();j++){if(DPs.contains(Levels.elementAt(i).DParticleAt(j)))drawarrow=true;if(!Levels.elementAt(i).DParticleAt(j).q().equals("?"))isdashed=false;}
                    if(drawarrow){
                        out.write("drawarrow ("+(z-arrowW)+","+(finalLevelLabelY[i]-slope*arrowW)+")--("+z+","+finalLevelLabelY[i]+")");
                        if(isdashed)out.write(" dashed evenly");
                        out.write(";\n");
                    }
                }
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E("+ptype+") etex,("+(tbx+10)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf I("+ptype+") etex,("+(tbx+10+colWidth)+","+(tby+10*(DPs.size()))+"));\n");

                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+EI.A()+"}_{"+EI.ZS()+"}$"+EI.En()+"$_{"+EI.N()+"}^{~}$) etex,("+(tbx+70)+","+(tby+10*(DPs.size()))+"));\n");
                //out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$) etex,("+(tbx+100)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+midNuc.A()+"}_{"+midNuc.ZS()+"}$"+midNuc.En()+") etex,("+(tbx+10+colWidth+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");
                out.write("label.rt(btex "+levelLabelSize()+"\\bf E($^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+") etex,("+(tbx+10+colWidth*2+riColWidth)+","+(tby+10*(DPs.size()))+"));\n");

                //out.write("draw ("+(tbx-5)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+140)+","+(tby+10*(DPs.size()-0.5))+");\n");
                //out.write("draw ("+(tbx-8)+","+(tby-8)+")--("+(tbx+143)+","+(tby-8)+")--("+(tbx+133)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx-8)+","+(tby-8)+");\n");
                
                //draw table border
                out.write("draw ("+(tbx+10)+","+(tby+10*(DPs.size()-0.5))+")--("+(tbx+10+colWidth*3+riColWidth)+","+(tby+10*(DPs.size()-0.5))+");\n");
                out.write("draw ("+(tbx+8)+","+(tby-8)+")--("+(tbx+10+colWidth*3+riColWidth+3)+","+(tby-8)+")--("+(tbx+10+colWidth*3+riColWidth+3)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby+10*(DPs.size())+8)+")--("+(tbx+8)+","+(tby-8)+");\n");

                //write nucleus name
                out.write("label(btex $^{"+midNuc.A()+"}_{"+midNuc.ZS()+"}$"+midNuc.En()+"$_{"+midNuc.N()+"}^{~}$ etex,("+(z-eigap*aShrink-40)+","+(minMidLevelLabelY-40)+"));\n");
                out.write("label(btex $^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$ etex,("+(z+(width-z)/2+5)+",-20));\n");
            }


            if(delaytype.equals("B-"))
            	z=0;
            else 
            	width+=eigap*aShrink+70;
            
            
            int yoffset=60;
            if(delaytype.length()>0) {
            	if(finalLevelLabelY[finalLevelLabelY.length-1]<middleLevelLabelY[maxDPIndex])
            		finalLevelLabelY[finalLevelLabelY.length-1]=middleLevelLabelY[maxDPIndex];
            	
            }else {
            	yoffset=80;
            }
            
            //The Parent Records
            
            for(int p=0;p<ens.nParents();p++){
                //Parent Level
                out.write("pickup pencircle scaled 0.5;\n");
                if(p==0)out.write("pickup pencircle scaled 2;\n");
                out.write("draw (");
                if(delaytype.equals("B-"))
                	out.write("-90,");
                else 
                	out.write((width+30)+",");
               
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--(");
                if(delaytype.equals("B-"))
                	out.write("-30,");
                else 
                	out.write((width+90)+",");
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+");\n");

                //Arrow
                if(delaytype.trim().isEmpty()){
                    out.write("pickup pencircle scaled .5;\n");
                    //out.write("drawarrow ("+(width+30)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--("+(width+30)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH)+");\n");
                	
                }else if(delaytype.equals("A")){
                    out.write("drawarrow ("+(width+80)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--("+(width+80)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH)+") withpen pencircle scaled 2;\n");
                    out.write("pickup pencircle scaled .5;\n");
                    out.write("drawarrow ("+(width+80)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--("+(width+80)+","+(finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH)+") withcolor white;\n");
                }else{
                    out.write("pickup pencircle scaled .5;\n");
                    out.write("drawarrow (");
                    if(delaytype.equals("B-"))
                    	out.write("-30,");
                    else 
                    	out.write((width+30)+",");
                    out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--(");
                    if(delaytype.equals("B-"))
                    	out.write((arrowW-30)+",");
                    else 
                    	out.write((width+30-arrowW)+",");
                    out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH)+");\n");
                }

                Level pl=ens.parentAt(p).level();
                
                //Energy
                out.write("label.ulft(btex "+levelLabelSize()+Translator.plainValue(pl.ES(),pl.DES())+" etex, (");
                if(delaytype.equals("B-"))
                	out.write("-30,");
                else 
                	out.write((width+90)+",");
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+"));\n");

                //System.out.println("DelayedCart 1166: nl="+finalLevelLabelY.length+" final level ypos="+ finalLevelLabelY[finalLevelLabelY.length-1]+" EP="+pl.ES()+" np="+p+" yoffset="+yoffset);
                
                //JPi
                out.write("label.urt(btex "+levelLabelSize()+Translator.spin(ens.parentAt(p).level().JPiS())+" etex, (");
                if(delaytype.equals("B-"))
                	out.write("-90,");
                else 
                	out.write((width+30)+",");
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+"));\n");

                //Half-Life
                //out.write("label(btex "+levelLabelSize()+Translator.value(pl.T12VS(),pl.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(ens.parentAt(p).level().T12US())
                //		+" etex, (");
                out.write("label.urt(btex "+levelLabelSize()+Translator.printNumber(pl.T12S(),pl.DT12S(),pl.T12Unit())+" etex, (");
                                
                if(delaytype.equals("B-"))
                	out.write("-25,");
                else 
                	out.write((width+95)+",");
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+"));\n");

                //Q
                String typeS=ens.fullDSId().substring(ens.fullDSId().indexOf(" ")+1,ens.fullDSId().indexOf(" ",ens.fullDSId().indexOf(" ")+1));
                if(typeS.length()==1 && !typeS.equals("A"))
            		typeS=typeS.toLowerCase();
                else
                	typeS=Translator.process(typeS);
                
                out.write("label(btex "+levelLabelSize()+"Q");
                out.write("$_{"+typeS+"}$");
                
                String qs=ens.parentAt(p).QS();
                String dqs=ens.parentAt(p).DQS();
                if(!dc.isShowGSQValue() && ens.parentAt(p).level().EF()>0){
                	SDS2XDX qv=new SDS2XDX();
                	qv.setValues(qs, dqs);
                	
                	Level pLev=ens.parentAt(p).level();
                	String eps=pLev.ES();
                	String deps=pLev.DES();
                	if(pLev.EF()==0 && deps.isEmpty())
                		deps="0";
                	
                	qv.add(eps,deps);
                	qs=qv.S();
                	dqs=qv.DS();
                }
                     
                if(!dc.isShowQValueUnit())
                	out.write(LatexWriter.printNumber(qs, dqs,true)+" etex, (");   
                else
                	out.write(LatexWriter.printNumber(qs, dqs,"keV",true)+" etex, (");   
                
                if(delaytype.equals("B-"))
                	out.write((-60+moveQ)+",");
                else 
                	out.write((width+60+moveQ)+",");
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-7)+"));\n");

                //B.R.
                Normal norm=ens.norm();
                if(ens.nNorms()>0 && p<ens.nNorms())
                	norm=ens.normAt(p);
                
                if(norm.BRS()!=null && !norm.BRS().isEmpty()){
                	SDS2XDX BR=new SDS2XDX();
                	BR.setValues(norm.BRS(), norm.DBRS());
                	BR=BR.multiply(100.f);
                	
                	if(BR.DS().trim().length()==0 || Str.isNumeric(BR.ds()))          	
                		out.write("label(btex "+levelLabelSize()+"\\%"+typeS+"="+BR.s()+" etex, (");
                	else
                    	out.write("label(btex "+levelLabelSize()+"\\%"+typeS+Translator.plainValue(BR.s(),BR.ds())+" etex, (");


                    
                }
                else{
                    out.write("label(btex "+levelLabelSize()+"$\\%"+typeS);
                    
                    /*
                    String dpType=ens.DPType();
                    if(dpType.equals("A"))
                        dpType="\\alpha";
                    else
                        dpType=dpType.toLowerCase();
                    
                    if(delaytype.equals("B-"))
                    	out.write("\\beta^{-}"+dpType);
                    else if(delaytype.equals("EC")){
                    	String s="\\epsilon{}";
                    	//if(ens.parentAt(p).QF()>1022)
                    	//	s="("+s+"+\\beta^{+})";
                    	
                    	out.write(s+dpType);
                    }else if(delaytype.equals("B+"))
                    	out.write("\\beta^{+}"+dpType);
                    else if(delaytype.equals("A"))
                    	out.write("\\alpha{}"+dpType);
                    else{ 
                    	String s=ens.decayTypeInDSID();
                    	if(s.length()==1 && !s.equals("A"))
                    		s=s.toLowerCase();
                    	else
                    		s=Translator.process(s);
                    	
                    	out.write(s);
                    }
                    */
                    
                    out.write("$=? etex, (");
                    
                    /*
                    if(delaytype.equals("B-"))
                    	out.write((-15+arrowH)+",");
                    else if(delaytype.equals("A"))
                    	out.write((width+105)+",");
                    else 
                    	out.write((width+10-arrowH)+",");
                    
                    out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH)+"));\n");
                    */
                }
                
                if(delaytype.equals("B-"))
                    out.write((-5+arrowH)+",");
                else if(delaytype.equals("A"))
                    out.write((width+105)+",");
                else 
                    out.write((width+10-arrowH)+",");
                
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowH+10)+"));\n");
                
                out.write("drawarrow (");
                
                if(delaytype.equals("B-"))
                    out.write("-30,");
                else 
                    out.write((width+30)+",");
                
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset)+")--(");
                
                if(delaytype.equals("B-"))
                    out.write((arrowH-30)+",");
                else 
                    out.write((width+30-arrowH)+",");
                
                out.write((finalLevelLabelY[finalLevelLabelY.length-1]+p*20+yoffset-arrowW)+");\n");
            }

            out.write("label(btex $^{"+ens.parentAt(0).nucleus().A()+"}_{"+ens.parentAt(0).nucleus().ZS()+"}$"+ens.parentAt(0).nucleus().En()+"$_{"+ens.parentAt(0).nucleus().N()+"}^{~}$ etex,(");

            if(delaytype.equals("B-"))
            	out.write((-60)+",");
            else 
            	out.write((width+60)+",");
            
            out.write((finalLevelLabelY[finalLevelLabelY.length-1]+yoffset-22)+"));\n");

            
            // title of dataset
            if(dc.isShowTitle()){
                String TopTitle="";
                String tempID="";
                TopTitle+="{\\bf \\small \\underline{";
                
                //x position of the center of the chart 
                float xc=width/2;
                float tbw=120;//table width=120, see above
                
                if(tbx<0)
                	xc=(width+tbx)/2;
                else if((tbx+tbw)>width)
                	xc=(tbx+tbw)/2;
                
                	
                //System.out.println(" tbx="+tbx+" xc="+xc+" tbw="+tbw);
                
                
                if(etd.getAltID().length()>0){ 
                	tempID=etd.getAltID();
                	if(tempID.contains(":T")&&!tempID.contains(":T1/2"))
                		tempID=tempID.replace(":T", ":T1/2");
                	TopTitle+=Translator.process(tempID,true);
                }
                else {
                	tempID=ens.fullDSId();
                	if(tempID.contains(":T")&&!tempID.contains(":T1/2"))
                		tempID=tempID.replace(":T", ":T1/2");
                    TopTitle+=Translator.process(tempID,true);
                    
                    //if(ens.lineAt(1).contains("2   "))TopTitle+=Translator.process(ens.lineAt(1).substring(ens.lineAt(1).indexOf("2   ")+4).trim(),true);
                }           
     
                if(ens.DSRefS().length()>5) TopTitle+=("\\hspace{0.2in}"+Translator.process(ens.DSRefS()));    
                TopTitle+="}}";
                
                out.write("label(btex "+TopTitle+" etex,("+(xc)+","+(height+165+10*SubTitle.size())+"));\n");
                
                out.write("label(btex \\underline{Decay Scheme");
                if(sec/ndpperpage!=0)out.write(" (continued)");
                out.write("} etex,("+(xc)+","+(height+140+10*SubTitle.size())+"));\n");
                for(int i=0;i<SubTitle.size();i++)out.write("label(btex "+Translator.process(SubTitle.elementAt(i))+" etex,("+(xc)+","+(height+135+10*i)+"));\n");
                
                float tempY=height+125;
                
                if(!supPN&&(PN<8&&PN>=0 && ens.nGamWI()>0)){
                    out.write("label(btex "+otherLabelSize()+"$\\gamma$ Intensities: ");
                    switch(PN){
                        case 1:out.write("Relative I$_{(\\gamma+ce)}$");break;
                        case 2:out.write("I$_{(\\gamma+ce)}$ per 100 decays through this branch");break;
                        case 3:out.write("I$_{(\\gamma+ce)}$ per 100 parent decays");break;
                        case 4:out.write("I$_{\\gamma}$ per 100 parent decays");break;
                        case 5:out.write("Relative I$_{\\gamma}$");break;
                        case 6:out.write("Relative photon branching from each level");break;
                        case 7:out.write("\\% photon branching from each level");break;
                        case 0:
                        	out.write("Type not specified");                          	
                        	break;
                    }
                    out.write(" etex,("+(xc)+","+tempY+"));\n");
                    tempY=tempY-10;
                }
                
                //for delayed-particle intensity
                if(ens.nDPWI()>0){
                	out.write("label(btex "+otherLabelSize()+"I("+ptype+") Intensities: ");
                	
                	if(!ens.norm().NPS().isEmpty())
                	//if(ens.norm().NPD()==1)
                		out.write("I("+ptype+") per 100 parent decays");
                	else
                		out.write("Relative I("+ptype+")");
                	
                    out.write(" etex,("+(xc)+","+tempY+"));\n");
                    tempY=tempY-10;
                }
                
                if(!supFlag){               
                    if(atflag){
                    	out.write("label(btex "+otherLabelSize()+"@ Multiply placed: intensity suitably divided etex,("+(xc)+","+tempY+"));\n");
                    	tempY=tempY-10;
                    }
                    if(andflag){
                    	out.write("label(btex "+otherLabelSize()+"\\& Multiply placed: undivided intensity given etex,("+(xc)+","+tempY+"));\n");
                        tempY=tempY-10;
                    }
                }
            }
            
                        
            if(useColor || hasGCOIN || hasUncertainGCOIN){
            	//LegendChart lc=new LegendChart();
            	//lc.drawChart(out,false);
            	
            	//legend box width ~ 120pt for "-----> Ig>10%XIg^max"
            	float x=width/2+100;   //width is the actual width of drawing, not the page width
            	float y=height+70;
            	
            	if(!delaytype.equals("B-")){
            		if(width/2<120)
            			x=width/2-140;
            		else
            			x=width/2-200;
            	}
            	if(dc.isShowLegend())
            		drawLegend(out,x,y);//(x,y) is the lower-left corner of legend rectangle
            	
            	hasGCOIN=false;
            	hasUncertainGCOIN=false;
            	hasUncertainGamma=false;
            }
            
            
            writeFigureTail(out);



            widths[npage]=width+180;
            heights[npage]=height+165+10*SubTitle.size()+20+20;
            
            npage++;

        }
        writeTail(out);
        
        nPages=npage;
        float temp[];

        temp=Arrays.copyOf(widths, nPages);
        widths=new float[nPages];
        widths=Arrays.copyOf(temp,nPages);

        temp=Arrays.copyOf(heights, nPages);
        heights=new float[nPages];
        heights=Arrays.copyOf(temp,nPages);
        
        if(DPT.size()<1)
        	return 0;
        else if(DPT.size()%ndpperpage==0) 
        	return DPT.size()/ndpperpage;
        else 
        	return DPT.size()/ndpperpage+1;

    }

    /**determines the optimal level layout and spacing. If levels are too clumpy, it tries to spread them out*/
    protected float[] findLevelY(float[] LevelEs, float height){
        float[] LevelY = new float[LevelEs.length];
        float[] LevelYt = new float[LevelEs.length];
        float[] LevelT = new float[LevelEs.length];
        int[] LevelP = new int[LevelEs.length];
        //Level lx;
        float low=LevelEs[0];
        float high=0;
        for(int i=0;i<LevelEs.length;i++)if(LevelEs[i]<low) low=LevelEs[i];
        for(int i=0;i<LevelEs.length;i++)if(LevelEs[i]>high) high=LevelEs[i];
        float x;
        for(int i=0;i<LevelEs.length;i++){
            x=LevelEs[i];
            LevelY[i]=((x-low)/(high-low))*height;
        }
        for(int i=0;i<LevelEs.length;i++)LevelT[i]=LevelY[i];
        int p=0;
        for(int i=0;i<LevelEs.length;i++){
            for(int j=0;j<LevelEs.length;j++){
                if(LevelT[j]<LevelT[p])p=j;
            }
            LevelY[i]=LevelT[p];
            LevelT[p]=99999;
            LevelP[p]=i;
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

        float oldsqrave=-1;
        while(sqrave>2){
        	
        	
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
            
            float diff=Math.abs(sqrave-oldsqrave);
            if(diff/sqrave<0.01)
            	break;
            
            oldsqrave=sqrave;
        }
        for(int i=0;i<LevelEs.length;i++){
            LevelT[i]=LevelY[LevelP[i]];
        }
        return LevelT;
    }
    
    /**Determines the layout for the labels. Goes up first, putting them as low as it can, then goes back down, trying to put them flush with the last one*/
    
    protected float[] findLevelLabelY(float[] LevelY, float miny, boolean isEI){
        float[] LevelL = new float[LevelY.length];
        int p=-1;
        int t=-1;
        int max=0;
        int min=-1;
        
        if(miny<=(dc.levelLabelSize()+1.5f))
        	miny=dc.levelLabelSize()+1.5f;
        
        for(int i=0;i<LevelY.length;i++)if(LevelY[i]>LevelY[max])max=i;
        for(int i=0;i<LevelY.length;i++){
            int n=0;
            p=max;
            for(int j=0;j<LevelY.length;j++){
                if(LevelY[j]<LevelY[p]){
                    if(i==0)p=j;
                    else if(LevelY[j]>LevelY[t])p=j;
                }
            }
            t=p;
            n=min;
            if(i==0){if(isEI)LevelL[p]=LevelY[p]-3*miny;else LevelL[p]=LevelY[p];min=p;continue;}
            for(int j=0;j<LevelY.length;j++){
                if(LevelY[j]>LevelY[n]&&LevelY[j]<LevelY[p])n=j;
            }
            if(LevelY[p]-LevelL[n]>4*miny)LevelL[p]=LevelY[p]-3*miny;
            else LevelL[p]=LevelL[n]+miny;
        }
        for(int i=0;i<LevelY.length;i++){
            int n=0;
            p=min;
            for(int j=0;j<LevelY.length;j++){
                if(LevelY[j]>LevelY[p]){
                    if(i==0)p=j;
                    else if(LevelY[j]<LevelY[t])p=j;
                }
            }
            t=p;
            n=max;
            if(i==0){if(LevelL[p]<LevelY[p])LevelL[p]=LevelY[p];continue;}
            for(int j=0;j<LevelY.length;j++){
                if(LevelY[j]<LevelY[n]&&LevelY[j]>LevelY[p])n=j;
            }
            if(LevelL[p]<LevelY[p]){
                if(LevelL[n]-LevelY[p]>miny)LevelL[p]=LevelY[p];
                else LevelL[p]=LevelL[n]-miny;
            }
        }
        return LevelL;
    }

    @SuppressWarnings("rawtypes")
    //override parent function
	protected float[] findLevelY(Vector Levels, float height, float mindy){
    	return new float[0];
    }
    protected float[] findLevelLabelY(float[] LevelY, float miny){
    	return new float[0];
    }
    
    
    @SuppressWarnings("unused")
    private String makeData(String data, String dx){
        String b="";
        if(dx.equals("LT"))b+="<\\hspace{-0.05cm}";
        if(dx.equals("GT"))b+=">\\hspace{-0.05cm}";
        if(dx.equals("LE"))b+="\\leq\\hspace{-0.05cm}";
        if(dx.equals("GE"))b+="\\geq\\hspace{-0.05cm}";
        if(dx.equals("AP"))b+="\\approx\\hspace{-0.05cm}";
        data=b+Translator.exponential(data);
        return data;
    }

}

/*
 * compare intermediate level energy of two Delay object
 */
@SuppressWarnings("rawtypes")
class DelayComparatorByEI implements Comparator{
    public int compare(Object one,Object two){
        DParticle a=(DParticle)one;
        DParticle b=(DParticle)two;
        float Ea=a.EIF();
        float Eb=b.EIF();
        
        int out;
        if(Ea>Eb)out=-1;
        else if(Ea<Eb)out=1;
        else out=0;
        
        return out;
    }
}
