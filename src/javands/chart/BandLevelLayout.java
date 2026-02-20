package javands.chart;

import java.util.HashMap;
import java.util.Vector;

import ensdfparser.ensdf.*;
import ensdfparser.nds.ensdf.*;

/**
 * The layout object is used to insert artificial space between overly 
 * close levels. Right now it is only used in BandChart
 *
 */
public class BandLevelLayout {
    public class LevelWrap{
        public Level level=null;
        // position of level in cm
        public float pos=0;
        public float newpos=0;
        public int group = -1;
        
        
        public LevelWrap(Level l,int gr){
            level = l; group = gr;
        }
        
		public float e(){
        	if(level!=null)
        		return EnsdfUtil.e(level);
        	else
        		return -1;
        }
    }
    
    float emax=0,emin=0;
    float minLevelGap=1e6f;
    
    java.util.Vector<LevelWrap> levelWraps;
    HashMap<Integer,Band> bandMap;
    
    ENSDF ens;
    int ngroups=0;
    Vector<Integer> groupNumbers=new Vector<Integer>();
    
    //used for adjusting level energy for drawing purpose only when
    //there is large vertical blank gap between bands
    float[] eoffsets=new float[20];
    
    /** Creates a new instance of Layout */
    public BandLevelLayout(ENSDF e) {
        ens = e;
        levelWraps = new java.util.Vector<LevelWrap>();
        bandMap=new HashMap<Integer,Band>();
        
        //gam = new java.util.Vector();
        
        for(int i=0;i<20;i++)
        	eoffsets[i]=0;
    }
    public void clear(){
        levelWraps.clear();
        bandMap.clear();
        
        ngroups=0;
        emax=0;
        emin=0;
        minLevelGap=1e6f;
        groupNumbers.clear();
        
        for(int i=0;i<20;i++)
        	eoffsets[i]=0;
    }
    
    
    public void addLevel(Level l,int group){
        int pos;
        float e = EnsdfUtil.e(l);
        for (pos=0; pos<levelWraps.size(); pos++){
            if (EnsdfUtil.e(((LevelWrap)levelWraps.get(pos)).level) > e)
                break;
        }
        levelWraps.add(pos, new LevelWrap(l,group));   
        
        if(!containGroup(group)){
        	ngroups++;
        	groupNumbers.add(group);
        	
        	Band b=new Band();
        	b.addLevel(l);
        	bandMap.put(group, b);
        }else{
        	Vector<Level> levels=bandMap.get(group).levels();
        
        	for(pos=0;pos<levels.size();pos++){
        		if(EnsdfUtil.e(levels.get(pos)) > e)
        			break;
        	}        	
			levels.insertElementAt(l, pos);
        }
    }
    
    public boolean containGroup(int group){
    	if(groupNumbers.contains(new Integer(group)))
    		return true;
  
    	return false;
    }
    
    /*public void addGamma(Gamma g,Level startlev,Band group){
        Gam gm = new Gam();
        gm.gamma = g;
        gm.start = startlev;
        gm.end   = Ens.closest(ens,startlev.EF() - g.EF());
        gm.group = group;
        gam.add(gm);
    }*/
    
