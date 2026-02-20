
package javands.chart;
import java.util.Vector;

import ensdfparser.ensdf.*;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

import java.awt.Polygon;
/**
 * Creates the skeleton diagrams that appear at the start of every NDS publication
 * requires the ensdf files from all adopted levels
 */
public class SkeletonChart extends BaseChart{
    private Vector<EnsdfSkeleton> enskelV;
    private Vector <ENSDF>ensdfV; //the adopted levels datasets that this diagram will be based on
    private float H,W;
    private int nRecords;
    boolean portrait;
    private float ladderWidth;
    private float ladderSpacing;
    private float tableX,tableY;
    private int tablePage;
    private float parentSpace;
    private float bottomSpace;
    private Vector<String> forcedBreaks;
    private String forcedBreaksStr="";
    
    private Vector<Integer> pageBreaks;

    private float defaultTableX;
    private float defaultTableY;
    
    private float oneParentHeight=1.8f;//in c.m.
    
    private float right_pad,left_pad;
    
    private boolean foundTableLocation=false;
    private boolean isTableInNewPage=false;
    private boolean useFixedX=false,useFixedY=false;
    
    public SkeletonChart(Vector<ENSDF> ENSDFs,String options){

        ensdfV=ENSDFs;
        
        //get the various configuration options from the control file, or set defaults
        if(options.contains("/H:")) portrait=false;
        else portrait=true;
        
        try{ ladderWidth=Float.parseFloat(NDSControl.getLineAsString(options,"/W:"));
        }catch(Exception e){ ladderWidth=2.1f; 
        }
        
        try{ ladderSpacing=Float.parseFloat(NDSControl.getLineAsString(options,"/S:"));
        }catch(Exception e){ ladderSpacing=0.5f;}
        
        try{tablePage=Integer.parseInt(NDSControl.getLineAsString(options,"/P:"));//page No. for summary table to put on, last page of skeleton pages by default
        }catch(Exception e){ tablePage=-1;}

        try{ parentSpace=Float.parseFloat(NDSControl.getLineAsString(options,"/T:"));
        }catch(Exception e){ parentSpace=1.5f;}
        
        if(options.contains("/B:")) {forcedBreaks=NDSControl.getLine(options,"/B:");forcedBreaksStr=NDSControl.getLineAsString(options, "/B:");}
        
        enskelV=new Vector<EnsdfSkeleton>();
        
        //The code below in previous version caused a serious bug. Since RIGHT_PAD in parent class is static,
        //every call of this constructor increases RIGHT_PAD by 0.2cm, making the skeleton drawing
        //look very weird. Solution: add private left_pad and right_pad to this class        
        //RIGHT_PAD+=0.2f; //give us a little more space for labels
        
        
        left_pad=BaseChart.LEFT_PAD;
        right_pad=BaseChart.RIGHT_PAD+0.2f;
        
        //System.out.println("In SeletonChart line 76: isPortrait="+portrait);
        
        if(portrait){
            H=NDSConfig.MAX_TEXT_HEIGHT-0.3f;
            W=NDSConfig.MAX_TEXT_WIDTH;
        }else{
            W=NDSConfig.MAX_TEXT_HEIGHT-0.3f;
            H=NDSConfig.MAX_TEXT_WIDTH;
        }
        
        defaultTableX=W-0.5f;
        defaultTableY=0.f;
        
        useFixedX=false;
        useFixedY=false;
        
        //get location of summary table
        try{ 
        	tableX=Float.parseFloat(NDSControl.getLineAsString(options,"/X:"));
        	if(tableX>0)
        		useFixedX=true;
        	
        }catch(Exception e){ tableX=defaultTableX;}

        try{ 
        	tableY=Float.parseFloat(NDSControl.getLineAsString(options,"/Y:"));
            if(tableY>0)
            	useFixedY=true;
        }catch(Exception e){ tableY=defaultTableY;}

        
        nRecords=0;
        bottomSpace=0.5f;
        foundTableLocation=false;
        isTableInNewPage=false;
        
    }
    
    private float findLowestYPos(Vector<EnsdfSkeleton> enskelV,float Yconv) {
        float lowestY=0;

        for(int i=0;i<enskelV.size();i++) {
        	EnsdfSkeleton e=(EnsdfSkeleton)enskelV.elementAt(i);
        	float yposE=e.yposE;
        	int nQV=e.levelAt(0).nDecayModes();
        	float ypos=yposE*Yconv-1.1f-nQV*0.21f;//keep consistent with the ypos setting in drawing Q-value under the nucleus name
            if(ypos<lowestY) 
            	lowestY=ypos;
        }

        return lowestY;
    }
    
    public DrawingControl drawChart(java.io.Writer out)throws Exception{
        
        float Yconv=makeSkeletons();//return Y scale
        
        //System.out.println(" enskelV.size="+enskelV.size());
        
        pageBreaks=findPageBreaks();
        int nPages=pageBreaks.size()+1;
        
        int i=0,ibreak;
        String tableLines="";
        boolean isTableWritten=false;
        
        if(tablePage<=0) tablePage=nPages;
        
        for(int j=0;j<nPages;j++){
            writeFigureHead(out,j);
            EnsdfSkeleton e;
            out.write("label(btex \\normalsize \\scalefont{0.8} \\bf Skeleton Scheme for A="+
                    ((EnsdfSkeleton)enskelV.elementAt(0)).nucleus.A());
            if(j>0) out.write(" (continued)");
            out.write(" etex,"+Str.point(W/2,H)+");\n");
            out.write("draw"+Str.point(W/2-3f,H-0.2f)+"--"+Str.point(W/2+3f, H-0.2f)+" withcolor levcol;\n");

            if(j<nPages-1) 
            	ibreak=(Integer)pageBreaks.elementAt(j);
            else 
            	ibreak=-1;
            
            Vector<EnsdfSkeleton> tempSkelV=new Vector<EnsdfSkeleton>();
            
            int i1=i;
            while(i1<nRecords){  
                if(i1!=ibreak){
                    e=(EnsdfSkeleton)enskelV.elementAt(i1);
                    tempSkelV.add(e);
                    i1++;
                }else{
                    break;
                }
            }

            float lowestY=findLowestYPos(tempSkelV,Yconv);
            if(lowestY<0) {

            	float offsetE=(-lowestY-bottomSpace)/Yconv;//offset in energy unit; lowestY=-bottomSpace and above is acceptable
            	if(offsetE>0) {
                    for(EnsdfSkeleton es:tempSkelV) 
                    	es.yposE+=offsetE;
            	}
            }
            
            //System.out.println(" page#="+j+" lowestY="+lowestY);
            
            for(EnsdfSkeleton es:tempSkelV) {
            	writeNucleus(i,out,ladderWidth,Yconv,es);
            	i++;
            }
            
            if(j==tablePage-1 || (j==nPages-1&&tablePage>nPages)) {
            	
            	out.write("label(btex \\normalsize \\scalefont{0.8} etex,"+Str.point(W,H)+");\n");//to make the width of the whole page, otherwise the figure will be unexpectedly shifted to center 
            	                                                                                  //when the figure doesn't occupy the whole page, since all figures are placed in center environment.
            	
            	tableLines=printTable();
            	
            	if((useFixedX&&useFixedY&&tablePage<=nPages) || foundTableLocation){//fondTableLocation is determined in printTable() when not using fixed tableX and tableY
            		out.write(tableLines);
            		isTableWritten=true;
            	}
            }
            writeFigureTail(out);
        }
        
        //System.out.println("In SkeletonChart line 167: isTableWritten="+isTableWritten+" tableLines.length="+tableLines.length()+" useFixedX="+useFixedX+" foundTableLocation="+foundTableLocation);
        //System.out.println("      tablePage="+tablePage+" nPages="+nPages);
        
        //write table in a new page
        if(!isTableWritten&& tableLines.length()>0){
     
        	writeFigureHead(out,nPages);
        	out.write(tableLines);
        	writeFigureTail(out);
        	 
        	nPages++;      
        	tablePage=nPages;
        	isTableInNewPage=true;
        }
        
        DrawingControl d=new DrawingControl();
        d.setHeight(H);d.setWidth(W);
        d.setLadderSpacing(this.ladderSpacing);
        d.setLadderWidth(this.ladderWidth);
        d.setPortrait(portrait);
        d.setPages(nPages);
       
        
    	String options="SKELETON";
    	
    	//portrait or landscape
        if(!portrait)       options+="/H:";
        if(tablePage>=0)   options+="/P:"+tablePage;
        if(ladderWidth>0)  options+="/W:"+ladderWidth;        
        if(ladderSpacing>0)options+="/S:"+ladderSpacing;
        if(parentSpace>0)  options+="/T:"+parentSpace;
        if(forcedBreaksStr.length()>0) 
        	options+="/B:"+forcedBreaksStr;
        
        options+="/X:"+tableX;
        options+="/Y:"+tableY;

    
        if(options.length()>0)
        	NDSControl.skelOptions=options;
    
        return d;     
    }
    
    public boolean isTableInNewPage(){return isTableInNewPage;}
    
    public void setPortrait(boolean b){portrait=b;}
    public boolean isPortrait(){return portrait;}
    
