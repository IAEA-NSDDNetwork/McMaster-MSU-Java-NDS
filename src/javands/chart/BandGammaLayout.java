package javands.chart;
import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.ensdf.*;
/**
 * Keeps track of the various positioning information for gammas
 * within a band.  This only handles gammas that stay within their band
 *
 * @author Roy Zywina
 */
public class BandGammaLayout {
	
    protected class GammaWrap{
        public Gamma gamma=null;
        public Band band=null;
        public Level startLevel=null,endLevel=null;
        public float pos=0;
        public boolean inband=false;
        public int number=0;
        public int deltaJ=-100;
        public int nlevelCrossed=-1;//number of levels crossed between (not including) starting level and end level
    }
    java.util.Vector<GammaWrap> gammaWraps;
    // maximumnumber of columns gammas will be squeezed into
    protected final int MAXCOL=512;
    // class to help determine gamma placement
    private class LevelWrap{
        public Level level;
        public int[] gamnumsAbove,gamnumsBelow;
        public LevelWrap(Level l){
            level = l;
            
            //added by Jun, 6/9/2016
            //
            //NOTE: 1,index of following arrays is gcol
            //      2,connected transitions of different levels are in the same column and thus have the same index
            //      3,for a gamma line crossing a level, the index (number) of this gamma is stored in both arrays (above and below) of the crossed level
            //      4,for a gamma connecting two levels, the index of this gamma is stored in gamnumsAbove of final level and gamnumsBelow of initial level
            //        also in both arrays (above and below) of all crossed levels in between.
            
            gamnumsAbove = new int[MAXCOL];//array to store the indices of feeding gammas above this level, and gammas that cross this level
            gamnumsBelow = new int[MAXCOL];//array to store the indices of decaying gammas below this level, and gammas that cross this level
            
            Arrays.fill(gamnumsAbove,0);
            Arrays.fill(gamnumsBelow,0);
        }
        // defined so i can use this object properly in a Set
        @SuppressWarnings("unused")
		public boolean equals(LevelWrap l2){
            return level.ES().equals(l2.level.ES());
        }
    }
    
    LevelWrap[] levelWraps;
    Vector<Vector<GammaWrap>> gammaColumns=new Vector<Vector<GammaWrap>>();//store gammas in columns
    int numcols=0;
    int indexOfPrimaryColumn=0;//longest gamma column
   
    protected boolean hasDeltaJColumn(int deltaJ){
    	if(getColumnOfDeltaJ(deltaJ)>=0)
    		return true;
    	
    	return false;
    }
    
    protected int getColumnOfDeltaJ(int deltaJ){
    	int ncol=-1;
    	for(int col=0;col<deltaJOfColumns.length;col++){
    		int dj=deltaJOfColumns[col];
    		if(dj<0)
    			break;
    		
    		if(dj==deltaJ)
    			ncol=col;
    	}
    	
    	return ncol;   	
    }
    
