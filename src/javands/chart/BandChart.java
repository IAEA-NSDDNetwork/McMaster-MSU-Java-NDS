
package javands.chart;

import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
import javands.chart.BandLevelLayout.LevelWrap;

/**
 * The BandChart object can draw a type of chart with several 
 * different groupings.
 *
 * @author Roy Zywina
 */
@SuppressWarnings("unused")
public class BandChart extends BaseChart{

    protected Vector<Band> bands;
    protected Vector<String> bandLabels;
    
    protected float[] bandTitleHeights;
    
    // data object
    protected ENSDF ens;
    
    protected EnsdfTableData etd;
    // space between bands
    float BAND_GAP = ensdfparser.nds.config.NDSConfig.bandGap;
    
    float BAND_GAP_RATIO = 0.25f;
    boolean MID_GAMMA_LABEL=true;
    
    
    float LEFTMAR=BaseChart.LEFT_PAD;
    float RIGHTMAR=BaseChart.RIGHT_PAD;
    
    /* Used when I want to have a single layout for multiple charts */
    BandLevelLayout defaultLevelLayout=null;
    public void setLevelLayout(BandLevelLayout l){
        defaultLevelLayout=l;
    }
    /**
     * Enable/disable mid gamma labelling.
     * Enabled by default.
     */
    public void midGammaLabel(boolean enable){
        MID_GAMMA_LABEL=enable;
    }

    /** Creates a new instance of BandChart */
    public BandChart(ENSDF e) {
        ens = e;
        bands = new Vector<Band>();
        bandLabels = new Vector<String>();
    }
    
    /** Creates a new instance of BandChart */
    public BandChart(EnsdfTableData etd) {
    	this.etd=etd;
        ens = etd.getENSDF();
        bands = new Vector<Band>();
        bandLabels = new Vector<String>();
    }
    
    /**
     * Remove data but keep settings.  Used for several charts in a 
     * group.
     */
    public void clear(){
        bands.clear();
        bandLabels.clear();
    }
    

