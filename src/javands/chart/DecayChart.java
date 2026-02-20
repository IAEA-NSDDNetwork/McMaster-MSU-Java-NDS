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

public class DecayChart extends BaseChart{
    private float defaultChartWidth;//include arrow width but not include widths of the parent level and side table
    private float defaultChartHeight;
    private float CM_TO_POINT=28.3464567f;
    
    public int drawChart(java.io.Writer out,EnsdfTableData etd,DrawingControl drw)throws Exception{
    	ArrayList<String> outstrings=new ArrayList<String>();
    	int n=drawChart(outstrings,etd,drw);    
    	
    	//debug
    	//System.out.println("In DecayChart line 33: npages="+n);
    	
    	//if in portrait mode and n==2 (pages), test if a landscape page can hold all
    	if(drw.isPortrait() && n==2){
    		ArrayList<String> list=new ArrayList<String>();
    		drw.setPortrait(false);
    		int n1=drawChart(list,etd,drw);
    		
        	//debug
        	//System.out.println("In DecayChart line 42: npages="+n1);
        	
    		if(n1==1){
    			outstrings.clear();
    			outstrings.addAll(list);
    			n=n1;
    		}else{
    			drw.setPortrait(true);
    			outstrings.clear();
    			n=drawChart(outstrings,etd,drw);
    		}
    	}
    	
    	//write into metapost file
    	for(int i=0;i<outstrings.size();i++)
    		out.write(outstrings.get(i));
    	
    	return n;
    }
    /**draws a decay chart
     * @throws Exception */
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public int drawChart(ArrayList<String> out,EnsdfTableData etd,DrawingControl drw)throws Exception{
    	
    	ArrayList<String> tempOut=new ArrayList<String>();
    	
    	ENSDF ens=etd.getENSDF();
    	dc=drw;
    	reset();
    	
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

        //******diagram parameters*******
        defaultChartWidth=pageWidth*CM_TO_POINT-120;
        defaultChartHeight=pageHeight*CM_TO_POINT-140;
        
        float miny=7;
        float LEFTMAR=20;
        float RIGHTMAR=30;
        float slopemar=5;
        float minlevd=3;
        float pad=5;
        float levelX0=0;//starting X of a level line, default=0

        int defaultNgamperpage;
        if(pageHeight<pageWidth)defaultNgamperpage=56;
        else defaultNgamperpage=36;
        
        int nMaxgamperpage=defaultNgamperpage;
        
        int nDrawings;
        float mindy=drw.getMinY();
        float arrowH=20;
        float arrowW=10;
        
        //for decay table, value is initialized in EnsdfTableData and could be set in Drawing creator
        float defaultXOffset=drw.getDecayTableXOffset()*CM_TO_POINT;
        

        //********************************

        int maxLengthJPI=0;
        int maxLengthES=0;
        int maxLengthTS=0;
        
        boolean isLogft;
        boolean isIB;
        boolean isIE;
        boolean isE;
        boolean isHF;
        boolean atflag=false;
        boolean andflag=false;
        float rescaleW=drw.rescaleW();
        float rescaleH=drw.rescaleH();
        Vector<String> SubTitle=new Vector<String>();
        SubTitle=drw.subtitle();
        boolean supFlag=drw.supFlag();
        boolean supPN=drw.supPN();


        Vector<Gamma> GammaT = new Vector<Gamma>();
        Gamma g;
        Level l;
        char type;
        int PN;
        if(!ens.norm().OS().isEmpty())
        	PN=Integer.valueOf(ens.norm().OS());
        else if(ens.norm().implicitPN()>=0)
        	PN=ens.norm().implicitPN();
        else
        	PN=0;
        
        String CPN=ens.norm().CS();//PN record col=77
        
        //Load up levels
        int reorder=1;//by default, reorder=0, gamma energies of lines increase from left to right in level scheme, as index increases
                      //reorder=1, print order is opposite, for this case, gammas of each level need to be sorted with energy decreases as index increases

        boolean isGSExist=false; //mark if there exists a ground state
        Level groundLevel=new Level();
        
        for(int i=0;i<ens.nLevels();i++){
        	l=ens.levelAt(i);
        	int n=ens.levelAt(i).nGammas();
            for(int j=0;j<n;j++){               
            	int index=(1-reorder)*j+reorder*(n-1-j);    
            	Gamma gam=ens.levelAt(i).gammaAt(index);
                if(gam.FLI()>-1 || (gam.ES().length()>0 && !Str.isNumeric(gam.ES())))
                	GammaT.addElement(gam);
            }
            
            if(!isGSExist && ens.levelAt(i).EF()==0){
            	isGSExist=true;
            	groundLevel=ens.levelAt(i);
            }
        }      
        
        /*This should be used if one wants to display the most direct route to the ground state
        for(int i=0;i<ens.nLevels();i++){
            if(ens.levelAt(i).nBetas()>0||ens.levelAt(i).nECBPs()>0||ens.levelAt(i).nAlphas()>0){
                LevelT.addElement(ens.levelAt(i));
                for(int j=ens.levelAt(i).nGammas()-1;j>=0&&j>ens.levelAt(i).nGammas()-3;j--){
                    g=ens.levelAt(i).gammaAt(j);
                    if(g.FLI()>0) GammaT.addElement(g);
                }
            }
        }
        for(int i=0;i<GammaT.size();i++){
            g=(Gamma)GammaT.elementAt(i);
            l=ens.levelAt(g.FLI());
            if(!LevelT.contains(l)){
                LevelT.addElement(l);
                for(int j=l.nGammas()-1;j>l.nGammas()-2&&j>=0;j--){
                    g=l.gammaAt(j);
                    GammaT.addElement(g);
                }
            }
        }
        */
        Gamma[] TEMP=new Gamma[GammaT.size()];
        for(int i=0;i<GammaT.size();i++)TEMP[i]=GammaT.elementAt(i);
        Arrays.sort(TEMP,new GammaComparator());
        GammaT=new Vector<Gamma>();
        for(int i=0;i<TEMP.length;i++)GammaT.addElement(TEMP[i]);

        int minNG=6;
        if(!drw.isPortrait())
        	minNG=(int)(minNG*pageWidth/pageHeight+0.5f);
        
        while(GammaT.size()>defaultNgamperpage&&GammaT.size()%defaultNgamperpage<minNG){
            defaultNgamperpage++;
        }

        float maxI=0;
        for(int i=0;i<GammaT.size();i++){
            g=(Gamma)GammaT.elementAt(i);
            if(!g.RIS().isEmpty()&&Str.isNumeric(g.RIS())&&Float.valueOf(g.RIS())>maxI){
                maxI=Float.valueOf(g.RIS());
            }
        }
        
        //evening out the number of gammas per page
        nDrawings=GammaT.size()/defaultNgamperpage;

        
        //debug
        //System.out.println("In DecayChart line 196: GammaT.size="+GammaT.size()+" ngamperpage="+defaultNgamperpage+" ndrawings="+nDrawings+" minNG="+minNG);
        
        if(GammaT.size()%defaultNgamperpage!=0)
        	nDrawings++;
        
        if(nDrawings>0) {
        	defaultNgamperpage=(GammaT.size()/nDrawings);
        	if(GammaT.size()%nDrawings!=0)
        		defaultNgamperpage+=1;
        }
        
        int start=0;
        int end=0;
        boolean drawDecayFedLevels=dc.toDrawDecayFedLevels();
        boolean moveToNextPage=false;
        boolean moveToThisPage=false;
        boolean readNewGammaLevel=true;
        int ngcurrent=-1;//number of gammas of current level in current page
        int ngrest=-1;//number of the rest gammas of current level after current page
        
        boolean includeGS=false;//mark if current page has ground state level
        
        int ng=defaultNgamperpage;
        float pW=0.5f;
        float pH=1.5f;
        int NG=20;
        if(ng<NG){
        	ng=NG;
        	pH=1.0f;
        	pW=0.7f;
        	
        	dc.resetLabelSize();
        	dc.scaleLevelLabelSize(1.1f);
        	dc.scaleGammaLabelSize(1.1f); 
        	
        }
        
        
        //NOTE: the order of each column matters and is different for B and E(or A)
        //      For B decay, table is placed at left and columns are drawn from right to left,
        //                   logft column first and then IB column, E(decay)
        //      For E and A decay, table is placed at right and columns are drawn from left to right,
        //                   E(decay) first, then IB (or IA), (IE), logft (or HF)
        Vector tableColumns=new Vector();
        
        isLogft=false;
        isIB=false;
        isIE=false;
        isE=false;
        isHF=false;
        
        if(ens.decayRecordType().equals("B")){
            type='B';
            for(int i=0;i<ens.nLevels();i++){
            	int n=ens.levelAt(i).nBetas();
            	if(n<=0)
            		continue;
            	
            	Beta d=ens.levelAt(i).betaAt(0);
                if(!d.LOGFTS().isEmpty()){
                	isLogft=true;
                }
                if(!d.ES().isEmpty()){
                	isE=true;
                }
                if(!d.RIS().isEmpty()){
                	isIB=true;
                }
            }
            
            
            if(isLogft)tableColumns.addElement("L");
            if(isIB)tableColumns.addElement("I");
            //if(isE)tableColumns.addElement("E");
        }else if(ens.decayRecordType().equals("A")){
            type='A';
            for(int i=0;i<ens.nLevels();i++){
            	int n=ens.levelAt(i).nAlphas();
            	if(n<=0)
            		continue;
            	
            	Alpha d=ens.levelAt(i).alphaAt(0);
                if(!d.HFS().isEmpty())isHF=true;
                if(!d.ES().isEmpty())isE=true;
                if(!d.RIS().isEmpty())isIB=true;
            }
            if(isE)tableColumns.addElement("E");
            if(isIB)tableColumns.addElement("I");
            if(isHF)tableColumns.addElement("H");
        }else if(ens.decayRecordType().equals("E")){
            type='E';
            for(int i=0;i<ens.nLevels();i++){
            	int n=ens.levelAt(i).nECBPs();
            	if(n<=0)
            		continue;
            	
            	ECBP d=ens.levelAt(i).ECBPAt(0);
                if(!d.LOGFTS().isEmpty())isLogft=true;
                if(!d.ES().isEmpty())isE=true;
                if(!d.IBS().isEmpty())isIB=true;
                if(!d.IES().isEmpty())isIE=true;
                

            }
            //if(isE)tableColumns.addElement("E");
            if(isIB)tableColumns.addElement("B");
            if(isIE)tableColumns.addElement("I");
            if(isLogft)tableColumns.addElement("L");
        }else{
        	type='O';//other types
        }
        
        
        HashMap<String,Float> colWidths=new HashMap<String,Float>();        
        float defaultWidth=29f;
        for(int i=0;i<tableColumns.size();i++){
        	colWidths.put((String)tableColumns.get(i), Float.valueOf(defaultWidth));
        }
        
        
        writeHead(out);

        
        //Writes each figure
        
        Vector<Level> decayFedLevelsBelow=new Vector<Level>();//levels only fed by decays of parent below all gamma-decaying levels in a page
        Vector<Level> decayFedLevelsAbove=new Vector<Level>();//levels only fed by decays of parent above all gamma-decaying levels in a page
        Vector<Level> decayFedLevelsDrawn=new Vector<Level>();//store decay-fed levels that have been drawn
        int preMinLI=-1;
        int tempPreMinLI=-1;
        

        //debug
        //System.out.println("In DecayChart line 325: GammaT.size="+GammaT.size()+" ngamperpage="+defaultNgamperpage+" ndrawings="+nDrawings);

        int ngamperpage;
        int[] ngamperpages=new int[nDrawings+10];
        for(int i=0;i<ngamperpages.length;i++)
        	ngamperpages[i]=defaultNgamperpage;
        
        
        for(int i=0;i<widths.length;i++){
        	widths[i]=defaultChartWidth;
        	heights[i]=defaultChartHeight;
        }
        
		int ntries=0;
		
		levelX0=0;
		
        for(int sec=0; sec<nDrawings;sec++){
            Vector<Gamma> Gammas = new Vector<Gamma>();
            Vector<Level> Levels = new Vector<Level>();
            
            
            tempOut.clear();
            
            ngamperpage=ngamperpages[sec];
            
            start=end;
            end=start+ngamperpage;
            includeGS=false;
            
            
            //check if the level of last transition has other transitions that are in next page
            //if number of such transitions in next page is greater than half of total transitions of this level
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
            
            //debug
            //System.out.println("In DecayChart lin 392: ngcurrent="+ngcurrent+" ngrest="+ngrest+" ntotal="+ntotal+" index="+index+" start="+start+" end="+end+" ngamperpage="+defaultNgamperpage);
            //System.out.println(" last gamma="+lastGamma.ES());
            
            int nvacancy=nMaxgamperpage-ngamperpage;
            if(nvacancy<=0)
            	nvacancy=1;
            
            moveToThisPage=false;
            moveToNextPage=false;
            
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

            int old_ngcurrent=ngcurrent;
            int old_ngrest=ngrest;
            boolean old_readNewGammaLevel=readNewGammaLevel;
            
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
            
            
            if(sec==nDrawings-1 && GammaT.size()-end<=3)//if there is less than 3 gamma left, move them to current page instead of staring a new page 
        		end=GammaT.size();

            
            ////////////////
             
            //debug
            //System.out.println("In DecayChart lin 461: ngcurrent="+ngcurrent+" ngrest="+ngrest+" ntotal="+ntotal+" start="+start+" end="+end+" ngamperpage="+defaultNgamperpage);  
                     
            float[] LevelY = null;
            float[] LevelL = null;
            float[] GammaX = null;
            int nRI=0;//count the gammas with intensities
            
            //NOTE: till now, the first and last gamma levels have all gammas in current page.  
            //      order of gammas in GammaT: gammas of top level go first, and order of gammas in each level is determined by "reorder"
            boolean goodLevelY=false;
            boolean stopped=false;
            boolean toStop=false;
            boolean resetH=false,resetW=false;
            

            int ngTotal=end-start;
            int nlessTotal=0;
            

            while(!goodLevelY){
            	Gammas = new Vector<Gamma>();
            	Levels = new Vector<Level>();
            	
                for(int i=start;i<end&&i<GammaT.size();i++){
                    Gammas.addElement(GammaT.elementAt(i));
                }
                
                //debug
                //System.out.println("In DecayChart line 475: @@@ start="+start+" end="+end+"  Gammas.size="+Gammas.size()+" sec="+sec+" ndrawings="+nDrawings);
                
                //add levels directly from decay feeding that don't have gamma decay nor feeding, in between the first and last that have gamma decay in Levels 
                Gamma firstGammaOnNextPage;
                int minIndex=100000,maxIndex=0;
                int maxLF=0,minLF=100000;
                int li=-1;
                int lf=-1;

                
                for(int i=0;i<Gammas.size();i++){
                	
                    Gamma addgam=new Gamma();
                    addgam = (Gamma)Gammas.elementAt(i);
                    
                	//debug
                	//System.out.println("In DecayChart line 332: size="+Gammas.size()+" Eg="+addgam.ES()+" level.size="+Levels.size());
                    if(addgam.RIS().length()>0)
                    	nRI++;
                    
                	li=addgam.ILI();
                    if(li>=0 && !Levels.contains(ens.levelAt(li))){
                    	Levels.addElement(ens.levelAt(li));
                    	
                    	if(li<minIndex)
                    		minIndex=li;
                    	if(li>maxIndex)
                    		maxIndex=li;                   	
                    }
                    
                    lf=addgam.FLI();
                    if(lf>=0 && !Levels.contains(ens.levelAt(lf))){
                    	Levels.addElement(ens.levelAt(lf));
                    	if(lf>maxLF)
                    		maxLF=lf;
                    	if(lf<minLF)
                    		minLF=lf;
                    }
                    

                }

                if(minIndex<=maxIndex) 
                	tempPreMinLI=minIndex;
                
                if(end<GammaT.size()) {
                	firstGammaOnNextPage=GammaT.get(end);
                }else {
                	firstGammaOnNextPage=GammaT.lastElement();
                }
                
                //NOTE: drawing order of levels: high index to low index
                li=firstGammaOnNextPage.ILI();
                if(li>maxIndex)
                	maxIndex=li;
                if(li<minIndex)
                	minIndex=li;
                
                
                //if(maxLF>=0 && maxLF<minIndex)
                //	minIndex=maxLF;
                if(minLF>=0 && minLF<minIndex)
                	minIndex=minLF;
                	
            
                if(end==GammaT.size()&&minIndex>0)
                	minIndex=0;
                
            	//add levels directly from decay feeding that don't have gamma decay nor feeding, in between the first and last that have gamma decay in Levels 
                //Drawing order for levels: from high to low
                
                                
                decayFedLevelsBelow.clear();
                for(int i=minIndex;i<=maxIndex;i++){
                	Level level=ens.levelAt(i);
                	
                	//debug
                	//System.out.println("In DecayChart line 373: minIndex="+minIndex+" maxIndex="+maxIndex+" Level="+level.ES()+" end="+end+" GammaT.size="+GammaT.size());
                	
                	if(!Levels.contains(level)){
                		int nDecays=level.nBetas()+level.nECBPs()+level.nAlphas();
                		if(nDecays>0 && level.nGammas()==0 && !level.isFinalLevel()){
                			decayFedLevelsBelow.addElement(level);
                		}
                	}
                }
                
            	//debug
            	//System.out.println("In DecayChart line 564: start="+start+" end="+end+" minIndex="+minIndex+" maxIndex="+maxIndex+" maxLF="+maxLF+" preMinLI="+preMinLI);
                
                decayFedLevelsAbove.clear();
                if(preMinLI<0)
                	preMinLI=ens.nLevels();
            
            	//debug
            	//System.out.println("In DecayChart line 561: start="+start+" end="+end+" minIndex="+minIndex+" maxIndex="+maxIndex+" maxLF="+maxLF+" preMinLI="+preMinLI);

            	
                for(int i=maxIndex;i<preMinLI;i++){
                	Level level=ens.levelAt(i);
                	
                	//debug
                	//System.out.println("In DecayChart line 373: minIndex="+minIndex+" maxIndex="+maxIndex+" Level="+level.ES()+" end="+end+" GammaT.size="+GammaT.size());
                	
                	if(!Levels.contains(level)){
                		int nDecays=level.nBetas()+level.nECBPs()+level.nAlphas();
                		if(nDecays>0 && level.nGammas()==0 && !level.isFinalLevel()){
                			decayFedLevelsAbove.addElement(level);
                		}
                	}
                }

                
                //debug
                //System.out.println("In DecayChart line 587: Levels.size="+Levels.size()+" Gammas.size="+Gammas.size()+" sec="+sec+" nDrawings="+nDrawings+" drawDecayFedLevels="+drawDecayFedLevels);
                //System.out.println("           minIndex="+minIndex+" maxIndex="+maxIndex+" preMinLI="+preMinLI+" fedlevelabove.size="+decayFedLevelsAbove.size()+" below.size="+decayFedLevelsBelow.size());
                //iterate with levels that are fed only by decay of parent
                
                if(drawDecayFedLevels){
                    Levels.addAll(decayFedLevelsAbove);
                    Levels.addAll(decayFedLevelsBelow);
                }

                while(!goodLevelY){
                    //debug
                    //System.out.println("In DecayChart line 425: Levels.size="+Levels.size());
                	
                	
                    Level[] TEMPL=new Level[Levels.size()];
                    for(int i=0;i<Levels.size();i++)
                    	TEMPL[i]=Levels.elementAt(i);
                    
                    Arrays.sort(TEMPL,new LevelComparator());
                    
                    Levels=new Vector<Level>();
                    for(int i=0;i<TEMPL.length;i++)
                    	Levels.addElement(TEMPL[i]);

                    //debug
                    //System.out.println("In DecayChart line 602: Levels.size="+Levels.size()+" Gammas.size="+Gammas.size()+" sec="+sec+" nDrawings="+nDrawings);
                    //System.out.println("In DecayChart line 610: width="+width+" height="+height+" start="+start+" end="+end);
                    
                    float tempH,tempW;
                    if(rescaleH==-1)
                    	tempH=heights[sec]*(float)(Math.pow((double)Gammas.size()/(double)ng, pH));
                    else 
                    	tempH=heights[sec]*rescaleH;
                    
                    if(rescaleW==-1)
                    	tempW=widths[sec]*(float)Math.pow((double)Gammas.size()/(double)ng,pW);
                    else 
                    	tempW=widths[sec]*rescaleW;
                    
                  
                    if(!resetH)
                    	height=tempH;
                    if(!resetW)
                    	width=tempW;
                    
                    //debug
                    //System.out.println("In DecayChart line 629: height="+height+" width="+width+" rescaleH="+rescaleH+" rescaleW="+rescaleW+" Gammas.size="+Gammas.size()+" ngammaperpage="+ngamperpage);
                    //System.out.println("In DecayChart line 630:  height="+height+" HEIGHT="+HEIGHT);

                    
                    if(height>defaultChartHeight) height=defaultChartHeight;
                    if(width>defaultChartWidth) width=defaultChartWidth;
                    
                    
                    if(Gammas.size()<=0 && Levels.size()>=15){
                    	//width=WIDTH;
                    	//height=HEIGHT;
                    }
                    else if(Gammas.size()<4 || height<10){
                    	if(width<defaultChartWidth/3){
                    		height=height*(defaultChartWidth/3)/width;
                    		width=defaultChartWidth/3;
                    	}else if(width<=defaultChartWidth/2){
                        	width+=40;
                        	height+=10;
                    	}
                    }

                    

                    
                    LevelY = new float[Levels.size()];
                   
                    
                    //Format the Y coordinates of the levels
                    LevelY=findLevelY(Levels, height, mindy);    
                    
                    //debug
                    //System.out.println("In DecayChart line 653: Levels.size="+Levels.size()+" LevelY.size="+LevelY.length+" miny="+miny+" Gammas.size="+Gammas.size());
                    //System.out.println("In DecayChart line 653: Levels[size-1]="+LevelY[Levels.size()-1]+" height="+height+" width="+width);
                    
                    if(toStop){
                    	stopped=true;
                    	break;
                    }
                    
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
                        		if(width>widths[sec])
                        			width=widths[sec];
                        		
                        		resetW=true;
                        	}
                        	
                        	if(height>heights[sec]){
                        		height=heights[sec];
                        		break;
                        	}
                        	LevelY=findLevelY(Levels, height, mindy);
                        	nbad=checkLevelGap(LevelY,tempGap);
                        	
                        }                	
                    }
                    

                    LevelL = new float[Levels.size()];
                    LevelL=findLevelLabelY(LevelY, miny);
                    
                    //System.out.println("In DecayChart line 700: LevelsY[size-1]="+LevelY[Levels.size()-1]+" LevelL[last]="+LevelL[LevelL.length-1]+" width="+width+" height="+height+" defaultChartHeight="+defaultChartHeight);
                    //System.out.println("In DecayChart line 701: sec="+sec+" end="+end+" size="+Gammas.size()+" total size="+GammaT.size()+" nDrawings="+nDrawings);
                    
                    //top levels go beyond given height, remove last gamma (and all others from the same level) in current page
                    if(LevelY[LevelY.length-1]>defaultChartHeight+20 || LevelL[LevelL.length-1]>LevelY[LevelY.length-1]+20 || nbad>=5){
                        if(end<=GammaT.size() && end>0)
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
                        
                        //debug
                        //System.out.println("In DecayChart line 762: decayFedLevelsBelow.size()="+decayFedLevelsBelow.size()+" gamma.size="+Gammas.size());
                        //System.out.println("                        start="+start+" end="+end+" total="+ngTotal+" nless="+nless+" nlessTotal="+nlessTotal);
                        
                        if(decayFedLevelsBelow.size()>0){//remove decayFedLevels first, then last gammas
                        	Levels.remove(decayFedLevelsBelow.elementAt(0));//order of levels in decayFedLevels: low-lying levels first
                        	decayFedLevelsBelow.removeElementAt(0);
                        	continue;//iterate with decayFedLevels
                        }
                        else if(nlessTotal+nless<ngTotal/4 || nless<=5){//now remove all gammas from the lowest levels that should be removed to reduce the height if condition met
                        	end=end-nless;
                        	nlessTotal+=nless;
                        	
                        	if(end-start<=0) {
                        		Gammas.clear();
                        		end=start;
                        	}

                        }
                        else{
                        	end=end-nless/2;
                        	nlessTotal+=nless/2;
                        	if(end-start>0 && end-start<ngTotal/2 && nbad<=10){
                        		end=start+ngTotal/2;
                        		toStop=true;
                        	}else if(end-start<=0 && decayFedLevelsAbove.size()>0){
                        		decayFedLevelsBelow.clear();
                        		
                        		Gammas.clear();
                        		end=start;
                        	}
                        	
                        }
                        
                        //debug
                        //System.out.println("In DecayChart line 791: decayFedLevelsAbove.size()="+decayFedLevelsAbove.size()+" nbad="+nbad+" gamma.size="+Gammas.size());
                        //System.out.println("                        start="+start+" end="+end+" total="+ngTotal+" nless="+nless+" level.size="+Levels.size());
                        
                        
                        //no gamma
                        if(end-start<=0 && decayFedLevelsAbove.size()>0){                       	
                        	if(Levels.size()==decayFedLevelsAbove.size())                        		                       			
                        		decayFedLevelsAbove.removeElementAt(0);//order of levels in decayFedLevels: low-lying levels first
                       	                        		
                        	decayFedLevelsBelow.clear();
                        	
                        	Levels.clear();
                        	Levels.addAll(decayFedLevelsAbove);
                        	end=start;
                        }
                        
                        
                        //debug
                        //System.out.println("In DecayChart line 803: defaultChartHeight="+defaultChartHeight+" nbad="+nbad+" LevelY.length="+LevelY.length+" gamma.size="+Gammas.size());
                        //System.out.println("                        start="+start+" end="+end+" total="+ngTotal+" nless="+nless+" height="+height);
                        
                        	
                    }
                    else
                    	goodLevelY=true;
                    
                    if(decayFedLevelsBelow.size()==0 && Levels.size()>decayFedLevelsAbove.size())//no decayFedLevels for iteration, break out to main loop
                    	break;
                }
                