    /*Note: This is only for in-band gamma transition*/
    protected void levAddGamma(GammaWrap g,int gamnum){
        //NOTE: the gammaWraps must have been sorted by calling sortGammas() in the specified order 
        	
    	//System.out.printf("%% %2d, %10s, %10s\n",new Integer(gamnum),
        //        g.startLevel.ES(),g.gamma.ES());
        g.number = gamnum;
        int i;
        int start=-1,end=-1;
       
        for (i=0; i<levelWraps.length; i++){
            if (g.startLevel.equals(levelWraps[i].level)){
                start=i;
            }
            if (g.endLevel.equals(levelWraps[i].level)){
                end=i;
            }
        }
        if (start<0 || end<0 || start<=end){
            return; // shouldn't happen
        }

        int nlevelCrossed=0;
        Vector<Level> bandLevels=g.band.levels();
        for (i=start-1; i>end; i--){
        	//System.out.println("In BandGammLayout line 106 eg="+g.gamma.ES()+" startlevel="+start+" end="+end+" leveli="+i+" "+levelWraps[i].level.ES());
        	if(bandLevels.contains(levelWraps[i].level))
        		nlevelCrossed++;       	
        }
        
        g.nlevelCrossed=nlevelCrossed;
        
        // find an open gamma column that a gamma line can fit in.
        // continue to move to next column (right) until one is found. 
        
        int col;
        for (col=0;col<levelWraps[start].gamnumsBelow.length;col++){
        	int thisColumnDeltaJ=deltaJOfColumns[col];//=-1, if this column has never been filled before
        	int thisColumnDeltaL=deltaLOfColumns[col];
        	
        	
        	//System.out.println("####BandGammaLabel line 122 col="+col+" levelWraps[start]="+levelWraps[start].level.JPiS()+"  levelWraps[end]="+levelWraps[end].level.JPiS());
        	//System.out.println("                      levelWraps[start].gamnumsBelow[col]="+levelWraps[start].gamnumsBelow[col]+"  levelWraps[end].gamnumsAbove[col]="+levelWraps[end].gamnumsAbove[col]);
        	
            if (levelWraps[start].gamnumsBelow[col]==0 && levelWraps[end].gamnumsAbove[col]==0){
                boolean isGoodColumn=true;
                for (i=start-1; i>end; i--){
                    isGoodColumn = levelWraps[i].gamnumsBelow[col]==0 && levelWraps[i].gamnumsAbove[col]==0;
                    if (!isGoodColumn) 
                    	break;
                }
                
                //System.out.println("*****col="+col+" nbelow="+levelWraps[start].gamnumsBelow.length+" good="+isGoodColumn+" g.es="+g.gamma.ES()+" g.band.ncol="+g.band.nColumns()
                //+"  prevGammaNum="+levelWraps[start].gamnumsAbove[col]+" thisColumnDeltaJ="+thisColumnDeltaJ);
                
                //check if the available empty column space has,
                //First,  the same deltaJ (non-empty) as any of the existing columns, if not, move to the new column
                //Second, ( when no JPI are given) the same number of levels crossed as any of the existing columns, if not move 
                //
                if(isGoodColumn){
                	
                	int prevGammaNum=levelWraps[start].gamnumsAbove[col];//NOTE gamNum=(gamIndex+1)
            		GammaWrap prevGamma=null;//previous gamma in the current gamma column=col
            		int prevGammaDJ=-100;
            		int prevGammaDL=-1;//number of levels crossed of gamma
            		
					
                	if(g.band.nColumns()>=1){//even if ncolumns=1 (based on number of in-band transtions from each level), transitions can still be assigned to different column if deltaJ is different
                		if(prevGammaNum>0){
                			prevGamma=gammaWraps.get(prevGammaNum-1);
                			
                			prevGammaDJ=prevGamma.deltaJ;
                			prevGammaDL=prevGamma.nlevelCrossed;
                    		
                    		if(g.deltaJ>=0 && g.deltaJ!=prevGammaDJ && g.deltaJ!=thisColumnDeltaJ){
                    			
                    			if(col==0 && thisColumnDeltaJ<0 && prevGammaDJ<0 && thisColumnDeltaL==0 && g.nlevelCrossed==prevGammaDL && gammaColumns.size()==1)
                    				isGoodColumn=true;
                    			else
                    				isGoodColumn=false;
                    			
                    		}else if(g.deltaJ<0 && prevGammaDJ<0 && thisColumnDeltaJ<0){
                    			if(g.nlevelCrossed!=prevGammaDL && g.nlevelCrossed!=thisColumnDeltaL){
                        			isGoodColumn=false;
                    			}
                    		}
                		}else if(thisColumnDeltaJ>=0){
                			if(g.deltaJ!=thisColumnDeltaJ)
                				isGoodColumn=false;
                			else{//for case where there are multiple columns with same deltaJ, choose the column that has prevGamma
                				for(int j=0;j<gammaColumns.size();j++){
                					if(deltaJOfColumns[j]==g.deltaJ && j!=col){
                						//since gammas are filled from top to bottom, levelWraps[start].gamnumsBelow[j]==0 will assure there is nothing below
                						//current level in current column
                						if(levelWraps[start].gamnumsAbove[j]>0 && levelWraps[start].gamnumsBelow[j]==0){
                							
                							//prevGammaNum=levelWraps[start].gamnumsAbove[j];
                                			//prevGamma=gammaWraps.get(prevGammaNum-1);
                                			//prevGammaDJ=prevGamma.deltaJ;            							
                                			//System.out.println("In BandGammaLayout line 169: j="+j+" col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length+" gammaColumns.size()="+gammaColumns.size());
                							//System.out.println("  g.deltaJ="+g.deltaJ+" prevDJ="+prevGammaDJ+" g.e="+g.gamma.ES()+" preG.e="+prevGamma.gamma.ES());
                							
                							col=j;
                							break;
                						}
                					}
                				}
                			}
                		}else if(thisColumnDeltaL>=0){
                			//debug                            			
                			//System.out.println("In BandGammaLayout line 179:  col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length);
							//System.out.println("  g.deltaJ="+g.deltaJ+" preGN="+prevGammaNum+" prevDJ="+prevGammaDJ+" g.e="+g.gamma.ES()+" g.nlevelCrossed="+g.nlevelCrossed+" thisColumnDeltaL="+thisColumnDeltaL);
							
                			if(g.nlevelCrossed!=thisColumnDeltaL)
                				isGoodColumn=false;
                			else{//for case where there are multiple columns with same deltaL, choose the column that has prevGamma
                				for(int j=0;j<gammaColumns.size();j++){
                					if(deltaLOfColumns[j]==g.nlevelCrossed && j!=col){
                						//since gammas are filled from top to bottom, levelWraps[start].gamnumsBelow[j]==0 will assure there is nothing below
                						//current level in current column
                						if(levelWraps[start].gamnumsAbove[j]>0 && levelWraps[start].gamnumsBelow[j]==0){
                							
                							//prevGammaNum=levelWraps[start].gamnumsAbove[j];
                                			//prevGamma=gammaWraps.get(prevGammaNum-1);
                                			//prevGammaDL=prevGamma.nlevelCrossed;            							
                                			//System.out.println("In BandGammaLayout line 195: j="+j+" col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length+" gammaColumns.size()="+gammaColumns.size());
                							//System.out.println("  g.nlevelCrossed="+g.nlevelCrossed+" prevDL="+prevGammaDL+" g.e="+g.gamma.ES()+" preG.e="+prevGamma.gamma.ES());
                							
                							col=j;
                							break;
                						}
                					}
                				}
                			}              		 
                		}

                	}
                	//debug
                	//System.out.println("In BandGammaLayout line 208: col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length);
                	//System.out.println("  g.deltaJ="+g.deltaJ+" prevDJ="+prevGammaDJ+" g.e="+g.gamma.ES()+" isGoodColumn="+isGoodColumn);
                	//if(prevGamma!=null) System.out.println("  prevGamma.e="+prevGamma.gamma.ES());
                }
                
                if (isGoodColumn) 
                	break;
            }
        }
        
        
        
        if (col>=levelWraps[start].gamnumsBelow.length) return; // shouldn't happen
        
        //fill the gamma (set gamnum) at gamma column=col
        //if there are more than one column in this band, group the gamma into different columns based on deltaJ
        //otherwise put all gammas in the same column.
        
        levelWraps[start].gamnumsBelow[col]=gamnum;
        levelWraps[end].gamnumsAbove[col]=gamnum;
        
        
    	//System.out.println("In BandGammaLayout line 229 col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length);
    	//System.out.println("  g.deltaJ="+g.deltaJ+"  g.e="+g.gamma.ES()+" start="+start+" end="+end+" gamnum="+gamnum+" gammaColumns.size()="+gammaColumns.size());
    	//for(int j=0;j<gammaColumns.size();j++){
    	//	System.out.println("   "+gammaColumns.get(j).get(0).gamma.ES()+"  "+gammaColumns.get(j).size());
    	//}
    	
        for (i=end+1; i<start; i++){
            levelWraps[i].gamnumsBelow[col]=gamnum;
            levelWraps[i].gamnumsAbove[col]=gamnum;
        }
        
        int size=gammaColumns.size();
        Vector<GammaWrap> gammaColumn;
        if(col>=size){
        	for(int j=size;j<=col;j++){
        		gammaColumn=new Vector<GammaWrap>();
        		gammaColumns.addElement(gammaColumn);
        	}
        }
        	
        
        if(deltaJOfColumns[col]<0)
        	deltaJOfColumns[col]=g.deltaJ;
        
        if(deltaLOfColumns[col]<0)
        	deltaLOfColumns[col]=g.nlevelCrossed;
        
        
        gammaColumns.get(col).add(g);
        
    	//System.out.println("In BandGammaLayout line 259 col="+col+" l.e="+levelWraps[start].level.ES()+" ngOfLev="+levelWraps[start].gamnumsBelow.length);
    	//System.out.println("  g.deltaJ="+g.deltaJ+"  g.e="+g.gamma.ES()+" start="+start+" end="+end+" gamnum="+gamnum+" gammaColumns.size()="+gammaColumns.size());
    	//for(int j=0;j<gammaColumns.size();j++){
    	//	System.out.println("   "+gammaColumns.get(j).get(0).gamma.ES()+"  "+gammaColumns.get(j).size());
    	//}
    	
        if (col>=numcols) numcols=col+1;
    }
    
