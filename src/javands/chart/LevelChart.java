/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javands.chart;
import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.config.NDSConfig;
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

public class LevelChart extends BaseChart{
    private float CM_TO_POINT=28.3464567f;
    private float defaultChartWidth;
    private float defaultChartHeight;
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public int drawChart(java.io.Writer out,EnsdfTableData etd,DrawingControl drw)throws Exception{
    	ENSDF ens=etd.getENSDF();
    	
    	dc=drw;
    	
        float pageWidth;
        float pageHeight;
        if(drw.isPortrait()){pageWidth=16.5f;pageHeight=20.0f;}
        else{pageWidth=22.0f;pageHeight=16.5f;}
        
        //override default width and height
        //height and width in DrawingControl is set by user, -1 if not set
        if(drw.getWidth()>0 && drw.getHeight()>0){
        	pageWidth=drw.getWidth();
        	pageHeight=drw.getHeight();
        }

        //debug
        //System.out.println("In LevelChart: line45: w="+w+" h="+h);
        
        //******diagram parameters*******
        //all lengths and widths are in units of point.
        defaultChartWidth=pageWidth*CM_TO_POINT;
        defaultChartHeight=pageHeight*CM_TO_POINT-100;
        float miny=7;
        float LEFTMAR=40;
        float RIGHTMAR=40;
        float slopemar=5;
        float minlevd=3;
        float mindy=drw.getMinY();
        float pad=5;
        int ngamperpage;
        if(pageHeight<pageWidth)ngamperpage=66;
        else ngamperpage=46;
        int nDrawings;
        
        int nMaxgamperpage=ngamperpage;
        //*******************************

        int maxLengthJPI=0;
        int maxLengthES=0;
        
        float rescaleW=drw.rescaleW();
        float rescaleH=drw.rescaleH();
        Vector<Gamma> GammaT = new Vector<Gamma>();
        int PN;
        String CPN=ens.norm().CS();//PN record col=78
        if(!ens.norm().OS().isEmpty())
        	PN=Integer.valueOf(ens.norm().OS());
        else if(ens.norm().implicitPN()>=0)
        	PN=ens.norm().implicitPN();
        else
        	PN=0;
        
        boolean atflag=false;
        boolean andflag=false;
        Vector<String> SubTitle=new Vector<String>();
        SubTitle=drw.subtitle();
        boolean supFlag=drw.supFlag();
        boolean supPN=drw.supPN();

        
        //Load up levels
        int reorder=1;//by default, reorder=0, gamma energies of lines increase from left to right in level scheme, as index increases
                      //reorder=1, print order is opposite, for this case, gammas of each level need to be sorted with energy decreases as index increases
        
        boolean isGSExist=false; //mark if there exists a ground state
        Level groundLevel=new Level();
        
        
        //NOTE: order of gammas in GammaT: gammas of top level go first, and order of gammas in each level is determined by "reorder"
        int count=0;
        
        //for(int i=ens.nLevels()-1;i>=0;i--){//order of gammas in GammaT: gammas of lower level go first, and order of gammas in each level is determined by "reorder"
        for(int i=0;i<ens.nLevels();i++){//order of gammas in GammaT: gammas of top level go first, and order of gammas in each level is determined by "reorder"       	
        	int n=ens.levelAt(i).nGammas();
        	
        	count=0;
            for(int j=0;j<n;j++){
            	       
            	//debug
            	//System.out.println("In LevelChart line 102: level"+i+"="+ens.levelAt(i).ES()+" gamma"+j+"="+ens.levelAt(i).gammaAt(j).ES());
            	//System.out.println("               A GammaT.size="+GammaT.size()+" IL="+ens.levelAt(i).gammaAt(j).ILI()+" FL="+ens.levelAt(i).gammaAt(j).FLI());     
            	
            	int index=(1-reorder)*j+reorder*(n-1-j);
            	Gamma g=ens.levelAt(i).gammaAt(index);
                if(g.FLI()>-1 || (g.ES().length()>0 && !Str.isNumeric(g.ES())) ){
                	GammaT.insertElementAt(g, count);
                	count++;
                }
                
            	//debug
            	//System.out.println("               B GammaT.size="+GammaT.size()+" "+ens.levelAt(i).gammaAt(j).FLI());     
            }
            
            if(!isGSExist && ens.levelAt(i).EF()==0){
            	isGSExist=true;
            	groundLevel=ens.levelAt(i);
            }
            
        }
        
        Gamma g;
        float maxI=0;
        for(int i=0;i<GammaT.size();i++){
            g=(Gamma)GammaT.elementAt(i);
            if(!g.RIS().isEmpty()&&Str.isNumeric(g.RIS()) && Float.valueOf(g.RIS())>maxI){
                maxI=Float.valueOf(g.RIS());
            }
        }
        //evening out the number of gammas per page
        nDrawings=GammaT.size()/ngamperpage;
        
        //debug
        //System.out.println("In LevelChart1: GammaT.size="+GammaT.size()+" ngamperpage="+ngamperpage+" ndrawings="+nDrawings);
        
        if(GammaT.size()%ngamperpage!=0)
        	nDrawings++;
        
        if(nDrawings>0) ngamperpage=(GammaT.size()/nDrawings)+1;

        writeHead(out);
        int start=0;
        int end=0;
        boolean moveToNextPage=false;
        boolean moveToThisPage=false;
        boolean readNewGammaLevel=true;
        int ngcurrent=-1;//number of gammas of current level in current page
        int ngrest=-1;//number of the rest gammas of current level after current page
        
        boolean includeGS=false;//mark if current page has ground state level
        
        int ng=ngamperpage;
        float pW=0.5f;
        float pH=1.5f;
        int NG=20;
        if(ng<NG){
        	ng=NG;
        	pH=1.0f;
        	pW=0.7f;
        	LEFTMAR=30;
        	RIGHTMAR=30;
        	
        	dc.resetLabelSize();
        	dc.scaleLevelLabelSize(1.1f);
        	dc.scaleGammaLabelSize(1.1f);            	
        }
        
        for(int sec=0; sec<nDrawings;sec++){
            Vector<Gamma> Gammas = new Vector<Gamma>();
            Vector<Level> Levels = new Vector<Level>();
            
            start=end;
            end=start+ngamperpage;
            includeGS=false;
            
            //System.out.println("In LevelChart184: start="+start+" end="+end+" GammaT.size="+GammaT.size()+" ngamperpage="+ngamperpage+" ndrawings="+nDrawings);
            
            //check if the level of last transition has other transitions that are in next page
            //if number of such transitions in next page is greater half of total transitions of this level
            //then also move transitions of this level in this page to next page, otherwise move transitions
            //of this level in next page to this page.
            Gamma lastGamma;
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
            //System.out.println("In LevelChart211: index="+index+" ntotal="+ntotal+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
            //System.out.println("   1 ngcurrent="+ngcurrent+" ngrest="+ngrest+" nvacancy="+nvacancy);
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
            }else {
        		moveToNextPage=false;
        		moveToThisPage=false;
            }
            
            //System.out.println("   2 ngcurrent="+ngcurrent+" ngrest="+ngrest);
            //System.out.println("In LevelChart244: index="+index+" ntotal="+ntotal+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage
            //		+" "+moveToNextPage);       
            
            if(moveToNextPage){
            	end=end-ngcurrent;
            	ngcurrent=0;
            	ngrest=ntotal;
            }else if(moveToThisPage){
            	end=end+ngrest;
            	ngcurrent+=ngrest;
            	ngrest=0;          	            	
            }
            
            //System.out.println("In LevelChart255: index="+index+" ntotal="+ntotal+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
            
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
            //System.out.println("In LevelChart267: index="+index+" ntotal="+ntotal+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
            
        	if(sec==nDrawings-1 && GammaT.size()-end<=3)//if there is less than 3 gamma left, move them to current page instead of staring a new page 
        		end=GammaT.size();
        	
            ////////////////
            //debug
            //System.out.println("In LevelChart274: index="+index+" ntotal="+ntotal+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);


            ///////////////////////////////////////
            // positions of level and gamma lines
            //////////////////////////////////////
            
            float[] LevelY = null;
            float[] LevelL = null;
            float[] GammaX = null;
            int nRI=0;//count the gammas with intensities
            
            //NOTE: till now, the first and last gamma level has all gammas in current page, unless the level has >ngamperpage gammas.  
            //      order of gammas in GammaT: gammas of top level go first, and order of gammas in each level is determined by "reorder"
            boolean goodLevelY=false;
            boolean stopped=false;
            boolean resetH=false,resetW=false;
            @SuppressWarnings("unused")
			int ntries=0;
            int ngTotal=end-start;
            while(!goodLevelY && start<end){
            	Gammas = new Vector<Gamma>();
            	Levels = new Vector<Level>();
            	
            	ntries++;
                for(int i=start;i<end&&i<GammaT.size();i++){
                    Gammas.addElement(GammaT.elementAt(i));

                }
                
                
                for(int i=0;i<Gammas.size();i++){
                    Gamma addgam=new Gamma();
                    addgam = (Gamma)Gammas.elementAt(i);
                    
                    if(addgam.RIS().length()>0)
                    	nRI++;
          
                    
                    if(!Levels.contains(ens.levelAt(addgam.ILI())))
                    	Levels.addElement(ens.levelAt(addgam.ILI()));
                    
                    
                    if(addgam.FLI()>=0 && !Levels.contains(ens.levelAt(addgam.FLI())))
                    	Levels.addElement(ens.levelAt(addgam.FLI()));
                    
                }
                


                Level[] TEMPL=new Level[Levels.size()];
                for(int i=0;i<Levels.size();i++)
                	TEMPL[i]=Levels.elementAt(i);
                
                
                Arrays.sort(TEMPL,new LevelComparator());
                
                Levels=new Vector<Level>();
                for(int i=0;i<TEMPL.length;i++)
                	Levels.addElement(TEMPL[i]);

                

                //debug
                //System.out.println("In LevelChart3 line 308: nLevels="+Levels.size()+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
                //System.out.println("         p.level().ES()="+ens.parentAt(0).level().ES()+"  "+ens.parentAt(0).level().ERF()+"  "+ens.parentAt(0).level().ERPF());
                //if(Levels.size()>0) System.out.println(Levels.lastElement().ES());
                
                //for IT decay, always add parent level even if it has no decaying gamma
                if(ens.fullDSId().contains("IT DECAY") && Levels.size()>0 && ens.nParents()>0){
                  	Parent p=(Parent)ens.parentAt(0);
                  	float pe=p.level().EF();
                  	float lastEF=Levels.lastElement().EF();
                  	String pJS=p.level().JPiS();
                  	String lastJS=Levels.lastElement().JPiS();
                   	if(Math.abs(pe-lastEF)>2.0 && !pJS.equals(lastJS)){
                   		Levels.addElement(p.level());
                   	}
                }
                
                //debug
                //System.out.println("In LevelChart3: nLevels="+Levels.size()+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
                
                float tempH,tempW;
                if(rescaleH==-1)
                	tempH=defaultChartHeight*(float)(Math.pow((double)Gammas.size()/(double)ng, pH));
                else 
                	tempH=defaultChartHeight*rescaleH;
                
                if(rescaleW==-1)
                	tempW=defaultChartWidth*(float)Math.pow((double)Gammas.size()/(double)ng,pW);
                else 
                	tempW=defaultChartWidth*rescaleW;
                
                if(!resetH)
                	height=tempH;
                if(!resetW)
                	width=tempW;
                
                
                if(Gammas.size()<4 || height<10){
                	width+=30;
                	height+=10;

                	if(width<defaultChartWidth/3){
                		height=height*(defaultChartWidth/3)/width;
                		width=defaultChartWidth/3;
                	}
                	
                }

                
                if(height>defaultChartHeight) height=defaultChartHeight;
                if(width>defaultChartWidth) width=defaultChartWidth;
                
                LevelY = new float[Levels.size()];
               

                //debug
                //System.out.println("In LevelChart: height="+height+" width="+width+" rescaleH="+rescaleH+" rescaleW="+rescaleW+" Gammas.size="+Gammas.size()+" ngammaperpage="+ngamperpage);

                //Format the Y coordinates of the levels
                LevelY=findLevelY(Levels, height, mindy);

                if(stopped)
                	break;
                
                //increase height if levels are too dense
                //nbad: number of consecutive level gaps <=minGap
                int nbad=0;
                float tempGap=1.2f*mindy;
                if(rescaleH==-1){
                    nbad=checkLevelGap(LevelY,tempGap);
                    while(nbad>=5 && height<defaultChartHeight){
                    	resetH=true;
                    	height=height*1.1f;
                    	if(rescaleW==-1){
                    		width=width*1.1f;
                    		if(width>defaultChartWidth)
                    			width=defaultChartWidth;
                    		
                    		resetW=true;
                    	}
                    	
                    	if(height>defaultChartHeight){
                    		height=defaultChartHeight;
                    		break;
                    	}
                    	LevelY=findLevelY(Levels, height, mindy);
                    	nbad=checkLevelGap(LevelY,tempGap);
                    }                	
                }

            	//debug  
                //if(sec==3){                	
            	//System.out.println("****In LevelChart line 267: ntries="+ntries+" start="+start+" end="+end+" lastLevelY="+LevelY[LevelY.length-1]+" mindy="+mindy);
                //System.out.println("      sec="+sec+" nLevels="+Levels.size()+" height="+height+" HEIGHT="+HEIGHT+" ngamperpage="+ngamperpage+" ngammas="+Gammas.size()+" nbad="+nbad);
                //}
                
                //top levels go beyond given height, remove last gamma (and all others from the same level) in current page
                if(LevelY[LevelY.length-1]>defaultChartHeight+50 || nbad>=5){
                    if(end<=GammaT.size())
                    	lastGamma=GammaT.elementAt(end-1);
                    else
                    	lastGamma=GammaT.lastElement();
                    
                    lastGammaLevel=ens.levelAt(lastGamma.ILI());

                    int nless=0;
                    for(int i=0;i<lastGammaLevel.nGammas();i++){
                    	if(Gammas.contains(lastGammaLevel.gammaAt(i)))
                    		nless++;
                    }
                    
                    goodLevelY=false;
                    if(nless<ngTotal/4)
                    	end=end-nless;
                    else{
                    	end=end-1;
                    	if(end-start<ngTotal/2){//if too many gammas have been removed, then stop doing that and put about half of the gammas in current page
                    		end=start+ngTotal/2;
                    		stopped=true;
                    	}
                    }
                                    
                    //debug
                    //if(sec==3) System.out.println("*****In LevelChart3.5:  start="+start+" end="+end+" lastGammaSize="+lastGammaLevel.nGammas()+" nbad="+nbad);                    	
                }
                else
                	goodLevelY=true;
                
                //goodLevelY=true;

            }
            
        
            LevelL = new float[Levels.size()];
            GammaX = new float[Levels.size()];
            
            //Format the Y coordinates of the level labels
            LevelL=findLevelLabelY(LevelY, miny);
            
            //reset leftmar and rightmar based on the maxLengthJPI and maxLengthES
            maxLengthES=0;
            maxLengthJPI=0;
            float leftmar=LEFTMAR;
            float rightmar=RIGHTMAR;
            
            for(int i=0;i<Levels.size();i++){
            	Level l=Levels.get(i);    
            	
            	//debug
            	//if (sec==1) System.out.println("IN LevelChart line 406: l.ES="+l.ES()+" l.EF()="+l.EF()+" l.ERF="+l.ERF());
            	
            	if(l.ES().length()>maxLengthES)            	
            		maxLengthES=l.ES().length();           
            	if(l.JPiS().length()>maxLengthJPI)         	
            		maxLengthJPI=l.JPiS().length();
            }
            if(maxLengthJPI*4>leftmar)//assue 1 char= point
            	leftmar=maxLengthJPI*4;
            if(maxLengthES*4>rightmar)
            	rightmar=maxLengthES*4;
        
            float minWidth1=rightmar+leftmar+2*slopemar+2*pad+20+(Gammas.size()-1)*BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT;          
            float minWidth2=rightmar+leftmar+2*slopemar+2*pad+25+(Gammas.size()-1)*pad;
            if(width<minWidth1)
            	width=minWidth1+20;
            if(width<minWidth2)
            	width=minWidth2+20;
            
            //Format the X coordinates of the gammas
            if(Gammas.size()>1)
            	GammaX=findGammaX(Gammas.size(), (width-rightmar-leftmar-2*slopemar-2*pad-25), pad);
            else if(Gammas.size()==1){
            	GammaX=new float[1];
            	GammaX[0]=(width-rightmar-leftmar-2*slopemar)/2;
            }
            
            //rest gammas can not fit in last page, then increase page number
            if(sec==nDrawings-1 && end<GammaT.size()-1 && end>start){
            	nDrawings++;
            	
                //debug
                //System.out.println("In LevelChart4: nLevels="+Levels.size()+" nDrawings="+nDrawings+" size="+GammaT.size()+" pageNo="+sec+" start="+start+" end="+end+" ngammapage="+ngamperpage);
            }

            Vector Tics = new Vector<Comparable>();
            Gamma t;
            for(int k=0;k<GammaX.length;k++){
                for(int i=0;i<LevelY.length-1;i++){
                    for(int j=i;j<LevelY.length-1&&LevelY[j+1]-LevelY[j]<=minlevd&&j-i<3;j++){
                        t=(Gamma)Gammas.elementAt(k);
                        if(ens.levelAt(t.FLI())==Levels.elementAt(i)){
                            Tics.addElement(LevelY[j+1]);
                            Tics.addElement(GammaX[k]);
                            Tics.addElement("UP");
                            Tics.addElement((3-(j-i)));
                        }
                    }
                }
            
                for(int i=LevelY.length-1;i>0;i--){
                    for(int j=i;j>1&&LevelY[j]-LevelY[j-1]<=minlevd&&j-i<2;j--){
                        t=(Gamma)Gammas.elementAt(k);
                        if(ens.levelAt(t.ILI())==Levels.elementAt(i)){
                            Tics.addElement(LevelY[j-1]);
                            Tics.addElement(GammaX[k]);
                            Tics.addElement("DOWN");
                            Tics.addElement((2-(i-j)));
                        }

                    }
                }
            }

            writeFigureHead(out,(sec));

            Level l;
            float originalScale=0.5f;
            float scale=0.5f;
            
            boolean isIsomer=false;
            
            for(int i=0;i<LevelY.length;i++){
                l=(Level)Levels.elementAt(i);
                isIsomer=false;
                
                if(l.EF()==0){ 
                	includeGS=true;
                	scale=2f;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                else if(l.EF()>0 && l.T12D()>=1E-7 && (l.DT12S().length()==0 || l.DT12S().charAt(0)!='L')){//isomer

                	scale=1.2f;
                	isIsomer=true;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                
                
                out.write("draw (0,"+LevelL[i]+")--("+leftmar+","+LevelL[i]+")--("+(leftmar+slopemar)+","+LevelY[i]+")-");
                for(int j=0;j<Tics.size();j+=4){
                    if(Float.valueOf(Tics.elementAt(j).toString())==LevelY[i]){
                        if(Tics.elementAt(j+2)=="UP"){
                            out.write("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                        else if(Tics.elementAt(j+2)=="DOWN"){
                            out.write("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                    }
                }
                out.write("-("+(width-(rightmar+slopemar))+","+LevelY[i]+")--("+(width-rightmar)+","+LevelL[i]+")--("+width+","+LevelL[i]+")");
                if(l.q().equals("?")||l.q().equals("S"))out.write(" dashed evenly");
                out.write(";\n");
                
                if(scale>originalScale){
                	scale=originalScale;
                	out.write("pickup pencircle scaled "+scale+";\n");
                }
                
                float xleftSpin=-0.1f/NDSConfig.POINT2CM;
                
                if(l.EF()==0){
                    out.write("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+xleftSpin+","+(LevelL[i])+"));\n");                  
                    out.write("label.ulft(btex "+levelLabelSize()+Translator.value(l.ES(),l.DES())+" etex,("+width+","+(LevelL[i])+"));\n");
                }else{
                	
                	//System.out.println("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,(-0.8,"+(LevelL[i]-1)+"));\n");
                	float yoff=0;
                	if(isIsomer) yoff=1f;
                	
                    out.write("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+xleftSpin+","+(LevelL[i]-1+yoff)+"));\n");
                    out.write("label.ulft(btex "+levelLabelSize()+Translator.plainValue(l.ES(),l.DES())+" etex,("+width+","+(LevelL[i]-1+yoff)+"));\n");               	
                }

                //out.write("label.urt(btex "+levelLabelSize()+"$"+Translator.value(l.T12VS(),l.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(l.T12US())
                //		+"$ etex,("+(width+5)+","+(LevelL[i]-2)+"));\n");
                out.write("label.urt(btex "+levelLabelSize()+Translator.printNumber(l.T12S(),l.DT12S(),l.T12Unit())+" etex,("+(width+5)+","+(LevelL[i]-2)+"));\n");

            }
            
            //add ground state level 
            if(isGSExist && !includeGS){
            	scale=2f;
            	
            	float xleftSpin=-0.1f/NDSConfig.POINT2CM;
            	
            	out.write("pickup pencircle scaled "+scale+";\n");
            	out.write("draw (0,-20)---("+width+","+"-20);\n");
                out.write("label.urt(btex "+levelLabelSize()+Translator.spin(groundLevel.JPiS())+" etex,("+xleftSpin+",-20));\n");
                out.write("label.ulft(btex "+levelLabelSize()+groundLevel.ES()+" etex,("+width+",-20));\n");
                
                //out.write("label.urt(btex "+levelLabelSize()+"$"+Translator.value(groundLevel.T12VS(),groundLevel.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(groundLevel.T12US())
                //		+"$ etex,("+(width+5)+",-22));\n");
                out.write("label.urt(btex "+levelLabelSize()+Translator.printNumber(groundLevel.T12S(),groundLevel.DT12S(),groundLevel.T12Unit())+" etex,("+(width+5)+",-22));\n");
                
            	out.write("pickup pencircle scaled "+originalScale+";\n");
            }
            
            String label;
            String colour="";
            String temp="";
            
            float len2x_digi=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for digit. 2.2f was set originally for gammaLabelSize=6pt
            float len2x_lett=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for letter and others
            
            useColorLine=true;
            if(PN==6 || PN==7 || nRI<2)
            	useColorLine=false;
            
            float topGap=65;//gap between the top level and bottom title
            
            for(int i=0;i<GammaX.length;i++){
                g=(Gamma)Gammas.elementAt(i);
                boolean toDraw=false;
                boolean unknown=false;
                float yi=0,yf=0;
            	//debug
            	//System.out.println("In LevelChart ine 617:  GammaX.length="+GammaX.length+" i="+i+" EG="+g.ES()+" IL="+g.ILI()+" e="+ens.levelAt(g.ILI()).ES()+" index="+Levels.indexOf(ens.levelAt(g.ILI()))+" FL="+g.FLI());  
            	//System.out.println("                level.size="+Levels.size());        
            	
                if(g.ILI()>=0){
                    yi=LevelY[Levels.indexOf(ens.levelAt(g.ILI()))];
                    yf=yi-10;
                    if(g.FLI()>=0){
                    	toDraw=true;
                    	yf=LevelY[Levels.indexOf(ens.levelAt(g.FLI()))];
                    }else if(g.ES().length()>0){//EG="X"
                    	toDraw=true;
                    	unknown=true;
                    }	
                }
                
                if(toDraw){
                	
                	/* old
                    if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.1){
                        colour="withcolor red";
                    }else if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.02){
                        colour="withcolor blue";
                    }
                    */
                	
                    if(useColorLine && !g.RIS().isEmpty() && Str.isNumeric(g.RIS())){
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

                   
                    //debug
                    //if(!Str.isNumeric(g.ES()))
                    //	System.out.println("In LevelChart line 634: g.ES="+g.ES()+" g.IL="+g.ILI()+" e="+ens.levelAt(g.ILI()).ES()+" g.FL="+g.FLI()+" e="+ens.levelAt(g.ILI()).ES());
                    
                    
                    //break level line by adding a tiny white box at gamma and level line crossing
                    breakLevelAtCross(out,GammaX[i]+leftmar+slopemar,LevelY,yi,yf);
                    
                    out.write("drawarrow ("+(GammaX[i]+leftmar+slopemar)+","+yi+")--("+(GammaX[i]+leftmar+slopemar)+","+yf+")");
                    
                    if(g.q().equals("?") || g.q().equals("S")){
                    	out.write(" dashed evenly ");
                    	hasUncertainGamma=true;
                    }
                    out.write(colour+";\n");
                    
                    
                    if(unknown || g.FLRecord().equals("?")){//if FL=?, it means there are multiple candidates for final level,choose the cloest one for drawing
                    	out.write("label.bot(btex "+gammaLabelSize()+"? etex, ("+(GammaX[i]+leftmar+slopemar)+","+yf+"));\n");
                    }
                              
                                    
                    float labelLength=(g.ES().trim().length()+1)*len2x_digi;                
                    label=Translator.value(g.ES(),g.DES());
                    if(label.length()>g.ES().length())//for uncertainty=AP,LT,LE,GT,GE, or value is exponential
                    	labelLength+=1.5*len2x_lett;
                    
                    //label+=" "+g.MS()+" "+g.IS();
                    SDS2XDX s2x=RI(g,ens.norm());
                    temp=s2x.s();
                    
                    //System.out.println("LevelChart 730: EG="+g.ES()+" temp="+temp);
                    
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
                            String str=Translator.value(s2x.s(), s2x.ds());
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
                    
                    //debug
                    //System.out.println("In LevelChart line 663: g.MS="+g.MS()+" RI="+RI(g,ens.norm()).s()+" label="+label);
                    
                    //these calculations rotate and shift a white box that goes behind the labels
                    float xpos=(GammaX[i]+leftmar+slopemar);
                    float ypos=LevelY[Levels.indexOf(ens.levelAt(g.ILI()))]+0.5f;
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
                    
                    if(ypos+labelLength>height+topGap){
                    	topGap=ypos+labelLength-height;
                    }
                }
            }
            
            // title of dataset   
            // write order: from bottom to top
            
            
            //debug
            //System.out.println("In LevelChart line 627: top LevelY="+LevelY[LevelY.length-1]+" height="+height);

            
            float tempY=height+topGap;
            float tempH=height;
                      
            if(dc.isShowTitle()){
               String TopTitle="";
               String tempID="";

               
               if(LevelY[LevelY.length-1]>height)
               	tempY=LevelY[LevelY.length-1]+topGap;
               
               if(ens.fullDSId().contains("IT DECAY") && ens.nParents()>0){
               	Parent p=(Parent)ens.parentAt(0);
               	String s="\\%IT";
                   //if(p.DM().unc().length()==0||p.DM().unc().charAt(0)<=57)
                   //	s+="=";
                   
                   if(p.DM().value().length()>0)
                   	s+=LatexWriter.printNumber(p.DM().value(),p.DM().unc(),true);    
                   else
                   	s+="=?";
                   
               	out.write("label(btex "+otherLabelSize()+s+" etex,("+((width)/2)+","+tempY+"));\n");
               	tempY=tempY+10;
               }
               
               if(!supFlag){
                   if(atflag){
                   	out.write("label(btex "+otherLabelSize()+"@ Multiply placed: intensity suitably divided etex,("+((width)/2)+","+tempY+"));\n");
                   	tempY=tempY+10;
                   }
                   if(andflag){
                   	out.write("label(btex "+otherLabelSize()+"\\& Multiply placed: undivided intensity given etex,("+((width)/2)+","+tempY+"));\n");
                       tempY=tempY+10;
                   }
               }
               
               if(!supPN&&( (PN<8&&PN>=0) || !CPN.isEmpty() ) && ens.nGamWI()>0){
                   
            	   //note that the lines are written in the chart from bottom up
                   if(CPN.toUpperCase().equals("C")){    

                	   int n=ens.norm().nCommentsPN();  
                	   for(int i=n-1;i>=0;i--){      
                           String line=ens.norm().commentPNTextAt(i).trim();
                           line=Translator.procGenCom(line,0,false);
                           Vector<String> linesV=Str.wrapString(line, 70);
                           
                           //System.out.println("i="+i+" n="+n+"  line="+line);
                           int nL=linesV.size();
                           for(int j=nL-1;j>=0;j--) {
                        	   //System.out.println("i="+i+" j="+j+" n="+n+"  linesV.size="+linesV.size()+" line="+linesV.get(j));
                        	   if(i==0 && j==0)
                        		   out.write("label(btex "+otherLabelSize()+"Intensities: ");
                        	   else
                        		   out.write("label(btex "+otherLabelSize()+" ");
                        	   
                        	   out.write(Translator.translate(linesV.get(j)));   
                               out.write(" etex,("+((width)/2)+","+tempY+"));\n");
                               tempY=tempY+10;
                           }                  
                	   }
                   }
                   else if(PN<8 && PN>=0){
                	   out.write("label(btex "+otherLabelSize()+"Intensities: ");
                	   
                       switch(PN){
                       case 1:out.write("Relative I$_{(\\gamma+ce)}$");break;
                       case 2:out.write("I$_{(\\gamma+ce)}$ per 100 decays through this branch");break;
                       case 3:
                       	if(ens.DSTypeS().equals("neutron"))
                       		out.write("I$_{(\\gamma+ce)}$ per 100 neutron captures");
                       	else
                       		out.write("I$_{(\\gamma+ce)}$ per 100 parent decays");
                       	
                       	break;
                       case 4:
                       	if(ens.DSTypeS().equals("neutron"))
                       		out.write("I$_{\\gamma}$ per 100 neutron captures");
                       	else
                       		out.write("I$_{\\gamma}$ per 100 parent decays");
                       	break;
                       case 5:out.write("Relative I$_{\\gamma}$");break;
                       case 6:out.write("Relative photon branching from each level");break;
                       case 7:out.write("\\% photon branching from each level");break;
                       case 0:
                    	out.write("Type not specified");
                      	
                       	break;
                       }
                       
                       out.write(" etex,("+((width)/2)+","+tempY+"));\n");
                       tempY=tempY+10;
                   }
               }
               
               //subtitle
               int ns=SubTitle.size();
               for(int i=ns-1;i>=0;i--)               
            	   out.write("label(btex "+Translator.process(SubTitle.elementAt(i))+" etex,("+(width/2)+","+(tempY+10*(i-ns+1))+"));\n");

               
               tempY=tempY+10*SubTitle.size()+5;
                           
               if(ens.fullDSId().contains("IT DECAY")) 
                   out.write("label(btex \\underline{Decay Scheme");
               else
               	out.write("label(btex \\underline{Level Scheme");
               
               if(sec!=0)out.write(" (continued)");
               out.write("} etex,("+((width)/2)+","+tempY+"));\n");
               
               tempY=tempY+25;
               
               TopTitle+="{\\bf \\small \\underline{"; 
               
               /*
               if(etd.getAltID().length()>0){ 
               	tempID=etd.getAltID();
               	if(tempID.contains(":T")&&!tempID.contains(":T1/2"))
               		tempID=tempID.replace(":T", ":T1/2");
               	TopTitle+=Translator.process(tempID,true);
               }
               else {
               	tempID=ens.DSId();
               	if(tempID.contains(":T")&&!tempID.contains(":T1/2"))
               		tempID=tempID.replace(":T", ":T1/2");
                   TopTitle+=Translator.process(tempID,true);
                   
                   //if(ens.lineAt(1).contains("2   "))TopTitle+=Translator.process(ens.lineAt(1).substring(ens.lineAt(1).indexOf("2   ")+4).trim(),true);
               }        
               
               if(ens.DSRefS().length()>5) TopTitle+=("\\hspace{0.2in}"+Translator.process(ens.DSRefS()));   
               */
               
               TopTitle+=LatexWriter.makeDatasetTitleAndRef(etd,false);
                
               TopTitle+="}}";
               
               out.write("label(btex "+TopTitle+" etex,("+((width)/2)+","+tempY+"));\n");
           }

            
            tempH=tempY;
            
            //nucleus name at bottom
            tempY=-20;
            if(isGSExist&&!includeGS)//a ground level line is added
            	tempY=-40;
            
            tempH=tempH-tempY;
            
            out.write("label(btex $^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$ etex,("+width/2+","+tempY+"));\n");
            
     
            if(useColorLine || hasGCOIN || hasUncertainGCOIN || hasUncertainGamma){
            	//LegendChart lc=new LegendChart();
            	//lc.drawChart(out,false);
            	
            	//legend box width ~ 120pt for "-----> Ig>10%XIg^max"
            	float x=width/2+100;   //width is the actual width of drawing, not the page width
            	float y=height+55;
            	
        		if(width/2>120)
        			x=width/2+120;

            	if(dc.isShowLegend())
            		drawLegend(out,x,y);//(x,y) is the lower-left corner of legend rectangle
            	
            	hasGCOIN=false;
            	hasUncertainGCOIN=false;
            	hasUncertainGamma=false;
            	useColorLine=false;
            }
            
            writeFigureTail(out);
            
            widths[sec]=width+60;
            heights[sec]=tempH+20;
        }

        writeTail(out);
        
        nPages=nDrawings;
        
        float temp[];

        temp=Arrays.copyOf(widths, nPages);
        widths=new float[nPages];
        widths=Arrays.copyOf(temp,nPages);

        temp=Arrays.copyOf(heights, nPages);
        heights=new float[nPages];
        heights=Arrays.copyOf(temp,nPages);
        
        return nDrawings;
    }
    
    
    
}

@SuppressWarnings("rawtypes")
class LevelComparator implements Comparator{
    public int compare(Object one,Object two){
        Level a=(Level)one;
        Level b=(Level)two;
        int out;
        if(a.getIndex()<b.getIndex())out=-1;
        else if(a.getIndex()>b.getIndex())out=1;
        else out=0;
        return out;
    }
}
@SuppressWarnings("rawtypes")
class LevelComparatorUP implements Comparator{
    public int compare(Object one,Object two){
        Level a=(Level)one;
        Level b=(Level)two;
        int out;
        if(a.getIndex()<b.getIndex())out=1;
        else if(a.getIndex()>b.getIndex())out=-1;
        else out=0;
        return out;
    }
}