    @SuppressWarnings("unused")
	private float makeSkeletons()throws Exception{
        ENSDF ens;
        EnsdfSkeleton e;
        float prevLevel=0,prevQ=0;
        float lowestE=1e6f,highest=-1e6f;
        
        //initializes a bunch of ens objects for each ENSDF dataset
        for(int i=0;i<ensdfV.size();i++){
            ens=(ENSDF)ensdfV.elementAt(i);
            if(ens.lineAt(0).indexOf("ADOPTED")==9&&!ens.lineAt(0).contains(":")&&ens.nLevels()>0){
                e=new EnsdfSkeleton();
                e.yposE=prevLevel-prevQ;
                
                e.parseENSDF(ens);
                prevLevel=e.yposE;
                prevQ=ens.qv().QBMF();
                enskelV.add(e);
                nRecords++;
            }
        }

        EnsdfSkeleton temp=null,lowestSkel=null;
        
        //sets the bottom one to have zero pos
        for(int i=0;i<enskelV.size();i++) {
            if(((EnsdfSkeleton)enskelV.elementAt(i)).yposE<lowestE) {
            	lowestE=(((EnsdfSkeleton)enskelV.elementAt(i)).yposE);
            	lowestSkel=enskelV.elementAt(i);
            }
        }
        for(int i=0;i<enskelV.size();i++)
            ((EnsdfSkeleton)enskelV.elementAt(i)).yposE-=(lowestE);
        
        //if(lowestSkel!=null && lowestSkel.levelAt(0).nDecayModes()>1) {
        //	int n=lowestSkel.levelAt(0).nDecayModes();
        //	//bottomSpace+=(n-1)*0.3;
        //	//System.out.println(" n="+n+" bottomSpace="+bottomSpace);
        //}
        //find the highest energy so we can see how big our total graph is
        float maxParentH=0;
        
        for(int i=0;i<enskelV.size();i++){
            temp=(EnsdfSkeleton)enskelV.elementAt(i);
            
            /*
            for(int j=0;j<temp.levels.size();j++){
                if(temp.yposE+temp.levelAt(j).EF()>highest) highest=temp.yposE+temp.levelAt(j).EF();
            }
            */
            	
            if(temp.yposE+temp.levels.lastElement().EF()>highest)
            	highest=temp.yposE+temp.levels.lastElement().EF();
            
            if(temp.parentHeight>maxParentH)
            	maxParentH=temp.parentHeight;
        }
        //returns scaling factor between energies and centimetres
        float scale=(H-maxParentH)/(highest*1.f);
        
        //add room for nucleus labels at the bottom
        float offset=bottomSpace/scale;//offset in energy unit
        
        for(int i=0;i<enskelV.size();i++)
            ((EnsdfSkeleton)enskelV.elementAt(i)).yposE+=(offset);//300 is an arbitrary factor designed to leave room for nucleus labels at the bottom
        
        return scale;
    }
    