    public Vector<GammaWrap> gammaColumnAt(int k){
    	if(k>=gammaColumns.size())
    		return null;
    	
    	return gammaColumns.get(k);
    }
    
    /**
     * Return number of columns of gammas that will be needed.
     */
    public int ncolumns(){
        return numcols;
    }
    
    public int indexOfPrimaryColumn(){return indexOfPrimaryColumn;}
    
    
    /**
     * Return the column number of a particular gamma.
     */
    public int gammaNColumn(Gamma g){
        int gn=-1,x;
        for (x=0; x<gammaWraps.size(); x++)
            if (((GammaWrap)gammaWraps.get(x)).gamma.equals(g)){
                gn = ((GammaWrap)gammaWraps.get(x)).number;
                
                //if(g.ES().equals("496.4"))
                //	System.out.println("***1gn="+gn+" g.ei="+g.ILS()+" x="+x+" gsize="+gammaWraps.size()+" g.e="+gammaWraps.get(x).gamma.ES()+" g.fl="+gammaWraps.get(x).gamma.FLS());
     
                break;
            }
        if (gn<=0) return -1;
        for (int c=0; c<MAXCOL; c++){
            boolean emp = true;
            for (int l=0; l<levelWraps.length; l++){
                //if(g.ES().equals("848.0"))
               	//System.out.println("***2gn="+gn+"  "+levelWraps[l].gamnumsBelow[c]+" x="+x+" c="+c+" l="+l+" lsize="+levelWraps.length+" l.e="+levelWraps[l].level.ES()+" gbelows="+levelWraps[l].gamnumsBelow.length);
                
                if (levelWraps[l].gamnumsBelow[c]==gn)
                    return c;
                if (levelWraps[l].gamnumsBelow[c]!=0) emp=false;
            }
            if (emp){
                System.out.printf("%% Gamma not found %s\n",g.ES());
                return -1;
            }
        }
        return -1;
    }
    