    public String findBandTitle(Band b){
        String s = Str.firstSentance(b.comment().getTranslated());
        //s=Str.firstSentance2(s);
        //before ANYTHING, 
        
        //for band label
        //By default, the first sentence of the band comment (before the first ".")
        //is used as band label in band drawing. But if the second line is marked by
        //"x" instead of "2" at column 6, only the first line of the comment in the
        //ENSDF file is used as the label.
        if(b.comment().lines().size()>1){
            String secondLine=b.comment().lineAt(1);
            if(secondLine.toUpperCase().charAt(5)=='X'){
            	Comment c=new Comment();
            	Vector<String> lines=new Vector<String>();
            	lines.add(b.comment().lineAt(0));
            	try {
					c.setValues(lines);
	            	s=Str.firstSentance(c.getTranslated());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }
        }
        
        return s;
    }
    /**
     * Add a band, supply own label.
     */
    public void addBand(Band b,String label){
        bands.add(b);
        bandLabels.add(label);
    }
    /**
     * Add a band, use it's (potentially huge) label in the chart.
     */
    public void addBand(Band b){
        String s=findBandTitle(b);
        
        addBand(b, s);
    }
    /**
     * Add a group of levels that are not in a Band object.
     */
    public void addGroup(int[] lev,String label){
        Band b = new Band();
        for (int x=0; x<lev.length; x++)
            b.addLevel(ens.levelAt(lev[x]));
        bands.add(b);
        bandLabels.add(label);
    }
    protected float MaxE=0f,MinE=0f;
    
    public int getNumberOfBands()
    {
    	if(bands==null)
    		return 0;
    	
    	return bands.size();
    }
    
    protected void getConstraints(){
        float maxe = -1e6f;
        float mine = 1e6f;
        for (int x=0; x<bands.size(); x++){
            Band b = (Band)bands.get(x);
            for (int y=0; y<b.nLevels(); y++){
                float e = b.levelAt(y).EF();
                if (e>maxe) maxe=e;
                if (e<mine) mine=e;
                
            }
        }
        MaxE = maxe;
        MinE = mine;
    }
    /**
     * Most of the work for this object is in this function.  Writes
     * MetaPost output to the writer object supplied.
     * @throws Exception 
     */
    public void write(java.io.Writer out,int offset) throws Exception{
        int x,y,z;
        //float ymul;
        //float step = width / group.size();
        //float bstep= step * (1.0f-BAND_GAP_RATIO);
        //if (group.size()>1)
        //    step = step - (width/(group.size()-1)) * BAND_GAP_RATIO;
        //float gpad = step / 4.0f;
        //getConstraints();
        //float yoff = MinE;
        //float ymul = height/(MaxE-MinE);
        
        //float gstep = 0.3f;
        //float gstep = step / 15.0f;
        //if (gstep<0.2f) gstep=0.2f;
        
        // labels are stored in memory so they can be added at the end
        // this ensures they aren't overwritten by gamma lines
        Vector<String> labels = new Vector<String>();
        
        // highest observed level
        //float levmax = 0.0f;
        
        if(bands.size()==0){
           throw new java.io.IOException("No band to be drawn!"); 	
        }
        
        BandLevelLayout allLevelsLayout = new BandLevelLayout(ens);
        BandGammaLayout[] gammaLayouts = new BandGammaLayout[bands.size()];
        LevelLabelLayout[] fanLayouts = new LevelLabelLayout[bands.size()];
   
        Band[] bandCopies = new Band[bands.size()];
        bands.copyInto(bandCopies);
        
        //debug
        //System.out.println("############# In BandChart line 193:\n\n");
        
        BandLayout bandLayout = new BandLayout(width,BAND_GAP,offset);
                
        for (x=0; x<bands.size(); x++){
            Band b = (Band)bands.get(x);
            if (defaultLevelLayout==null)
                gammaLayouts[x] = new BandGammaLayout(ens,bandCopies);
            else
                gammaLayouts[x] = new BandGammaLayout(ens);
            
            for (y=0; y<b.nLevels(); y++){
                Level lev = b.levelAt(y);
                
                allLevelsLayout.addLevel(lev,x);
                
                for (z=0; z<lev.nGammas(); z++)
                    gammaLayouts[x].addGamma(lev.gammaAt(z),lev,b);
            }
            
            //debug
            //System.out.println("@@@In BandChart line 200: band "+x+" calc1 beginning");
            
            gammaLayouts[x].calc(100f,GAP_BETWEEN_GAMMAS_IN_BAND,true);
            
            bandLayout.addBand(b,gammaLayouts[x].ncolumns());
        }
       
        
        bandLayout.calc();
        
        // recalculate gamma layouts with better band layout information
        for (x=0; x<bands.size(); x++){
            //debug
            //System.out.println("%%%In BandChart line 200: band "+x+" calc2 beginning");
        	
            gammaLayouts[x].calc(bandLayout.getRight(x)-bandLayout.getLeft(x)
                    ,GAP_BETWEEN_GAMMAS_IN_BAND,true);
        }
        
        
        //calculate layout of all levels from all bands in the current page
        //Note that height doesn't include the height of band title
        allLevelsLayout.calc(height,GAP_BETWEEN_LEVELS);
              
        if (defaultLevelLayout!=null){
            // overwrite layout object with user supplied one
            allLevelsLayout=defaultLevelLayout;
        }
        
        //determine label y position
        for (x=0; x<bands.size(); x++){
            Band b = (Band)bands.get(x);
            fanLayouts[x] = new LevelLabelLayout();
            for (y=0; y<b.nLevels(); y++){
                Level lev = b.levelAt(y);
                float pos = allLevelsLayout.getPos(lev);
                fanLayouts[x].addLevel(lev,pos);
                
                //debug
                //System.out.println("In BandChart line 236: level="+lev.EF()+" pos="+pos);
            }
            fanLayouts[x].calc(height,FONT_SIZE);
        }
        //levmax = layout.getMax();
        
        // draw levels
        
        boolean drawInterBandG=true;//for drawing interband transitions between neighbouring bands
        //boolean drawInterBandG=Control.showInterBand;
        
        boolean moveFirstGToCenter=true;//for moving the first (lowest, primary) transition from each level to the center
        
        //debug
        //System.out.println("In BandChart: drawIntraBand="+drawInterBandG);
        
        float leftmar=LEFTMAR;
        float rightmar=RIGHTMAR;
        
        for (x=0; x<bands.size(); x++){
            BandGammaLabelLayout labelLayout = new BandGammaLabelLayout(FONT_SIZE);
            TagLayout tagLayout = new TagLayout(FONT_SIZE);
            
            Band b = (Band)bands.get(x);
            Band nextband=null,lastband=null;
            if (x>0) lastband = (Band)bands.get(x-1);
            if (x<bands.size()-1) nextband = (Band)bands.get(x+1);
          
            //float goff = gpad;
            float xposleft = bandLayout.getLeft(x);//p*x;
            float xposright= bandLayout.getRight(x);//step*x + bstep;
            float prevxposright = 0;//step*(x-1) + bstep;
           
            if (x>0) prevxposright = bandLayout.getRight(x-1);
            
            float nextxposleft  = 0;//step*(x+1);
            if (x<bands.size()-1) nextxposleft = bandLayout.getLeft(x+1);
           
            // separation between gammas
            float gSep = bandLayout.getGap(x);
            if (gSep < GAP_BETWEEN_GAMMAS_IN_BAND) gSep = GAP_BETWEEN_GAMMAS_IN_BAND;
          
            int gcolOffset=0;
            int ngammaColumns=gammaLayouts[x].ncolumns();//total columns of gamma lines (in-band) within a band
            int indexOfPrimaryColumn=gammaLayouts[x].indexOfPrimaryColumn();//first, get the longest (or deltaJ=1 for most cases) column
            
            //set offset for shifting gamma columns
            //Note for moveFirstGToCenter:
            //Originally, the longest gamma column (primary) is assigned as the first column since the placement were made
            //based on the availability of the empty columns. Now it is also based on the deltaJ of the gamma columns and
            //the first column could not be the longest one. Also note that gamma columns are filled to top to bottom levels,
            //and from low to high energy from the same level 
            

            
            if(moveFirstGToCenter){     
            	//gcolOffset=(ngammaColumns-1)/2;
            	gcolOffset=(ngammaColumns-1)/2-indexOfPrimaryColumn;
            
            }
            
            //debug
            //System.out.println("In BandChart Line 295: band#="+x+" indexOfPrimaryColumn="+indexOfPrimaryColumn+" offset="+gcolOffset);
            
            // number of gamma columns that are visible
            int visCol = bandLayout.getVisable(x);
            
            leftmar=LEFTMAR;
            rightmar=RIGHTMAR;
            if(ngammaColumns>1){
                float[] margins=recalculateMargins(b.levels(),LEFTMAR,RIGHTMAR);
                leftmar=margins[0];
                rightmar=margins[1];
            }

            
            //for (y=b.nLevels()-1; y>=0; y--){
            for (y=0; y<b.nLevels(); y++){
                // are there gammas that will not be shown
                boolean invisgammas = false;
                //goff = gstep*2.0f;
                
                Level lev = b.levelAt(y);
                
                out.write("% Level "+lev.ES()+"\n");
                float e = EnsdfUtil.e(lev);
                //if (e>levmax) levmax = e;
                
                //float ypos = (e-MinE)*ymul;
                float ypos = allLevelsLayout.getPos(lev);
                //        lev.ES(),new Float(ypos));
                float fpos = fanLayouts[x].getPosition(lev);
                
                //debug
                //System.out.println("In BandChart line 331: level="+lev.EF()+" pos="+ypos+" fpos="+fpos);
                
                String com="";
                
                if (fpos==ypos){
                    // no fanning needed
                    com = "draw "+Str.point(xposleft,ypos)+"--"
                            + Str.point(xposright,ypos)+
                            " withcolor levcol";
                }else{
                    // fanning required
                    com = "draw "+Str.point(xposleft,fpos)+"--"
                            + Str.point(xposleft+leftmar-0.2f,fpos)+"--"
                            + Str.point(xposleft+leftmar-0.1f,ypos)+"--"
                            + Str.point(xposright-rightmar+0.1f,ypos)+"--"
                            + Str.point(xposright-rightmar+0.2f,fpos)+"--"
                            + Str.point(xposright,fpos)
                            + " withcolor levcol";
                }
                // check for question mark flag
                if ("?".equals(lev.q())){
                    com += " dashed levdash";
                }
                // check for level zero
                if (e==0.0f)
                    com += " withpen pencircle scaled 0.03cm";
                out.write(com+";\n");
                //com = "label.ulft(btex "+levelLabelSize()+"\\bf "+lev.ES().toLowerCase()+" etex,"+Str.point(xposright,fpos)+");\n";
                com = "label.ulft(btex "+levelLabelSize()+"\\bf "+Translator.plainValue(lev.ES(),lev.DES())+" etex,"+Str.point(xposright,fpos)+");\n";
                
                //out.write(com);
                labels.add(com);
                labelLayout.addFixedLabel(xposright,fpos,ngammaColumns);
                tagLayout.addFixedTag(fpos);
                
                String spin = lev.JPiS();
                if (spin.trim().length()>0){
                	spin=Translator.spin(spin.trim());
                    com = "label.urt(btex "+levelLabelSize()+"\\bf "+spin+" etex,"+Str.point(xposleft-0.1f,fpos)+");\n";//minus 0.1f to account for the size of the boundary box of the point 
                    //out.write(com);
                    labels.add(com);
                    labelLayout.addFixedLabel(xposleft, fpos, -1);
                }
                
                // draw gammas
                for (z=0; z<lev.nGammas(); z++){
                	
                    Gamma g = lev.gammaAt(z);
                    
                    out.write("% Gamma "+g.ES()+"\n");
                    float ge = EnsdfUtil.e(g);
                    float le = e-ge;
                    Level lev2;
                    
                    lev2 = EnsdfUtil.finalLevel(ens,g);
                    if (lev2==null) 
                    	continue;
                    le = EnsdfUtil.e(lev2); // use a more accurate number
                    
                    float ypos2 = allLevelsLayout.getPos(lev2);
                    if (ypos2<0){
                        // level not in chart
                        invisgammas = true;
                        continue;
                    }
                    // get column number that gamma lays in
                    int gcol = gammaLayouts[x].gammaNColumn(g);
                    // check if this gamma can be drawn within boundaries
                    out.write("% gcol: "+gcol+" visCol: "+visCol+"\n");
                    
                    //debug
                    //System.out.println("In bandChart line 418: g="+g.ES()+" gcol="+gcol+" visCol="+visCol);
                    
                    if (gcol >= visCol){
                        invisgammas = true;
                        continue;
                    }
                    // in this/left/right band?
                    boolean inb=false,inlb=false,inrb=false;
                    inb = EnsdfUtil.contains(b,lev2);
                    
                    if (!inb && nextband!=null)
                        inrb = EnsdfUtil.contains(nextband,lev2);
                    
                    if (!inb && !inrb && lastband!=null)
                        inlb = EnsdfUtil.contains(lastband,lev2);
                    
                    if (inb || (!inlb && !inrb)){
                    	
                    	if(!inb && !drawInterBandG){
                    		invisgammas=true;
                    		continue;
                    	}
                    	
                        //float xpos = glayout[x].getPos(g);
                        float xpos;
                        if (gcol<0){
                            //throw new RuntimeException("Inconsitency");
                            invisgammas = true;
                            continue;
                        }
                        
                        
                        //shift gamma columns
                        int col=gcol+gcolOffset;
                        if(col>=ngammaColumns)
                        	col=col-ngammaColumns;
                        if(col<0)
                        	col=col+ngammaColumns;
                        
                        xpos = xposleft + leftmar + col*gSep+0.1f;
                        if (visCol<=1){
                            // if theres only one column, center it
                            xpos = xposleft + leftmar + 
                                    (xposright-xposleft-leftmar-rightmar)*0.5f;
                        }
                        
                        //debug
                        //System.out.println("In BandChart line 410: inb="+inb+" xpos="+xpos+"Le="+lev.EF()+" Eg="+ge+" ngammaColumns="+ngammaColumns+" gcol="+gcol+" col="+col+" inband="+inb);
                        
                        // draw normal arrow
                        com = "drawarrow "+
                            Str.point(xpos,ypos)+
                            "--"+Str.point(xpos,ypos2);
                        // check for uncertainty
                        if ("?".equals(g.q())){
                            com += " dashed gamdash";
                        }
                        // color appropriately
                        if (inb)
                            com += " withcolor gamcol;\n";
                        else
                            com += " withcolor outcol;\n";
                        out.write(com);
                        
                        
                        //for transitions to other band other than left and right bands
                        //right now, this part of code will not be reached for default setting that inter-band transitions (not to left or right) is not drawn
                        if (!inb){
                            //debug
                            //System.out.println("In BandChart !inb: xpos="+xpos+"Le="+lev.EF()+" Eg="+ge+" ngammaColumns="+ngammaColumns+" gcol="+gcol+" col="+col);

                        	labelLayout.addFixedLabel(ypos2);
                            // little landing pad for out of band arrows
                            com = "draw "+Str.point(xpos-GAP_BETWEEN_GAMMAS_IN_BAND*0.45f,ypos2)+"--"+
                                Str.point(xpos+GAP_BETWEEN_GAMMAS_IN_BAND*0.45f,ypos2) +
                                " withcolor levcol";
                            if (lev2.isFinalLevel())
                                com += " withpen pencircle scaled 0.03cm";
                            out.write(com+";\n");
                            
                            // draw band label tag
                            for (int bn=0; bn<ens.nBands(); bn++){
                                if (EnsdfUtil.contains(ens.bandAt(bn),lev2)){
                                    String tag = ens.bandAt(bn).comment().flagAt(0,0);
                                    tag = "label.urt(btex "+bandLabelSize()+"\\it \\bf "+tag+" etex,"+
                                            Str.point(xposleft,ypos2)+");\n";
                                    tag += "draw "+Str.point(xposleft,ypos2)+"--"+
                                            Str.point(xposleft+leftmar/2.5f,ypos2)+";\n";
                                    tagLayout.addTag(ypos2,tag);
                                    break;
                                }
                            }
                        }
                        //goff += gstep;
                        
                        
                        // write gamma label
                        //MID_GAMMA_LABEL=false;
                        if (MID_GAMMA_LABEL){ // add option to disable this
                            float ypos3 = (ypos2+ypos)*0.5f;
                            float eg = g.EF();
                            String egs = Str.roundToStr(eg);
                            //debug
                            
                            //if(g.ES().endsWith(".5"))
                            //System.out.println("In BandChart A: Eg="+eg+" egs="+egs+" el="+g.ILS());
                            
                          
                            labelLayout.addLabel("btex "+gammaLabelSize()+"\\bf "+egs+" etex",col,
                                    xpos,ypos3,ypos2,ypos);
                            //debug
                            //System.out.println("In BandChart.java line 416: ypos ypos2 ypos3="+ypos+" "+ypos2+" "+ypos3+" label="+egs);
                        }
                    }                    
                    else if (inlb){ // in left band
                    	
                    	if(!drawInterBandG){
                    		invisgammas=true;
                    		continue;
                    	}
                    		
                        float fpos2 = fanLayouts[x-1].getPosition(lev2);
                        if (fpos2<0)
                            fpos2=ypos2;
                        com = "drawarrow "+
                            Str.point(xposleft,fpos) + "--" +
                            Str.point(prevxposright,fpos2);
                        // check for uncertainty
                        if ("?".equals(g.q())){
                            com += " dashed gamdash";
                        }
                        com += " withcolor outcol;\n";
                        out.write(com);
                    }else if (inrb && drawInterBandG){ // in right band
                    	
                    	if(!drawInterBandG){
                    		invisgammas=true;
                    		continue;
                    	}
                    	
                        float fpos2 = fanLayouts[x+1].getPosition(lev2);
                        if (fpos2<0)
                            fpos2=ypos2;
                        com = "drawarrow "+
                            Str.point(xposright,fpos) + "--" +
                            Str.point(nextxposleft,fpos2);
                        // check for uncertainty
                        if ("?".equals(g.q())){
                            com += " dashed gamdash";
                        }
                        com += " withcolor outcol;\n";
                        out.write(com);
                    }else{ // shouldnt ever get here
                        // can't draw this gamma
                        invisgammas = true;
                    }
                    
                } // end for z in gammas
                
                // put a little marker to indicate that there are gammas 
                // not shown in this level
                if (invisgammas){
                    out.write("% Undrawn gammas on this level.\n");
                    com = "fill "+
                        Str.point(xposright-rightmar*0.5f-0.05f,fpos)+"--"+
                        Str.point(xposright-rightmar*0.5f+0.05f,fpos)+"--"+
                        Str.point(xposright-rightmar*0.5f,fpos-0.07f)+
                        "--cycle;\n";
                    out.write(com);
                }
            }// end for y in labels
            
            // calculate how this bands mid gamma labels should
            // be positioned
            
            labelLayout.setShowGammaLabels(etd.getBandDrawingControl().supGammaLabels(), etd.getBandDrawingControl().showAllGammaLabels());
            
            //debug
            //System.out.println("In BandChart: supGammaLables="+etd.getBandDrawingControl().supGammaLabels()+" show="+etd.getBandDrawingControl().showAllGammaLabels());
            
            
            //debug
            //System.out.println("In BandChart.java1 line 487: label size="+labelLayout.sizeOfLabels());
            
            labelLayout.calc();
            for (int la = 0; la<labelLayout.sizeOfLabels(); la++){
                String llab = labelLayout.getLabelAt(la);
                float lxpos = labelLayout.getXPosAt(la);
                float lypos = labelLayout.getYPosAt(la);
                
                //if(labelLayout.isVerticalAt(la))
                //	labels.add(colorbox(lypos,-lxpos,"red",90)+"label("+llab+" rotated 90,"+Str.point(lxpos,lypos)+");\n");
                //else
                	labels.add(colorbox(lxpos,lypos,"white")+"label("+llab+","+Str.point(lxpos,lypos)+");\n");
              //debug
              //  System.out.println("In BandChart.java line 493: lxpos lypos="+lxpos+" "+lypos+" label="+llab+" vertical="+labelLayout.isVerticalAt(la));
            }
            
            //debug
            //System.out.println("In BandChart.java2 line 487: label size="+labelLayout.sizeOfLabels());
            
            // calculate tags
            tagLayout.calc();
            for (int tn=0; tn<tagLayout.sizeOfTags(); tn++){
                String tag = tagLayout.getTagAt(tn);
                if (tag==null) continue;
                out.write(tag);
            }
        }// end for x in groups
        
        
        /*
         * Labels
         */
        // write group labels
        
        float labelYMax=0;
        for (x=0; x<bands.size(); x++){
            Band b = (Band)bands.get(x);
            String label="";
            String head=b.comment().headAt(0);
            if(head.equals("BAND"))
            	label="Band";
            else if(head.equals("SEQ"))
            	label="Seq.";
            
            String tag = b.comment().symbol();
            //tag = b.comment().type();
            tag = b.comment().flagAt(0,0);
            
            // add the band symbol as subscript
            if (tag.length()>0){
                // funny char kludge
                char c = tag.charAt(0);
                if (Character.isLetter(c))
                    label += "("+tag+"): ";
            }
            
            label += (String)bandLabels.get(x);
            if(label.charAt(label.length()-1)=='.'){
                label=label.substring(0,label.length()-1);
            }
            
            float xposleft = bandLayout.getLeft(x);
            float xposright= bandLayout.getRight(x);
            //float xposleft = step*x;
            //float xposright= step*x + bstep;
            
            
            float fpos=fanLayouts[x].getPosition(b.levelAt(b.nLevels()-1))+0.5f;          
            float ypos = allLevelsLayout.getPos(b.levelAt(b.nLevels()-1))+0.5f;
            float cent = (xposleft+xposright)*0.5f;

        
            if(fpos>ypos)
            	ypos=fpos;
            
            if (label.length()>0){
                //to make band labels not so long that they overlap
                int commentspace=(int)NDSConfig.bandWidths[x+offset]*10;
                //puts everything but the last band label in lower case
                label=label.substring(0,3)+label.substring(3);
                
                //debug
                //System.out.println("In BandChart Line 651: band#="+x+" label="+label);

                //for band label (title)
                String [] labelArray=Str.bandSplit(label,commentspace);
                for(int i=0;i<labelArray.length;i++){
                    String com = "label(btex "+bandLabelSize()+"\\bf "+labelArray[i]+" etex,"+Str.point(cent,ypos+0.3f*(labelArray.length-i))+");\n";
                    out.write(com);
                    //debug
                    //System.out.println("In BandChart : band label split"+i+"="+labelArray[i]);
                }
                
                if(ypos+0.3f*labelArray.length>labelYMax)
                	labelYMax=ypos+0.3f*labelArray.length;
            }
          
            //String com="label(btex \\labelfont \\bf Band "+(x+1)+" etex,"+Str.point(cent,ypos)+");\n";
            //out.write(com);
        }
        
        
        // stored labels: level labels like spin and energy, gamma labels like gamma energy
        for (x=0; x<labels.size(); x++){
            out.write((String)labels.get(x));
            //debug
            //System.out.println("In BandChart B: label="+labels.get(x));
        }
        
        float xposleft=bandLayout.getLeft(0);
        float xposright=bandLayout.getRight(bands.size()-1);
        float center=(xposleft+xposright)/2.0f;
        
        //if(!nds.config.Config.portrait)
        out.write("label(btex \\ensuremath{^{"+ens.nucleus().A()+"}_{"+ens.nucleus().ZS()+"}}"+ens.nucleus().En()+"\\ensuremath{_{"+ens.nucleus().N()+"}^{~}} etex, ("+center+"cm,-1.2cm));\n");

        // title of dataset
        
        float ypos=findMaxYPos(allLevelsLayout);
        float xpos=center;
        
        if(ypos<labelYMax)
        	ypos=labelYMax;
        
        ypos+=1.0f;//in cm
        
        String TopTitle="";
        String tempID="";
        TopTitle+="{\\bf \\small \\underline{";
        
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
        if(offset>0)TopTitle+=" (continued)";
        TopTitle+="}}";
        
        out.write("label(btex "+TopTitle+" etex, ("+(xpos)+"cm,"+(ypos)+"cm));\n");
    }
    
    public float findBandTitleHeight(Band b,String title,int commentspace){
    	
    	float titleHeight=0;
    	
        String label="";
        String head=b.comment().headAt(0);
        if(head.equals("BAND"))
        	label="Band";
        else if(head.equals("SEQ"))
        	label="Seq.";
        
        String tag = b.comment().symbol();
        //tag = b.comment().type();
        tag = b.comment().flagAt(0,0);
        
        // add the band symbol as subscript
        if (tag.length()>0){
            // funny char kludge
            char c = tag.charAt(0);
            if (Character.isLetter(c))
                label += "("+tag+"): ";
        }
        
        label += title;
        if(label.charAt(label.length()-1)=='.'){
            label=label.substring(0,label.length()-1);
        }
        
        if (label.length()>0){
            label=label.substring(0,3)+label.substring(3);
            String [] labelArray=Str.bandSplit(label,commentspace);
            titleHeight=0.3f*labelArray.length;    
        }
        
        return titleHeight;
	
    }
    
    //including title height
    public float findMaxBandHeight(BandLevelLayout levelLayout,Band[] bands){
    	float maxY=findMaxBandTitleYPos(levelLayout,bands);
    	float minY=findMinYPos(levelLayout);
    	return (maxY-minY);
    }
    
    public float findMaxBandHeight(Band[] bands){
    	BandLevelLayout levelLayout=findBandLevelLayout(bands);
    	return findMaxBandHeight(levelLayout,bands);
    }
    
    public float findMaxBandTitleYPos(BandLevelLayout levelLayout,Band[] bands){
    	float maxYPos=-1;
    	float YPos=-1;
    	Vector<Band> allBands=new Vector<Band>();
    	for(int i=0;i<NDSConfig.bands.length;i++)
    		allBands.add(NDSConfig.bands[i]);
    	
    	for(int i=0;i<bands.length;i++){
    		Band b=bands[i];
    		int index=allBands.indexOf(b);
    		int commentspace=(int)NDSConfig.bandWidths[index]*10;
    		String title=findBandTitle(b);
    		float titleHeight=findBandTitleHeight(b,title,commentspace);
    		
            //debug
            //System.out.println("In BandChart line 801: nlevels="+b.nLevels()+" i="+i+" titleHeight="+titleHeight+"  Ypos="+YPos);
    		
    		for(int j=0;j<b.nLevels();j++){
    			YPos=levelLayout.getPos(b.levelAt(j))+titleHeight;
                //debug
                //System.out.println("In BandChart line 803: i="+i+" titleHeight="+titleHeight+"  Ypos="+YPos);
    			
    			if(YPos>maxYPos)
    				maxYPos=YPos;
    		}
    			
    	}
    	
    	return maxYPos;
    	
    }
    
    public float findMaxYPos(BandLevelLayout levelLayout,Band[] bands){
    	float maxYPos=-1;
    	float YPos=-1;
    	for(int i=0;i<bands.length;i++){
    		Band b=bands[i];
    		for(int j=0;j<b.nLevels();j++){
    			YPos=levelLayout.getPos(b.levelAt(j));
    			if(YPos>maxYPos)
    				maxYPos=YPos;
    		}
    			
    	}
    	
    	return maxYPos;
    	
    }
  
    public float findMaxYPos(BandLevelLayout levelLayout){
    	float maxYPos=-1;
    	float YPos=-1;
    	for(int i=0;i<levelLayout.levelWraps.size();i++){
			YPos=levelLayout.levelWraps.get(i).pos;
			if(YPos>maxYPos)
				maxYPos=YPos;
    			
    	}
    	
    	return maxYPos;
    }
    
    public float findMaxYPos(Band[] bands){
    	BandLevelLayout levelLayout=findBandLevelLayout(bands);
    	return findMaxYPos(levelLayout);
    }
    
    public float findMinYPos(BandLevelLayout levelLayout,Band[] bands){
    	float minYPos=1.0E6f;
    	float YPos=-1;
    	for(int i=0;i<bands.length;i++){
    		Band b=bands[i];
    		for(int j=0;j<b.nLevels();j++){
    			YPos=levelLayout.getPos(b.levelAt(j));
    			if(YPos<minYPos)
    				minYPos=YPos;
    		}
    			
    	}
    	
    	return minYPos;
    	
    }
    
    public float findMinYPos(BandLevelLayout levelLayout){
    	float minYPos=1.0E6f;
    	float YPos=-1;
    	for(int i=0;i<levelLayout.levelWraps.size();i++){
			YPos=levelLayout.levelWraps.get(i).pos;
			if(YPos<minYPos)
				minYPos=YPos;
    			
    	}
    	
    	return minYPos;
    }
    
    public float findMinYPos(Band[] bands){
    	BandLevelLayout levelLayout=findBandLevelLayout(bands);
    	return findMinYPos(levelLayout);
    }
    
    /*Determine number of bands per page with given width*/
    public int[] findNBandsAPage(Band[] bands,float width){

        /*The following code finds the number of bands to put on each page by figuring out how large 
         * each band is, filling the first page with bands, then filling up the second page and so on
         */
        float usedTotalWidth=0;//space used
        int x;
        int pageNo=0;
             
        if(bands.length==0)
        	return new int[0];
        
        // worst case scenario is one band per page
        int[] nbandsInPage = new int[bands.length];
        nbandsInPage[0]=0;
        for (x=0; x<bands.length; x++){

            usedTotalWidth+=NDSConfig.bandWidths[x]+0.3f;           
            
            if (usedTotalWidth<width){
                nbandsInPage[pageNo]++;
            }
            else{
                pageNo++;                    
                nbandsInPage[pageNo]=1;
                usedTotalWidth=NDSConfig.bandWidths[x];
            }
        }
        
        int[] nbands=new int[pageNo+1];
        for(int i=0;i<=pageNo;i++)
        	nbands[i]=nbandsInPage[i];
        
        return nbands;
         
        
        /*this code assumes one band for every 3 units of width
         * figures out the number of pages, and then divides the bands up equally
         *
        int pages=(int)(3.0*bands.length/width);
        int x;
        
        groups = new int[pages];
        for (x=0; x<pages; x++)
            groups[x] = g[x];
        return groups;
         * */
    }
    
    /*find the number of gammas from each band in bands to its neighbor bands*/
    public int[][] findNGammasToNeighbor(Band[] bands){
    	int[][] nGammas=new int[2][bands.length];
    	for(int i=0;i<2;i++)//i=0 for left neighbor band, i=1 for right
    		for(int j=0;j<bands.length;j++)
    			nGammas[i][j]=0;
    	int x,y,z;
    	
        for (x=0; x<bands.length; x++){         
            Band b = (Band)bands[x];
            Band nextband=null,lastband=null;
            if (x>0) lastband = (Band)bands[x-1];
            if (x<bands.length-1) nextband = (Band)bands[x+1];
            
            for (y=0; y<b.nLevels(); y++){           
                Level lev = b.levelAt(y);

                for (z=0; z<lev.nGammas(); z++){            	
                    Gamma g = lev.gammaAt(z);
                    Level lev2;
                    
                    lev2 = EnsdfUtil.finalLevel(ens,g);
                    if (lev2==null) 
                    	continue;

                    // in this/left/right band?
                    boolean inb=false,inlb=false,inrb=false;
                    inb = EnsdfUtil.contains(b,lev2);    
                    if(inb)
                    	continue;
                    
                    if (nextband!=null)
                        inrb = EnsdfUtil.contains(nextband,lev2);
                    
                    if (!inrb && lastband!=null)
                        inlb = EnsdfUtil.contains(lastband,lev2);
                    
                    if(inlb)
                    	nGammas[0][x]++;
                    else if(inrb)
                    	nGammas[1][x]++;
                }
            }
        }
                        	
    	return nGammas;
    }
    
    /*Determine maximum number of consecutive small level gaps (<limit) in a band*/
    public int findNConsecutiveSmallGaps(BandLevelLayout layout,float limit){
    	int nmax=0;
    	int ngroup=layout.getNGroups();
    	for(int i=0;i<ngroup;i++){
    		Vector<LevelWrap> group=layout.getGroup(i);
    		int n=0;
    		for(int j=1;j<group.size();j++){
    			float pos1=group.get(j-1).pos;
    			float pos2=group.get(j).pos;

    			float gap=pos2-pos1;
    			if(gap<=limit){   					
    				n++;
    			}else{
    				if(n>nmax)
    					nmax=n;
    				n=0;
    			}
    		}
    	}
    	
    	return nmax;
    }
    
    /*Determine number of bands per page with given width*/
    public int[] findNBandsAPage(Band[] bands,float width,boolean tight)throws Exception{

        /*The following code finds the number of bands to put on each page by figuring out how large 
         * each band is, filling the first page with bands, then filling up the second page and so on
         */
        float usedTotalWidth=0;//space used
        int x;
        int pageNo=0;
             
        if(bands.length==0)
        	return new int[0];
        
        int[] nGammasToLeft=findNGammasToNeighbor(bands)[0];
        int[] nGammasToRight=findNGammasToNeighbor(bands)[1];
        
        //fill band drawings in the whole page even when some bands have very tight level scheme
        if(tight)
        	return findNBandsAPage(bands,width);
        
        // worst case scenario is one band per page
        int next=0;
        int[] nbandsInPage = new int[bands.length];
        float[] totalWidthInPage=new float[bands.length];
        
        Vector<Band> bandV=new Vector<Band>();
        nbandsInPage[0]=0;
        for (x=0; x<bands.length; x++){
            
        	if(NDSConfig.bandWidths[x]>=width){
        		String message="Error: width of band"+x+"="+NDSConfig.bandWidths[x]+" exceeds the total width="+width+". Too many transitions in band!";
        		message+="\n       Band scheme is not drawn.";
        		throw new Exception(message);
        	}
        	
            usedTotalWidth+=NDSConfig.bandWidths[x]+0.3f;
            

            //debug
            //System.out.println("In BandChart line 1055: calculate: x="+x+" band width="+Config.bandWidths[x]+" usedtot="+usedTotalWidth+" given total width="+width);
            //System.out.println("                               pageNo="+pageNo+" nbandsInPage="+nbandsInPage[pageNo]+" bandV.size="+bandV.size());
    
            
            if (usedTotalWidth<width){
                nbandsInPage[pageNo]++;
            	totalWidthInPage[pageNo]=usedTotalWidth;
                bandV.add(bands[x]);
                next=x+1;
            }else
            	next=x;
            
            
            if(usedTotalWidth>=width || x==bands.length-1){
            	float minLevelGap=0;
            	
            	int nRemoved=0;
            	float widthRemoved=0;
            	BandLevelLayout tempBandLevelLayout;
            	while(bandV.size()>0){
            		boolean toRemove=false;
                	Band[] tempBands=new Band[bandV.size()];
                	bandV.copyInto(tempBands);
                	
                	tempBandLevelLayout=findBandLevelLayout(tempBands);//find the layout using the default height=height, set by setHeight() from external call
                	float tempHeight=findMaxBandHeight(tempBandLevelLayout,tempBands);//total height of band levels+band title
                	float r=1.05f;//ratio of max height of (band levels+band title) over default height=height;
                	
                    //debug
                    //System.out.println("In BandChart line 1084: tempHeight="+tempHeight+"  height="+height);
                	
                	
                	//adjust the band height if the total heights of band level+band title exceeds the max allowed height
                	heights[pageNo]=height;//band height only
                	if(tempHeight>r*height){
                		tempHeight=(1+r)*height-tempHeight;
                		
                        //debug
                        //System.out.println("In BandChart line 1093: tempHeight="+tempHeight+"  height="+height);
                        
                		tempBandLevelLayout.calc(tempHeight,GAP_BETWEEN_LEVELS);
                		heights[pageNo]=tempHeight;//set the actual band height (not including band title) in current page
                	}
                	
                	
                	minLevelGap=tempBandLevelLayout.getMinGap();
                
                	//FONT_SIZE=0.29 cm, GAP_BETWEEN_LEVELS=0.12 cm
                	int nContinuousSmallGaps=findNConsecutiveSmallGaps(tempBandLevelLayout,FONT_SIZE*0.8f);
                	
                    //debug
                    //System.out.println("In BandChart line 1106: minLevelGap="+minLevelGap+" bandsize="+bandV.size()+" BaseChart.GAP_BETWEEN_LEVELS="+BaseChart.GAP_BETWEEN_LEVELS);
                    //System.out.println("  page#="+pageNo+" gammas to left="+nGammasToLeft[next-1]+" gammas to right="+nGammasToRight[next-1]+" nContinuousSmallGaps="+nContinuousSmallGaps);
                    
                    
                    if(minLevelGap<=BaseChart.GAP_BETWEEN_LEVELS)
                    	toRemove=true;
                    else if(bandV.size()==1 && minLevelGap<1.5*BaseChart.GAP_BETWEEN_LEVELS)//if no significant improvement for minLevGap when only one band is left, remove it
                    	toRemove=true;
                    else if(nGammasToLeft[next-1]<=1 && nGammasToRight[next-1]>=4 && nGammasToLeft[next]>=4)//if the last band in current page is closely coupled with the first band in next page, move it to next page to keep the coupled relationship
                    	toRemove=true;
                    else if(nContinuousSmallGaps>=4)
                    	toRemove=true;
                    	
                    
                    
                	if(toRemove){
                		bandV.remove(bandV.lastElement());
                		next--;
                		nRemoved++;
                		widthRemoved+=NDSConfig.bandWidths[next]+0.3f;
                		continue;
                	}else
                		break;
                		
            	}
                
            	
                //debug
                //System.out.println("In BandChart line 1134: calculate: x="+x+" band width="+Config.bandWidths[x]+" usedtot="+usedTotalWidth+" given total width="+width);
                //System.out.println("     pageNo="+pageNo+" nbandsInPage="+nbandsInPage[pageNo]+" bandV.size="+bandV.size()+" totalWidthInPage[pageNo]="+totalWidthInPage[pageNo]);
                //System.out.println("      nRemoved="+nRemoved+" widthRemoved="+widthRemoved+" next="+next+" minLevelGap="+minLevelGap+" BaseChart.GAP_BETWEEN_LEVELS="+BaseChart.GAP_BETWEEN_LEVELS);
                
            	//if level gap gets better after removing some bands at right, 
            	//then only keep the remaining bands in current page and move 
            	//those removed bands to next page. If all bands are removed, 
            	//return to the original layout, that is, keep all bands
            	if(bandV.size()>0){
            		
                    //debug
                    //System.out.println("In BandChart line 1145: minLevelGap="+minLevelGap+" bandsize="+bandV.size()+" next="+next);
                    
                	nbandsInPage[pageNo]=bandV.size();
                	totalWidthInPage[pageNo]-=widthRemoved;
                	
                	bandV.clear();     
                	x=next-1;
            	}else if(nRemoved>0){//all bands have been removed from a page, check if they can be squeezed into previous page
            		if(pageNo>0 && (widthRemoved+totalWidthInPage[pageNo-1])<width){
            			nbandsInPage[pageNo-1]+=nRemoved;
            			totalWidthInPage[pageNo-1]+=widthRemoved;
            			usedTotalWidth=0;
            			totalWidthInPage[pageNo]=0;
            			nbandsInPage[pageNo]=0;
            			x=next-1+nRemoved;
            			pageNo--;
            			continue;
            		}else{//keep the layout as is and continue                     			
            			x=next-1+nRemoved; 
            		}
            		
            		
            	}
          	
            	if(usedTotalWidth>=width || x<bands.length-1){
            		
                    pageNo++;
                    nbandsInPage[pageNo]=0;
                    totalWidthInPage[pageNo]=0;
                    usedTotalWidth=0;
                    
                    //if(usedTotalWidth>=width)
                    //    bandV.add(bands[x]);
                    
                    //nbandsInPage[pageNo]=1;
                    //usedTotalWidth=Config.bandWidths[x];
            	}


            }
        }

        
        int[] nbands=new int[pageNo+1];
        for(int i=0;i<=pageNo;i++)
        	nbands[i]=nbandsInPage[i];
        
        return nbands;
         
        
        /*this code assumes one band for every 3 units of width
         * figures out the number of pages, and then divides the bands up equally
         *
        int pages=(int)(3.0*bands.length/width);
        int x;
        
        groups = new int[pages];
        for (x=0; x<pages; x++)
            groups[x] = g[x];
        return groups;
         * */
    }
    
   public float findMinLevelGap(Band[] bands){
       BandLevelLayout levelsLayout = findBandLevelLayout(bands);    
       return levelsLayout.getMinGap();
   }
   
   public BandLevelLayout findBandLevelLayout(Band[] bands){
	   return findBandLevelLayout(bands,this.height);
   }
   
   public BandLevelLayout findBandLevelLayout(Band[] bands,float height){
       BandLevelLayout levelsLayout = new BandLevelLayout(ens);
       
       for (int x=0; x<bands.length; x++){
           Band b = (Band)bands[x];

           for (int y=0; y<b.nLevels(); y++){
               Level lev = b.levelAt(y);             
               levelsLayout.addLevel(lev,x);
           }
       }
      
       levelsLayout.calc(height,GAP_BETWEEN_LEVELS);   
      
       return levelsLayout;
   }
   
   public String whitebox(float x,float y){
	   return colorbox(x,y,"white");
   }
   
   public String whitebox(float x,float y,int rotated){
	   return colorbox(x,y,"white",rotated);
   }
   
   public String colorbox(float x,float y,String color){
       return ("fill"+Str.point((float)(x-0.2),(float)(y-0.1))+"--"+Str.point((float)(x-0.2),(float)(y+0.1))
    		   +"--"+Str.point((float)(x+0.2),(float)(y+0.1))+"--"+Str.point((float)(x+0.2),(float)(y-0.1))
    		   +"--cycle withcolor "+color+"; \n");

   }
   
   public String colorbox(float x,float y,String color,int rotated){
       return ("fill"+Str.point((float)(x-0.2),(float)(y-0.1))+"--"+Str.point((float)(x-0.2),(float)(y+0.1))
    		   +"--"+Str.point((float)(x+0.2),(float)(y+0.1))+"--"+Str.point((float)(x+0.2),(float)(y-0.1))
    		   +"--cycle rotated "+rotated+" withcolor "+color+"; \n");

   }
}