    /**
     *for all bands
     */
    protected void padPos_old(float mingap){
        if (levelWraps.size()==0) return;
        float[] gap = new float[levelWraps.size()-1];
        int x;
        float tot=0,need=0,avail=0;
        for (x=0; x<gap.length; x++){
            LevelWrap a,b;
            a = (LevelWrap)levelWraps.get(x);
            b = (LevelWrap)levelWraps.get(x+1);
            gap[x] = b.pos - a.pos;
            //System.out.println(gap[x]);
            // calc the total (should be equal to height)
            tot += gap[x];
            // special case of levels with equal (0 vs 0+x) energies
            if (a.level.ES().equals(b.level.ES())){
                gap[x]=-1;
                continue;
            }
            
            // calc needed space
            if (gap[x]<mingap){
                need += mingap-gap[x];
            }
            // calc space available to squeeze out
            if (gap[x]>mingap){
                avail += gap[x]-mingap;
            }
        }
        
        //debug
        //System.out.println("In LevelLayout1: line 90: minLevelGap="+minLevelGap);
        
        // no close levels
        if (need==0)
            return;
        // what i have to multiply extra space by
        float gmul = (avail-need)/avail;
        // if i've been asked to make an impossible chart
        // just try to make it fit on the screen
        if (need>avail && minLevelGap<mingap){//minLevelGap is for levels in the same band
            mingap = tot / (levelWraps.size()-1);
            gmul=0.0f;
        }
        // modify gaps so that they still total up to the same
        for (x=0; x<gap.length; x++){
            if (gap[x]<0.0f){
                //gap[x]=0.001f;
                // do nothing, special case
            }else if (gap[x]<mingap){
                gap[x] = mingap;
            }else{
                float ext = gap[x] - mingap;
                ext = ext * gmul;
                gap[x] = mingap + ext;
            }
            
        }
        float pos=0;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap l = (LevelWrap)levelWraps.get(x);
            l.pos = pos;
            if (x<gap.length){
                if (gap[x]>0.0f)
                    pos+=gap[x];
            }
        }
        
        //find minLevelGap 
        float min=findMinLevelGap(levelWraps);
        if(min<minLevelGap)
        	minLevelGap=min;