    /*set xpos of each skeleton and find pagebreak xpos*/
    private Vector<Integer> findPageBreaks(){

        Vector<Integer> xposV=new Vector<Integer>();
        EnsdfSkeleton temp;
        float pos=0.0f;
        //sets the x positions of the bands

        //loop through each dataset
        for(int i=0;i<enskelV.size();i++){
            temp=(EnsdfSkeleton)enskelV.elementAt(i);
            if(pos+ladderWidth>W){
                pos=0.0f;
                xposV.add(i);
            }
            if(forcedBreaks!=null){
                for(int j=0;j<forcedBreaks.size();j++){
                    if(i+1==Integer.parseInt((String)forcedBreaks.elementAt(j))){
                        pos=0.0f;
                        xposV.add(i);
                    }
                }
            }
            temp.xpos=pos;
            pos+=ladderWidth+ladderSpacing;
        }
        return xposV;
    }
    private void writeNucleus(int index,java.io.Writer out,float Xwidth,float Yscale,EnsdfSkeleton temp)throws Exception{
        Vector<String> labels=new Vector<String>();//print labels last so they don't get overwritten by drawings
        EnsdfSkeleton e2; 
        EnsdfSkeleton e=temp;
       
        
        out.write("%Nucleus "+e.nucleus.A()+e.nucleus.En()+"\n");
        String com;
        out.write("%"+e.xpos+" "+e.yposE+" (keV)\n");
        
        float xpos=e.xpos;                   //e.xpos in cm, bottom-left corner
        float ypos=e.yposE*Yscale;//e.ypos in energy for E=0.0 level, Yscale=factor to convert energy to cm, bottom-left corner
                                  //But note that the first level could also be a level that is not GS, e.g., parent level is isomeric level
        
        float firstLevelY=ypos+e.firstLevel().EF()*Yscale;//y position for the first level (not necessarily the GS), =ypos if it is GS
                                                          //no fanning for the first level of a skeleton
        
        e.lowerLeftPoint().setXY(xpos,ypos);
        e.lowerRightPoint().setXY(xpos+Xwidth+0.1f,ypos);
        e.upperLeftPoint().setXY(xpos, (e.yposE+e.lastLevel().EF())*Yscale);
        e.upperRightPoint().setXY(xpos+Xwidth+0.1f, (e.yposE+e.lastLevel().EF())*Yscale);

        
        //debug
        //System.out.println("********In SkeletonChart 320: writeNucleus: "+e.nucleus.A()+e.nucleus.En()+" xpos="+xpos+" ypos="+ypos+" Xwidth="+Xwidth+" height="+e.lastLevel().EF()*Yscale+" lastE="+e.lastLevel().EF());

        String tempStr="";
        
        //////////////////
        //print half-life
        //////////////////
        //if(!e.firstLevel().T12S().equals("STABLE")){
        	tempStr=Translator.halfLife(e.firstLevel().halflife());
            com="label(btex \\labelfont \\it "+tempStr+" etex,"+
                    Str.point(xpos+Xwidth/2,firstLevelY-0.15f)+");\n";
            labels.add(com);
            
            e.lowerLeftPoint().setY(ypos-0.15f);
            e.lowerRightPoint().setY(ypos-0.15f);
        //}
        
         //debug
         //System.out.println("1 In Skeletonchart line 263: nucleus="+e.nucleus.En()+" toLeft="+e.toLeft+" toRight="+e.toRight+" e.qb="+e.qb);
            
        //figure out whether this nucleus decays by beta minus, beta plus, both or neither
        e.toLeft=false;
        e.toRight=false;
        if(e.qb>0&&e.qv!=null){
        	if(index<enskelV.size()-1) 
        		e.toRight=true;        				        	
        }
        
        if(index>0&&e.qv!=null){
            if(((EnsdfSkeleton)enskelV.elementAt(index-1)).qb<0) 
            	e.toLeft=true;
        }
        
        //debug
        //System.out.println("2 In Skeletonchart line 341: nucleus="+e.nucleus.A()+e.nucleus.En()+" toLeft="+e.toLeft+" toRight="+e.toRight);
        //for(int i=0;i<e.levels.get(0).nDecayModes();i++)
        //	System.out.println("  decayMode "+i+"  "+e.levels.get(0).decayModeAt(i).name());
        
        
        /////////////////////////////////////
        //print nucleus ID
        /////////////////////////////////////
        com=("label(btex \\normalsize \\scalefont{0.8}  \\ensuremath{^{"+e.nucleus.A()+"}_{"+e.nucleus.ZS()+"}}"+e.nucleus.En()
                +"\\ensuremath{_{"+e.nucleus.N()+"}^{~}} etex,"+Str.point(xpos+Xwidth/2, firstLevelY-0.55f)+") "/*+"withcolor"*/);
            
        e.lowerLeftPoint().setY(ypos-0.55f);
        e.lowerRightPoint().setY(ypos-0.55f);
        
        /*if(e.isLeft&&!e.isRight) com+=" outcol";
        else if(e.isLeft&&e.isRight) com+=" (0.5,0,0.5)";
        else if(e.isRight&&!e.isLeft) com+= " gamcol";
        else com+=" levcol";*/
        com+=";\n";
        
        labels.add(com);
        
        ///////////////////
        //level stuff
        ///////////////////
        Level l;
        float fpos;
        LevelLabelLayout flayout=e.fan(Yscale);
        for(int i=0;i<e.levels.size();i++){
        	

            l=(Level)e.levels.elementAt(i);
            
            
            if(l.EF()>0.0f || i<e.nRealLevels()){//S(n),S(p),S(a) are stored as pseudo level
            	fpos=flayout.getPosition(l)+e.yposE*Yscale;
                
                if(i==e.levels.size()-1){
                    e.upperLeftPoint().setY(fpos+0.2f);
                    e.upperRightPoint().setY(fpos+0.2f);
                }
                
                //draw lines for levels
                if(fpos==(e.yposE+l.EF())*Yscale){ //if no fanning
                    com=("draw "+Str.point(e.xpos,fpos)+"--"+Str.point((e.xpos+Xwidth),fpos)+
                            " withcolor levcol");                
                }else{
                    // fanning required
                    com = "draw "+Str.point(e.xpos,fpos)+"--"
                            + Str.point(e.xpos+left_pad-0.2f,fpos)+"--"
                            + Str.point(e.xpos+left_pad-0.1f,(e.yposE+l.EF())*Yscale)+"--"
                            + Str.point(e.xpos+Xwidth-right_pad+0.1f,(e.yposE+l.EF())*Yscale)+"--"
                            + Str.point(e.xpos+Xwidth-right_pad+0.2f,fpos)+"--"
                            + Str.point(e.xpos+Xwidth,fpos)
                            + " withcolor levcol";
                }
                if(i==0) com +=" withpen pencircle scaled 0.03cm";
                
                //if(i>=e.nRealLevels()) com+= " dashed levdash";
                //if(i>=e.nRealLevels()) com+=" dashed dashpattern(off 0.1cm on 0.05 off 0.1cm)";//com+= " dashed levdash";
                if(i>=e.nRealLevels()) com+=" dashed levdot";//for S(n), S(p), S(a) pseudo levels
                //if(i>=e.nRealLevels()) com+=" dashed withdots"; 
                
                out.write(com);
                out.write(";\n");


                String levelE=l.ES();
                if(!Str.isNumeric(levelE))
                	levelE=levelE.toLowerCase();
                
                if (levelE.equals("0")) levelE="0.0"; 
                
                
                //move energy label to left if there is vertical arrow from levels above
                float offset=0f;
                if(i<e.levels.size()-1){
                	Level nl=(Level)e.levels.elementAt(i+1);
                	
                	for(int j=0;j<nl.nDecayModes();j++) {
                		String name=nl.decayModeAt(j).name();             		
                		if(!name.equals("B-") && !name.equals("B+") && !name.equals("EC") && !name.equals("EC+%B+") && !name.equals("IT")) {
                			offset=-0.2f;
                			break;
                		}
                	}
                	
                	//System.out.println("In SkeleonChart line 416: nucleus="+e.nucleus.EN()+" i="+i+"  fpos="+fpos+" offset="+offset+"  hasAlpha="+nl.hasDecayMode("A"));

                }
                
                //print energy and spin-parity labels on the level
                com="label.ulft(btex \\labelfont "+printEnergy(levelE,l.DES())+" etex,"+Str.point(e.xpos+Xwidth+0.1f+offset,fpos)+");\n";
                labels.add(com);
                com="label.urt(btex \\labelfont ";
                com+=Translator.spin(l.JPiS());
                com+=" etex,"+Str.point(e.xpos-0.1f,fpos)+");\n";
                labels.add(com);
                
                //if no decay daughter, continue
                //if(!e.toLeft&&!e.toRight&&!l.hasDecayMode("A")&&!l.hasDecayMode("IT"))
                //	continue;
                
                
                
                //////////////////////////////////////////////////////////////////////////////////////////////
                //print b+ and b- decay arrows, branching ratios and Q values for GS (or the first level) only
                //For parent skeletons, the parent state could also be isomer
                //////////////////////////////////////////////////////////////////////////////////////////////
                float howmuch=0.7f;
                int nQs=0;
                DecayMode dm;
                boolean betaPlusDecay=false, betaMinusDecay=false;
                if(e.toRight&&i==0){//for possible B- decay
                	                    
                    e2=(EnsdfSkeleton)enskelV.elementAt(index+1);

                    
                    if((fpos-e2.yposE*Yscale)<howmuch) 
                    	howmuch=fpos-e2.yposE*Yscale;
                    
                    if(l.hasDecayMode("B-"))
                    	out.write("drawarrow "+Str.point(e.xpos+Xwidth,fpos)+"--"+Str.point(e.xpos+Xwidth+0.2f,fpos-howmuch)+" withcolor gamcol;\n");
                    
                    e.lowerRightPoint().setX(e.xpos+Xwidth+0.2f);
                    
                    //if(i==0){
                        dm=null;
                        int nvalues=0;
                        for(int j=0;j<l.nDecayModes();j++){
                                if(l.decayModeAt(j).name().equals("B-")) 
                                	dm=l.decayModeAt(j);
                                if(l.decayModeAt(j).value().length()>0)
                                	nvalues++;
                        }
                        
                        
                        float xBR=0,yBR=0;
                        if(dm!=null){
                        	//BR label starts (left aligned) from xBR
                            com="label.lrt(btex \\labelfont ";
                            
                            if(!dm.symbol().equals("=")) 
                            	com+=latexSymbol(dm.symbol());
                            else if(dm.value().equals("0"))
                            	com+="$\\approx$";
                            
                            if(nvalues>1)//move down the label to avoid overlap when there are more than one decay branches with percentage values
                            	howmuch+=0.25f;
                            
                            xBR=e.xpos+Xwidth- ladderSpacing/2;
                            yBR=fpos-howmuch;
                            com+=dm.value()+"\\% etex,"+Str.point(xBR, yBR)+");\n";

                            
                            labels.add(com);
                        }
                        
                        float xQ=e.xpos+Xwidth/2;
                        float yQ=fpos-1.1f-0.21f*nQs;
                        
                        String QS=e.qv.QBMS()+e.qv.DQBMS();
                        float QSLen=(QS.length()+3)*NDSConfig.CHAR2CM;
                                                
                        //NOTE that Q label is centered on (xQ,yQ) point, while BR label starts from (xBR,yBR) point
                        if(xBR>0 && xBR<(xQ+QSLen/2) && Math.abs(yBR-yQ)<NDSConfig.DEFAULT_LINEWIDTH) {//Q-value and branching overlap in drawing
                        	xQ=xBR-QSLen/2;
                        }
                        
                        com="label(btex \\labelfont Q\\ensuremath{^-}"+printEnergy(e.qv.QBMS(),e.qv.DQBMS(),"",true)+" etex,"+
                                Str.point(xQ,yQ)+");\n";
                        
                        
                        e.lowerLeftPoint().setY(fpos-1.1f-0.21f*nQs);
                        e.lowerRightPoint().setY(fpos-1.1f-0.21f*nQs);
                        
                        
                        labels.add(com);
                        nQs++;
                    //}
                    betaMinusDecay=true;
                }
                
                if(e.toLeft&&i==0){//for possible B+ or EC decay
                    com="label.lrt(btex \\labelfont ";
                    
                    if(!betaPlusDecay){
                        dm=null;
                        e2=(EnsdfSkeleton)enskelV.elementAt(index-1);
                        float totalBplus=0;
                        for(int k=0;k<l.nDecayModes();k++){
                            if(l.decayModeAt(k).name().equals("B+")||l.decayModeAt(k).name().equals("EC")){
                                totalBplus+=l.decayModeAt(k).doubleValue();
                                dm=l.decayModeAt(k);
                            }
                            if(l.decayModeAt(k).name().equals("EC+%B+")){
                                totalBplus=(float)l.decayModeAt(k).doubleValue();
                                dm=l.decayModeAt(k);
                                break;
                            }
                        }
                        if((fpos-e2.yposE*Yscale)<howmuch) 
                        	howmuch=fpos-e2.yposE*Yscale;
                        
                        if(l.hasDecayMode("B+") || l.hasDecayMode("EC") || l.hasDecayMode("EC+%B+"))
                        	out.write("drawarrow "+Str.point(e.xpos,fpos)+"--"+Str.point(e.xpos-0.2f,fpos-howmuch)+" withcolor outcol;\n");
                        
                        e.lowerLeftPoint().setX(e.xpos-0.2f);
                        
                        //if(i==0){
                            if(dm!=null){
                                if(!dm.symbol().equals("=")) com+=latexSymbol(dm.symbol());
                                
                                if(dm.name().equals("EC+%B+"))
                                	com+=dm.value()+"\\% etex,"+Str.point(e.xpos-ladderSpacing, fpos-howmuch)+");\n";
                                else
                                	com+=(int)totalBplus+"\\% etex,"+Str.point(e.xpos-ladderSpacing, fpos-howmuch)+");\n";
                                
                                
                                labels.add(com);
                            }

                            
                            com="label(btex \\labelfont Q\\ensuremath{^+}";
                            
                            String s=e2.qv.QBMS().trim();
                           	if(s.charAt(0)=='-')//must be negative here
                        		s=s.substring(1);
                            
                            com+=printEnergy(s,e2.qv.DQBMS(),"",true);
                            
                            com+="etex,"+ Str.point(e.xpos+Xwidth/2,fpos-1.1f-0.21f*nQs)+");\n";
                            
                            e.lowerLeftPoint().setY(fpos-1.1f-0.21f*nQs);
                            e.lowerRightPoint().setY(fpos-1.1f-0.21f*nQs);
                            
                            labels.add(com);
                            nQs++;
                        //}
                        betaPlusDecay=true;
                    }
                }
               
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //print arrows and Q labels for other modes (other than B+ and B- above) of GS or the first level (i==0) and arrows for all modes of excited levels (i>0)
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                @SuppressWarnings("unused")
				boolean verticalDecay=false;
                int nParBR=0;
                
                Vector<DecayMode> dmV=new Vector<DecayMode>();
                dmV.addAll(l.decayModesV());
                if(i==0) {//sort decay modes (other than B- and EC) to print %A at the top 
                    for(int j=0;j<dmV.size();j++){
                        dm=dmV.get(j);
                        if(dm.name().equals("A")){
                            dmV.remove(j);
                            dmV.add(0,dm);
                            break;
                        }
                        
                    }                    
                }

                for(int j=0;j<dmV.size();j++){
                    howmuch=0.6f;
                    dm=dmV.get(j);
                    
                    //System.out.println("In SkeleonChart line 528: nucleu="+e.nucleus.EN()+" dm.name="+dm.name()+" i="+i);
                    
                    if(dm.name().equals("B-")){
                        //if(!betaMinusDecay){
                    	if(i!=0) {//print arrows for B- mode of EXCITED levels
                            e2=(EnsdfSkeleton)enskelV.elementAt(index+1);
                            if((fpos-e2.yposE*Yscale)<howmuch) howmuch=fpos-e2.yposE*Yscale;
                            out.write("drawarrow "+Str.point(e.xpos+Xwidth,fpos)+"--"+Str.point(e.xpos+Xwidth+0.2f,fpos-howmuch)+" withcolor gamcol;\n");                        
                            betaMinusDecay=true;
                        }
                    }else if(dm.name().equals("B+")||dm.name().equals("EC")||dm.name().equals("EC+%B+")){
                        //if(!betaPlusDecay){
                    	if(i!=0) {//print arrows for B+ or EC mode of EXCITED levels
                            e2=(EnsdfSkeleton)enskelV.elementAt(index-1);
                            if((fpos-e2.yposE*Yscale)<howmuch) howmuch=fpos-e2.yposE*Yscale;
                            out.write("drawarrow "+Str.point(e.xpos,fpos)+"--"+Str.point(e.xpos-0.2f,fpos-howmuch)+" withcolor outcol;\n");
                            betaPlusDecay=true;
                        }
                    }else if(dm.name().equals("IT")&&i>0){//print vertical arrow for IT mode of EXCITED levels (gamma decays inside a nucleus)
                        howmuch=0.2f;
                        if(fpos-(flayout.getPosition(e.levelAt(i-1))+e.yposE*Yscale)<howmuch) howmuch=fpos-(flayout.getPosition(e.levelAt(i-1))+e.yposE*Yscale);
                        out.write("drawarrow "+Str.point(e.xpos+Xwidth-right_pad+0.25f,fpos)+"--"
                                +Str.point(e.xpos+Xwidth-right_pad+0.25f, fpos-howmuch)+" ;\n");
                    }else if(dm.name().length()>0){//all other modes other than B-, B+, EC and IT of GS and EXCITED levels, including A decay and particle decays
                     //else if(dm.name().length()>0&&!verticalDecay){//all other decays
 
                        if(e.toRight)  
                        	e2=(EnsdfSkeleton)enskelV.elementAt(index+1);
                        else if(e.toLeft)
                        	e2=(EnsdfSkeleton)enskelV.elementAt(index-1);
                        else
                            e2=null;
        
                            
                        
                        if(e2!=null && (fpos-e2.yposE*Yscale)<howmuch) 
                        	howmuch=fpos-e2.yposE*Yscale;
                        
                        if(howmuch<=0.0f) 
                        	howmuch=0.35f;
                        
                        if(i>0) {
                        	float lastLevelY=flayout.getPosition(e.levels.elementAt(i-1))+e.yposE*Yscale;
                        	if((fpos-howmuch)<lastLevelY)
                        		howmuch=fpos-lastLevelY-0.01f;
                        	
                        	//System.out.println("   lastlevelY="+lastLevelY+"   fpos-lastLevelY="+(fpos-lastLevelY));
                        }
                        	
                        float x=0.0f;
                        x=e.xpos+Xwidth;
                                               
                        ///////////////////////////////////////////////////////////////////////////////////////////
                        //draw arrows for all other modes (other than B-, B+, EC and IT) for GS or the first level and EXCITED levels
                        ///////////////////////////////////////////////////////////////////////////////////////////
                        if(dm.name().contains("A")){
                        	String name=dm.name();
                        	String color1S="(0,0.6,0)";//green
                        	String color2S="(1,1,1)";//white
                        	if(!name.equals("A")){
                        		if(dm.name().contains("B+")||dm.name().contains("EC")) 
                        			color1S="outcol";//blue
                        		else if(dm.name().contains("B-")) 
                        			color1S="gamcol";//red
                        		else
                        			color1S="levcol";//black
                        	}
                     
                        	
                            out.write("drawarrow "+Str.point(x-0.1f, fpos-0.02f)+"--"+Str.point(x-0.1f,fpos-howmuch)+" withcolor "+color1S+" withpen pencircle scaled 2 ;\n");
                            out.write("drawarrow "+Str.point(x-0.1f, fpos-0.02f)+"--"+Str.point(x-0.1f,fpos-howmuch)+" withcolor "+color2S+" withpen pencircle scaled 0.5;\n");
                        	
                            //System.out.println("In SkeleonChart line 570: nucleu="+e.nucleus.EN()+" dm.name="+dm.name()+" i="+i+"  fpos="+fpos+" howmuch="+howmuch+
                            //		" e2.ypos*Yscale="+(e2.ypos*Yscale));
                            
                        }else{
                            out.write("drawarrow ");
                            out.write(Str.point(x,fpos)+"--"+Str.point(x,fpos-howmuch));
                            if(dm.name().contains("B+")||dm.name().contains("EC")) 
                            	out.write(" withcolor outcol");
                            else if(dm.name().contains("B-")) 
                            	out.write(" withcolor gamcol");
                            else 
                            	out.write(" withcolor (0,0.6,0)");
                            
                            //System.out.println("In SkeleonChart line 570: nucleu="+e.nucleus.EN()+" dm.name="+dm.name()+" i="+i+"  fpos="+fpos+" howmuch="+howmuch+
                            //		" e2.ypos*Yscale="+(e2.ypos*Yscale));
                        }
                        

                        
                        out.write(";\n");
                        
                        /////////////////////////////////////////////////////////////////////////
                        //print decay branching ratios and Q values of other branches for GS ONLY 
                        /////////////////////////////////////////////////////////////////////////
                        if((i==0)&&(dm.name().equals("A")||dm.name().equals("P")||dm.name().equals("N")||e.qv==null)){
                        	
                            nParBR++;
                            
                        	//BR label is centered at xBR
                            com="label(btex \\labelfont ";
                            
                    	    if(!dm.symbol().equals("=")) 
                    	    	com+=latexSymbol(dm.symbol());
                    	    else if(dm.value().equals("0"))
                            	com+="$\\approx$";
                            
                    	    if(dm.value().length()>0) 
                            	com+=dm.value();
                            else 
                            	com+="?";
                                                                        
                            float xBR=x;
                            float yBR=fpos-howmuch-0.21f*nParBR;//here x=e.xpos+Xwidth (right edge of level line)     
                            
                            if(!dm.name().equals("A"))
                                xBR=x+0.1f;
                            
                            if((dm.symbol().equals("AP") || dm.value().equals("0")) && betaMinusDecay) 
                            	xBR=x-0.2f;
                            	
                            com+="\\% etex,"+Str.point(xBR, yBR)+");\n"; 
                            
                            labels.add(com);
                            
                            
                            com="label(btex \\labelfont Q";
                            if(dm.name().equals("A")) com+="\\ensuremath{_{\\alpha}}";
                            else if(dm.name().equals("P"))  com+="\\ensuremath{_{p}}";
                            else if(dm.name().equals("N"))  com+="\\ensuremath{_{n}}";
                            else com+="("+Translator.process(dm.name(),true)+")";
                                
                            
                            String QS="";                                
                            if(e.qv==null) {   
                            	if(e.qbs!=null && e.dqbs!=null) {
                            		com+=printEnergy(e.qbs,e.dqbs,"",true);
                            		QS=e.qbs+" "+e.dqbs;
                            	}else
                            		com+="?";
       

                            }else if(dm.name().equals("A")) { 
                            	com+=printEnergy(e.qv.QAS(),e.qv.DQAS(),"",true);
                            	QS=e.qv.QAS()+" "+e.qv.DQAS();
                            }else if(dm.name().equals("P")){
                            	String s=e.qv.SPS().trim();
                            	if(s.charAt(0)=='-')
                            		s=s.substring(1);
                            	
                            	com+=printEnergy(s,e.qv.DSPS(),"",true);
                            	QS=e.qv.DSPS();
                            }else if(dm.name().equals("N")){
                            	String s=e.qv.SNS().trim();
                            	if(s.charAt(0)=='-')
                            		s=s.substring(1);
                            	
                            	com+=printEnergy(s,e.qv.DSNS(),"",true);
                            	QS=e.qv.DSNS();
                            }
                                        
                             
                            float QSLen=(QS.length()+3)*NDSConfig.CHAR2CM;
                            float xQ=e.xpos+Xwidth/2;
                            float yQ=fpos-1.13f-0.22f*nQs;
                            
                            String BRS=dm.symbol()+dm.value();
                            float BRSLen=(BRS.length()+1)*NDSConfig.CHAR2CM;
                            
                            //NOTE that here BR label is centered at xBR and Q label is centered on (xQ,yQ) point
                            if(xBR>0 && (xBR-BRSLen/2)<(xQ+QSLen/2) && Math.abs(yBR-yQ)<NDSConfig.DEFAULT_LINEWIDTH) {//Q-value and branching overlap in drawing
                            	xQ=xBR-BRSLen/2-QSLen/2;
                            }

                            
                            //com+=" etex,"+Str.point(e.xpos+Xwidth/2,fpos-1.13f-0.21f*nQs)+");\n";
                            com+=" etex,"+Str.point(xQ,yQ)+");\n";

                            labels.add(com);
                            nQs++;                           	
                        }
                        verticalDecay=true;
                    }//other decay modes (A and particle)
                    
                }//loop processing all decay modes (other than B-, B+ and EC of GS) 
            }//loop for all levels meeting conditions
        }//loop for all levels

        
        e.getShape().addPoint(e.lowerLeftPoint());
        e.getShape().addPoint(e.lowerRightPoint());
        e.getShape().addPoint(e.upperLeftPoint());
        e.getShape().addPoint(e.upperRightPoint());
        
        //print labels
        for(int i=0;i<labels.size();i++){
            out.write((String)labels.elementAt(i));
        }
        
        //System.out.println(" nucleus="+e.nucleus.A()+e.nucleus.En()+"  parent n="+e.parentSkels.size());
        
        /////////////////////////////////////////////////////////////////////////////
        //draw skeletons for all parents of the current nucleus (recursive calling)
        /////////////////////////////////////////////////////////////////////////////
        if(e.parentSkels.size()>0){
            for(int i=0;i<e.parentSkels.size();i++){
                e2=(EnsdfSkeleton)e.parentSkels.elementAt(i);
                
                //NOTE that it is ypos that is set in initializing skeleton of a PDecay parent
                //while for regular skeleton, it is the yposE=level energy is set and
                //in writeNucleus, yposE and Yscale are used.
                
                //make the first level the ground, for parent levels not the GS
                //since in drawing levels, level.EF() will be added to yposE to get level position
                e2.yposE=e2.ypos/Yscale-e2.firstLevel().EF();
             
                e2.xpos=e.xpos;
                
                //recursive calling
                writeNucleus(index,out,Xwidth,Yscale,(EnsdfSkeleton)e.parentSkels.elementAt(i));
            }
        }
    }
    