    int [] deltaJOfColumns;
    int [] deltaLOfColumns;//for numbers of levels crossed by each gamma in each column
    
    Band[] bandCopies;
    ENSDF ens;
    /** Creates a new instance of GammaLayout */
    public BandGammaLayout(ENSDF e,Band[] g) {
        gammaWraps = new java.util.Vector<GammaWrap>();
        deltaJOfColumns=new int[MAXCOL];
        deltaLOfColumns=new int[MAXCOL];
        
        ens = e;
        bandCopies = g;
        
        Arrays.fill(deltaJOfColumns,-1);
        Arrays.fill(deltaLOfColumns,-1);
    }
    /** Creates a new instance of GammaLayout.
     * Assume all bands are included.
     */
    public BandGammaLayout(ENSDF e) {
        gammaWraps = new java.util.Vector<GammaWrap>();
        deltaJOfColumns=new int[MAXCOL];
        deltaLOfColumns=new int[MAXCOL];
        
        ens = e;
        Band[] b = new Band[e.nBands()];
        for (int x=0; x<b.length; x++)
            b[x] = e.bandAt(x);
        bandCopies = b;
        
        Arrays.fill(deltaJOfColumns,-1);
        Arrays.fill(deltaLOfColumns,-1);
    }
    
    private void resetGammaColumns(){  
    	this.gammaColumns.clear();
    	
        Arrays.fill(deltaJOfColumns,-1);
        Arrays.fill(deltaLOfColumns,-1);
    }
    
    public void addGamma(Gamma g,Level startlev,Band band){
        GammaWrap gm = new GammaWrap();
        gm.gamma = g;
        gm.startLevel = startlev;
        
        gm.endLevel   = EnsdfUtil.finalLevel(ens,g);
        
       
                //Ens.closest(ens,startlev.EF() - g.EF());
        gm.band = band;
        
        gm.inband=false;
        if(EnsdfUtil.contains(band, gm.endLevel))
        	gm.inband=true;

        
        //if(g.ES().equals("670.0") || g.ES().equals("778.6")){
        //	System.out.println("In BandGammaLabel line 366: g.es="+g.ES()+" fi="+startlev.ES()+" "+startlev.JPiS()+" fl="+gm.endLevel.ES()+" "+gm.endLevel.JPiS()+" inband="+gm.inband);
        //	
        //	for(int i=0;i<band.nLevels();i++)
       // 		System.out.println("    band level"+i+"  "+band.levelAt(i).ES()+"  "+band.levelAt(i).JPiS());
        //}

        if(gm.startLevel.jVal().size()>0 && gm.endLevel!=null && gm.endLevel.jVal().size()>0){
        	float startJ=gm.startLevel.jVal().lastElement().floatValue();
        	float endJ=gm.endLevel.jValAt(0);
        	if(startJ>=0 && endJ>=0)
        		gm.deltaJ=(int)(startJ-endJ);
        }
        
        gammaWraps.add(gm);
    }
    