        //debug
        //System.out.println("In BandLevelLayout: line 195: minLevelGap="+minLevelGap+" minGap="+mingap);
    }
    
    /**
     *for all bands
     */
    protected void padPos(float mingap){
        if (levelWraps.size()==0) return;
        float[] gap = new float[levelWraps.size()-1];
        float[] newgap = new float[levelWraps.size()-1];
        boolean[] inSameBand=new boolean[levelWraps.size()-1];
        int x;
        float tot=0,need=0,avail=0,tempgap=0;
        
        for (x=0; x<gap.length; x++){
            LevelWrap a,b;
            inSameBand[x]=false;
            
            a = (LevelWrap)levelWraps.get(x);
            b = (LevelWrap)levelWraps.get(x+1);
            gap[x] = b.pos - a.pos;
            //System.out.println(gap[x]);
            // calc the total (should be equal to height)
            tot += gap[x];
            // special case of levels with equal (0 vs 0+x) energies
            if (a.level.ES().equals(b.level.ES())){
                gap[x]=-1;
                newgap[x]=-1;
                continue;
            }
                        
            if(a.group==b.group)
            	inSameBand[x]=true;
            
            
            // calc needed space
            newgap[x]=gap[x];
            
            if (gap[x]<mingap){
            	if(inSameBand[x]){        
            		newgap[x]=mingap;            		
            	}
            	else{
            	    
            	    Vector<LevelWrap> levelGroups=getGroup(b.group);
            	    int index=levelGroups.indexOf(b);

            		if(index>0){
            			LevelWrap prevLevel;//previous level in the same band
            			prevLevel=levelGroups.get(index-1);
            			tempgap=b.pos-prevLevel.pos;
            			if(tempgap<mingap){
            				newgap[x]=(prevLevel.newpos+mingap)-a.newpos;
            		        if(newgap[x]<gap[x])
            		        	newgap[x]=gap[x];
            		                  		        
            			}
            		}
            	}
            	
            	need+=newgap[x]-gap[x];
            }
            else{// calc space available to squeeze out
                avail += gap[x]-mingap;
            }
            
            b.newpos=a.newpos+newgap[x];

            //System.out.println("In BandLevelLayout1: line 263: minLevelGap="+minLevelGap+" need="+need+" avail="+avail);
            //System.out.println("                     a.level="+a.level.EF()+" b.level="+b.level.EF()+" a.oldPos="+a.pos+" a.newPos="+a.newpos+" b.oldPos="+b.pos+" b.newPos="+b.newpos);
            
            gap[x]=newgap[x];
        }
        
        //debug
        //System.out.println("In BandLevelLayout1: line 267: minLevelGap="+minLevelGap+" need="+need+" avail="+avail);
        
        // no close levels
        if (need==0)
            return;
               

        // what i have to multiply extra space by
        float gmul = (avail-need)/avail;
        
        // if i've been asked to make an impossible chart
        // just try to make it fit on the screen
        if (need>avail && minLevelGap<mingap){//minLevelGap is for levels in the same band
            mingap = tot / (levelWraps.size()-1);
            gmul=0.0f;
        }
        
        
        // modify gaps so that they still total up to the same
        for (x=0; x<gap.length; x++){
            if (gap[x]<0.0f){
                //gap[x]=0.001f;
                // do nothing, special case
            }else if(gap[x]>mingap){  
                float ext = gap[x] - mingap;
                ext = ext * gmul;
                gap[x] = mingap + ext;
        	}            
        }
        
        float pos=0;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap l = (LevelWrap)levelWraps.get(x);
            l.pos = pos;
            if (x<gap.length){
                if (gap[x]>0.0f)
                    pos+=gap[x];
            }
        }
        
        //find minLevelGap 
        float min=findMinLevelGap(levelWraps);
        if(min<minLevelGap)
        	minLevelGap=min;

        //debug
        //System.out.println("In BandLevelLayout: line 313: minLevelGap="+minLevelGap+" minGap="+mingap);
    }
    
    /**
     *for single band
     */
    protected void padPos(float mingap,int igroup){
        if (this.levelWraps.size()==0) return;
        
        Vector<LevelWrap> levelWraps=new Vector<LevelWrap>();
        for(int i=0;i<this.levelWraps.size();i++)
        	if(this.levelWraps.get(i).group==igroup)
        		levelWraps.add(this.levelWraps.get(i));
        
        if(levelWraps.size()==0)
        	return;
        
        float[] gap = new float[levelWraps.size()-1];
        int x;
        float tot=0,need=0,avail=0;
        for (x=0; x<gap.length; x++){
            LevelWrap a,b;
            a = (LevelWrap)levelWraps.get(x);
            b = (LevelWrap)levelWraps.get(x+1);
            gap[x] = b.pos - a.pos;
            //System.out.println(gap[x]);
            // calc the total (should be equal to height)
            tot += gap[x];
            // special case of levels with equal (0 vs 0+x) energies
            if (a.level.ES().equals(b.level.ES())){
                gap[x]=-1;
                continue;
            }
            
            // calc needed space
            if (gap[x]<mingap){
                need += mingap-gap[x];
            }
            // calc space available to squeeze out
            if (gap[x]>mingap){
                avail += gap[x]-mingap;
            }
        }
        
     
        // no close levels
        if (need==0)
            return;
        // what i have to multiply extra space by
        float gmul = (avail-need)/avail;
        // if i've been asked to make an impossible chart
        // just try to make it fit on the screen
        if (need>avail && minLevelGap<=mingap){
            mingap = tot / (levelWraps.size()-1);
            gmul=0.0f;
        }
        // modify gaps so that they still total up to the same
        for (x=0; x<gap.length; x++){
            if (gap[x]<0.0f){
                //gap[x]=0.001f;
                // do nothing, special case
            }else if (gap[x]<mingap){
                gap[x] = mingap;
            }else{
                float ext = gap[x] - mingap;
                ext = ext * gmul;
                gap[x] = mingap + ext;
            }
            
        }
        
        float pos=0;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap l = (LevelWrap)levelWraps.get(x);
            l.pos = pos;
            if (x<gap.length){
                if (gap[x]>0.0f)
                    pos+=gap[x];
            }
        }
        
        //find minLevelGap 
        float min=findMinLevelGap(igroup);
        if(min<minLevelGap)
        	minLevelGap=min;

    }   
    
    /*
     * adjust vertical band gap between bands from bottom to top,
     * when one band is far away from all bands above. First, sort
     * bands by the energy of the top level of each band from low to 
     * high
     */
    public boolean padBandVGap(float height,float mingap){
    	boolean padded=false;
    	
    	if(ngroups<=0)
    		return padded;

    	float minE=1E6f;
    	float maxE=0;
    	
    	if(levelWraps.size()<=0)
    		return padded;
    	
    	LevelWrap first=levelWraps.firstElement();
    	LevelWrap last=levelWraps.lastElement();
    	minE=EnsdfUtil.e(first.level)+eoffsets[first.group];
    	maxE=EnsdfUtil.e(last.level)+eoffsets[last.group];
    	
    	float cm2energy=(maxE-minE)/height;
    	
    	////////////////////////////////////////////////////
    	float VGapLimit=height/10.0f;//in unit of cm
    	////////////////////////////////////////////////////
    	
    	VGapLimit=VGapLimit*cm2energy;//in unit of energy
    	
    	for(int i=0;i<groupNumbers.size();i++){
    		int igroup=groupNumbers.get(i);
    		Band b=bandMap.get(igroup);//levels in band is already sorted from low to high
    		if(b==null)
    			continue;
    		float thisHighE=EnsdfUtil.e(b.lastLevel())+eoffsets[igroup];
    		
    		boolean toMoveDown=false;
    		Vector<Integer> moveDownGroupNumbers=new Vector<Integer>();
    				
    		float min=1E6f;
    		for(int j=0;j<groupNumbers.size();j++){
    			
    			int jgroup=groupNumbers.get(j);
    			if(jgroup==igroup)
    				continue;
    			
    			float lowE=EnsdfUtil.e(bandMap.get(jgroup).firstLevel())+eoffsets[jgroup];
    			float highE=EnsdfUtil.e(bandMap.get(jgroup).lastLevel())+eoffsets[jgroup];

    			if(highE<=thisHighE)//skip bands below this band
    				continue;
    			
    			//System.out.println("j="+j+" jgroup="+jgroup+"  lowE="+lowE+" highE="+thisHighE+" gap="+VGapLimit);
    			
    			if(lowE<thisHighE+VGapLimit){
    				toMoveDown=false;
    				break;
    			}else{
    				toMoveDown=true;
    				moveDownGroupNumbers.add(jgroup);
    				float diff=lowE-thisHighE;
    				if(diff<min)
    					min=diff;
    			}
    			   			
    		}
    		
    		//move down all levels above this band by adding an negative offset
    		if(toMoveDown){
    			padded=true;
    			
    			float newMinVGap=(float)(VGapLimit/2+VGapLimit/2*(1-Math.pow(0.5,(min-VGapLimit)/VGapLimit)));
    			
    			//System.out.println("In BandLevelLayout line 472:  min="+min+" newMinVGap="+newMinVGap+" limit="+VGapLimit);
    			
    			if(newMinVGap<min){
        			for(int j=0;j<moveDownGroupNumbers.size();j++){
        				int jgroup=moveDownGroupNumbers.get(j);
        					
        				eoffsets[jgroup]-=min-newMinVGap;
        				
        				//System.out.println("In BandLevelLayout line 453: eoffset["+jgroup+"]="+eoffsets[jgroup]+" min="+min+" newMinVGap="+newMinVGap+" limit="+VGapLimit);
        			}
    			}

    		}
    	}
    	
    	//System.out.println("In BandLevelLayout line 478: padded="+padded+" minE="+minE+" maxE="+maxE);
    	
    	return padded;
    		   				
    }
    
    /**
     * Calculate using default band gap.
     */
    public void calc(float height){
       calc(height,BaseChart.GAP_BETWEEN_LEVELS); 
    }
    /**
     * Calculate positions with given the total height,
     * the minimum gap and minimum label gap.
     * All values in cm.
     */
    public void calc(float height,float mingap){
        int x;
        
        boolean padded=true;
        int ntries=0;
        while(padded){
        	padded=padBandVGap(height,mingap);//padded=false if nothing is done in this call
        	ntries++;
        	if(padded && ntries>=10){
        		//for(int i=0;i<ngroups;i++)
        		//	eoffsets[groupNumbers.get(i)]=0;
        		
        		break;
        	}
        }
        
        
        // find max/min energies
        float maxe=-1e6f,mine=1e6f;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap l = (LevelWrap)levelWraps.get(x);
            float e = EnsdfUtil.e(l.level);
 
            //debug
            //System.out.println("In BandLevelLayout line 520: group="+l.group+" e="+e+"  offset="+eoffsets[l.group]);
            
            e=e+eoffsets[l.group];

            
            if (e>maxe) maxe=e;
            if (e<mine) mine=e;
        }
        
        emax=maxe; emin=mine;
        
                
        //System.out.printf("%f %f\n",
        //    new Float(emax),new Float(emin));
        // apply simplistic math
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap l =(LevelWrap)levelWraps.get(x);
            float e = EnsdfUtil.e(l.level);
            
            e=e+eoffsets[l.group];
            
            if(maxe-mine==0) maxe+=0.1f;
            l.pos = (e-mine) * height / (maxe-mine);
     
            //System.out.print("POS ");
            //System.out.println(" In BandLevelLayout line 560: level.e="+EnsdfUtil.e(l.level)+" e="+e+" offset="+eoffsets[l.group]+" l.pos="+l.pos+" height="+height+" mingap="+mingap);
        }
        
        //find minLevelGap 
        float min=findMinLevelGap(levelWraps);
        
        //System.out.println("In BandLevelLayout line 566: input mingap="+mingap+" found min="+min+" minLevelGap="+minLevelGap);
        
        if(min<minLevelGap)
        	minLevelGap=min;
        
        // pad out level scheme appropriately
        padPos(mingap);
        
        //System.out.println("In BandLevelLayout line 574: input mingap="+mingap+" found min="+min+" minLevelGap="+minLevelGap);
    }
    
    
    protected float findMinLevelGap(Vector<LevelWrap> levelWraps){
        //find minLevelGap 
        float min=1e6f;
        float tempMin=min;
        for(int i=0;i<ngroups;i++){
        	tempMin=findMinLevelGap(groupNumbers.get(i));
        	if(tempMin<min)
        		min=tempMin;
        }
        return min;
    }
    
    protected float findMinLevelGap(int group){
    	float min=1e6f;
    	LevelWrap prevLevel=null;
        for (int x=0; x<levelWraps.size(); x++){
            LevelWrap l =(LevelWrap)levelWraps.get(x);
            if(l.group==group){
            	float gap=-1;
            	if(prevLevel!=null)
            		gap=l.pos-prevLevel.pos;
            	
            	if(gap>0 && gap<min)
            		min=gap;
            		
            	prevLevel=l;
            }
        }
    	
    	return min;
    }
        
        
    public float getMinGap(){
    	return minLevelGap;
    }
    public float getPos(Level l){
        String s = l.ES();
        int x;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap lv = (LevelWrap)levelWraps.get(x);
            if (lv.level.ES().equals(s))
                return lv.pos;
        }
        return -1.0f;
    }
    public int getGroupNum(Level l){
        String s = l.ES();
        int x;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap lv = (LevelWrap)levelWraps.get(x);
            if (lv.level.ES().equals(s))
                return lv.group;
        }
        return -1;
    }
    
    public Vector<LevelWrap> getGroup(int group){
        Vector<LevelWrap> lw=new Vector<LevelWrap>();
        
        for (int x=0; x<levelWraps.size(); x++){
            LevelWrap l = (LevelWrap)levelWraps.get(x);
            if (l.group==group)
                lw.add(l);
        }
        
        return lw;
    }
    
    public Band getBand(int group){
        return bandMap.get(group);
    }
    
    public int getNGroups(){return ngroups;}
    
    public float getMax(){ return emax; }
    public float getMin(){ return emin; }
    
    /**
     * Return if a given level is in this layout object.
     */
    public boolean contains(Level l){
        String s = l.ES();
        int x;
        for (x=0; x<levelWraps.size(); x++){
            LevelWrap lv = (LevelWrap)levelWraps.get(x);
            if (lv.level.ES().equals(s))
                return true;
        }
        return false;
    }
    /*protected void calcGammas(){
        for (int x=0; x<gam.size(); x++){
            Gam g = (Gam)gam.get(x);
            
        }
    }*/
}