	@SuppressWarnings("unused")
	private void writeTable(java.io.Writer out)throws Exception{
		out.write(printTable());
	}
	
	private  String printTable()throws Exception{
    	int ncolumn=5;
    	Vector<String> columnStringsMax=new Vector<String>();
    	Vector<String> temp;
    	Vector<Vector<String>> tempV;
    	
    	String lines="";
    	int maxLength=0;
    	int nlines=0;
    	
        columnStringsMax.add("Nuclide");
        columnStringsMax.add("Level");
        columnStringsMax.add("Jp");
        columnStringsMax.add("T1/2");
        columnStringsMax.add("Decay Mode");
        
        nlines+=3;//title,heading,underline
        
        EnsdfSkeleton e;   
        
        for(int i=0;i<enskelV.size();i++){
        	tempV=(Vector<Vector<String>>)printTableLines((EnsdfSkeleton)enskelV.elementAt(i));
        	for(int j=0;j<tempV.size();j++){      	
            	temp=(Vector<String>)tempV.get(j);//last element is the whole table line, the rest are column strings
            	lines+=temp.lastElement();
            	nlines++;
                for(int k=0;k<ncolumn;k++){
                	if(temp.elementAt(k).length()>columnStringsMax.elementAt(k).length()){
                		columnStringsMax.remove(k);
                		columnStringsMax.add(k,temp.elementAt(k));
                	}
                }
        	}

        }
        
        for(int i=0;i<enskelV.size();i++){
            e=(EnsdfSkeleton)enskelV.elementAt(i);

            for(int j=0;j<e.parentSkels.size();j++){
                temp=(Vector<String>)printTableLineGS((EnsdfSkeleton)e.parentSkels.elementAt(j));
                lines+=temp.lastElement();
                nlines++;
                for(int k=0;k<ncolumn;k++){
                	if(temp.elementAt(k).length()>columnStringsMax.elementAt(k).length()){
                		columnStringsMax.remove(k);
                		columnStringsMax.add(k,temp.elementAt(k));
                	}
                }
            }
        }
        
        String tempS="";
        for(int i=0;i<ncolumn;i++)
        	tempS+=columnStringsMax.elementAt(i)+"   ";
        
        maxLength=tempS.length();
        
        float tableW=maxLength*NDSConfig.CHAR2CM*0.7f;
        float tableH=nlines*NDSConfig.DEFAULT_LINEWIDTH*0.8f;
        
    	//debug
    	//System.out.println("In SkeletonChart0: tableX,Y="+tableX+" "+tableY+" default X,Y="+defaultTableX+" "+defaultTableY+" maxString="+tempS+" nlines="+nlines);
    	//System.out.println("         "+(tableX==defaultTableX)+" "+(tableY==defaultTableY));
    	
        if(!useFixedX || !useFixedY){
        	
        	Vector<SkeletonShape> shapes=new Vector<SkeletonShape>();
        	int breakIndex=-1;
        	int lastIndex=enskelV.size();//last index in the page where the summary table is placed
        	
        	if(tablePage>1)
        		breakIndex=pageBreaks.elementAt(tablePage-2);
        	
        	if(tablePage>0 && tablePage<pageBreaks.size()+1)//tablePage=1 for the first page
        		lastIndex=pageBreaks.elementAt(tablePage-1);
        		
        	if(breakIndex==-1) breakIndex=0;
        	
        	for(int i=breakIndex;i<lastIndex;i++){
        		e=(EnsdfSkeleton)enskelV.elementAt(i);
        		shapes.add(e.getShape());
        		for(int j=0;j<e.parentSkels.size();j++)
        			shapes.add(e.parentSkels.elementAt(j).getShape());
        	}
        	
        	//debug
        	//System.out.println("In SkeletonChart1: tableX,Y="+tableX+" "+tableY);
        	
        	foundTableLocation=findTableLocation(tableW,tableH,shapes);
        	
        	//debug
        	//System.out.println("In SkeletonChart2: tableX,Y="+tableX+" "+tableY+" findTableLocation="+foundTableLocation);
        	
            //can't find location to place decay table in skeleton, so put it in a new page
            //(tableX,tableY) the lower-right point of the table
            if(!foundTableLocation){
            	tableX=tableW+0.5f;
            	tableY=H-tableH-1.5f;           	
            }
        }
        
              
        String font="\\labelfont";
        
        String header="";
        header+="%Decay table\n";
        header+="label.ulft(btex "+font+" \n";
        header+="\\begin{tabular}{|lllll|}\n";
        header+="\\hline\n";
        header+="\\multicolumn{5}{|c|}{\\bf Ground-State and Isomeric-Level Properties}\\\\\n";
        header+="Nuclide&Level&J\\ensuremath{\\pi}&T\\ensuremath{_{1/2}}&Decay Mode\\\\\n";
        header+="[-.2cm]\n";
        header+="\\hrulefill&\\hrulefill&\\hrulefill&\\hrulefill&\\hrulefill\\\\\n";
        
        lines=header+lines;
        
                
        lines+="\\hline\n";
        lines+="\\end{tabular} etex,"+Str.point(tableX, tableY)+");\n";
        
        return lines;
	}
	