                //System.out.println("In DecayChart line 768: sec="+sec+" end="+end+" size="+Gammas.size()+" total size="+GammaT.size()+" nDrawings="+nDrawings);
                
                if(stopped)
                	break;
                
            }
            
                        
            tempPreMinLI=ens.levelsV().indexOf(Levels.firstElement());
           
            //debug
            //System.out.println("In DecayChart line 779:  LevelY.length="+LevelY.length+" gamma.size="+Gammas.size()+" level.size="+Levels.size());
            //System.out.println("                        start="+start+" end="+end+" total="+ngTotal+" height="+height+" tempPreMinLI="+tempPreMinLI);

            
            LevelL = new float[Levels.size()];
            GammaX = new float[Levels.size()];
            
            //Format the Y coordinates of the level labels
            LevelL=findLevelLabelY(LevelY, miny);
            
            
            //reset leftmar and rightmar based on the maxLengthJPI and maxLengthES
            maxLengthES=0;
            maxLengthJPI=0;
            maxLengthTS=0;
            float leftmar=LEFTMAR;
            float rightmar=RIGHTMAR;
            String ts="";
            
            for(int i=0;i<Levels.size();i++){
            	l=Levels.get(i);           
            	if(l.ES().length()>maxLengthES)            	
            		maxLengthES=l.ES().length();           
            	if(l.JPiS().length()>maxLengthJPI)         	
            		maxLengthJPI=l.JPiS().length();
            	
            	if(l.T12S().length()>0){
            		String dts=l.DT12S().replace("+", "").replace("-", "").trim();
                	if(l.dt12IsNumber() || Str.isNumeric(dts)){         	
                		ts=l.halflife();	
                	}else{
                		ts=l.DT12S()+l.T12S()+" "+l.T12Unit();
                		ts=ts.trim();
                	}
                	
                	if(ts.length()>maxLengthTS)
                		maxLengthTS=ts.length();
                		
            	}

            }
            if(maxLengthJPI*4>leftmar)//assume 1 char=4 point
            	leftmar=maxLengthJPI*4;
            if(maxLengthES*4>rightmar)
            	rightmar=maxLengthES*4;
        
            maxLengthTS=maxLengthTS*4;//convert to length in point
            
            float minWidth1=rightmar+leftmar+2*slopemar+2*pad+20+(Gammas.size()-1)*BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT;
            float minWidth2=rightmar+leftmar+2*slopemar+2*pad+25+(Gammas.size()-1)*pad;       
            if(width<minWidth1)
            	width=minWidth1+20;
            if(width<minWidth2)
            	width=minWidth2+20;
            
            //Format the X coordinates of the gammas, with respect to x=0 for staring of a level line           
            if(Gammas.size()>1)
            	GammaX=findGammaX(Gammas.size(), (width-rightmar-leftmar-2*slopemar-2*pad-20), pad);
            else if(Gammas.size()==1){
            	GammaX=new float[1];
            	GammaX[0]=(width-rightmar-leftmar-2*slopemar)/2;
            }
            
            if(Gammas.size()<=0)
            	GammaX=null;
            
            //debug
            //System.out.println("In DecayChart line 843: sec="+sec+" start="+start+" end="+end+" size="+Gammas.size()+" total size="+GammaT.size()+" nDrawings="+nDrawings);
            //System.out.println("                       width="+width+" rightmar="+rightmar+" leftmar="+leftmar+" slopemar="+slopemar+" pad="+pad);
            
            
            Vector Tics = new Vector();
            Gamma t;
            int ngammas=0;
            if(GammaX!=null)
            	ngammas=GammaX.length;
            
            for(int k=0;k<ngammas;k++){
                t=(Gamma)Gammas.elementAt(k);
                for(int i=0;i<LevelY.length-1;i++){
                    for(int j=i;j<LevelY.length-1&&LevelY[j+1]-LevelY[j]<=minlevd&&j-i<3;j++){
                        l=(Level)Levels.elementAt(j+1);
                        if(l.getIndex()>=t.ILI())break;
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
                        l=(Level)Levels.elementAt(j-1);
                        if(l.getIndex()<=t.FLI())break;
                        if(ens.levelAt(t.ILI())==Levels.elementAt(i)){
                            Tics.addElement(LevelY[j-1]);
                            Tics.addElement(GammaX[k]);
                            Tics.addElement("DOWN");
                            Tics.addElement((2-(i-j)));
                        }
                    }
                }
            }

            writeFigureHead(tempOut,sec);
            tempOut.add("ahlength:=4;\nahangle:=22;\n");
            
            //set the X of table columns
            //float tableColumnX[]=new float[table.size()];
            
            
            
            float moreIEWidth=0;//for columns after IE columns
            float moreIBWidth=0;
            int maxLength=0;
            float r=3.0f;
            for(int i=0;i<LevelY.length;i++){
                l=(Level)Levels.elementAt(i);
                
                
                int length=0;
                if(l.nECBPs()>0&&type=='E'&&isIE){
                	String IS=l.ECBPAt(0).AIES();
                	String DIS=l.ECBPAt(0).DAIES();
                	if(IS.length()==0){
                    	IS=l.ECBPAt(0).IES();
                    	DIS=l.ECBPAt(0).DIES();
                	}
                		
                	length=IS.length()+DIS.length();
                	if(IS.indexOf("E")>0) {
                		length=length+5;
                		if(IS.indexOf("E+")>0 ||IS.indexOf("E-")>0) {
                			length+=2;
                		}
                	}
                }
                
                if(length>maxLength)
                	maxLength=length;
            }

            if(maxLength>7) {
            	moreIEWidth=7+r*(float)(maxLength-7);
            }
            maxLength=0;
            for(int i=0;i<LevelY.length;i++){
                l=(Level)Levels.elementAt(i);
                                
                int length=0;
                
                String IS="",DIS="";
                if(isIB){               	
                    if(l.nBetas()>0&&type=='B'){
                    	IS=l.betaAt(0).AIS();
                    	DIS=l.betaAt(0).DAIS();
                        if(IS.length()==0){
                        	IS=l.betaAt(0).RIS();
                        	DIS=l.betaAt(0).DRIS();
                        }
                    }
                    else if(l.nECBPs()>0&&type=='E'){
                    	IS=l.ECBPAt(0).AIBS();
                    	DIS=l.ECBPAt(0).DAIBS();
                    	if(IS.length()==0){
                        	IS=l.ECBPAt(0).IBS();
                        	DIS=l.ECBPAt(0).DIBS();	
                    	}
                    }
                    else if(l.nAlphas()>0&&type=='A'){
                    	IS=l.alphaAt(0).AIS();
                    	DIS=l.alphaAt(0).DAIS();
                    	if(IS.length()==0){
                        	IS=l.alphaAt(0).RIS();
                        	DIS=l.alphaAt(0).DRIS();	
                    	}
                    }
                	
                    length=IS.length()+DIS.length();
                	if(IS.indexOf("E")>0) {
                		length=length+5;
                		if(IS.indexOf("E+")>0 ||IS.indexOf("E-")>0) {
                			length+=2;
                		}
                	}
                }
                
                if(length>maxLength)
                	maxLength=length;
            }


            if(maxLength>7)
            	moreIBWidth=7+r*(float)(maxLength-7)/7;
            
            float originalScale=0.5f;
            float scale=0.5f;
            
            boolean isIsomer=false;
            
            
            //debug
            //System.out.println("In DecayChart line 1030: nDrawing="+sec+"  LevelY.size="+LevelY.length+"  Levels.size="+Levels.size()+"  Gammas.size="+Gammas.size());
            
            for(int i=0;i<LevelY.length;i++){
                l=(Level)Levels.elementAt(i);
                
                isIsomer=false;
                
                if(l.EF()==0){ 
                	includeGS=true;
                	scale=2f;
                	tempOut.add("pickup pencircle scaled "+scale+";\n");
                }
                else if(l.EF()>0 && l.T12D()>1E-9 && (l.DT12S().length()==0 || l.DT12S().charAt(0)!='L')){//isomer
                	scale=1.2f;
                	isIsomer=true;
                	tempOut.add("pickup pencircle scaled "+scale+";\n");
                }
                else if(l.ES().contains("+") && l.msS().trim().equals("R")){
                	//scale=4f;
                	tempOut.add("pickup pencircle scaled "+scale+";\n");
                }
                	
                //debug
                //System.out.println("In DecayChart line 1052: l.ES="+l.ES()+" l.EF="+l.EF()+" l.ERF="+l.ERF()+" size="+Levels.size());
                
                //debug
                //System.out.println("T1/2="+l.T12LVD()+" e="+l.EF());
                
                if(l.ES().contains("+") && l.msS().trim().equals("R")){
                }
                else
                	tempOut.add("draw ("+levelX0+","+LevelL[i]+")--("+(leftmar+levelX0)+","+LevelL[i]+")--("+(leftmar+slopemar+levelX0)+","+LevelY[i]+")-");
                
                for(int j=0;j<Tics.size();j+=4){
                    if(Float.valueOf(Tics.elementAt(j).toString())==LevelY[i]){
                        if(Tics.elementAt(j+2)=="UP"){
                            tempOut.add("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString())+Float.valueOf(Tics.elementAt(j+3).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                        else if(Tics.elementAt(j+2)=="DOWN"){
                            tempOut.add("-("+(Float.valueOf(Tics.elementAt(j+1).toString())-3+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())-2+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())+2+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString())-Float.valueOf(Tics.elementAt(j+3).toString()))+")"
                            		 + "--("+(Float.valueOf(Tics.elementAt(j+1).toString())+3+(leftmar+slopemar+levelX0))+","+(Float.valueOf(Tics.elementAt(j).toString()))+")-");
                        }
                    }
                }
                
                if(l.ES().contains("+") && l.msS().trim().equals("R")){
                }
                else{
                	tempOut.add("-("+(width-(rightmar+slopemar)+levelX0)+","+LevelY[i]+")"
                			 + "--("+(width-rightmar+levelX0)+","+LevelL[i]+")"
                			 + "--("+(width+levelX0)+","+LevelL[i]+")");
                    if(l.q().equals("?")||l.q().equals("S"))
                    	tempOut.add(" dashed evenly");
                    
                    tempOut.add(";\n");
                }
                
                if(scale>originalScale){
                	scale=originalScale;
                	tempOut.add("pickup pencircle scaled "+scale+";\n");
                }
                
                float xleftSpin=-0.1f/NDSConfig.POINT2CM+levelX0; //note that all coordinates here are in units of points, in BandChart they are in units of cm
                
                if(l.EF()==0){
                    tempOut.add("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+xleftSpin+","+(LevelL[i])+"));\n");//minus 0.1f to account for the size of the boundary box of the point 
                    tempOut.add("label.ulft(btex "+levelLabelSize()+Translator.value(l.ES(),l.DES())+" etex,("+(width+levelX0)+","+(LevelL[i])+"));\n");
                }
                else if(l.ES().contains("+") && l.msS().trim().equals("R")){
                    tempOut.add("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+xleftSpin+","+(LevelL[i])+"));\n");//minus 0.1f to account for the size of the boundary box of the point 
                    tempOut.add("label.ulft(btex "+levelLabelSize()+Translator.value(l.ES(),l.DES())+" etex,("+(width+levelX0)+","+(LevelL[i]+1)+"));\n");
                    
                    
                   
                    
                    //draw hatched lines to represent a range of levels
                    float tempLevelY=LevelY[i]-10;
                    tempOut.add("draw ("+levelX0+","+tempLevelY+")--("+(width+levelX0)+","+tempLevelY+")  dashed evenly;\n");
                    
                    String es=Translator.value(l.ES(),l.DES());
                    int pos=l.ES().indexOf("+");
                    es=Translator.value(l.ES().substring(0, pos),l.DES());
                    
                    tempOut.add("label.llft(btex "+levelLabelSize()+es+" etex,("+(width+levelX0)+","+tempLevelY+"));\n");
                    
                    float d=6;
                    int N=(int)(width/d)+1;
                    for(int k=0;k<N;k++){
                    	float tempX=k*d;
                    	tempOut.add("draw ("+(tempX+levelX0)+","+tempLevelY+")--("+(tempX+d+levelX0)+","+LevelY[i]+");\n");
                    }
                }else{
                	float yoff=0;
                	if(isIsomer) yoff=1f;
                	
                    tempOut.add("label.urt(btex "+levelLabelSize()+Translator.spin(l.JPiS())+" etex,("+xleftSpin+","+(LevelL[i]-1+yoff)+"));\n");
                    tempOut.add("label.ulft(btex "+levelLabelSize()+Translator.plainValue(l.ES(),l.DES())+" etex,("+(width+levelX0)+","+(LevelL[i]-1+yoff)+"));\n");
                }
                
                //out.add("label.urt(btex "+levelLabelSize()+"$"+Translator.value(l.T12VS(),l.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(l.T12US())
                //		+"$ etex,("+(width+5+arrowW)+","+(LevelL[i]-2)+"));\n");
                tempOut.add("label.urt(btex "+levelLabelSize()+Translator.printNumber(l.T12S(),l.DT12S(),l.T12Unit())+" etex,("+(width+5+arrowW+levelX0)+","+(LevelL[i]-2)+"));\n");

                float offset=0;
                float extra=0;
                String IS="",DIS="";
                //debug
                //System.out.println("In DecayChart line 1011 arrowW="+arrowW+" maxLengthTS="+maxLengthTS+" defulatXOffset="+defaultXOffset);
                
                if(l.nECBPs()>0&&type=='E'){               	
                	offset=arrowW+5+maxLengthTS+5;//should be consistent with offset of T1/2 column and also leave a little gap after T1/2 column
                	if(offset<defaultXOffset)
                		offset=defaultXOffset;              	
                	               	
                	
                	//System.out.println("In decaychart line 1008: defaultoffset="+defaultXOffset+" maxLengthTS="+maxLengthTS);
                	
                	extra=0;
                	
                    tempOut.add("drawarrow ("+(width+arrowW+levelX0)+","+(LevelL[i]+arrowH)+")--("+(width+levelX0)+","+(LevelL[i])+")");                                  
                    if(l.ECBPAt(0).q().equals("?"))tempOut.add(" dashed evenly");
                    tempOut.add(";\n");
                    
                    if(l.msS().equals("R")) {//draw arrow at fake level for particle decay
                        String dpType=l.ECBPAt(0).dpType();
                        if(dpType.length()>0) {
                            if(dpType.length()==1 && dpType.equals("p"))
                                dpType="1p";
                            
                            tempOut.add("drawarrow ("+(xleftSpin-0.1)+","+(LevelL[i]-10)+")--("+(xleftSpin-0.1-arrowW)+","+(LevelL[i]-arrowH-10)+");\n");
                            
                            tempOut.add("label.ulft(btex "+levelLabelSize()+dpType+" etex,("+(xleftSpin-0.1-arrowW)+","+(LevelL[i]-arrowH-10)+"));\n");
                        }
                    }
                    
                    if(l.ECBPAt(0).coinS().contains("C")||l.ECBPAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+(width+levelX0)+","+(LevelL[i])+") withpen pencircle scaled 4;\n");
                    
                    if(l.ECBPAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+(width+levelX0)+","+(LevelL[i])+") withpen pencircle scaled 2.5 withcolor white;\n");
                    
                    
                    //for inset table
                    if(isIB){
                    	extra=0;
                    	if(tableColumns.indexOf("B")>tableColumns.indexOf("I"))
                    		extra=moreIEWidth;

                    	IS=l.ECBPAt(0).AIBS();
                    	DIS=l.ECBPAt(0).DAIBS();                     	
                      	
                    	if(IS.length()==0){
                        	IS=l.ECBPAt(0).IBS();
                        	DIS=l.ECBPAt(0).DIBS();
                    	}
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(IS,DIS)+"$ etex,("+(width+offset+(29*tableColumns.indexOf("B"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");
                    }
                    if(isIE){
                    	extra=0;
                    	if(tableColumns.indexOf("I")>tableColumns.indexOf("B"))
                    		extra=moreIBWidth;
                    	
                    	IS=l.ECBPAt(0).AIES();
                    	DIS=l.ECBPAt(0).DAIES();
                    	if(IS.length()==0){
                        	IS=l.ECBPAt(0).IES();
                        	DIS=l.ECBPAt(0).DIES();
                    	}
                    	
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(IS,DIS)+"$ etex,("+(width+offset+(29*tableColumns.indexOf("I"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");
                    }
                    //if(isE)out.add("label.urt(btex "+levelLabelSize()+"$"+makeData(l.ECBPAt(0).ES(),l.ECBPAt(0).DES())+"$ etex,("+(width+(29*(table.indexOf("E")+1))+arrowW+levelX0)+","+(LevelL[i]-2)+"));\n");
                    if(isLogft){
                    	extra=0;
                    	if(tableColumns.indexOf("L")>tableColumns.indexOf("B"))
                    		extra+=moreIBWidth;
                    	if(tableColumns.indexOf("L")>tableColumns.indexOf("I"))
                    		extra+=moreIEWidth;
                    	
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(l.ECBPAt(0).LOGFTS(),l.ECBPAt(0).DLOGFTS(),l.ECBPAt(0).US().trim().toLowerCase())
                    	+"$ etex,("+(width+offset+(29*tableColumns.indexOf("L"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");

                    }
                }else if(l.nBetas()>0&&type=='B'){
                	offset=-(arrowW+29*1.1f);//x position of the first column
                	extra=0;
                	
                	if(Math.abs(offset)<Math.abs(defaultXOffset))
                		offset=-Math.abs(defaultXOffset);   
                	
                    tempOut.add("drawarrow ("+(-arrowW+levelX0)+","+(LevelL[i]+arrowH)+")--("+(0+levelX0)+","+(LevelL[i])+")");
                    if(l.betaAt(0).q().equals("?"))tempOut.add(" dashed evenly");
                    tempOut.add(";\n");
                    
                    if(l.msS().equals("R")) {//draw arrow at fake level for particle decay
                        String dpType=l.betaAt(0).dpType();
                        if(dpType.length()>0) {
                            if(dpType.length()==1 && dpType.equals("n"))
                                dpType="1n";
                            
                            tempOut.add("drawarrow ("+(width+0.1+levelX0)+","+(LevelL[i]-10)+")--("+(width+0.1+arrowW+levelX0)+","+(LevelL[i]-arrowH-10)+");\n");
                            
                            tempOut.add("label.urt(btex "+levelLabelSize()+dpType+" etex,("+(width+arrowW+levelX0)+","+(LevelL[i]-arrowH-10)+"));\n");
                        }
                    }
                    
                    if(l.betaAt(0).coinS().contains("C")||l.betaAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+levelX0+","+(LevelL[i])+") withpen pencircle scaled 4;\n");
                    
                    if(l.betaAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+levelX0+","+(LevelL[i])+") withpen pencircle scaled 2.5 withcolor white;\n");
                    
                  
                    //for inset table
                    if(isIB){
                    	extra=moreIBWidth;
                    	
                    	IS=l.betaAt(0).AIS();
                    	DIS=l.betaAt(0).DAIS();
                    	if(IS.length()==0){
                           	IS=l.betaAt(0).RIS();
                        	DIS=l.betaAt(0).DRIS();
                    	}
                    	
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(IS,DIS)
                    	+"$ etex,("+(offset-(29*tableColumns.indexOf("I"))-extra+levelX0)+","+(LevelL[i]-1)+"));\n");
                    }
                    //if(isE)out.add("label.urt(btex "+levelLabelSize()+"$"+makeData(l.betaAt(0).ES(),l.betaAt(0).DES())+"$ etex,("+(-(29*(table.indexOf("E")+1.1))-arrowW+levelX0)+","+(LevelL[i]-1)+"));\n");
                    if(isLogft){
                    	extra=0;
                    	if(tableColumns.indexOf("L")>tableColumns.indexOf("I"))
                    		extra=moreIBWidth;
                    	
                    	//System.out.println("In decaychart line 1011: "+tableColumns.indexOf("L")+" offset="+offset);
                    	
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(l.betaAt(0).LOGFTS(),l.betaAt(0).DLOGFTS(),l.betaAt(0).US().trim().toLowerCase())
                    	+"$ etex,("+(offset-(29*tableColumns.indexOf("L"))-extra+levelX0)+","+(LevelL[i]-1)+"));\n");
                    }
                }else if(l.nAlphas()>0&&type=='A'){
                	extra=0;
                	offset=arrowW+5+maxLengthTS+5;
                	
                	if(offset<defaultXOffset)
                		offset=defaultXOffset;                  	
                	
                    tempOut.add("pickup pencircle scaled 2;\n");
                    tempOut.add("drawarrow ("+(width+arrowW+levelX0)+","+(LevelL[i]+arrowH)+")--("+(width+levelX0)+","+(LevelL[i])+")");
                    
                    if(l.alphaAt(0).q().equals("?"))
                    	tempOut.add(" dashed evenly");
                    
                    tempOut.add(";\n");
                    
                    if(l.alphaAt(0).coinS().contains("C")||l.alphaAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+(width+levelX0)+","+(LevelL[i])+") withpen pencircle scaled 4;\n");
                    if(l.alphaAt(0).coinS().contains("?"))
                    	tempOut.add("draw ("+(width+levelX0)+","+(LevelL[i])+") withpen pencircle scaled 2.5 withcolor white;\n");
                    
                    tempOut.add("pickup pencircle scaled .5;\n");
                    tempOut.add("drawarrow ("+(width+arrowW+levelX0)+","+(LevelL[i]+arrowH)+")--("+(width+levelX0)+","+(LevelL[i])+") withcolor white;\n");
                  
                    //for inset table
                    if(isIB){
                    	extra=0;
                    	
                    	IS=l.alphaAt(0).AIS();
                    	DIS=l.alphaAt(0).DAIS();
  
                    	if(IS.length()==0){
                        	IS=l.alphaAt(0).RIS();
                        	DIS=l.alphaAt(0).DRIS();
                    	}
                    	
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(IS,DIS)
                    	+"$ etex,("+(width+offset+(29*tableColumns.indexOf("I"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");
                    }
                    if(isHF){
                    	extra=0;
                    	if(tableColumns.indexOf("H")>tableColumns.indexOf("I"))
                    		extra+=moreIBWidth;
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(l.alphaAt(0).HFS(),l.alphaAt(0).DHFS())
                    	+"$ etex,("+(width+offset+(29*tableColumns.indexOf("H"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");
                    }
                    if(isE){
                    	extra=0;
                    	Alpha alp=l.alphaAt(0);
                    	String ES=alp.ES0();//original ES given in the dataset if existing
                    	String DES=alp.DES0();
           
                    	if(ES.isEmpty()) {
                    		//calculated E
                    		ES=alp.ES();
                    		DES=alp.DES();
                    	}
                    	tempOut.add("label.urt(btex "+levelLabelSize()+"$"+printValue(ES,DES)
                    	+"$ etex,("+(width+offset+(29*tableColumns.indexOf("E"))+extra+levelX0)+","+(LevelL[i]-2)+"));\n");
                    }
                }
            }
            
            
            //add ground state level 
            if(isGSExist && !includeGS){
            	scale=2f;
            	
            	float xleftSpin=-0.1f/NDSConfig.POINT2CM;
            	
            	tempOut.add("pickup pencircle scaled "+scale+";\n");
            	tempOut.add("draw ("+levelX0+",-20)---("+(width+levelX0)+","+"-20);\n");
                tempOut.add("label.urt(btex "+levelLabelSize()+Translator.spin(groundLevel.JPiS())+" etex,("+xleftSpin+",-20));\n");
                tempOut.add("label.ulft(btex "+levelLabelSize()+groundLevel.ES()+" etex,("+(width+levelX0)+",-20));\n");
                
                //out.add("label.urt(btex "+levelLabelSize()+"$"+Translator.value(groundLevel.T12VS(),groundLevel.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(groundLevel.T12US())
                //		+"$ etex,("+(width+5)+",-22));\n");
                tempOut.add("label.urt(btex "+levelLabelSize()+Translator.printNumber(groundLevel.T12S(),groundLevel.DT12S(),groundLevel.T12Unit())
                +" etex,("+(width+5+arrowW+levelX0)+",-22));\n");
                
            	tempOut.add("pickup pencircle scaled "+originalScale+";\n");
            }
            
            
            String label;
            String colour;
            String temp="";
            
            float len2x_digi=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for digit. 2.2f was set originally for gammaLabelSize=6pt
            float len2x_lett=2.2f*(dc.gammaLabelSize()+0.6f)/6f;//for letter and others
            
            
            useColorLine=true;
            if(PN==6 || PN==7 || nRI<2)
            	useColorLine=false;
           
            ngammas=0;
            if(GammaX!=null)
            	ngammas=GammaX.length;
            
            float topGap=110;//gap between the top level and bottom title
            
            for(int i=0;i<ngammas;i++){
                g=(Gamma)Gammas.elementAt(i);
                
                boolean toDraw=false;
                boolean unknown=false;
                float yi=0,yf=0;

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

                //System.out.println("In DecayChart line 948 g="+g.ES()+" FLI="+g.FLI()+" ILI="+g.ILI()+" Level="+g.ILS());
                
                if(toDraw){
                    colour="";
                    /* old
                    if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.1){
                        colour="withcolor red";
                    }else if(!g.IS().isEmpty()&&Float.valueOf(g.IS())>maxI*.02){
                        colour="withcolor blue";
                    }
                    */
                	
                    if(useColorLine&&!g.RIS().isEmpty()&&Str.isNumeric(g.RIS())){
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
                    //note that GammaX[i] here is the X position relative to x=0 for starting of the level line, that is levelX0=0,
                    //but for some drawing, levelX0 might be a negative value
                    breakLevelAtCross(tempOut,GammaX[i]+leftmar+slopemar+levelX0,LevelY,yi,yf);
                    
                    tempOut.add("drawarrow ("+(GammaX[i]+leftmar+slopemar+levelX0)+","+yi+")--("+(GammaX[i]+leftmar+slopemar+levelX0)+","+yf+")");
                    
                    if(g.q().equals("?") || g.q().equals("S")){
                    	tempOut.add(" dashed evenly ");
                    	hasUncertainGamma=true;
                    }
                    tempOut.add(colour+";\n");
                    
                    if(unknown || g.FLRecord().equals("?")){//if FL=?, it means there are multiple candidates for final level,choose the cloest one for drawing
                    	tempOut.add("label.bot(btex "+gammaLabelSize()+"? etex, ("+(GammaX[i]+leftmar+slopemar+levelX0)+","+yf+"));\n");
                    }
                        

                    float labelLength=(g.ES().trim().length()+1)*len2x_digi;
                    label=Translator.plainValue(g.ES(),g.DES());
                    if(label.length()>g.ES().length())//for uncertainty=AP,LT,LE,GT,GE,, or value is exponential
                    	labelLength+=1.5*len2x_lett;
                    
                    //label+=" "+g.MS()+" "+g.IS();
                    SDS2XDX s2x=RI(g,ens.norm());
                    temp=s2x.s();
              
                    
                    //System.out.println("In decaychart 1374: MS="+g.MS()+" RI="+g.RIS()+" normalized RI="+temp);
                    
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
                            
                            //debug
                            //System.out.println("In decayChart line 1085: s2x.s()="+s2x.s()+" ds="+s2x.ds()+" str="+str);
                            
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
                    //System.out.println("In DecayChart: PN="+PN+" g.IS(),DIS()="+g.IS()+" "+g.DIS()+" printedRI="+RI(g,ens.norm()).s()+" "+RI(g,ens.norm()).ds());
                    
                    
                    //these calculations rotate and shift a white box that goes behind the labels. Basic rotation matrix.
                    float xpos=(GammaX[i]+leftmar+slopemar+levelX0);
                    float ypos=LevelY[Levels.indexOf(ens.levelAt(g.ILI()))]+0.5f;
                    String s="("+(xpos*.5+ypos*0.866)+","+(-xpos*0.866+ypos*.5)+")--("
                            +((xpos*.5+(ypos)*0.866)+labelLength)+","+(-xpos*0.866+ypos*.5)+")--("
                            +((xpos*.5+(ypos)*0.866)+labelLength)+","+(-xpos*0.866+ypos*.5+5)+")--("
                            +(xpos*.5+(ypos)*0.866)+","+(-xpos*0.866+ypos*.5+5)+")";
                    
                    s=s.replace("E", "**");//metapost does not understand exponential notation, like 1.234E5
                    
                    tempOut.add("fill "+s+"--cycle rotated 60 withcolor white;\n");
                    
                    tempOut.add("label.urt(btex "+gammaLabelSize()+label+" etex rotated 60,("+(xpos-6)+","+(ypos-1)+"));\n");
                    
                    if(g.coinS().contains("C")||g.coinS().contains("?")){
                    	tempOut.add("draw ("+xpos+","+(ypos)+") withpen pencircle scaled 4;\n");
                    	hasGCOIN=true;
                    }
                    if(g.coinS().contains("?")){
                    	tempOut.add("draw ("+xpos+","+(ypos)+") withpen pencircle scaled 2.5 withcolor white;\n");
                    	hasUncertainGCOIN=true;
                    }
                    
                    if(ypos+labelLength>height+topGap){
                    	topGap=ypos+labelLength-height;
                    }
                    
                }
            }
            
            
            //parent level
            tempOut.add("label(btex $^{"+ens.parentAt(0).nucleus().A()+"}_{"+ens.parentAt(0).nucleus().ZS()+"}$"+ens.parentAt(0).nucleus().En()
            		+"$_{"+ens.parentAt(0).nucleus().N()+"}^{~}$ etex,(");
            
            if(type=='B')
            	tempOut.add((-60+levelX0)+",");
            else if(type=='A') 
            	tempOut.add((width+66+levelX0)+",");
            else 
            	tempOut.add((width+60+levelX0)+",");
            
            tempOut.add((LevelL[LevelL.length-1]+46)+"));\n");
            
            //The Parent Records
            int pGap=25;
            for(int p=0;p<ens.nParents();p++){
                //Parent Level
                if(p==0)tempOut.add("pickup pencircle scaled 2;\n");
                tempOut.add("draw (");
                if(type=='B')
                	tempOut.add((-90+levelX0)+",");
                else 
                	tempOut.add((width+30+levelX0)+",");
                
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+70)+")--(");
                if(type=='B')
                	tempOut.add((-30+levelX0)+",");
                else if(type=='A')
                	tempOut.add((width+105+levelX0)+",");
                else
                	tempOut.add((width+90+levelX0)+",");
                
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+70)+");\n");

                //Arrow
                if(type=='A'){
                    tempOut.add("drawarrow ("+(width+95+levelX0)+","+(LevelL[LevelL.length-1]+p*pGap+70)+")--("+(width+95+levelX0)+","+(LevelL[LevelL.length-1]+p*pGap+70-arrowH)+");\n");
                    tempOut.add("pickup pencircle scaled .5;\n");
                    tempOut.add("drawarrow ("+(width+95+levelX0)+","+(LevelL[LevelL.length-1]+p*pGap+70)+")--("+(width+95+levelX0)+","+(LevelL[LevelL.length-1]+p*pGap+70-arrowH)+") withcolor white;\n");
                }else{
                    tempOut.add("pickup pencircle scaled .5;\n");
                    tempOut.add("drawarrow (");
                    if(type=='B')
                    	tempOut.add((-30+levelX0)+",");
                    else 
                    	tempOut.add((width+30+levelX0)+",");
                    
                    tempOut.add((LevelL[LevelL.length-1]+p*pGap+70)+")--(");
                    if(type=='B')
                    	tempOut.add((arrowW-30+levelX0)+",");
                    else 
                    	tempOut.add((width+30-arrowW+levelX0)+",");
                    
                    tempOut.add((LevelL[LevelL.length-1]+p*pGap+70-arrowH)+");\n");
                }

                Level pl=ens.parentAt(p).level();
                
                //Energy                
                tempOut.add("label.ulft(btex "+levelLabelSize()+Translator.plainValue(pl.ES(),pl.DES())+" etex, (");
                if(type=='B')
                	tempOut.add((-30+levelX0)+",");
                else if(type=='A') 
                	tempOut.add((width+105+levelX0)+",");
                else 
                	tempOut.add((width+90+levelX0)+",");
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+70)+"));\n");

                //JPi
                tempOut.add("label.urt(btex "+levelLabelSize()+Translator.spin(pl.JPiS())+" etex, (");
                if(type=='B')
                	tempOut.add((-90+levelX0)+",");
                else 
                	tempOut.add((width+30+levelX0)+",");
                
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+70)+"));\n");

                //Half-Life
                //out.add("label.urt(btex "+levelLabelSize()+Translator.value(pl.T12VS(),pl.DT12S()).toLowerCase()+"~"+Translator.halfLifeUnits(pl.T12US())
                //		+" etex, (");
                tempOut.add("label.urt(btex "+levelLabelSize()+Translator.printNumber(pl.T12S(),pl.DT12S(),pl.T12Unit())+" etex, (");
    
                
                if(type=='B')
                	tempOut.add((-25+levelX0)+",");
                else if(type=='A') 
                	tempOut.add((width+110+levelX0)+",");
                else 
                	tempOut.add((width+95+levelX0)+",");
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+68)+"));\n");

                //Q
                if(ens.parentAt(p).QS().length()>0){
                    tempOut.add("label(btex "+levelLabelSize()+"Q");
                    if(type=='B')
                    	tempOut.add("$_{\\beta^{-}}$");
                    else if(type=='E')
                    	tempOut.add("$_{\\varepsilon}$");
                    else if(type=='A')
                    	tempOut.add("$_{\\alpha}$");
                    
                    
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
                    	
                    	qv=qv.add(eps,deps);
                    	
                    	//System.out.println("DecayChart line 1218: qs="+qs+" dqs="+dqs+" level="+ens.parentAt(p).level().ES()+" qv.s="+qv.S()+" qv.ds="+qv.ds());
                    	
                    	qs=qv.S();
                    	dqs=qv.DS();
                    }
                    
                    if(!dc.isShowQValueUnit())
                    	tempOut.add(LatexWriter.printNumber(qs, dqs,true)+" etex, (");
                    else
                    	tempOut.add(LatexWriter.printNumber(qs, dqs,"keV",true)+" etex, (");
                    
                    if(type=='B') 
                    	tempOut.add((-60+moveQ+levelX0)+",");
                    else if(type=='A') 
                    	tempOut.add((width+60+moveQ+levelX0)+",");
                    else 
                    	tempOut.add((width+60+moveQ+levelX0)+",");
                    
                    tempOut.add((LevelL[LevelL.length-1]+p*pGap+62)+"));\n");               	
                }


                //B.R.
                tempOut.add("label(btex "+levelLabelSize()+"$\\%");
                if(type=='B')
                	tempOut.add("\\beta^{-}");
                else if(type=='E'){
                	tempOut.add("\\epsilon");
                    if(ens.parentAt(p).QF()>1022)tempOut.add("+\\%\\beta^{+}");
                }
                else if(type=='A')
                	tempOut.add("\\alpha");
                else
                	tempOut.add(Translator.process(ens.decayTypeInDSID()));
                
                //debug
                //System.out.println("In DecayChart: decayType="+type+" QP="+ens.parentAt(p).QF());
                //System.out.println("               ens.norm().BRS().isEmpty()="+ens.norm().BRS().isEmpty()+" ens.norm().BRS()="+ens.norm().BRS());
                
                Normal norm=ens.norm();
                if(ens.nNorms()>0 && p<ens.nNorms())
                	norm=ens.normAt(p);
                else
                	norm=null;
                
                if(norm!=null && norm.BRS()!=null && !norm.BRS().isEmpty()){
                	/*
                	SDS2XDX BR=new SDS2XDX();
                	BR.setValues(norm.BRS(), norm.DBRS());
                	BR=BR.multiply(100.f);
                	*/
                	String s=norm.BRS();
                	String ds=norm.DBRS();
                	
                	s=Str.shiftDecimalPoint(s,2,"RIGHT");
                	
                	if(ds.trim().length()==0 || Str.isNumeric(ds))
                		tempOut.add("$="+s+" etex, (");
                	else
                		tempOut.add("$"+Translator.plainValue(s,ds)+" etex, (");
                }else
                	tempOut.add("$=?"+" etex, (");//for SF decay and others like 14C decay, and so on
                
                if(type=='B')
                	tempOut.add((-18+arrowH+levelX0)+",");
                else if(type=='A')
                	tempOut.add((width+120+levelX0)+",");
                else 
                	tempOut.add((width+12-arrowH+levelX0)+",");
                
                tempOut.add((LevelL[LevelL.length-1]+p*pGap+70-arrowH*0.5)+"));\n");
            }
            
            //debug
            //System.out.println("In DecayChart line804: isLogft="+isLogft+" isIB="+isIB);
            
            //Inset Table Title
            float offset=0;
            float extra=0;
            if(type=='E'){
            	offset=arrowW+5+maxLengthTS+5;//must be consistent with offset set for IE column of each level
            	extra=0;
            	
            	if(offset<defaultXOffset)
            		offset=defaultXOffset;  
            	
            	dc.setDecayTableXOffset(offset*NDSConfig.POINT2CM);
            	
                if(isIB){
                	extra=0;
                	if(tableColumns.indexOf("B")>tableColumns.indexOf("I"))
                		extra=moreIEWidth;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{I$\\beta^{+}$} etex,("
                		+(width+offset+(29*tableColumns.indexOf("B"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                    
                	//System.out.println("In DecayChart line 1319: width="+width+"  offset="+offset+"  "+(29*(tableColumns.indexOf("B")+1))+" "+arrowW+" "+tableColumns.indexOf("B"));
                }
                if(isIE){
                	extra=0;
                	if(tableColumns.indexOf("I")>tableColumns.indexOf("B"))
                		extra=moreIBWidth;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{I$\\epsilon$} etex,("
                		+(width+offset+(29*tableColumns.indexOf("I"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
                //if(isE)out.add("label.urt(btex "+otherLabelSize()+"\\underline{E$\\epsilon$} etex,("+(width+(29*(table.indexOf("E")+1))+arrowW)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                if(isLogft){
                	extra=0;
                	if(tableColumns.indexOf("L")>tableColumns.indexOf("I"))
                		extra+=moreIEWidth;
                	if(tableColumns.indexOf("I")>tableColumns.indexOf("B"))
                		extra+=moreIBWidth;
                	
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{Log $ft$} etex,("
                	+(width+offset+(29*tableColumns.indexOf("L"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                	
                	//System.out.println("In DecayChart line 1507: pos="+((width+offset+(29*tableColumns.indexOf("L"))+extra)/28.3464567)+" extra="+(extra/28.3464567)+" offset="+(offset/28.3464567)+" width="+(width/28.3464567));

                }
            }else if(type=='B'){
            	offset=-(arrowW+29*1.1f);
            	extra=0;
            	
            	if(Math.abs(offset)<Math.abs(defaultXOffset))
            		offset=-Math.abs(defaultXOffset);  
            	
            	dc.setDecayTableXOffset(offset*NDSConfig.POINT2CM);
            	
                if(isIB){
                	extra=moreIBWidth;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{I$\\beta^{-}$} etex,("
                	+(offset-(29*tableColumns.indexOf("I"))-extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
                
                //if(isE)out.add("label.urt(btex "+otherLabelSize()+"\\underline{E$\\beta^{-}$} etex,("+(-(29*(table.indexOf("E")+1.1))-arrowW)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                
                if(isLogft){
                	extra=0;
                	if(tableColumns.indexOf("L")>tableColumns.indexOf("I"))
                		extra=moreIBWidth;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{Log $ft$} etex,("
                		+(offset-(29*tableColumns.indexOf("L"))-extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
            }else if(type=='A'){
            	offset=arrowW+5+maxLengthTS+5;
            	extra=0;
            	
            	
            	if(offset<defaultXOffset)
            		offset=defaultXOffset; 
            	
            	dc.setDecayTableXOffset(offset*NDSConfig.POINT2CM);
            	
                if(isIB){
                	extra=0;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{I$\\alpha$} etex,("
                	+(width+offset+(29*tableColumns.indexOf("I"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
                if(isHF){
                	extra=0;
                	if(tableColumns.indexOf("H")>tableColumns.indexOf("I"))
                		extra=moreIBWidth;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{HF} etex,("
                		+(width+offset+(29*tableColumns.indexOf("H"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
                if(isE){
                	extra=0;
                	tempOut.add("label.urt(btex "+otherLabelSize()+"\\underline{E$\\alpha$} etex,("
                	+(width+offset+(29*tableColumns.indexOf("E"))+extra+levelX0)+","+(LevelL[LevelL.length-1]+10)+"));\n");
                }
            }
            

            // title of dataset
            float tempY=height+topGap;
            float tempH=height;
            if(dc.isShowTitle()){
                String TopTitle="";
                String tempID="";
                
                if(LevelY[LevelY.length-1]>height)
                   	tempY=LevelY[LevelY.length-1]+topGap;
                
                if(!supFlag){  
                	//DO NOT APPLY levelX0 here, since the titles are to be center aligned with the page center, not the center of of level lines
                	//width/2+levelX0 would be the center of level lines
                    if(atflag){
                    	tempOut.add("label(btex "+otherLabelSize()+"@ Multiply placed: intensity suitably divided etex,("+((width)/2)+","+tempY+"));\n");
                    	tempY=tempY+10;
                    }
                    if(andflag){
                    	tempOut.add("label(btex "+otherLabelSize()+"\\& Multiply placed: undivided intensity given etex,("+((width)/2)+","+tempY+"));\n");
                        tempY=tempY+10;
                    }
                }
                
                if(!supPN&&((PN<8&&PN>=0) || !CPN.isEmpty()) && ens.nGamWI()>0){
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
                        		   tempOut.add("label(btex "+otherLabelSize()+"Intensities: ");
                        	   else
                        		   tempOut.add("label(btex "+otherLabelSize()+" ");
                        	   
                        	   tempOut.add(Translator.translate(linesV.get(j)));   
                               tempOut.add(" etex,("+((width)/2)+","+tempY+"));\n");
                               tempY=tempY+10;
                           }                  
                	  
                    	}               	   
                    }
                    else if(PN<8 && PN>=0){
                        tempOut.add("label(btex "+otherLabelSize()+"Intensities: ");
                        
                        switch(PN){
                        case 1:tempOut.add("Relative I$_{(\\gamma+ce)}$");break;
                        case 2:tempOut.add("I$_{(\\gamma+ce)}$ per 100 decays through this branch");break;
                        case 3:tempOut.add("I$_{(\\gamma+ce)}$ per 100 parent decays");break;
                        case 4:tempOut.add("I$_{\\gamma}$ per 100 parent decays");break;
                        case 5:tempOut.add("Relative I$_{\\gamma}$");break;
                        case 6:tempOut.add("Relative photon branching from each level");break;
                        case 7:tempOut.add("\\% photon branching from each level");break;
                        case 0:
                        	tempOut.add("Type not specified");                   	
                        	
                        	break;
                        			
                        }
                        
                        tempOut.add(" etex,("+((width)/2)+","+tempY+"));\n");
                        tempY=tempY+10;
                    }

                }
                
                
                for(int i=0;i<SubTitle.size();i++)
                	tempOut.add("label(btex "+Translator.process(SubTitle.elementAt(i))+" etex,("+(width/2)+","+(tempY+10*i)+"));\n");
                
                tempY=tempY+10*SubTitle.size()+5;
                
                tempOut.add("label(btex \\underline{Decay Scheme");
                if(sec!=0)tempOut.add(" (continued)");
                
                tempOut.add("} etex,("+((width)/2)+","+tempY+"));\n");
                
                tempY=tempY+25;
                
                
                TopTitle+="{\\bf \\small \\underline{";
                
                TopTitle+=LatexWriter.makeDatasetTitleAndRef(etd, true);//addLink=true, but link doesn't work in metapost
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
                
                TopTitle+="}}";
                
                tempOut.add("label(btex "+TopTitle+" etex,("+((width)/2)+","+tempY+"));\n");
                                         
            }
            
            tempH=tempY;
            
            //nucleus name at bottom
            tempY=-20;
            if(isGSExist&&!includeGS)//a ground level line is added
            	tempY=-40;
            
            tempH=tempH-tempY;
            
            tempOut.add("label(btex $^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}$"+ens.nucleus().En()+"$_{"+ens.nucleus().N()+"}^{~}$ etex,("+width/2+","+tempY+"));\n");
            
            float legendExtra=0;
            if(useColorLine || hasGCOIN || hasUncertainGCOIN || hasUncertainGamma){
            	//LegendChart lc=new LegendChart();
            	//lc.drawChart(out,false);
            	
            	//legend box width ~ 120pt for "-----> Ig>10%XIg^max"
            	float x=width/2+100;   //width is the actual width of drawing, not the page width
            	float y=height+70;
            	
            	if(type=='B') {
            		if(x>(width+maxLengthTS-20)){
            			legendExtra=20;
            		}
            	}else{
            		if(width/2<120)
            			x=width/2-200;
            		else
            			x=width/2-220;
            		
            		if(x<-20) {
            			levelX0=x/2;
            			legendExtra=-x/2;
            		}
            	}

            	//System.out.println(" y="+y+" LevelL[LevelL.length-1]="+LevelL[LevelL.length-1]);
            	
            	if(y<(LevelL[LevelL.length-1]+5)){
            		y=LevelL[LevelL.length-1]+8;
            	}
            			
            	if(dc.isShowLegend())
            		drawLegend(tempOut,x,y);//(x,y) is the lower-left corner of legend rectangle
            	
            	//System.out.println("DecayChart 1882: sec="+sec+"  legend x="+x+" y="+y+" page width (point)="+width);
            	
            	hasGCOIN=false;
            	useColorLine=false;
            	hasUncertainGCOIN=false;
            	hasUncertainGamma=false;
            }
            
            writeFigureTail(tempOut);
                                             
            extra+=legendExtra;
            
            widths[sec]=width+Math.abs(offset)+extra+29*tableColumns.size();
            //if(dc.isShowTitle())
            //	heights[sec]=height+165+10*SubTitle.size()+20-tempY;
            //else
            //	heights[sec]=height+70-tempY;
            heights[sec]=tempH+20;
                                 
        	//debug
        	//System.out.println("In DecayChart line 1720:  sec="+sec+" preMinLI="+preMinLI+" start="+start+" end="+end+" width="+width+" total width="+widths[sec]+" max="+(pageWidth*CM_TO_POINT+20)); 
        	//System.out.println("     "+drw.isPortrait());
            //System.out.println((width+Math.abs(offset)+extra+29*tableColumns.size())/28.3464567+" width="+(width+150)/28.3464567);
            //System.out.println("   x gap="+(GammaX[1]-GammaX[0]));
        	
            if(widths[sec]>(pageWidth*CM_TO_POINT+20) && ntries<5){
            	int n=0;
            	float gap=0;
            	if(GammaX!=null && GammaX.length>(defaultNgamperpage-10) && GammaX.length>1){
                	gap=(GammaX[1]-GammaX[0]);
                	n=(int)((widths[sec]-(pageWidth*CM_TO_POINT))/gap);
            	}

            	//System.out.println(" x gap="+gap+"  minXGap="+(BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT*1.1));
            	
            	boolean redraw=false;
            	if(gap>BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT*1.1 || n==0){
            		//temporarily used to reset the width for redrawing
                	widths[sec]=pageWidth*CM_TO_POINT+15-(Math.abs(offset)+extra+29*tableColumns.size()); 
                	heights[sec]=defaultChartHeight;
                	redraw=true;
            	}else{            	
                	n=n/2;
                	if(n>1 || (n==1 && sec<nDrawings-1)){
                		
                    	//System.out.println((int)((widths[sec]-(pageWidth*CM_TO_POINT))/gap)+"  "+ngamperpages[sec]+" "+widths[sec]+" pageWidth="+pageWidth+" "+(pageWidth*CM_TO_POINT)+" start="+start+" end="+end); 
                    	//System.out.println("  ngp="+ngamperpages[sec]+" n="+(int)((widths[sec]-(pageWidth*CM_TO_POINT))/gap)+" chart WIDTH="+defaultChartWidth+" width="+width);
                    	//System.out.println("  Gammas.size="+Gammas.size()+" ng="+ng+" pW="+pW+"  "+defaultChartWidth*(float)Math.pow((double)Gammas.size()/(double)ng,pW));
     
                    	ngamperpages[sec]-=n;//re-draw the page   
                    	
                		//temporarily used to reset the width for redrawing
                    	widths[sec]=defaultChartWidth;
                    	heights[sec]=defaultChartHeight;
                    	
                    	redraw=true;
                	}
            	}
            	
            	//System.out.println("In DecayChart line 1744: gap="+gap+" BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT*1.1="+(BaseChart.GAP_BETWEEN_GAMMAS*CM_TO_POINT*1.1)+" ndrawings="+nDrawings);
            	//System.out.println("  redraw="+redraw+"  n="+n+" width="+widths[sec]);
            	
            	if(redraw){
                	sec=sec-1;
                	end=start;
     
                	//restore the original values for re-drawing
                	ngcurrent=old_ngcurrent;
                	ngrest=old_ngrest;
                	readNewGammaLevel=old_readNewGammaLevel;
                	
                	ntries++;
                	
                	continue;
            	}else {
            		levelX0=0;
            	}
            }
            
            
            ntries=0;
            
            decayFedLevelsDrawn.addAll(decayFedLevelsAbove);
            decayFedLevelsDrawn.addAll(decayFedLevelsBelow);
            
            preMinLI=tempPreMinLI;//for next page
            

            //rest gammas can not fit in last page, then increase page number
            if(sec==nDrawings-1 && end<=GammaT.size()-1){
            	nDrawings++;
            }
            
            if(!tempOut.isEmpty())
            	out.addAll(tempOut);
            	
            //System.out.println("In DecayChart line 1761: ###  Gammas.size="+Gammas.size()+" GammaT.size="+GammaT.size()+" start="+start+" end="+end+" sec="+sec+" ndrawings="+nDrawings);
            
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

    private String printValue(String data, String dx){
    	return printValue(data,dx,"");
    }
    
    //print only the value
    private String printValue(String data, String dx,String flagS){
        String b="";
        if(dx.equals("LT"))b+="<\\hspace{-0.05cm}";
        if(dx.equals("GT"))b+=">\\hspace{-0.05cm}";
        if(dx.equals("LE"))b+="\\leq\\hspace{-0.05cm}";
        if(dx.equals("GE"))b+="\\geq\\hspace{-0.05cm}";
        if(dx.equals("AP"))b+="\\approx\\hspace{-0.05cm}";
        
        if(data.indexOf("E")>0 && Str.isNumeric(data)){
        	float value=Float.valueOf(data);
        	if(Math.abs(value)<1.0E5 && Math.abs(value)>1.0E-5){
                data=b+Str.deformatExpo(data);
                return data;
        	}
        		
        }
        data=b+Translator.exponential(data);
        if(flagS.length()>0)
        	data+="\\ensuremath{^{"+flagS+"}}";
        return data;
    }

}
@SuppressWarnings("rawtypes")
class GammaComparator implements Comparator{
    public int compare(Object one,Object two){
        Gamma a=(Gamma)one;
        Gamma b=(Gamma)two;
        int out;
        if(a.ILI()>b.ILI())out=-1;
        else if(a.ILI()<b.ILI())out=1;
        else out=0;
        return out;
    }
}