    /// Sort gammas to a more ideal ordering
    //order of the resulting gammaWraps, as index increases:
    //1. level energy from high to low
    //2. at the same level energy, gamma energy from low to high
    protected void sortGammas(){
        //java.util.HashSet levs = new java.util.HashSet();
        Vector<LevelWrap> levelWrapsV = new Vector<LevelWrap>();
        Vector<Level> levels=new Vector<Level>();
        
        for (int x=0; x<gammaWraps.size(); x++){
        	GammaWrap gw=gammaWraps.get(x);
        	
            if (!levels.contains(gw.startLevel)){
            	LevelWrap lw = new LevelWrap(gw.startLevel);
                levelWrapsV.add(lw);
                levels.add(lw.level);
            }
            
            
            if (!levels.contains(gw.endLevel) && gw.inband){
            	LevelWrap lw = new LevelWrap(gw.endLevel);
                levelWrapsV.add(lw);
                levels.add(lw.level);
            }
        }
        
        levelWraps = new LevelWrap[levelWrapsV.size()];
        levelWrapsV.toArray(levelWraps);
        for (int x=levelWraps.length; x>0; x--){
            for (int y=0; y<x-1; y++){
                if (EnsdfUtil.e(levelWraps[y].level) > EnsdfUtil.e(levelWraps[y+1].level)){
                    LevelWrap tmp = levelWraps[y];
                    levelWraps[y]=levelWraps[y+1];
                    levelWraps[y+1]=tmp;
                }
            }
        }

        for (int x=gammaWraps.size()-1; x>0; x--){
            for (int y=0; y<x; y++){
                float le1,le2,ge1,ge2;
                le1 = EnsdfUtil.e(((GammaWrap)gammaWraps.get(y)).startLevel);
                ge1 = ((GammaWrap)gammaWraps.get(y)).gamma.ERF();
                le2 = EnsdfUtil.e(((GammaWrap)gammaWraps.get(y+1)).startLevel);
                ge2 = ((GammaWrap)gammaWraps.get(y+1)).gamma.ERF();
                
                boolean swap = false;
                if (le1<le2) swap = true;
                else if (le1==le2 && ge1>ge2) swap=true;
                if (swap){
                    GammaWrap tmp = gammaWraps.get(y);
                    gammaWraps.set(y,gammaWraps.get(y+1));
                    gammaWraps.set(y+1,tmp);
                }
            }
        }
        
        //debug
        //for(int i=0;i<gammaWraps.size();i++){
        //	GammaWrap gw=gammaWraps.get(i);
        //	System.out.println("In BandGammaLayout line 275:i="+i+" l.e="+gw.startLevel.ES()+" g.e="+gw.gamma.ES());
        //}
        //for (int x=0; x<gam.size(); x++){
        //    levAddGamma((Gam)gam.get(x),x+1);
        //}

    }
    /**
     * Calculate positions of gammas given drawable width of chart and
     * whether the chart uses diagonal connections.
     */
    public void calc(float width,float minsep,boolean showInterBandGamma){
        sortGammas();
        float offset = minsep;
        
        //debug
        //System.out.println("**** In BandGammaLabel line 293: gamma size="+gammaWraps.size());
        
        //this.gammaColumns.clear();//it is filled in levAddGamma()
        resetGammaColumns();
        
        for (int x=0; x<gammaWraps.size(); x++){
            GammaWrap g = (GammaWrap)gammaWraps.get(x);
            // does the gamma stay in its group
            boolean inBand = false;
            boolean isInterBandG=false;//for transitions connecting to neighbor bands
            boolean isOutBandG=false;  //for transitions connecting to bands other than neighbor bands
            
            boolean showOutBandGamma=false;
            
            // true only if gamma is in group and goes to next level
            //boolean isNext = false;
            if (EnsdfUtil.contains(g.band,g.endLevel)){
                inBand = true;
                //isNext = Ens.isNext(g.group,g.start,g.end);
            }else if (bandCopies!=null && showInterBandGamma){
                int index=0;
                for (index=0; index<bandCopies.length; index++){
                    if (EnsdfUtil.contains(bandCopies[index],g.startLevel)){
                        if (index>0)
                            isInterBandG = EnsdfUtil.contains(bandCopies[index-1],g.endLevel);
                        if (index<bandCopies.length-1 && !isInterBandG)
                            isInterBandG = EnsdfUtil.contains(bandCopies[index+1],g.endLevel);
                    }
                }
            }
            //debug
            //System.out.println("In BandGammaLayout line 310: i="+x+" g.e="+g.gamma.ES()+" inBand="+inBand);
            
            
            // check for gammas to uncharted levels
            if (!inBand && !isInterBandG){
                boolean found=false;
                for (int i=0; i<bandCopies.length; i++){
                    if (EnsdfUtil.contains(bandCopies[i],g.endLevel)){
                        found=true;
                        isOutBandG=true;
                        break;
                    }
                }
                if (!found)
                    continue;
            }
            /*if (isNext){ // transition to next level
                g.pos = 0.0f;
            }else */
            
            if(isOutBandG && !showOutBandGamma)
            	continue;
            
            //debug
            //System.out.println("In BandGammaLayout line 477: i="+x+" g.e="+g.gamma.ES()+" l.e="+g.startLevel.ES()+" size="+gammaWraps.size());
            
            if (isInterBandG){ // inter-band transition
                g.pos = 0.0f; 
            }else{ // normal transition
            	
                levAddGamma(g,x+1);//add gamma transition to corresponding gamma column based on deltaJ
                
                g.pos = offset;
                offset+=minsep;
                if (offset>width) 
                	offset = minsep;
            }
            //System.out.printf("%% Gamma %s, Pos %f, Next %s, Group %s\n",g.gamma.ES(),new Float(g.pos),
            //    new Boolean(isNext),new Boolean(inGroup));
        }
        
        
        //re-arrange gamma columns (sequences)
        //if there are more than one sequences (different DJ) and they can be placed in one columns, 
        //re-arrange them into one column.
        rearrangeGammaColumns();
        
        findPrimaryGammaColumn();
        
        /*System.out.printf("%% Gamma Layout\n");
        for (int a=levels.length-1; a>=0; a--){
            System.out.printf("%% %10s",levels[a].level.ES());
            for (int b=0; b<10; b++){
                System.out.printf(" (%2d,%2d)",
                    new Integer(levels[a].hi[b]),
                    new Integer(levels[a].lo[b]));
            }
            System.out.printf("\n");
        }/**/
    }
    