    /**prints the summary table */    
    @SuppressWarnings("unused")
    private void writeTable_old(java.io.Writer out)throws Exception{
    	int ncolumn=5;
    	Vector<String> columnStringsMax=new Vector<String>();
    	Vector<String> temp;

    	String lines="";
    	int maxLength=0;
    	int nline=0;
    	float width,height;
    	
        columnStringsMax.add("Nuclide");
        columnStringsMax.add("Level");
        columnStringsMax.add("Jp");
        columnStringsMax.add("T1/2");
        columnStringsMax.add("Decay Mode");
        
        nline+=3;//title,heading,underline
        
        EnsdfSkeleton e;
        for(int i=0;i<enskelV.size();i++){
        	temp=(Vector<String>)printTableLineGS((EnsdfSkeleton)enskelV.elementAt(i));
        	lines+=temp.lastElement();
            for(int k=0;k<ncolumn;k++){
            	if(temp.elementAt(k).length()>columnStringsMax.elementAt(k).length()){
            		columnStringsMax.remove(k);
            		columnStringsMax.add(k,temp.elementAt(k));
            	}
            }

        }
       
        
        nline+=enskelV.size();
        
        for(int i=0;i<enskelV.size();i++){
            e=(EnsdfSkeleton)enskelV.elementAt(i);
            nline+=e.parentSkels.size();

            for(int j=0;j<e.parentSkels.size();j++){
                temp=(Vector<String>)printTableLineGS((EnsdfSkeleton)e.parentSkels.elementAt(j));
                lines+=temp.lastElement();
                for(int k=0;k<ncolumn;k++){
                	if(temp.elementAt(k).length()>columnStringsMax.elementAt(k).length()){
                		columnStringsMax.remove(k);
                		columnStringsMax.add(k,temp.elementAt(k));
                	}
                }
            }
        }
        
        String tempS="";
        for(int i=0;i<ncolumn;i++)
        	tempS+=columnStringsMax.elementAt(i)+"   ";
        
        maxLength=tempS.length();
        
        float tableW=maxLength*NDSConfig.CHAR2CM*0.7f;
        float tableH=nline*NDSConfig.DEFAULT_LINEWIDTH*0.8f;
        
    	//debug
    	//System.out.println("In SkeletonChart0: tableX,Y="+tableX+" "+tableY+" default X,Y="+defaultTableX+" "+defaultTableY+" maxString="+tempS);
    	//System.out.println("         "+(tableX==defaultTableX)+" "+(tableY==defaultTableY));
    	
        boolean found=false;
        if(tableX==defaultTableX&&tableY==defaultTableY){
        	
        	Vector<SkeletonShape> shapes=new Vector<SkeletonShape>();
        	int breakIndex=-1;
        	if(tablePage>1)
        		breakIndex=pageBreaks.elementAt(tablePage-2);
        	
        	if(breakIndex==-1) breakIndex=0;
        	
        	for(int i=breakIndex;i<enskelV.size();i++){
        		e=(EnsdfSkeleton)enskelV.elementAt(i);
        		shapes.add(e.getShape());
        		for(int j=0;j<e.parentSkels.size();j++)
        			shapes.add(e.parentSkels.elementAt(j).getShape());
        	}
        	
        	//debug
        	//System.out.println("In SkeletonChart1: tableX,Y="+tableX+" "+tableY);
        	
        	found=findTableLocation(tableW,tableH,shapes);
        	
        	//debug
        	//System.out.println("In SkeletonChart2: tableX,Y="+tableX+" "+tableY);
        }
        
        
        String font="\\labelfont";
        
        out.write("%Decay table\n");
        out.write("label.ulft(btex "+font+" \n");
        out.write("\\begin{tabular}{|lllll|}\n");
        out.write("\\hline\n");
        out.write("\\multicolumn{5}{|c|}{\\bf Ground-State and Isomeric-Level Properties}\\\\\n");
        out.write("\\underline{Nuclide}&\\underline{Level}&\\underline{J\\ensuremath{\\pi}}&\\underline{" +
                "T\\ensuremath{_{1/2}}}&\\underline{Decay Mode}\\\\\n");
        
        out.write(lines);


        
        out.write("\\hline\n");
        out.write("\\end{tabular} etex,"+Str.point(tableX, tableY)+");\n");
    }
    
    
    private boolean findTableLocation(float tableW,float tableH,Vector<SkeletonShape> shapes){
    	float xstep=1.0f;
    	float ystep=2.0f;
    	
    	//(defaultTableX,defaultTableY) is the lower-right point of the page
    	//(defaultTableX,defaultTableY) is always for the lower-right point of the table
    	float x=tableX; 
    	float y=tableY;
    	
    	//float ymax=H-tableH-1.5f;
    	float ymax=H-tableH-1.5f;
    	float xmax=W-0.5f;
    	
    	float xmin=tableW+0.2f;
    	float ymin=0.1f;
    	float extraW=1.0f;
    	float extraH=1.6f;
    	
    	int nshapes=shapes.size();
    	
    	    	
    	//debug
    	//System.out.println("findTableLocation: tableW,H="+tableW+" "+tableH+" ymax="+ymax);
    	
    	//first check from lower right of the page    	
    	for(y=ymin;y<ymax;y+=ystep){
    		int i;
    		SkeletonShape tableShape=new SkeletonShape(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH);
    		for(i=0;i<nshapes;i++){
    			//debug
    			//System.out.println("findTableLocation 1: x,y="+x+" "+y+" shape "+i+" "+shapes.get(i).overlaps(x-tableW, y, tableW, tableH)+" nshapes="+nshapes);
    			//for(int k=0;k<shapes.get(i).nPoints();k++)
    			//   System.out.println("    shape points: x,y="+shapes.get(i).pointAt(k).x()+"    "+shapes.get(i).pointAt(k).y());
    			
    			if(shapes.get(i).overlaps(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH)){// in overlap(x,y,w,h): parameters (x,y) is the lower-left point of the rectangle
    	    		//debug
    	    		//System.out.println("findTableLocation 2: x,y="+x+" "+y+" shape "+i+" "+shapes.get(i).overlaps(x-tableW, y, tableW, tableH)+" nshapes="+nshapes);
    				
    				break;
    			}
    			if(tableShape.contains(shapes.get(i)))
    				break;
    		}
    		
    		if(i==nshapes){//no overlap found 
    			tableY=y;
    			tableX=x;
    			return true;
    		}    		
    	}
    	
    	//debug
    	//System.out.println("findTableLocatin: still not found 1");
    	
    	//if good (x,y) is still not found, then try from upper left of the page
    	x=xmin;
    	for(y=ymax;y>ymin;y-=ystep){
    		int i;
    		SkeletonShape tableShape=new SkeletonShape(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH);
    		for(i=0;i<nshapes;i++){
    			if(shapes.get(i).overlaps(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH))
    				break;
    			if(tableShape.contains(shapes.get(i)))
    				break;

    		}
    		

    		if(i==nshapes){//no overlap found 
    			tableX=x;
    			tableY=y;
    			return true;
    		}    		
    	}
    	
    	//System.out.println("findTableLocatin: still not found 2");
    	
    	//if still not found, check the rest area
    	float xc=(xmin+xmax)/2.0f;
    	for(x=xc;x<xmax;x+=xstep){
        	for(y=ymax;y>ymin;y-=ystep){
        		int i;
        		
        		SkeletonShape tableShape=new SkeletonShape(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH);
        		//check right to center
        		for(i=0;i<nshapes;i++){
        			if(shapes.get(i).overlaps(x-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH))
        				break;
        			if(tableShape.contains(shapes.get(i)))
        				break;
        		}
        		
        		if(i==nshapes){//no overlap found 
        			tableX=x;
        			tableY=y;
        			return true;
        		}  
        		
        		//check left to center
        		float xl=2.0f*xc-x;
        		
        		tableShape=new SkeletonShape(xl-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH);
        		for(i=0;i<nshapes;i++){
        			if(shapes.get(i).overlaps(xl-tableW-extraW/2.0f, y-extraH/2.0f, tableW+extraW, tableH+extraH))
        				break;
        			if(tableShape.contains(shapes.get(i)))
        				break;
        		}
        		
        		if(i==nshapes){//no overlap found 
        			tableX=xl;
        			tableY=y;
        			return true;
        		} 
        	}
    	}
    	     
    	return false;
    }
    