    private void rearrangeGammaColumns(){

        for(int i=0;i<gammaWraps.size();i++){
    		GammaWrap gw=gammaWraps.get(i);
    		if(gw.nlevelCrossed>0) //gammas can not be re-arranged into one column
    			return;
        }
        
        gammaColumns.clear();
        Vector<GammaWrap> gammaColumn=new Vector<GammaWrap>();
        
        for(int i=0;i<gammaWraps.size();i++){
    		GammaWrap gw=gammaWraps.get(i);
    		gammaColumn.add(gw);
        }
        
        gammaColumns.add(gammaColumn);
        
        
        for(int i=0;i<levelWraps.length;i++){
    		LevelWrap lw=levelWraps[i];
    		for(int col=0;col<lw.gamnumsAbove.length;col++){
    			if(lw.gamnumsAbove[col]>0){
    				if(col>0){
    					lw.gamnumsAbove[0]=lw.gamnumsAbove[col];
    				    lw.gamnumsAbove[col]=0;
    				}
    				
    				break;
    			}
    		}
    		
    		for(int col=0;col<lw.gamnumsBelow.length;col++){
    			if(lw.gamnumsBelow[col]>0){
    				if(col>0){
        				lw.gamnumsBelow[0]=lw.gamnumsBelow[col];
    					lw.gamnumsBelow[col]=0;
    				}

    				break;
    			}
    		}
        }
               
    }
    
    public int findPrimaryGammaColumn(){
    	int max=0;
        for(int i=0;i<gammaColumns.size();i++){
        	int n=gammaColumns.get(i).size();
        	if(n>max){
        		max=n;
        		indexOfPrimaryColumn=i;
        	}
        }
    	
    	return indexOfPrimaryColumn;
    }
    
    /**
     * Get the position of a particular gamma.
     */
    public float getPos(Gamma ga){
        String s = ga.ES();
        int x;
        for (x=0; x<gammaWraps.size(); x++){
            GammaWrap g = (GammaWrap)gammaWraps.get(x);
            if (g.gamma.ES().equals(s))
                return g.pos;
        }
        return -1.0f;
    }
}