    /*
     * print summary lines of properties of g.s. and isomer of an isotope
     */
    private Vector<Vector<String>> printTableLines(EnsdfSkeleton e)throws Exception{
    	Vector<Vector<String>> columnStringsV=new Vector<Vector<String>>();
    	Vector<Level> levels=new Vector<Level>();
        
    	levels.add(e.firstLevel());
    	
    	for(int i=1;i<e.levels.size();i++){
    		Level l=e.levelAt(i);
      		
    		if(l.msS().trim().indexOf("M")==0)
    			levels.add(l);
    	}

    	for(int i=0;i<levels.size();i++)
    		columnStringsV.add(printTableLine(levels.get(i),e.nucleus));
    	
    	return columnStringsV;
    	
    }
    
    
    /**prints one line of the summary table */
    //The last element is the line to be written into the latex file, the rest are column strings
    private Vector<String> printTableLine(Level l, Nucleus nucleus)throws Exception{
        Vector<String> columnStrings=new Vector<String>();//for counting the actual string length
        String temp="",line="";
        DecayMode dm;
        String name;
        int modes;
        boolean moreThanOneMode=false;
     
        line+="\\ensuremath{^{"+nucleus.A()+"}}"+nucleus.En()+"& ";
        
        columnStrings.add(nucleus.A()+nucleus.En());
        
        String levelE=l.ES().toLowerCase();
        if(levelE.equals("0")) levelE="0.0";
        line+=levelE+"& "+Translator.spin(l.JPiS())+"& "+printNumber(l.T12S(),l.DT12S(),(Translator.halfLifeUnits(l.T12Unit())))+"& ";
        
        columnStrings.add(levelE);
        columnStrings.add(l.JPiS());
        temp=l.T12S()+" "+l.T12Unit()+" "+l.DT12S();
        columnStrings.add(temp.trim());
        
        if(l.nDecayModes()>3) 
        	modes=3;
        else 
        	modes=l.nDecayModes();
        
        temp="";
        for(int j=0;j<modes;j++){
            dm=l.decayModeAt(j);
            name="";
            if(dm.name().equals("A"))
            	name="\\ensuremath{\\alpha}";
            else if (dm.name().equals("B-"))
            	name="\\ensuremath{\\beta^{-}}";
            else if(dm.name().length()==1)
            	name=dm.name().toLowerCase();
            else            	
            	name=Translator.process(dm.name(),false);
            
            if(moreThanOneMode){ 
            	line+="; ";
            	temp+="; ";
            }
            else 
            	moreThanOneMode=true;
            
            line+="\\%"+name;
            temp+="%"+dm.name();
            
            String tempsymbol=latexSymbol(dm.symbol());
            if(tempsymbol.length()<1){
                if(!latexSymbol(dm.unc()).equals(dm.unc())) 
                	tempsymbol=latexSymbol(dm.unc());
                else 
                	tempsymbol="=";
            }
            line+=tempsymbol;
            temp+="=";
            
            //debug
            //System.out.println("in SkeletonChart:"+line+" dm.name="+dm.name()+" name="+name+" symbol="+dm.symbol()+"  tempSymbol="+tempsymbol);
            //System.out.println("          j="+j+" nModes="+modes);
            //System.out.println("    dm.unc()="+dm.unc()+" dm.txt="+dm.txt());
            //System.out.println("In SkeletonChart: %EC+%B+"+Translator.process("%EC+%B+",false)+"  "+Translator.process("%EC+%B+=100",false));
            
            if(dm.value().length()>0){ 
            	line+=Translator.exponential(dm.value());
            	temp+=dm.value();
            	if(dm.value().contains("E"))
            		temp+="...";
            }
            else{ 
            	line+="?";
            	temp+="?";
            }
            if(dm.unc().trim().length()>0 && !tempsymbol.equals(latexSymbol(dm.unc()))){ 
            	line+=" {\\it "+dm.unc()+"}";
            	temp+=" "+dm.unc();
            }
        }
        if(modes<l.nDecayModes()) {
        	line+="...";
        	temp+="...";
        }
        line+="\\\\\n";
        columnStrings.add(temp);
        
        moreThanOneMode=false; 
        
        columnStrings.add(line);
        
        return columnStrings;
    }
    
    /**prints one line of the summary table for GS state */
    //The last element is the line to be written into the latex file, the rest are column strings
    private Vector<String> printTableLineGS(EnsdfSkeleton e)throws Exception{
        Vector<String> columnStrings=new Vector<String>();//for counting the actual string length
        String temp="",line="";
        DecayMode dm;
        String name;
        int modes;
        boolean moreThanOneMode=false;
        Level l=e.firstLevel();
        
        line+="\\ensuremath{^{"+e.nucleus.A()+"}}"+e.nucleus.En()+"& ";
        
        columnStrings.add(e.nucleus.A()+e.nucleus.En());
        
        String levelE=l.ES().toLowerCase();
        if(levelE.equals("0")) levelE="0.0";
        line+=levelE+"& "+Translator.spin(l.JPiS())+"& "+printNumber(l.T12S(),l.DT12S(),(Translator.halfLifeUnits(l.T12Unit())))+"& ";
        
        columnStrings.add(levelE);
        columnStrings.add(l.JPiS());
        temp=l.T12S()+" "+l.T12Unit()+" "+l.DT12S();
        columnStrings.add(temp.trim());
        
        if(l.nDecayModes()>3) 
        	modes=3;
        else 
        	modes=l.nDecayModes();
        
        temp="";
        for(int j=0;j<modes;j++){
            dm=l.decayModeAt(j);
            name="";
            
            //System.out.println(" dm name="+dm.name()+" size="+dm.name().length());
            
            if(dm.name().equals("A"))
            	name="\\ensuremath{\\alpha}";
            else if (dm.name().equals("B-"))
            	name="\\ensuremath{\\beta^{-}}";
            else if(dm.name().length()==1)
            	name=dm.name().toLowerCase();
            else            	
            	name=Translator.process(dm.name(),false);
            
            if(moreThanOneMode){ 
            	line+="; ";
            	temp+="; ";
            }
            else 
            	moreThanOneMode=true;
            
            line+="\\%"+name;
            temp+="%"+dm.name();
            
            String tempsymbol=latexSymbol(dm.symbol());
            if(tempsymbol.length()<1){
                if(!latexSymbol(dm.unc()).equals(dm.unc())) 
                	tempsymbol=latexSymbol(dm.unc());
                else 
                	tempsymbol="=";
            }
            line+=tempsymbol;
            temp+="=";
            
            //debug
            //System.out.println("in SkeletonChart:"+line+" dm.name="+dm.name()+" name="+name+" symbol="+dm.symbol()+"  tempSymbol="+tempsymbol);
            //System.out.println("          j="+j+" nModes="+modes);
            //System.out.println("    dm.unc()="+dm.unc()+" dm.txt="+dm.txt());
            //System.out.println("In SkeletonChart: %EC+%B+"+Translator.process("%EC+%B+",false)+"  "+Translator.process("%EC+%B+=100",false));
            
            if(dm.value().length()>0){ 
            	line+=Translator.exponential(dm.value());
            	temp+=dm.value();
            	if(dm.value().contains("E"))
            		temp+="...";
            }
            else{ 
            	line+="?";
            	temp+="?";
            }
            if(dm.unc().trim().length()>0 && !tempsymbol.equals(latexSymbol(dm.unc()))){ 
            	line+=" {\\it "+dm.unc()+"}";
            	temp+=" "+dm.unc();
            }
        }
        if(modes<l.nDecayModes()) {
        	line+="...";
        	temp+="...";
        }
        line+="\\\\\n";
        columnStrings.add(temp);
        
        moreThanOneMode=false; 
        
        columnStrings.add(line);
        
        return columnStrings;
    }
    
    //prints numbers if no unit is given
    @SuppressWarnings("unused")
	private String printNumber(String s,String ds)throws Exception{
        return printNumber(s,ds,"");
    }
    private String printNumber(String s,String ds,String unit)throws Exception{
        return printNumber(s,ds,unit,false);
    }
    //prints numbers and uncertainties in an easy way
    private String printNumber(String s,String ds,String unit,boolean equals)throws Exception{
        //return Translator.printNumberSuperscipt(s, ds, unit, equals);
    	return Translator.printNumber(s, ds, unit, equals);
    }
    
    private String printEnergy(String s,String ds,String unit,boolean equals)throws Exception{
    	SDS2XDX s2x=new SDS2XDX();
    	s2x.setValues(s, ds);
    	double x=s2x.X();
    	double dx=s2x.DX();
    	
    	if(dx>99 && x>dx){
    		s=String.valueOf(Math.round(x));
    		ds=String.valueOf(Math.round(dx));
    	}
    	
    	return printNumber(s,ds,unit,equals);
    }
    
    private String printEnergy(String s,String ds)throws Exception{   	
    	return printEnergy(s,ds,"");
    }
    
    private String printEnergy(String s,String ds,String unit)throws Exception{
    	return printEnergy(s,ds,unit,false);
    }
    
    private String latexSymbol(String in){
        String symbol;
            //fix symbols
            if(in.equals("AP")) symbol="\\ensuremath{\\approx}";
            else if(in.equals("GT")) symbol="\\ensuremath{>}";
            else if(in.equals("GE")) symbol="\\ensuremath{\\geq}";
            else if(in.equals("LT")) symbol="\\ensuremath{<}";
            else if(in.equals("LE")) symbol="\\ensuremath{\\leq}";
            else if(in.equals(">")) symbol="\\ensuremath{>}";
            else if(in.equals("<")) symbol="\\ensuremath{<}";
            else symbol=in;
                
        return symbol;
        
    }

    private class EnsdfSkeleton{
        protected Nucleus nucleus;
        @SuppressWarnings("unused")
		protected float qb,dqb;
		protected String qbs,dqbs;
        protected QValue qv;
        protected Vector<Level> levels;
        protected Vector <EnsdfSkeleton> parentSkels;
        private float xpos,ypos;//position in cm
        private float yposE;//level energy corresponding to ypos, need to be set
        protected boolean toLeft,toRight;//toRight: beta minus decay to the right neighboring nucleus, toLeft:beta plus decay to the left
        private int nRealLevels;
        
        private float parentHeight=0;// in cm
        
        protected SkeletonPoint upperLeftPoint,upperRightPoint,lowerLeftPoint,lowerRightPoint;
        protected SkeletonShape shape;
        
        public EnsdfSkeleton(){

            levels=new Vector<Level>();
            parentSkels=new Vector<EnsdfSkeleton>();
            upperLeftPoint=new SkeletonPoint();
            upperRightPoint=new SkeletonPoint();
            lowerLeftPoint=new SkeletonPoint();
            lowerRightPoint=new SkeletonPoint();
            shape=new SkeletonShape();
        }
        
        public EnsdfSkeleton(float p){
            this();
            ypos=p;//position in cm
        }
        
        
        public void parseENSDF(ENSDF ens)throws Exception{
            
        	if(parentSkels!=null) parentSkels.clear();
            if(levels!=null) levels.clear();
        	
            boolean isAlphas=false,isProtons=false,isNeutrons=false;
            nucleus=ens.nucleus();
            QValue q=ens.qv();
            qb=q.QBMF();
            dqb=q.DQBMF();
            qv=ens.qv();
            nRealLevels=1; //number of levels (isomers) to be plotted
                     
            //create level objects for the separation energies so we can use the fanlayout object
            levels.add(ens.firstLevel());
            Level isomer;
            for(int i=1;i<ens.nLevels();i++){
                isomer=ens.levelAt(i);
                if(isomer.msS().contains("M")){
                    levels.add(isomer);
                    nRealLevels++;
                }
            }       
            
            /*
            Level l;
            for(int i=0;i<levels.size();i++){
                l=(Level)levels.elementAt(i); 
                for(int j=0;j<l.nDecayModes();j++){
                    if(l.decayModeAt(j).name().contains("A")) isAlphas=true;
                    if(l.decayModeAt(j).name().equals("P")) isProtons=true;
                    if(l.decayModeAt(j).name().equals("N")) isNeutrons=true;
                }
            }
            */
            
            if(q.QAF()<0&&Str.isNumeric(q.QAS()) && !isAlphas){
            	//System.out.println("###"+q.QAF()+"   $"+q.QAS()+" "+ens.nucleus().nameENSDF()+" "+ens.DSId0());
            	
                Level qa=new Level(q.QAS().replace('-', ' ').trim(),ens.qv().DQAS(),"S(\\ensuremath{\\alpha})");
                levels.add(qa);
            }
            if(q.SNF()>0&&!isNeutrons){
                Level sn=new Level(q.SNS().replace('-', ' ').trim(),ens.qv().DSNS(),"S(n)");
                levels.add(sn);
            }
            if(q.SPF()>0&&!isProtons){
                Level sp=new Level(q.SPS().replace('-', ' ').trim(),ens.qv().DSPS(),"S(p)");
                levels.add(sp);
            }
            
            sortLevels();
            
            //add particle-decay parent levels into separate skeletons in "parentSkels"
            //so parent levels are not included in the current skeleton
            addPDecayParents();
        }
        
        /*
         * set values for parent skeleton using Parent objects
         */
        private void setValues(Vector<Parent> pVector){
            Parent p=(Parent)pVector.elementAt(0);
            nucleus=p.nucleus();
            qb=p.QF();
            dqb=p.DQF();
            qbs=p.QS();
            dqbs=p.DQS();
            if(qb<0) qb=-qb;
            qv=null;
            for(int i=0;i<pVector.size();i++){
                p=(Parent)pVector.elementAt(i);
                if(!p.level().hasDecayMode(p.DMName()))
                	p.level().addDecayMode(p.DM());
                levels.add(p.level());
            }
            
            sortLevels();
            
            nRealLevels=levels.size();
        }
        
        public Level firstLevel(){
            return (Level)levels.elementAt(0);
        }
        
        public Level lastLevel(){
            return (Level)levels.lastElement();
        }
        
        public Level levelAt(int i){
            return (Level)levels.elementAt(i);
        }
        public int nRealLevels(){
            return nRealLevels;
        }
        
        
        public void sortLevels(){
        	Vector<Level> sortedLevels=new Vector<Level>();
        	while(levels.size()>1){
        		Level l=levels.get(0);
        		for(int i=1;i<levels.size();i++)
        			if(levels.get(i).EF()<l.EF())
        				l=levels.get(i);
        			
        		sortedLevels.add(l);
        		levels.remove(l);
        	}
        	sortedLevels.addAll(levels);
        	levels.clear();
        	levels.addAll(sortedLevels);
        }
        public SkeletonShape getShape(){return shape;}
        
        public SkeletonPoint upperLeftPoint(){return upperLeftPoint;}
        public SkeletonPoint upperRightPoint(){return upperRightPoint;}
        public SkeletonPoint lowerLeftPoint(){return lowerLeftPoint;}
        public SkeletonPoint lowerRightPoint(){return lowerRightPoint;}
        
        @SuppressWarnings("unused")
		public SkeletonPoint getPoint(String str){
        	String s=str.toUpperCase();
        	if(s.contains("LOWERLEFT"))
        		return lowerLeftPoint;
        	else if(s.contains("LOWERRIGHT"))
        		return lowerRightPoint;
        	else if(s.contains("UPPERLEFT"))
        		return upperLeftPoint;
        	else if(s.contains("UPPERRIGHT"))
        		return upperRightPoint;
        	
        	return new SkeletonPoint(-1,-1);
        	
        }
        
        @SuppressWarnings("unused")
		public void setPoint(SkeletonPoint p,String str){
        	String s=str.toUpperCase();
        	if(s.contains("LOWERLEFT"))
        		lowerLeftPoint=p;
        	else if(s.contains("LOWERRIGHT"))
        		lowerRightPoint=p;
        	else if(s.contains("UPPERLEFT"))
        		upperLeftPoint=p;
        	else if(s.contains("UPPERRIGHT"))
        	    upperRightPoint=p;
        	
        	return ;        	
        }
        
        public LevelLabelLayout fan(float Yscale){
            LevelLabelLayout flayout=new LevelLabelLayout();
            for(int i=0;i<levels.size();i++){
                if(i==0||i>=nRealLevels) flayout.addLevel((Level)levels.elementAt(i),((Level)levels.elementAt(i)).EF()*Yscale);
                else flayout.addLevel((Level)levels.elementAt(i),((Level)levels.elementAt(i)).EF()*Yscale+0.01f);
            }
            flayout.calc(H,FONT_SIZE);
            return flayout;
        }
        
        /*
         * from max (Q+E(parent)) to min (Q+E(parent)), since the parent skeleton is drawn starting from page top (max Q+E(parent))
         */
        private Vector<ENSDF> sortDecayENSDFs(Vector<ENSDF> decayENSDFsV) {
        	try {
        		
            	Vector<ENSDF> sortedENSDFs=new Vector<ENSDF>();
            	while(decayENSDFsV.size()>1){
            		ENSDF maxEns=decayENSDFsV.get(0);
            		Parent p=maxEns.parentAt(0);
            		double maxE=p.level().EF()+p.QF();
            		double e=-1;
            		for(int i=1;i<decayENSDFsV.size();i++) {
            			ENSDF ens=decayENSDFsV.get(i);
            			p=ens.parentAt(0);
            			e=p.level().EF()+p.QF();
            			if(e>maxE) {
            				maxE=e;
            				maxEns=ens;
            			}
            		}
            		
            		//System.out.println("#### nuclide="+nucleus.nameENSDF()+"  maxEF="+maxE);
            		
            		sortedENSDFs.add(maxEns);
            		decayENSDFsV.remove(maxEns);
            	}
            	sortedENSDFs.addAll(decayENSDFsV);
            	decayENSDFsV.clear();
            	decayENSDFsV.addAll(sortedENSDFs);
            	
            	//System.out.println("#### nuclide="+nucleus.nameENSDF()+"  parent size="+decayENSDFsV.size());
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	
        	return decayENSDFsV;
        }
        
        private void addPDecayParents()throws Exception{
            //print the (particle decay) parent records, if needed
            ENSDF ens;
            String massString="";
            int Pmass=0,nParent=0;
            Vector<String> labels=new Vector<String>();
            float temppos=H-parentSpace;//Y position in cm (from bottom to top)
            Vector<Parent> parentV=new Vector<Parent>();
            EnsdfSkeleton enskel;
            
            
            Vector<ENSDF> decayENSDFsV=new Vector<ENSDF>();
            for(int i=0;i<ensdfV.size();i++){
                parentV.clear();
                massString="";
                Pmass=0;
                ens=(ENSDF)ensdfV.elementAt(i);
                if(ens.nucleus().EN().equals(nucleus.EN())){
                    if(ens.fullDSIdS().contains("DECAY")){
                        for(int j=0;j<ens.fullDSIdS().length();j++){
                            if(ens.fullDSIdS().charAt(j)<='9'&&ens.fullDSIdS().charAt(j)>='0') 
                            	massString+=ens.fullDSIdS().charAt(j);
                            else 
                            	break;
                        }
                        try{
                            Pmass=Integer.parseInt(massString);
                        }catch(Exception ex){
                            System.out.println(ens.fullDSIdS()+" "+massString);
                            System.out.println("The DSId for the parent record has a problem");
                            continue;
                        }
                    }
                }
                if(Pmass>nucleus.A()&&Pmass<=nucleus.A()+4){//if this parent nucleus can particle decay
                    decayENSDFsV.add(ens);
                }
            } 
                       
            decayENSDFsV=sortDecayENSDFs(decayENSDFsV);  
            
            parentHeight=0;
            for(int i=0;i<decayENSDFsV.size();i++){
            	parentV.clear();
            	ens=(ENSDF)decayENSDFsV.elementAt(i);
            	
            	//System.out.println("nuclide="+nucleus.nameENSDF()+" y pos="+temppos+" E="+(ens.parentAt(0).level().EF()+ens.parentAt(0).QF())+" parentSpace="+parentSpace+" H="+H+" parentHeight="+parentHeight);
            	
                labels.clear();
                
                /////////////////////////////////////////////////
                //set Y-position of the level in PDecay parent
                /////////////////////////////////////////////////
                if(nParent>0) {
                	temppos-=oneParentHeight*ens.nParents();
                	parentHeight+=oneParentHeight*ens.nParents();
                }else
                	parentHeight=parentSpace;//parentSpace=1.5 by default
                
                
                enskel=new EnsdfSkeleton(temppos);
                for(int j=0;j<ens.nParents();j++){
                    parentV.add(ens.parentAt(j));
                }
                enskel.setValues(parentV);
                nParent++;
                parentSkels.add(enskel);
            }         
            
        }

    };
    
    //private class SkeletonPoint extends Point{
    private class SkeletonPoint{    	
    	//(x,y) of Point is integer coordinate
    	//(x,y) in skeleton is float values in units of cm
    	     
    	float x=-1,y=-1;
    	
        public SkeletonPoint(){
        }
        
    	public SkeletonPoint(float ix,float iy){
    		x=ix;y=iy;
    	}
    	
    	public void setPoint(float ix,float iy){
    		x=ix;y=iy;
    	}
    	
    	@SuppressWarnings("unused")
		public void setPoint(SkeletonPoint p){
    		setPoint(p.x(),p.y());
    	}
    	
    	public void setXY(float ix,float iy){
    		x=ix;y=iy;
    	}
    	
    	public void setX(float ix){
    		x=ix;
    	}
    	
    	public void setY(float iy){
    		y=iy;
    	}
    	
    	@SuppressWarnings("unused")
		public void addToX(float dx){
    		x+=dx;
    	}
    	
    	@SuppressWarnings("unused")
		public void addToY(float dy){
    		y+=dy;
    	}
    	public float x(){return x;}
    	public float y(){return y;}
    }
    
    
    //NOTE: the coordinate system of (x,y) in drawings has (0,0) at bottom left 
    //      while the Polygon coordinate system has (0,0) at upper left.
    //      This doesn't matter for all functions here but attention must be paid.
    
    private class SkeletonShape {
    	
    	   private Vector<SkeletonPoint> points=new Vector<SkeletonPoint>();
    	   private Polygon polygon=new Polygon();
    	   private float scale2int=0.1f;	
    	   
    	   public SkeletonShape(){}
    	   
    	   @SuppressWarnings("unused")
    	   public SkeletonShape(Vector<SkeletonPoint> points){
    		   this.points.addAll(points);
    		   int n=points.size();
    		   
    		   if(n>0){
        		   int xint,yint;
        		   
        		   for(int i=0;i<n;i++){
        			   xint=(int)(points.elementAt(i).x()/scale2int);
        			   yint=(int)(points.elementAt(i).y()/scale2int);
        			   polygon.addPoint(xint, yint);
        		   }       		   	   
    		   }

    	   }
    	   
    	   public SkeletonShape(float x,float y,float w,float h){
    		   addPoint(new SkeletonPoint(x,y));
    		   addPoint(new SkeletonPoint(x+w,y));
    		   addPoint(new SkeletonPoint(x,y+h));
    		   addPoint(new SkeletonPoint(x+w,y+h));
    		   
    	   }
    	   
    	   @SuppressWarnings("unused")
		   public float getScale2int(){return scale2int;}
    	   @SuppressWarnings("unused")
		   public Polygon getPolygon(){return polygon;}
    	   
    	   public void addPoint(SkeletonPoint p){
    		   points.addElement(p);
    		   
    		   int xint,yint;
			   xint=(int)(p.x()/scale2int);
			   yint=(int)(p.y()/scale2int);
			   polygon.addPoint(xint, yint);
    	   }
    	   
    	   @SuppressWarnings("unused")
    	   public void addPoint(float x,float y){
    		   addPoint(new SkeletonPoint(x,y));
    	   }

    	   public int nPoints(){
    		   return points.size();
    	   }
    	   
    	   public Vector<SkeletonPoint> getPoints(){
    		   return points;
    	   }
    	   
    	   public SkeletonPoint pointAt(int i){
    		   return points.elementAt(i);
    	   }
    	   
    	   public boolean contains(float x,float y){
    		    int xint=(int)(x/scale2int);
    		    int yint=(int)(y/scale2int);
    		    return polygon.contains(xint,yint);
    	   }
    	   
    	   public boolean contains(SkeletonPoint p){
   		       return contains(p.x(),p.y());
   	   
    	   }
    	   
    	   //(x,y) is the coordinate of lower-left point of the rectangle with (0,0) at page lower-left
    	   //while in Polygon coordinate system, (0,0) at page upper-left. This difference doesn't matter
    	   //in this function, as long as the (x,y) is for the point cloest to (0,0)
    	   @SuppressWarnings("unused")
    	   public boolean contains(float x,float y,float width,float height){	    
    		   double xd=(double)(x/scale2int);
   		       double yd=(double)(y/scale2int);
    		   double wd=(double)(width/scale2int);
   		       double hd=(double)(height/scale2int);
   		       
   		       return polygon.contains(xd,yd,wd,hd);   		   
    	   }
    	   
    	   public boolean contains(SkeletonShape other){
    		   for(int i=0;i<other.nPoints();i++)
    			   if(!contains(other.pointAt(i)))
    				   return false;
    		   
    		   return true;
    	   }
    	   
    	   public boolean overlaps(float x,float y,float width,float height){	    
    		   double xd=(double)(x/scale2int);
   		       double yd=(double)(y/scale2int);
    		   double wd=(double)(width/scale2int);
   		       double hd=(double)(height/scale2int);
   		       
   		       //debug
   		       //System.out.println("In overlaps: table x,y="+x+" "+y+" w,h="+width+" "+height);
   		       //for(int i=0;i<points.size();i++){
   		       //    System.out.println("             shape: x,y="+points.get(i).x+" "+points.get(i).y);
   		       //    System.out.println("           polygon.intersects(xd,yd,width,height)="+polygon.intersects(xd,yd,wd,hd));
   		       //}
   		       
   		       return polygon.intersects(xd,yd,wd,hd);   		   
    	   }
    	   @SuppressWarnings("unused")
    	   public boolean overlaps(SkeletonShape other){
    		   for(int i=0;i<points.size();i++)
    			   if(other.contains(points.elementAt(i)))
    				   return true;
    		   
    		   for(int i=0;i<other.getPoints().size();i++)
    			   if(this.contains(other.pointAt(i)))
    				   return true;
    		   
    		   return false;
    	   }
    	   @SuppressWarnings("unused")
    	   public boolean isEmpty(){
    		   return !(points.size()>0);
    	   }
    	}
}
