/*
 * Created 2007 Roy Zywina
 * Updated by Scott Geraedts May 4, 2010
 * Updated by Jun Chen since September 24, 2015
*/
package javands.main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ensdfparser.base.BaseRun;
import ensdfparser.ensdf.*;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.ensdf.MassChain.DocumentRecords;
import ensdfparser.nds.latex.LatexWriter;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
import javands.chart.*;
import javands.ui.EnsdfWrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import com.sun.nio.sctp.InvalidStreamException;

/**
 * Contains functions for creating bands drawings and tables
 * Used as an interface between the GUI files and the files which produce output
 */
@SuppressWarnings("unused")
public class Run extends BaseRun<JTextArea>{
    /// number of charts drawn 
    private DrawingControl skelControl;
    private String skelName;
	
	private LatexWriter<Run> tbl;

	private MassChain data;
	
	private Vector<String> infileNamesV=new Vector<String>();
	
	private String LaTeXFilename="";
	private String scriptFilename="";
	private boolean isForWebDisplay=false;
	private boolean passLaTeX=false;
	
	private Vector<String> documentFilenames;
	
	private String arrow="--->";
    private String doneS="......Done!";
	
    private PrintWriter out,script,controlFile;
    private String scripthead="";
    
	EnsdfWrap [] ensw=null;//wrappers for all of the endsfs in the file
	
    /// constructor
    public Run(){
       init1();
       super.init();   	
    }
    
    /// constructor
    public Run(JTextArea m){
    	init1();
    	super.init(m);
    }

    private void init1() {
        new Vector<DrawingControl>();
        
        //reset control settings
        NDSControl.reset();               
        isForWebDisplay=false;      
        documentFilenames=new Vector<String>();
        infileNamesV=new Vector<String>();
    }
    
    public void clear() throws Exception{
    	super.clear();
    	
        new Vector<DrawingControl>();
       
        data=null;
        LaTeXFilename="";
        passLaTeX=false;
        
        documentFilenames.clear();
    }

    public String drawLegendChart(int A)throws Exception{
        PrintWriter leg = new PrintWriter(new FileWriter(javands.main.Setup.outdir+"//"+A+"LEG.mp"));
        LegendChart LW=new LegendChart();
        LW.drawChart(leg);
        leg.close();
        return A+"LEG.mp";
    }
        
    /**Tries to figure out how wide each band should be
 * We need to do this early so that the user has a chance to change it
 */
 public float[] calcBandWidths(ENSDF ens){
     
     float [] out=new float[ens.nBands()];
     
     float leftpad=BaseChart.LEFT_PAD;//in units of cm
     float rightpad=BaseChart.RIGHT_PAD;
     
     int maxLengthES=0,maxLengthJPI=0;
     
     // worst case scenario is one band per page
     for (int x=0; x<ens.nBands(); x++){
         int maxGamColumns=0;
         
         leftpad=BaseChart.LEFT_PAD;
         rightpad=BaseChart.RIGHT_PAD;
                 
         
         // add space for gammas
         BandGammaLayout gammaLayout = new BandGammaLayout(ens);
         for (int y=0; y<ens.bandAt(x).nLevels(); y++){
             Level lev = ens.bandAt(x).levelAt(y);
             
             // assume densest possible packing
             //if(maxGamColumns<lev.nGammas())
             //	 maxGamColumns=lev.nGammas();
             
             
             for (int z=0; z<lev.nGammas(); z++)
                 gammaLayout.addGamma(lev.gammaAt(z),lev,ens.bandAt(x));
             
             //debug
             //System.out.println("In Run line 257: band "+x+" calc1 beginning");
             
             gammaLayout.calc(100f,BaseChart.GAP_BETWEEN_GAMMAS_IN_BAND,true);
             if(gammaLayout.ncolumns()>maxGamColumns)
            	 maxGamColumns=gammaLayout.ncolumns();
                          
             //debug
             //System.out.println("In Run.java line201:lev.nGammas()= "+lev.nGammas()+" gamamLayout.columns="+gammaLayout.ncolumns());
         }
         
      	
         //reset leftpad and rightpad based on the maxLengthJPI and maxLengthES
         //if(maxGamColumns>1){
             float[] margins=BaseChart.recalculateMargins(ens.bandAt(x).levels(), BaseChart.LEFT_PAD, BaseChart.RIGHT_PAD);
             leftpad=margins[0];
             rightpad=margins[1];
         //}

         
         //debug
         //System.out.println("In Run.java line 229: band "+ens.bandTagAt(x)+" BaseChart.LEFTPAD RIGHTPAD="+BaseChart.LEFT_PAD+" "+BaseChart.RIGHT_PAD+" left&right pad="+leftpad+" "+rightpad);
         //System.out.println("                      maxGammaColumns="+maxGamColumns+" gammaLayout.ncolumns="+gammaLayout.ncolumns());
         
         // add on space for labels
         out[x] = leftpad + rightpad;
         out[x] += 0.4f; // fudge factor
         
         if(maxGamColumns>1)
        	 out[x]+=(maxGamColumns-1)*(BaseChart.GAP_BETWEEN_GAMMAS_IN_BAND+0.1f);
     }

     return out;
 }
 
    /** write band charts, each chart having a unique energy scale */
    public int drawBandCharts(java.io.Writer out,int cnum) throws Exception{
        return cnum;
    }
    /** write band charts, use a normalized energy scale across charts 
     * @throws Exception */
    public String drawBandChartsNorm(int cnum,int index,DrawingControl d) throws Exception{
        int x,y;
        
        EnsdfTableData etd=NDSConfig.etd;
        ENSDF ens = NDSConfig.ensdf;
        
        String filename=ens.nucleus().A()+ens.nucleus().EN()+index+"B.mp";
        Band[] bandCopies = NDSConfig.bands;
        float W=0, H=0;
        PrintWriter out=new PrintWriter(new File(javands.main.Setup.outdir+"//"+filename));        
        if(NDSConfig.portrait){
            W=NDSConfig.getChartWidth();
            H=NDSConfig.getChartHeight();
        }else{
            W=NDSConfig.getChartHeight();
            H=NDSConfig.getChartWidth();
        }
        
        //debug
        //System.out.println("In Run: drawBandChartsNorm: ens.nbands="+ens.nBands());
        //System.out.println("                            Config.bands="+Config.bands.length);
        
        if (bandCopies==null){
        	if(ens.nBands()>0){
                bandCopies = new Band[ens.nBands()];
                for (x=0; x<bandCopies.length; x++)
                    bandCopies[x] = ens.bandAt(x);
                
        	}
            else{
            	out.close();
            	return "";
            }
        }

        for(x=0;x<bandCopies.length;x++){
            
            if(bandCopies[x].nLevels()==0){
            	printMessage("Empty band: "+bandCopies[x].comment().flagAt(0,0)+". Please check the input file.");
            	out.close();
            	return "";
            }
        }
        
        // find number of bands per page
        int[] nbandsInPage;
                
        BandLevelLayout layout = new BandLevelLayout(ens);
        BandChart bc = new BandChart(etd);
        
        bc.setDrawingControl(d);
        
        //initial size, actual size is calculated in bc.findNBandsAPage 
        bc.setSizes(W,0.92f*H);//set sizes of all pages
        bc.setSize(W,0.92f*H);//set size of current page      
        
        //tight=ture: fill bands in whole page even if level scheme is tight
        //otherwise move the rightmost bands to next page to improve level gaps
        boolean tight=false;
        try{
        	nbandsInPage=bc.findNBandsAPage(bandCopies, W, tight);
        }catch (Exception e){
        	printMessage(e.getMessage());
        	nbandsInPage=new int[0];
        }
        
        

        

        //for (x=0; x<bandCopies.length; x++){
        //    for (y=0; y<bandCopies[x].nLevels(); y++)
        //        layout.addLevel(bandCopies[x].levelAt(y),x);
        //}
        //layout.calc(H);
        
        int offset=0;
        bc.writeHead(out);
        for (x=0; x<nbandsInPage.length; x++){
            bc.clear();//clear bands not sizes

            //debug
            //System.out.println("In Run line 377: drawBandChartsNorm: ens.nbands="+ens.nBands()+" nbandsInPage.length="+nbandsInPage.length+" pageNo="+x+" nbands="+nbandsInPage[x]+" offset="+offset);

            for (y=0; y<nbandsInPage[x]; y++){
                bc.addBand(bandCopies[offset+y]);
            }
            
            //debug
            //System.out.println("In Run line 384: drawBandChartsNorm: ens.nbands="+ens.nBands()+" nbandsInPage.length="+nbandsInPage.length+" pageNo="+x+" nbands="+nbandsInPage[x]+" offset="+offset);
            
            if(bc.getNumberOfBands()==0){
            	printMessage("No band to be drawn! Please check the input file!");
            	//System.exit(0);
            	return "";
            }
            	
            //actual size is calculated and set in findNBandsAPage() above
            bc.setSize(bc.getWidthAt(x), bc.getHeightAt(x));
            //debug
            //System.out.println("In Run line 397: drawBandChartsNorm: ens.nbands="+ens.nBands()+" nbandsInPage.length="+nbandsInPage.length+" pageNo="+x+" nbands="+nbandsInPage[x]+" offset="+offset);
            //System.out.println("     actual height="+bc.getHeightAt(x)+" height="+0.92f*H);
            
            bc.writeFigureHead(out,cnum+x);
            
            bc.write(out,offset);
            
            bc.writeFigureTail(out);
            
            offset+=nbandsInPage[x];
        }
        
        d.setPortrait(NDSConfig.portrait);
        d.setPages(nbandsInPage.length);
        d.setDrawn(true);

        bc.writeTail(out);
        out.close();
        
        return filename;
    }
    /** Write the decay charts */
    public String drawDecayCharts(EnsdfTableData etd,int index, DrawingControl dc)throws Exception{
    	ENSDF ens=etd.getENSDF();
    	
        String filename=ens.nucleus().A()+ens.nucleus().EN()+index+".mp";
        int nPages=0;
        PrintWriter out=new PrintWriter(new FileWriter(javands.main.Setup.outdir+"//"+filename));
        float w=0,h=0;
        
        //System.out.println(" ens.nTotDP()="+ens.nTotDP()+" ens.nDPWEI="+ens.nDPWEI());
        
        if(ens.nDPWEI()>0){//DP record with EI (intermediate level)
            DelayChart delayChart=new DelayChart();
            
            nPages=delayChart.drawChart(out,etd,dc);
            
            //System.out.println("nPages="+nPages+" ens.nTotDP()="+ens.nTotDP());

            if(nPages>0){
                w=delayChart.getWidthAt(0);//size in unit of point
                h=delayChart.getHeightAt(0);
            }

        }
        else{
            DecayChart decayChart=new DecayChart();

            nPages=decayChart.drawChart(out,etd,dc);
            //debug
            //System.out.println("In Run line 431: npages="+nPages);
            

            if(nPages>0){
                w=decayChart.getWidthAt(0);
                h=decayChart.getHeightAt(0);
            }

        }
        
        h=h*NDSConfig.POINT2CM;//size in unit of cm
        
        //force to draw figure in new page if there is no enough room in current page
        if(nPages>1 || (h+NDSControl.breakLinePos)>NDSConfig.DEFAULT_HEIGHT)
           dc.setNewPage(true);	
        
        dc.setPages(nPages);
        out.close();
        return filename;
    }
    /** Write the level charts */
    public String drawLevelCharts(EnsdfTableData etd,int index, DrawingControl dc)throws Exception{
    	ENSDF ens=etd.getENSDF();
    	
        String filename=ens.nucleus().A()+ens.nucleus().EN()+index+".mp";
        int nPages=0;
        PrintWriter out=new PrintWriter(new FileWriter(javands.main.Setup.outdir+"//"+filename));
        float w=0,h=0;
        
        LevelChart levelChart=new LevelChart();
        
        //System.out.println("RUn 367: DSID="+ens.DSId0()+" filename="+filename);
        
        nPages=levelChart.drawChart(out,etd,dc);
        if(nPages>0){
            w=levelChart.getWidthAt(0);
            h=levelChart.getHeightAt(0);
        }

        
        h=h*NDSConfig.POINT2CM;//size in unit of cm
       
        //debug
        //System.out.println("In Run line 400: "+nPages+"  "+h+"  "+Control.currentLinePos+" "+Config.DEFAULT_HEIGHT);

        //force to draw figure in new page if there is no enough room in current page
        if(nPages>1 || (h+NDSControl.currentLinePos)>NDSConfig.DEFAULT_HEIGHT)
           dc.setNewPage(true);	
        
        dc.setPages(nPages);
        out.close();
        return filename;
    }
    
    
    //write the latex header and heading pages (skeleton, abstract, ...)
    private void writeLatexOfHeadings(MassChain data) throws Exception{
                
        //If there is a control file, read it to determine how to print abstracts, skeletons,toc
        //if not, set up defaults
        if(NDSControl.controls!=null && NDSControl.useControlFile && !NDSControl.isModified){//Control.isModified=Control.isTableControlModified || Control.isHeaderSettingModified
            try{
                NDSControl.readControlHead();
                NDSControl.autoAdjust=false;
                
            }catch(Exception e){
                printMessage("Problem in control file, check to make sure there are no errors in it");
                e.printStackTrace();
            }
        }else if(NDSControl.isTableControlModified || NDSControl.isBandSettingModified){
        	//control settings could be modified in two cases: 
        	//1, control file is loaded and modified based on loaded settings;
        	//2, no control file is loaded and modified based on default settings
        	NDSControl.autoAdjust=false;

        	
        }else{
        	    
        	/*
        	boolean hasRef=Control.hasReference;//false by default, can be set to be true in main panel  
        	                                    //preserve the setting from main panel and set the rest 
        	                                    //to default
            if(!Control.isModified)
            	Control.setControlDefaults();
            
            Control.hasReference=hasRef;
        	*/	
        		
            NDSControl.autoAdjust=true;
            NDSControl.needToFindBreaks=true;//it is also set to be true when autoAdjust=false but comment width is changed, which requires finding breaks again.
            
            NDSControl.currentLineNo=0;
            NDSControl.currentLinePos=0;
            NDSControl.isNewPage=false;
            

        }
        
        int nENSDFs=data.nENSDF();//number of ENSDF datasets excluding all other non-ENSDF databsets like abstract, comment, reference
        int nNuclei=data.nNucleus();
        int A=data.getA();
        
        ensdfparser.ensdf.Nucleus nucleus;
        String title,TopTitle;
        
        if(nENSDFs<=2 || data.nChains()!=1){
        	NDSControl.hasIndex=false;
        	NDSControl.hasSkeleton=false;
        	NDSControl.blankPage=false;
        }

        if(data.getComBlock().size()==0) 
        	NDSControl.hasAbstract=false;
                
        
        //not suppress any continuation records for web display 
        if(isForWebDisplay)
        	NDSControl.showSuppressed=true;
        
        
        //default heading="NUCLEAR DATA SHEETS" for NDS production
        if(NDSControl.isForWebDisplay){
        	NDSControl.heading="From ENSDF";
        	if(nENSDFs>0 && data.getENSDF(0).fullDSIdS().toUpperCase().contains("XUNDL"))
        		NDSControl.heading="From XUNDL";
        }
        
        tbl.writeHead(out);
        
        //System.out.println("Control.hasIndex="+Control.hasIndex+" Control.hasSkeleton="+Control.hasSkeleton
        //        +" Control.hasAbstract="+Control.hasAbstract+" Control.hasSkeleton="+Control.hasSkeleton);
        
        NDSControl.numberOfPages=0;
        
        //Write abstract and index page
        try{
            if(NDSControl.hasAbstract){ 
    			ENSDF abs=new ENSDF();
    			abs.setValuesHeaderOnly(data.getComBlock());
                nucleus=abs.nucleus();
                title=nucleus.A()+nucleus.En();
                title+=": "+abs.fullDSId();
                //printMessageAsIs(arrow+title);
                printMessage(arrow+title);
                
            	tbl.writeAbstract(out,data.getComBlock());//Control.numberOfPages incremented in this call
            	tbl.writeIndexPage(out, A);
            	
            	//printMessage(doneS);
            }
            
        }catch(Exception e){
            printMessage("Problems writing abstract");
            e.printStackTrace();
        }
        
        
        
        if(NDSControl.type.equals("FULL") && nENSDFs>0 && NDSControl.hasAbstract && NDSControl.hasIndex){
            out.write("\\newpage");
            out.write("\\begin{figure}\n");
            out.write("\\begin{center}\n");
            drawLegendChart(A);
            out.write("\\includegraphics{"+A+"LEG.1}\n");
            out.write("\\end{center}\n");
            out.write("\\end{figure}\n");
            script.write(scripthead+"mpost -tex=latex "+A+"LEG.mp\n");
            
            NDSControl.numberOfPages+=1;
        }
        
    	//debug
        //System.out.println(" hasAbs: "+Control.hasAbstract+" hasSkel="+Control.hasSkeleton+" nNuclei="+nNuclei+ " type="+Control.type);
        
        //write skeleton part
        try{
            if(NDSControl.hasSkeleton && NDSControl.hasAbstract && nNuclei>2) {
            	
            	drawSkeletonChart(A+"skeleton.mp",data);//Control.skelOptions is updated in this call.
            	
                if(skelControl!=null){
                    //write latex to print skeleton drawing, with the correct number of pages
                    script.write(scripthead+"mpost -tex=latex "+skelName+"\n");
                    //out.write("\\section[ ]{ }\n");
                    //out.write("\\subsection[\\ \\hspace{2.77cm}Skeleton Scheme for A="+A+"]{ }\n");
                    out.write("\\subsection[\\hspace{-0.2cm}Skeleton Scheme for A="+A+"]{ }\n");
                    out.write("\\vspace{-30pt}\n");
                    tbl.writeSkeletonFigure(out,skelControl,skelName);
                    
                    NDSControl.numberOfPages+=skelControl.getPages();
                   
                }
            }
        }catch(Exception e){
            printMessage("***Problems drawing and writing skeleton");
            e.printStackTrace();
        }

        //write control settings for header: abstract page, skeleton drawing, index page
        NDSControl.writeControlHead(controlFile);
        
        /////////////////////////////////////////////////
                  
        if(NDSControl.writeHeaderOnly){
        	String name=getExistingOutputFilename();
            //write script
            script.write(scripthead+"latex -interaction=nonstopmode "+name+".tex\n");
            script.write(scripthead+"latex "+name+".tex\n");
            script.write(scripthead+"latex "+name+".tex\n");
            script.write(scripthead+"dvips "+name+".dvi -t letter\n");
            script.write("ps2pdf "+name+".ps\n");
            if(!isForWebDisplay){
                if(os.equals("windows")){ 
                	script.write(name+".pdf\n");
                	script.write("pause\n");
                }else if(os.equals("linux")){
                	script.write("xdg-open "+name+".pdf\n");            	
                }else if(os.equals("mac")){
                	script.write("open "+name+".pdf\n");            
                }
            }

            script.write("exit");
            script.close();
            out.write("\\end{document}\n");
            out.close();
            controlFile.close();
            return;
            //System.exit(0);
        }
        /////////////////////////////////////////////////
       
       
    }
    
    private void writeLatexOfDatasets(MassChain data) throws Exception{
    	
        printMessage("===========================================");
        printMessage("Start writing tables and drawing figures...");
        
        
        int nENSDFs=data.nENSDF();//number of ENSDF datasets excluding all other non-ENSDF databsets like abstract, comment, reference
        int nNuclei=data.nNucleus();
        int A=data.getA();
        
        ensdfparser.ensdf.Nucleus nucleus;
        String title,TopTitle;
        
        
        if(nENSDFs>0){
            out.write("\\clearpage\n");
            
            if(!NDSControl.removeAllHeading)
            	out.write("\\pagestyle{bob}\n");
            
            out.write("\\begin{center}\n");
        }
        //write pictures in right places
        EnsdfTableData etd;
        boolean inDataset;
        ENSDF ens;
        Vector<String> v;
        
        
        //suppress S cont records if necessary    
        boolean suppress=!NDSControl.showSuppressed;
        boolean isAdopted=false;
        
        if(NDSControl.type.equals("PUBLICATION")) suppress=true;
        
        
        
        for(int i=0;i<nENSDFs;i++){
            v=new Vector<String>();
            ens=data.getENSDF(i);
            inDataset=false;
            boolean draw=false;

            
            //set control settings from control file, only when no modifications were made
            //from TableControlPanel, if modified, the control has been already set 
            //elsewhere (in TablePanelControl)
            if(NDSControl.controls!=null && !NDSControl.isModified){
            	
                data.getETD(i).setDrawn(false);
                data.getETD(i).setUpdate(NDSControl.isUpdate,NDSControl.updCom2);
                data.getETD(i).setDecayDrawn(NDSControl.type.equals("FULL"));
                
                for(int j=1;j<NDSControl.controls.size();j++){
                    String temp=NDSControl.controls.elementAt(j);
                    //System.out.println(temp);
                    //System.out.println(ens.lineAt(0));
                    //System.out.println(draw);
                    if(temp.equals(ens.lineAt(0))){ inDataset=true;draw=true;continue;}
                    if(inDataset){
                        if((temp.charAt(0)>='0'&&temp.charAt(0)<='9')||(temp.charAt(1)>='0'&&temp.charAt(1)<='9')||(temp.charAt(2)>='0'&&temp.charAt(2)<='9'))
                            { inDataset=false; break;}
                        else v.add(temp);
                    }
                }
                if(draw){
                	data.getETD(i).setTableControls(v,suppress);
                    data.getETD(i).setShowSuppressed(!suppress);
                    
                    NDSControl.needToFindBreaks=false;
                    NDSControl.isCommentWidthReset=false;
                    NDSControl.isBreakPointReset=false;
                }
            }
        }
        

        if(NDSControl.autoAdjust){
        	NDSControl.needToFindBreaks=true;
        	NDSControl.isCommentWidthReset=false;//true for manual setting
        	NDSControl.isBreakPointReset=false;  //true for manual setting
        }else{
        	if(NDSControl.isCommentWidthReset)
        		NDSControl.needToFindBreaks=true;
        	else if(NDSControl.isBreakPointReset)
        		NDSControl.needToFindBreaks=false;
        }
        
        
        
        boolean forcedDrawn=false;

        
        int i=-1;
        int nBlocks=data.nBlocks();
        
        //for(int i=0;i<nENSDFs;i++){
        for(int b=0;b<nBlocks;b++){	
        	String blockType=data.getBlockTypeAt(b);
        	if(blockType.equals("ENSDF")){
        		i++;
        	}else{
        		if(blockType.equals("COMMENT")){
        			
        			if(b==0 && NDSControl.hasAbstract)//b=0 is the abstract that has been written already
        				continue;
        			
        			if(nENSDFs>0) out.write("\\end{center}\n");//temporarily close the global "center" environment

        			out.write("\\clearpage");
        			
        			ENSDF abs=new ENSDF();
        			abs.setValuesHeaderOnly(data.getBlockAt(b));
                    nucleus=abs.nucleus();
                    title=nucleus.A()+nucleus.En();
                    title+=": "+abs.fullDSId();
                    
                    printMessage(arrow+title);
                    
        			tbl.writeAbstract(out, data.getBlockAt(b));
        			
        			//printMessage(doneS);
        			
        			if(nENSDFs>0) out.write("\\begin{center}\n");//re-enter the global "center" environment
        			
        		}else if(blockType.equals("REFERENCE")){
        			tbl.writeReferenceBlock(out,data.getBlockAt(b));//write the Reference data block. 
        			                                                //Note that this is given reference (should avoid using this kind of dataset)
        		}else{
        			Vector<String> block=data.getBlockAt(b);
        			if(block.size()>0){
        				printMessage("Unknown type of data block: <"+blockType+">");
        				NDSControl.hasReference=true;
        			}
        		}
        		
        		continue;
        	}
        	
            ens=data.getENSDF(i);
            
            nucleus=ens.nucleus();
            title=nucleus.A()+nucleus.En();
            title+=": "+ens.fullDSId();
                        
            //printMessageAsIs(arrow+title);
            printMessage(arrow+title);
            
            etd=data.getETD(i);
            
            v=new Vector<String>();

            isAdopted=false;
            if(ens.fullDSId().contains("ADOPTED LEVELS"))
            	isAdopted=true;
        	
       
            if(NDSControl.isForWebDisplay){//for web-display, header=From ENSDF-###(date)
            	NDSControl.heading="From ENSDF";
            	if(ens.fullDSId().toUpperCase().contains("XUNDL"))
            		NDSControl.heading="From XUNDL";
            }
            

            tbl.setHeading(out,ens);//only for web-display; write nothing if not for web-display and use default heading

            if(etd.isDrawn()){
            	
                etd.suppressContRrecords(!NDSControl.showSuppressed);
                etd.setShowSuppressed(NDSControl.showSuppressed);
                

                //global option for ordering gamma table (override PN option), if not set, ordering follows PN option or manual setting in Control Settings for each data set
                if(NDSControl.reorderGamma){
                	if(!etd.getGammaTableControl().reorderGamma()){//ordering option in Control Settings for each data set overrides that in global setting
                		                    //ordering priority: individual manual setting>global setting>PN option
                		etd.getGammaTableControl().setSortGammas(NDSControl.reorderByGamma);
                		
                		etd.getGammaTableControl().updateDefaultColumns();
                		
                	}
                }
                

                //debug
                //System.out.println("In Run: showSuppressed="+Control.showSuppressed+" etd.showSuppressed="+etd.isShowSuppressed());
                
                //write tables and images for a given data set, including appropriate page breaks
                //if(etd.isNewPage()&&i>0) {
                if(i>0){//start a new page for each data set no matter whether the data set is short or not, suggested by Balraj
                	out.write("\\clearpage\n");
                	NDSControl.currentLineNo=0;
                	NDSControl.currentLinePos=0;
                	NDSControl.isNewPage=false;//flag the end of a landscape table to draw next table in a new page 
                	                        //It is set to be true after each landscape table and set to be false after the next table in 
                	                        //the same dataset, unless it is still a landscape table. It is set to be false at the beginning
                	                        //of each dataset.
                }

                
                //debug
                //System.out.println("In Run: before writeBody: isDrawn="+etd.getDecayDrawingControl().isDrawn()+" etd.isNewPage="+etd.isNewPage()+" currentLinePos="+Control.currentLinePos);
                
                
                try{
                    //debug
                	//System.out.println("start writing table body...");
                	
                    //////////////////////////////
                	if(!NDSControl.drawFigureOnly && !NDSControl.drawBandOnly)
                		TopTitle=tbl.writeBody(out,etd,data);
                    
                	//System.out.println("write table body ok.");
                	
                    //////////////////////////////
                }catch(Exception e){
                    printMessage("***Problems writing table body");
                    e.printStackTrace();
                }
                
                //NOTE: Default TableControl and DrawingControl of a etd are set when an ENSDF file is loaded into a MassChain object
                //Here we following the NNDS rule that band drawings only include in Adopted datasets and decay drawings only
                //include individual data sets, unless "inlucdeAllDrawings" is set in control panel
                forcedDrawn=false;
                if(etd.getDecayDrawingControl().isDrawn() && (NDSControl.includeAllDrawings || (!isAdopted&&!NDSControl.nodrawing)))
                	forcedDrawn=true;
                
                if(NDSControl.drawFigureOnly)//override settings above, for drawing selected invidual dataset in table control panel in manual mode
                	forcedDrawn=true;

                if(NDSControl.nodrawingExceptAdoptedBands) {
                	forcedDrawn=false;
                }
                
                //if draw a decay drawing, draw it and add it to the table
                String filename="";
                
                //select datasets for drawing
                
                /*
                boolean isTempDrawing=true;
                if(forcedDrawn && isTempDrawing) {
                	String dsid=ens.DSId0();
                	int nLevs=ens.nLevels();
                	
                	if(dsid.contains(" DECAY")) {
                		if(dsid.contains(" IT ") || nLevs<=3) {
                			forcedDrawn=false;
                		}
                	}else if(dsid.contains("COULOMB") || dsid.contains("(G,G')") || dsid.contains("(G,G)")) {
                		forcedDrawn=false;
                	}else if(dsid.contains("(N,G)")) {
                		forcedDrawn=false;
                	}else if(!dsid.contains("ADOPTED") && !etd.getGammaTableControl().isSortGammas()){
                		forcedDrawn=false;
                	}else if(ens.nTotGam()<10) {
                		forcedDrawn=false;
                	}
                }
                */
                
                if(forcedDrawn && !NDSControl.drawBandOnly){
                	try{

                		//debug
                		//System.out.println("start drawing level scheme...");
                		
                        if(EnsdfUtil.findDSIDType(ens.fullDSId()).equals("decay") && !ens.fullDSId().contains(" IT ") && !ens.fullDSId().contains("SF DECAY")){ 
                        	if(ens.nParents()>0)
                        		filename=drawDecayCharts(etd,i,etd.getDecayDrawingControl());
                        	else{
                        		printMessage("Warning: P record is required for decay dataset but not given. Decay scheme will not be drawn.");
                        		filename="";
                        	}
                        }
                        else{ 
                        	if(ens.fullDSId().contains(" IT ") && ens.nParents()==0)
                        		printMessage("Warning: P record is required for decay dataset but not given.");
                        	
                        	filename=drawLevelCharts(etd,i,etd.getDecayDrawingControl());
                        }
                        
                        //if(isTempDrawing && etd.getDecayDrawingControl().getPages()>3) {
                        //	filename="";
                        //}
                        
                		//debug
                		//System.out.println("draw level scheme OK...");
                		
                        if(ens.norm().OS().trim().length()==0 && ens.nGamWI()>0){
                        	//printMessage("Warning: PN record is not given! Option 3 is assumed.");
                        	int implicitPN=ens.norm().implicitPN();
                        	String s="Intensity type is not specified in level/decay scheme.";
                        	if(implicitPN>=0)
                        		s="PN option="+implicitPN+" is assumed in level/decay scheme.";
                        	
                        	printMessage("Warning: PN record is not given! "+s);
                        }
                        
                        //debug
                        //System.out.println("In Run: drawing: DSID="+ens.DSId()+" filename="+filename+" type="+EnsdfUtil.findDSIDType(ens.DSId()));

                        if(filename.length()>0){
                        	if(NDSControl.isNewFigurePage || NDSControl.isNewPage){//Control.isNewPage is set true after landscape and must be set false after use
                        		etd.getDecayDrawingControl().setNewPage(true);
                        		if(NDSControl.isNewPage)
                        			NDSControl.isNewPage=false;
                        	}
                        	
                        	if(NDSControl.drawFigureOnly)
                        		etd.getDecayDrawingControl().setNewPage(false);//when drawing only figure, it is new page already
                        	                                                   //set newpage again will leave a blank page

                        	
                        	//if(TopTitle.length()>0) out.write(TopTitle+"\\\\" +"\n\\vspace{0.3cm}\n"); 
                            tbl.writeFigure(out,etd.getDecayDrawingControl(),filename);
                            script.write(scripthead+"mpost -tex=latex "+filename+"\n");
                        }                      

                    }catch(Exception e){
                        printMessage("***Problems writing level/decay scheme");
                        e.printStackTrace();
                    }

                }
            	

                //debug
                //System.out.println("In Run: drawingBand: isDrawn="+etd.getBandDrawingControl().isDrawn());

                
                forcedDrawn=false;
                
                if(etd.getBandDrawingControl().isDrawn() && (NDSControl.includeAllDrawings || (isAdopted&&!NDSControl.nodrawing)))
                	forcedDrawn=true;
                
                //by default, etd.getBandDrawingControl().isDrawn()=true
                //it will be set to false for all data sets when individual data
                //data sets in band creator are selected and the generate button
                //is clicked, except for the selected data sets
                if(forcedDrawn){               	
                    filename=ens.nucleus().A()+ens.nucleus().EN()+i+"B.mp";        
                    
                    //boolean isBandCreated=Control.isBandCreated;
                    boolean isBandCreated=etd.getBandDrawingControl().isBandCreated();
    
                    try{
                        //debug
                        //System.out.println("In Run1: drawingBand: npage="+etd.getBandDrawingControl().getPages()+" isBandCreated="+isBandCreated);
                    	
                        if(!isBandCreated){//set in BandFrame after individual data set is selected for band drawing
                        	EnsdfWrap ew;
                        	//ew=new EnsdfWrap(etd);
                            if(ensw!=null && ensw.length>i)
                            	ew=ensw[i];
                            else
                            	ew=new EnsdfWrap(etd);
                        	
                    		//debug
                    		//System.out.println("start drawing band...");

                            NDSConfig.bandWidths=calcBandWidths(ens);
                            ew.bandModel(NDSConfig.bandWidths);
                            ew.setConfig();
                            ew.setBands();
                            
                            
                        	filename=drawBandChartsNorm(0,i,etd.getBandDrawingControl());
                        	if(filename.length()>0) 
                        		isBandCreated=true;
                        
                    		//debug
                    		//System.out.println("draw band OK...");
                        }
                             
                        //debug
                        //System.out.println("In Run1.1: drawingBand: npage="+etd.getBandDrawingControl().getPages());
                        
                        if(isBandCreated){
                        	if(!NDSControl.drawBandOnly){
                        		etd.getBandDrawingControl().setNewPage(true);
                        		out.write("\\clearpage\n");
                        	}
                        	
                            //debug
                            //System.out.println("In Run2: drawingBand: isDrawn="+etd.getBandDrawingControl().isDrawn()+" filename="+filename);
                                
                            tbl.writeFigure(out,etd.getBandDrawingControl(),filename);
                            
                            script.write(scripthead+"mpost -tex=latex "+filename+"\n");
                        }
                    }catch(Exception e){
                        printMessage("***Problems drawing and writing bands");
                        e.printStackTrace();
                    }


                }

                //if draw a band drawing, draw it if it hasn't been drawn already and then add it to the table
            }
            
            //printMessage(doneS);
            
            //debug
            //System.out.println("In Run: etd.tc.commentColumnWidth="+etd.getDelayTableControl().getCommentColumnWidth());

            NDSControl.writeControlCommands(controlFile, etd, NDSControl.type);//wrap of etd.writeControl(controlFile,Control.type);
        }
        
        if(nENSDFs>0){
        	out.write("\\end{center}\n");
        }    	
    
    }
    
    private void writeLatexOfReference(MassChain data)throws Exception{
    	
        if(NDSControl.referenceA.length()==0 && data.nENSDF()>0)
        	NDSControl.referenceA=Integer.toString(data.getA());
        
        
        //debug
        //System.out.println(Control.hasReference+"  "+data.ref().getKeyNumbers().length);
        
        boolean isGood=false;
        
        if(NDSControl.hasReference && data.ref().getKeyNumbers().length>0) {
        	printMessage("\nWriting the list of references...");
        	out.write("\\clearpage\n");
        	NDSControl.numberOfPages+=1;//use longtable for reference list, can't simply get the number of pages for reference list
        	                         //so just assume one page
        	
        	String nsrfilename=Setup.outdir+"\\NSR_";
            if(!os.equals("windows")) 
            	nsrfilename=Setup.outdir+"/NSR_";
            
        	nsrfilename+=makeNSRFileName(data)+".dat";
        	    
        	if(!NDSControl.userNSRFilename.isEmpty())
        		nsrfilename=NDSControl.userNSRFilename;
        	
        	boolean isFileEmpty=true;
        	try{
        		File f=new File(nsrfilename);
        		if(f.exists()){
        			printMessage(arrow+"Reference information is being loaded from the file:\n    "+nsrfilename);
        			if(Str.isFileEmpty(f)){
        				printMessage("*** Warning: the input NSR file is empty!\n");
        				printMessage("    reference information will be downloaded from the NSR database and saved into the file.");
        			}else
        				isFileEmpty=false;
        			
        		}else
        			printMessage(arrow+"Reference information is being downloaded from the NSR database and saved into the file:\n    "+nsrfilename);    		
        		
            	boolean isLoadedFromFile=data.ref().loadNSRs(nsrfilename,NDSControl.NSRDataBaseLoginFileName,NDSControl.isForcedNSRWebQuery,NDSControl.isSilentJDBC);
            	
        		//System.out.println(NDSControl.NSRDataBaseLoginFileName+" isLoadedFromFile="+isLoadedFromFile);
        		
            	if(!isFileEmpty && !isLoadedFromFile){
    				printMessage("*** Warning: the input NSR file is invalid (MUST be in Exchange format from NSR serch page)!\n");
    				printMessage("    reference information has been downloaded from the NSR database and saved into the file.");
            	}
            	
            	isGood=true;
            	printMessage("Done.");
            	
            }catch(Exception e){
            	isGood=false;
                printMessage("*** Problems when loading NSR information");
                if(!isFileEmpty){
    				printMessage("    The input NSR file is invalid (MUST be in Exchange format from NSR serch page)!\n");
    				printMessage("    Reference information is being downloaded from the NSR database but an error occurs.\n");
                }
                	
                printMessage("    "+e.getMessage());
                e.printStackTrace();
            }

            try{
            	
            	//debug
            	//System.out.println("In Run Line 712: suppress="+suppress+" Control.includeRefTitle="+Control.includeRefTitle);
            	if(isGood){
                   	data.ref().write(out,NDSControl.referenceA,!NDSControl.includeRefTitle);
                   	String warnings=data.ref().getWarnings();//get warning message to be printed in the message box
                   	if(warnings.length()>0)
                   		printMessage(warnings+"\n");
            	}else
            		printMessage("Reference list is not written due to errors.");
               	
            }catch(Exception e){
                printMessage("***Problems writing reference list");
                e.printStackTrace();
            }
 

        }
    }
    
  //write the ending pages (reference ...) and latex tails 
    private void writeLatexOfEndings(MassChain data)throws Exception{
    	
    	writeLatexOfReference(data);
    	
        
        if(NDSControl.autoCorrectionForKeynumber) {
        	printMessage("");
        	printMessage("**Keynumbers are auto-corrected for letter case**");
        }
        
        String s="";
        if(NDSControl.autoAdjust)
        	s=" with automatic settings.";
        else
        	s=" with manual settings.";
        	
        printMessage("");
        printMessage("Table production completed"+s);
                
        tbl.writeTail(out);
        
        String name=getExistingOutputFilename();
        
        //write script
        script.write(scripthead+"latex -interaction=nonstopmode "+name+".tex\n");
        script.write(scripthead+"latex "+name+".tex\n");
        script.write(scripthead+"latex "+name+".tex\n");
        script.write(scripthead+"dvips "+name+".dvi -t letter\n");
        script.write("ps2pdf "+name+".ps\n");
        
        
        
        if(!isForWebDisplay){
        	script.write("echo done>dummy.txt\n");
        	
            if(os.equals("windows")){ 
            	script.write("\""+name+".pdf\"\n");//add "" around filename to avoid problems when "=" is in a filename
            	script.write("pause\n");
            }else if(os.equals("linux")){
            	script.write("xdg-open "+name+".pdf\n");            	
            }else if(os.equals("mac")){
            	script.write("open "+name+".pdf\n");            
            }
                      
            
            script.write("exit");
        }else{
        	
        	script.write("echo done>dummy.txt\n");//used as a checking point to check if PDF file is fully created.
        	
        	/*
        	//For mismatch between XREF DSID and dataset DSID (containing continuation DSID liens)
        	//that is, DSID in XREF that is just the first DSID line of a dataset which has continuation DSID lines,
        	//copy the pdf file named by dataset DSID to a pdf file named by the incomplete XREF DSID
        	if(nENSDFs==1){
        		ens=data.getENSDF(0);
        		String dsid0=ens.DSId0().trim();//only first DSID line
        		String dsid=ens.DSId().trim();//all DSID lines
        		if(dsid0.length()<dsid.length()){
        			String name0=EnsdfUtil.DSID2Filename(dsid0);
        			String command="cp "+name+".pdf "+name0+".pdf";
        			if(os.equals("windows"))
        				command="copy "+name+".pdf "+name0+".pdf";
        				
        			script.write(command+"\n");
        				
        		}
        	}
        	*/
        	
            if(os.equals("windows")){ 
            	script.write("exit");
            }
        }
              
        script.close();
        out.close();
        controlFile.close();
       
        //debug
        //System.out.println("In Run line 957: numberOfPages="+Control.numberOfPages);
        
        if(NDSControl.numberOfPages<=1)//remove page number in heading if there is only one page
        	Str.replace(NDSConfig.latex,"\\pagestyle{bob}","\\pagestyle{single}");

        //insert hyperlinks to all keynumbers using NSR link manager
        //For example,
        //     http://www.nndc.bnl.gov/nsr/nsrlink.jsp?2004HE05,B
        //It directs to the journal webpage if available.
        //If it cannot be directed to the journal but it is valid keynumber
        //it will direct to the NSR webpage of that keynumber.
        //If it is not valid keynumber, it will still direct to NSR webpage
        //but show "No entry found for..."
         
        try{
            insertKeynumberLink(NDSConfig.latex,data);
        }catch(Exception e){
        	printMessage("***Error when inserting hyperlinks for keynumbers.");
        	printMessage("   It could be that a LaTeX file with the same name is still");
        	printMessage("   run by latex.exe. If so, close it and run the program again.");
        	e.printStackTrace();
        }
       
        if(NDSControl.isForWebDisplay){
            boolean success=false;
            try{
            	writeDocumentRecords(data);
            	success=true;     
            }catch(Exception e){
            	printMessage("***Error when writing document records:");
                printMessage(e.getMessage());
                printMessage("   LaTeX files for document records are not generated.");
                e.printStackTrace();
            }
            
            if(success){
                try{
                	insertDocumentLink(NDSConfig.latex,data);
                }catch(Exception e){
                	printMessage("***Error when inserting hyperlinks for document records.");
                	e.printStackTrace();
                }
            }
     	
        }
    }
    
    /** create the tables, and also a script to do the latex/mpost to pdf conversion */
    public void writeLatex(String filename,MassChain data)throws Exception{
        
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();

    	String parentDir=Str.getParentDir(filename);
    	
        File file=new File(filename);
        out=new PrintWriter(file);
        LaTeXFilename=file.getName();
        
        if(!parentDir.isEmpty() && !parentDir.equals(Setup.outdir)){ 
            setOutputDir(parentDir);        
            setOutputFilename(Str.fileNamePrefix(LaTeXFilename));
        }
        
        String scriptName=Setup.outdir;       
        if(System.getProperty("os.name").toLowerCase().contains("mac"))
        	scripthead="";//"/usr/texbin/";
        
        
        if(os.equals("windows")) 
        	scriptName+="\\NDS.bat"; 
        else
        	scriptName+="/NDS.sh";  
        
        file=new File(scriptName);
        script=new PrintWriter(file);      
        scriptFilename=file.getName();
        
        printMessage("output dir: "+Setup.outdir+"\n");
        printMessage("output LaTeX file: "+LaTeXFilename+"\n");
        
        controlFile=new PrintWriter(new File(Setup.outdir+"//control"));//the control file    
        
        tbl=new LatexWriter<Run>(this);
                

        //////////////////////////////////////////////////////////////////////
        //write the latex header and heading pages (skeleton, abstract, ...)
        /////////////////////////////////////////////////////////////////////
        try {
            writeLatexOfHeadings(data);
        }catch(StackOverflowError e) {
        	printMessage("#### Fatal error when writing dataset headings: stack memory overflow ####");
        	printMessage("     It usually happens when translating a very long comment");
        	e.printStackTrace();
        	return;
        }

        
        if(NDSControl.writeHeaderOnly)
        	return;
        
        ///////////////////////////////////////////////////
        //write the main-body of the output --- datasets
        ///////////////////////////////////////////////////
        try {
            if(!NDSControl.isLoadedKeynumberFile)
            	writeLatexOfDatasets(data);//heading is reset here if for web display
        }catch(StackOverflowError e) {
        	printMessage("#### Fatal error when writing dataset body: stack memory overflow ####");
        	printMessage("     It usually happens when translating a very long comment");
        	e.printStackTrace();
        	return;
        }

                      
       
        //////////////////////////////////////////////////////////////////////
        //write the ending pages (reference ...) and latex tails 
        /////////////////////////////////////////////////////////////////////
        try {
            writeLatexOfEndings(data);
        }catch(StackOverflowError e) {
        	printMessage("#### Fatal error when writing dataset endings: stack memory overflow ####");
        	printMessage("     It usually happens when translating a very long comment");
        	e.printStackTrace();
        	return;
        }

        

        
        endTime=System.currentTimeMillis();
        timeElapsed=(float)(endTime-startTime)/1000;
        printMessage("Time elapsed: "+String.format("%.3f", timeElapsed)+" seconds");
        
    }
    
        
    //wrap of writeDocumentRecords() in LatexWriter
    //generate a separate LaTeX file for each dataset
    public void writeDocumentRecords(MassChain data)throws Exception{
    	//append latex scripts at the beginning of the script file for generating document PDF of each dataset

        PrintWriter out;   
        String script="",filename="";
        
        for(int i=0;i<data.nBlocks();i++){
        	
        	//String dsid=data.getENSDF(i).DSId();
        	
        	String dsid=EnsdfUtil.getDataBlockDSID(data.getBlockAt(i));
        	
        	filename=getDocumentFilename(data,i);      
        	
        	Vector<DocumentRecords> drV=data.getDocumentRecordsV(i);
        	
        	if(drV.size()==0)
        		continue;
        	
        	
        	script+="latex -interaction=nonstopmode "+filename+".tex\n";
            script+="latex "+filename+".tex\n";
            script+="latex "+filename+".tex\n";
            script+="dvips "+filename+".dvi -t letter\n";
            script+="ps2pdf "+filename+".ps\n";
            
            documentFilenames.add(filename);
            
            filename+=".tex";
            filename=Setup.outdir+this.dirSeparator+filename;
        	out=new PrintWriter(new File(filename));
        	
        	tbl.writeHead(out);

        	
        	tbl.writeDocumentRecords(out, drV,data.getBlockAt(i),data.getETDByBlockAt(i));
        	
        	tbl.writeTail(out);
        	
        	out.close();
        	
        	//System.out.println("In run line 1208: filename="+filename);
        	
        }
        
        Str.insertIntoFile(Setup.outdir+this.dirSeparator+this.scriptFilename, script,"beginning");

    }
    
    public String getDocumentFilename(MassChain data,int index){
    	if(data==null || data.nBlocks()==0)
    		return "";
    	
    	return EnsdfUtil.makeDocumentFilename(data.getBlockAt(index));
    }
    
    public String getDocumentFilename(int index){   	
    	return getDocumentFilename(data,index);
    }
    
    public void insertKeynumberLink(String filename,MassChain data)throws Exception{
    	
    	BufferedReader br=null;
    	try{
            br = new BufferedReader(new FileReader(new File(filename)));
    	}catch(Exception e){
    		System.out.println("***Error when inserting keynumber links: ");
    		System.out.println("   file <"+filename+"> does not exist!\n");
    		return;
    	}
        
        String s;
        EnsdfReferences ref=data.ref();
        
        Vector<String> text = new Vector<String>();
        while(true){
            s = br.readLine();
            if (s==null) break;
            //System.out.println(s);
            text.add(s);
        }
        br.close();
        
        if(text.size()<=0)
        	return;

        
        PrintWriter out=new PrintWriter(new File(filename));
        String line="";
        String newline="";
        String keynumber="";
        String url="";
        for(int i=0;i<text.size();i++){
        	newline="";
        	line=text.get(i);
        	while(line.length()>0){
                
            	keynumber=ref.findFirstKeyNumber(line);
                
            	if(keynumber.length()<=0){
            		newline+=line;
            		break;  		
            	}
                
            	int p=line.indexOf(keynumber);
            	newline+=line.substring(0, p);
            	if(line.length()>p+8)
            		line=line.substring(p+8);
            	else
            		line="";
                
            	url=ref.NSRLinkManager(keynumber, 'B');
                url=url.replace("&","\\&");
            	
            	if(NDSControl.autoCorrectionForKeynumber && keynumber.length()==8) {
            		String temp=keynumber.toUpperCase();
            		keynumber=keynumber.toLowerCase();
            		keynumber=temp.substring(0,5)+keynumber.charAt(5)+temp.substring(6);
            	}
            	newline+="\\href{"+url+"}{"+keynumber+"}";
        	}

        	out.write(newline+"\n");      	
        }
        
        out.close();
        
    }

    //insert links for document records which link to separate pdf files created for document records for each dataset
    public void insertDocumentLink(String filename,MassChain data)throws Exception{
    	String documentLabel=data.getDocumentLabel();
    	
    	BufferedReader br=null;
    	try{
            br = new BufferedReader(new FileReader(new File(filename)));
    	}catch(Exception e){
    		System.out.println("***Error when inserting document links: ");
    		System.out.println("   file <"+filename+"> does not exist!\n");
    		return;
    	}
        
        String s;
        
        Vector<String> text = new Vector<String>();
        while(true){
            s = br.readLine();
            if (s==null) break;
            //System.out.println(s);
            text.add(s);
        }
        br.close();
        
        if(text.size()<=0)
        	return;
      
        Vector<String> hrefs=new Vector<String>();
        for(int i=0;i<data.nBlocks();i++){
        	//String dsid=data.getENSDF(i).DSId();     
        	String dsid=EnsdfUtil.getDataBlockDSID(data.getBlockAt(i));
        	
        	Vector<DocumentRecords> drV=data.getDocumentRecordsV(i);
    		String href="";
    		if(drV.size()>0){
    			String documentfilename=getDocumentFilename(data,i);
    			if(documentfilename.length()>=0){
            		href=documentfilename+".pdf";
            		href=EnsdfUtil.makePDFLink(href);
    			}
    		}   
    		
    		hrefs.add(href);
        }

        
        
        PrintWriter out=new PrintWriter(new File(filename));
        String line="";

        for(int i=0;i<text.size();i++){
        	line=text.get(i);
        		
        	line=replaceDocumentMarks(line,documentLabel,hrefs);
        	
        	out.write(line+"\n");      	
        }
        
        out.close();
        
    }   
    
    //check if a line in the LaTeX file is part of a document label line
    //if yes, replace the marks with the document line label, set in MassChain.java
    public String replaceDocumentMarks(String line,String documentLabel,Vector<String> hrefs){
        String out="";
    	String labelLineMark="@B@";
    	int p0=line.indexOf(labelLineMark);
    	
    	if(p0<=0 || line.length()<4)
    		return line;
    	
    	String marks="";
    	//NOTE that: document line marks is set as like "@B@1@0@@@ @B@1@1@@@@2", 
    	// with the first number "@1" is block index (start from 0), 
    	//the second "@1" for word index in the first word (start from 0), 
    	//the last "@2" in the second word for document index (start from 1)
    	int i=p0;
    	for(;i<line.length();i++){
    		char c=line.charAt(i);
    		if(c!='@' && c!=' ' && !Character.isLetterOrDigit(c))
    			break;
    	}
    	
 	
		marks=line.substring(p0,i).trim(); //eg, marks="@B@1@0@@@ @B@1@1@@@2" or "@B@1@0@@@@"
			
		String[] markLabels=marks.split(" ");
		int nMarks=markLabels.length;
    	
		int iblock=-1,idocument=-1,iword=-1;
		
		int p2=marks.lastIndexOf('@');
		
		
		if(p2<marks.length()-1){
			String s=marks.substring(p2+1);

			if(Str.isNumeric(s)){
				idocument=Integer.parseInt(s);
				
				int p=p2;
				while(p>=0 && marks.charAt(p)=='@')
					p--;
				
				s="";
				if(p>0)
					s=marks.substring(0,p+1);
				
		    	
				while(p>=0 && Character.isDigit(marks.charAt(p)))
					p--;
				
				
		    	
				if(p>0 && s.length()>0)
					s=s.substring(p+1);
				
		    	
				if(Str.isNumeric(s))
					iword=Integer.parseInt(s);
			}
		}
		
		int p1=marks.substring(3).indexOf('@')+3;
		if(p1>3){
			String s=marks.substring(3, p1);
			if(Str.isNumeric(s)){
				iblock=Integer.parseInt(s);

				if(iword<0 && p1<marks.length()-1){//always get the last iword
					int p=marks.substring(p1+1).indexOf('@')+p1+1;
					if(p>(p1+1)){
						s=marks.substring(p1+1,p);
						if(Str.isNumeric(s))
							iword=Integer.parseInt(s);
					}							
				}

			}
		}
		
		i=iword-nMarks+1;
    	
		if(iblock<0 || iword<0 || i<0)
			return line;
		
		
		String[] labels=documentLabel.split(" ");
			
		String href="",label="";
		
		label="";
		for(;i<=iword;i++){
			label+=" "+labels[i];
		}

		label=label.trim();
		if(idocument>0){
			label+=" "+idocument;
		}
		
		if(iblock>=0 && iblock<hrefs.size())
			href=hrefs.get(iblock);
		
    	//System.out.println(i+"  line="+line+"marks="+marks+"  label="+label+" href="+href+" hrefs.size="+hrefs.size());
    	
		if(href.length()>0)
			label="\\href[pdfnewwindow]{"+href+"}{"+label+"}";

		out=line.replace(marks,label);
				
    	return out;
    }
    
    /*gets all the data together and creates a skeleton object to draw skeleton diagrams */
    public void drawSkeletonChart(String filename,MassChain data){
        Vector<ENSDF> dataSets=new Vector<ENSDF>();
        PrintWriter out;
        
        
        for(int i=0;i<data.nENSDF();i++){
            dataSets.add(data.getENSDF(i));
        }
        
        int ntries=2;
        String options=NDSControl.skelOptions;
        
        if(options.contains("/H:"))
        	ntries=1;
        
        //portrait=yes by default
        
        try{
            
        	for(int i=0;i<ntries;i++){
        	
                out=new PrintWriter(new File(javands.main.Setup.outdir+"//"+filename));
                SkeletonChart skl=new SkeletonChart(dataSets,options);
                
                skl.writeHead(out);
                skelControl=skl.drawChart(out);
                skelName=filename;
                skl.writeTail(out);
                out.close();
                
                //System.out.println("i="+i+" isInNewPage="+skl.isTableInNewPage()+" options="+options);
                
                if(ntries==1 || !skl.isTableInNewPage() )
                	break;
                else if(i==0)
                	options+="/H:";//try landscape orientation (horizontal)
                else if(i==1)
                	options=NDSControl.skelOptions;
        	}
        	
            //System.out.println(" skelControl==null "+(skelControl==null));
            
            System.out.println("Skeleton drawings finished");

        }catch(Exception e){
        	e.printStackTrace();
            printMessage("No skeleton will be drawn!");
        }
   
        
        /*dataSets.clear();
        for(int i=0;i<data.nENSDF();i++){
            data.getENSDF(i).resetLittleValues();
        }*/
        
    }
    
    
    public LatexWriter<Run> getLatexWriter(){
    	return tbl;
    }
    
    public DrawingControl getSkeletonControl(){
    	return skelControl;
    }
    
    public String makeNSRFileName(MassChain data){
    	String s="";
    	String filename="";
    	
    	int index=0;
    	int nENSDFs=data.nENSDF();
    	if(nENSDFs>1)
    		filename+="A"+data.getA();
    	else if(nENSDFs==1){
    		String DSID=data.getENSDF(0).fullDSId().trim();
			s=DSID.replace("-", "");
			s=s.replace("+", "");
			s=s.replace("(", "_");
			s=s.replace(",", "_");
			s=s.replace("/", "");
			
			index=s.indexOf(")");
			if(index>0)
				s=s.substring(0, index);
			s=s.trim().replace(" ", "_");
			s=s.trim().replace("__", "_");
			
			index=s.indexOf(":");
			if(index>0)
				s=s.substring(0, index).trim();
			
    		filename+=s;
    	}
    	else
    		filename+="cover";
    	
    	return filename.replace("*", "");
    }
    
        
    public void runScript() throws Exception{
      	
    	String script="NDS.bat";
    	String path=javands.main.Setup.outdir+"\\"+script;
    	String command="";
    	String os=System.getProperty("os.name").toLowerCase();
    	String outdir="";  
    
    	
    	if(os.contains("linux")||os.contains("mac")){
            script="NDS.sh";
            path=javands.main.Setup.outdir+"/"+script;
    	}
    	
    	String pdfName=this.getExistingOutputFilename().trim(); //return the LaTeX filename without extension.
    	if(pdfName.length()>0)
    		pdfName+=".pdf";
    	else{
    		printMessage("***Error when running script: no LaTeX file.");
    		return;
    	}
    	
    	printMessage("\n\nRunning the following shell script to create PDF file: "+pdfName+"\n");
    	printMessage(path);
    	
    	Process proc=null;
    	
    	passLaTeX=false;
    	
    	if(os.contains("linux")||os.contains("mac")){

            
            String shell="/bin/bash";
           
            //specified environment path. Note that it is needed for MacOS. For linux, it also works by setting it as null
            //because all commands in the script are in the default system path.
            //String[] envs=new String[]{"/Library/Tex/texbin:/usr/local/bin"};
            String[] envs=null;
            
            //NOTE that System.getenv("PATH") return the system default path, which is just "/usr/bin:/bin:/usr/sbin:/sbin" 
            //not even includes "/usr/local/bin". It is NOT the path in .bashrc or .zshrc (MacOS) set by users.
            
            //To set the return result to be the user's PATH, go to the Run configuration and add new environment variable 
            //PATH and set its value to the value of user's path.
            
            if(os.contains("mac")) {
                //envs=new String[] {"PATH=/Library/Tex/texbin:/usr/local/bin:"+System.getenv("PATH")};
                envs=new String[] {"PATH="+System.getenv("PATH")};
            }
            
            File wd = new File(javands.main.Setup.outdir);
    		proc = null;		   
    		
    		//method 1
    		proc = Runtime.getRuntime().exec(shell, envs, wd);
    		 
    		
    		/*
            //method 2 (modern): using ProcessBuilder (tested working)
            ProcessBuilder processBuilder = new ProcessBuilder(shell);
            processBuilder.directory(wd);          
            Map<String, String> envMap = processBuilder.environment();
            envMap.put("PATH", "/Library/Tex/texbin:/usr/local/bin:"+System.getenv("PATH"));                
            //envs.put("PATH", "/Library/Tex/texbin:/usr/local/bin");           
            processBuilder.redirectErrorStream();                
            proc=processBuilder.start();
            */
            
    		if (proc != null) {
    			
    		   BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    		   PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);

    		   //printMessage(" shell="+shell);
    		   
    		   //load environment variables
    		   out.println("source ~/.bashrc");
    		   out.println("source ~/.zshrc");
    		   
    		   //printMessage(" PATH="+System.getenv("PATH"));
    		   
    		   out.println("source "+script);
    		   out.println("pwd");
    		   out.println("exit");
 		       String line="";

 		       boolean isLatexError=false;
 		       while ((line=in.readLine()) != null) { 		    	   
 		    	 //printMessage(line);
 		         //System.out.println(line);
 		    	 if(line.trim().indexOf('?')==0){//try to catch latex error
 		    		//System.out.println("######################test");
 		    		 isLatexError=true;
 		    		break;
 		    	 }
 		       }
     		   
 		       if(isLatexError) {
 		    	   throw new Exception("***Error when converting LaTeX to PDF.\\Please check if there is any issue when compiling LaTeX.");
 		       }
 		        
 		         
 		               
 		       //proc.waitFor();
 		       in.close();
 		       out.close();
 		       proc.destroy();
    		}  
    	    		
    		//try{
            //    checkPDFViewer("acroread");
    		//}catch(Exception e){  			
    		//}

    	}
    	else if(os.contains("windows")){
        	Runtime rt = Runtime.getRuntime();
        	
        	//run cmd command in Java   
        	//
        	//method1: works only when run in the same drive, not work if script in a different drive
        	//
    		//command="cmd /c cd "+nds.Setup.outdir+"&& start "+script+"&exit";
    		//rt.exec(command);


    		/*
        	//
        	//method2: 
        	//
        	command="cmd /c "+script;             //If you do not want to start your process in it's separate console (that's what start does), 
        	                                      //you must wait with p.waitFor() or read it's input stream - otherwise it may silently fail.
    		Process proc=rt.exec(command,         //path to executable
    	            null,                                  // env vars, null means pass parent env
    	            new File(nds.Setup.outdir));

    	    InputStream is=proc.getInputStream();
    	    BufferedReader br= new BufferedReader(new InputStreamReader(is));
 		    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
 		    out.println("exit");
 		   
    	    String line=new String();
    	    while ((line=br.readLine())!=null) {
    	    	//System.out.println (line);
    	    }
    	    br.close();
    	    proc.destroy();
    	    */
    	    
        	//
        	//method2:easiest way
        	//

        	String[] envs=null;
        	//envs=new String[] {"PATH=$PATH:"+System.getenv("PATH")};//didn't work; work after rebooting
        	
        	command="cmd /c start "+script;       //start your process in it's separate console (that's what start does)
    		proc=rt.exec(command,         //path to executable
    	            envs,                         // env vars, null means pass parent env
    	            new File(javands.main.Setup.outdir));  // working directory 
    		  	
    	}
    	           	
    	
    	//delete figure files
    	//wait until the pdf file is created.
    	//Thread.sleep(100);
    	
    	//run.printMessage("\nDeleting MetaPost figure files.\n");
    	//run.deleteFigureFiles();
    	
    	//checkMetaPostOutPut(); 
    	passLaTeX=true;
    }
    
    
    public void setData(MassChain data){this.data=data;}
    public void setEnsdfWraps(EnsdfWrap[] ensw){this.ensw=ensw;}
    public EnsdfWrap[] getEnsdfWraps(){return ensw;}
    
    
    
    public void saveENSDF(Vector<String> lines,String filename)throws FileNotFoundException{
    	String msg=EnsdfUtil.saveENSDF(lines, filename);
		if(!msg.isEmpty()){
			printMessage(msg);
			return;
		}  	
    }
    
    //save an ENSDF dataset into a file
    public void saveENSDF(ENSDF ens,String filename) throws FileNotFoundException{
    	saveENSDF(ens.lines(),filename);
    }
    
    public void saveENSDF(ENSDF ens)throws FileNotFoundException{
    	String nucleus=ens.nucleus().En()+ens.nucleus().A();
    	String filename=nucleus+"_"+DSID2Filename(ens.DSId0())+".ens";
    	String path=Setup.outdir;
    	String folderName="A"+ens.nucleus().A()+"_autosaved_datasets";
    	
    	String s=dirSeparator;   	
        
        filename=path+s+folderName+s+filename;
        
        saveENSDF(ens,filename);
    }
    
    public void saveENSDF(ENSDF ens,String path,String extension)throws FileNotFoundException{
    	EnsdfUtil.saveENSDF(ens, path, extension);
    }
    
    public void saveAllENSDF(MassChain data)throws FileNotFoundException{
    	saveAllENSDF(data,"");
    }
    
    public void saveAllENSDF(MassChain data,String extension)throws FileNotFoundException{
    	int A=data.getA();

    	String path=Setup.outdir;
    	String folderName="A"+A+"_autosaved_datasets";
    	
    	path+=dirSeparator+folderName;
    	
    	saveAllENSDF(data,path,extension);
    }
    
    public void saveAllENSDF(MassChain data,String path,String extension)throws FileNotFoundException{
    	String msg=EnsdfUtil.saveAllENSDF(data, path,extension);
    	
    	if(!msg.isEmpty())
    		printMessage(msg);
    }
    
    /*
     * Set up and create folders for a new mass-chain evaluation from the input mass-chain file
     */
    public void setupEvaluation(MassChain data,String path)throws FileNotFoundException{
    	String msg=EnsdfUtil.setupEvaluation(data, path);
    	if(!msg.isEmpty())
    		printMessage(msg);
    	
    }
    
    public void setupEvaluation(MassChain data)throws FileNotFoundException{
    	setupEvaluation(data,Setup.outdir);
    }
    
    public String DSID2Filename(String dsid){
    	return EnsdfUtil.DSID2Filename(dsid);
    }

    public String DSIDType(String dsid){
    	return EnsdfUtil.findDSIDType(dsid);
    }
    
    public String DecayTypeInDSID(String dsid){
    	return EnsdfUtil.findDecayTypeInDSID(dsid);
    }
    
    
    ////////////////////////////////////////////////////////////////////////
    // Functions to be used in external calling programs for web-display
    ////////////////////////////////////////////////////////////////////////
    
    // must be called first
    public void setOutputDir(String path){
    	javands.main.Setup.outdir=path;
        javands.main.Setup.save();
    }
    
    public void setOutputPath(String path) {
    	setOutputDir(path);
    }
    
    public void  initWebDisplay(){
    	initDict();
    	isForWebDisplay=true;
    }
    
    public void initDict(){
    	try{
        	Translator.init(); //load dictionary
    	} catch (Exception e) {
        	String message="Error when loading dictionary!";
            printMessage("***"+message);
            printMessage("***"+e.getMessage());
            e.printStackTrace(); 
		}
    }
    
    public void loadENSDFDict(String filePath){
    	try{
        	Translator.loadENSDFDict(filePath); //load an ENSDF dictionary
    	} catch (Exception e) {
        	String message="Error when loading ENSDF dictionary!";
            printMessage("***"+message);
            printMessage("***"+e.getMessage());
            e.printStackTrace(); 
		}
    }
    
    public void loadLatexDict(String filePath){
    	try{
        	Translator.loadLatexDict(filePath); //load an LaTeX dictionary
    	} catch (Exception e) {
        	String message="Error when loading LaTeX dictionary!";
            printMessage("***"+message);
            printMessage("***"+e.getMessage());
            e.printStackTrace(); 
		}
    }
    
    //set default output filename
    public void setOutputFilename_old(){
    	if(data==null){
        	String message="Warning: setOutputFilename() is called before data are loaded!";
            printMessage("***"+message);
    		return;
    	}
    	
    	
        if(data.nENSDF()>1 || data.getAbstract().size()>0)
        	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+Integer.toString(data.getA())+".tex";
        else if(data.nENSDF()==1)
        	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+Integer.toString(data.getA())+data.getENSDF(0).nucleus().En()+".tex";
    
    	//debug
    	//System.out.println("In Run line 1525: data size="+data.nENSDF()+"  "+Config.latex);
    }
    
    //set default output filename
    public void setOutputFilename(){
    	if(data==null){
        	String message="Warning: setOutputFilename() is called before data are loaded!";
            printMessage("***"+message);
    		return;
    	}
    	
    	
        if(data.nENSDF()>1 || data.getAbstract().size()>0)
        	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+Integer.toString(data.getA())+".tex";
        else if(data.nENSDF()==1)
        	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+Integer.toString(data.getA())+data.getENSDF(0).nucleus().En()+".tex";
    
    	//debug
    	//System.out.println("In Run line 1525: data size="+data.nENSDF()+"  "+Config.latex);
    }
    
    //set user-defined output filename 
    public void setOutputFilename(String filenamePrefix){
    	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+filenamePrefix+".tex";
    }
    
    //set output filename using DSID of an ENSDF dataset
    public void setOutputFilenameByDSID(ENSDF ens){
    	String nucleus=ens.nucleus().A()+ens.nucleus().En();
    	String filename=nucleus+"_"+DSID2Filename(ens.DSId0());
    	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+filename+".tex";
    }
    
    public void setPDFLinkDir(String dir){
    	NDSConfig.linkDir=dir;
    }
    
    public String getLinkDir(){
    	return NDSConfig.linkDir;
    }
    
    //set output filename using DSID of an ENSDF dataset.
    //used only when a single dataset is loaded.
    //when more than one datasets are loaded (or abstract), 
    //use default filename
    public void setOutputFilenameByDSID(){
    	if(data==null){
        	String message="Warning: setOutputFilenameByDSID() is called before data are loaded!";
            printMessage("***"+message);
    		return;
    	}
    	
    	if(data.nENSDF()!=1){
    		setOutputFilename();
    		return;
    	}
    	
    	ENSDF ens=data.getENSDF(0);
    	String nucleus=ens.nucleus().A()+ens.nucleus().En();
    	String filename=nucleus+"_"+DSID2Filename(ens.DSId0());
    	NDSConfig.latex=javands.main.Setup.outdir+dirSeparator+filename+".tex";
    }
    
    /*return output file name only (no extension and path) if file existing*/
    public String getExistingOutputFilename(){
    	
    	//System.out.println("In Run 1578: config.latex="+Config.latex);
    	
    	File f=new File(NDSConfig.latex);
    	if(f.exists())
    		return Str.fileNamePrefix(f.getName());
    	else
    		return "";
    }
    
    /*return default output file name only (no extension and path)*/
    public String getDefaultOutputFilename(){
    	
    	//System.out.println("In Run 1578: config.latex="+Config.latex);
    	if(NDSConfig.latex.trim().isEmpty())
    		return "temp";
    	
    	File f=new File(NDSConfig.latex);   		
    	return Str.fileNamePrefix(f.getName());

    }
    
    public String getLogFilePath(){
    	//return "";
    	return Setup.outdir+dirSeparator+logFilename;
    }
    
    public void loadENSDF(ArrayList<String> lines,boolean reset){
        try {
			clear();
			if(reset)
				NDSControl.reset();
	        
			NDSControl.isForWebDisplay=isForWebDisplay;//run.isForWebDisplay is set in initWebDisplay()
	        NDSControl.printDocumentRecord=isForWebDisplay;
	        
	    	data=new MassChain(NDSControl.printDocumentRecord);
	    	data.load(lines);
	    	            
            int n=data.nENSDF();

            ensw=new EnsdfWrap[n];
            for(int i=0;i<n;i++){
                ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                ensw[i]=new EnsdfWrap(etd);
            }

            //set default output filename
            setOutputFilename();
            
		} catch (Exception e) {
        	String message="Error when loading ENSDF lines!";
            printMessage("***"+message);
            printMessage("***"+e.getMessage());
            e.printStackTrace(); 
		}

    }
    
    //by default, Control settings are reset when loading a file
    //unless otherwise specified
    public void loadENSDF(ArrayList<String> lines){
    	loadENSDF(lines,true);
    }
    
    public void loadENSDF(File f,boolean reset){
        try {
			clear();
			if(reset)
				NDSControl.reset();
	        NDSControl.isForWebDisplay=isForWebDisplay;
	        NDSControl.printDocumentRecord=isForWebDisplay;
	        
	    	data=new MassChain();
	    	data.load(f);
	    	            
            int n=data.nENSDF();

            ensw=new EnsdfWrap[n];
            for(int i=0;i<n;i++){
                ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                ensw[i]=new EnsdfWrap(etd);
            }

            //set default output filename
            setOutputFilename();
			
		} catch (Exception e) {
        	String message="Error when loading ENSDF file!";
            printMessage("***"+message);
            printMessage("***"+e.getMessage());
            e.printStackTrace(); 
		}
    }
    
    public void loadENSDF(File f){
    	loadENSDF(f,true);
    }
    
	public MassChain loadFile(File file) throws Exception{
		File[] files=new File[1];
		files[0]=file;
		return loadFiles(files);
	}

	public MassChain loadFiles(File[] files) throws Exception{

		infileNamesV.clear();
		
		data=new MassChain();
		
        if(files.length==1){
          	 File dataFile=files[0];
           	 String filePath=dataFile.getAbsolutePath();
           	 printMessage("Loading file: "+filePath);
           	 data.load(dataFile);
           	 
           	 infileNamesV.add(filePath);
       	 
        }else if(files.length>1){
          	 Vector<String> lines=new Vector<String>();
           	 
           	 printMessage("Loading files:");
           	 for(int i=0;i<files.length;i++){
           		 String filePath=files[i].getAbsolutePath();
           		 printMessage("    "+filePath);
           		 lines.addAll(Str.readFile(files[i]));
           		 if(!lines.lastElement().trim().isEmpty())
           			 lines.addElement("    \n");
           		 
           		 infileNamesV.add(filePath);
           		 
           		 //System.out.println("In MasterFrame line 362: lastline new line="+lines.lastElement().trim().indexOf("\n")+"  *"+lines.lastElement()+"*");
           	 }
           	 
           	 data.load(lines);

        }
   
                                        
        printMessage("Done loading");
        
        return data;

	}
	
    /*
     * convert ENSDF to LaTeX only (not running script to generate PDF)
     */
    public boolean convertToLaTeX(){
    	
    	String message="";
        boolean validOutdir=true;
        if(javands.main.Setup.outdir.trim().length()==0){
        	message="Error: output path is empty. ";           
        	validOutdir=false;
        }else{ 
            File f=new File(javands.main.Setup.outdir.trim());
        	if(!f.exists()){
        		message="Error: output path does not exist. ";
            	validOutdir=false;
        	}
        }
        
        if(!validOutdir){
        	message+="Please specify output path.";
        	printMessage(message);
            return false;
        }
          	        
        try{   	     
            writeLatex(NDSConfig.latex,data);        	        	
        }catch(Exception e){
        	message="***Error when writing LaTeX output file.";
        	printMessage(message);
        	e.printStackTrace();
        	return false;
        }
        
        return true;
    }
    
    public boolean convert(){
    	    	
    	String message="";
  
    	if(!convertToLaTeX())
    		return false;
    	
    	
    	//run script to generate PDF output
        try{   	      	
        	runScript();
        }catch(Exception e){
        	message="***Error when converting LaTeX to PDF.";
        	printMessage(message);
			String msg=e.getMessage();
			if(msg.length()>0) {
				printMessage(msg);						         
			}
        	e.printStackTrace();
        	return false;
        }
        
        return true;
    }
    
    public boolean convert(ArrayList<String> lines,boolean reset) throws InterruptedException{
    	return convert(lines,"DSID",reset);
    }
    
    public boolean convert(ArrayList<String> lines) throws InterruptedException{
    	return convert(lines,true);
    }

    
    //reset=true, reset Control settings
    public boolean convert(ArrayList<String> lines,String outfilename,boolean reset) throws InterruptedException{
    	loadENSDF(lines,reset);
    	
    	if(outfilename.trim().length()==0)
    		setOutputFilename();
    	else if(outfilename.toUpperCase().equals("DSID"))
    		setOutputFilenameByDSID();
    	else
    		setOutputFilename(outfilename);
    	
    	long startTime=System.currentTimeMillis();
    	
    	//There are cases in Linux that the files created in convert() has time < startTime (differences is small).
        //Those differences can be considered within the error range when two events start at very close time.
    	//To separate the two events, add a small delay
    	Thread.sleep(200);
    	
    	convert();//new PDF file is created

    	boolean success=waitLaTeX(startTime);//if the existing PDF file is created after startTime, no need to wait
    	                                     //otherwise, wait until the new PDF file is created.
     	
    	if(success)
    		cleanupFiles();
    	
    	return success;
    }
    
    //by default, Control settings are reset, unless otherwise specified.
    public boolean convert(ArrayList<String> lines,String outfilename) throws InterruptedException{
    	return convert(lines,outfilename,true);
    }
    
    public boolean convertForWeb(ArrayList<String> lines,String outfilename){

    	loadENSDF(lines);//note that in this call all settings in Control are reset to default values
    	
    	NDSControl.isForWebDisplay=true;

    	if(outfilename.trim().length()==0)
    		setOutputFilename();
    	else if(outfilename.toUpperCase().equals("DSID"))
    		setOutputFilenameByDSID();
    	else
    		setOutputFilename(outfilename);
    	
    	long startTime=System.currentTimeMillis();
    	
    	
    	convert();//new PDF file is created
    	
    	boolean success=waitLaTeX(startTime);//if the existing PDF file is created after startTime, no need to wait
    	                                     //otherwise, wait until the new PDF file is created.
    	if(success)
    		cleanupFiles();
    	
    	return success;
    }
    
    
    public boolean convertTest(ArrayList<String> lines){
    	return convertTest(lines,"DSID");
    }
    
    public boolean convertTest(ArrayList<String> lines,String outfilename){
    	loadENSDF(lines);
    	
    	if(outfilename.trim().length()==0)
    		setOutputFilename();
    	else if(outfilename.toUpperCase().equals("DSID"))
    		setOutputFilenameByDSID();
    	else
    		setOutputFilename(outfilename);
    	
    	long startTime=System.currentTimeMillis();
    	
    	convert();//new PDF file is created
    	
    	boolean success=false;
    	//success=waitLaTeX(startTime);//if the existing PDF file is created after startTime, no need to wait
    	                   //otherwise, wait until the new PDF file is created.
    	success=isFileUpdated(outfilename+".pdf",startTime-1000);
    	
    	//cleanupFiles();
    	
    	return success;
    }
    
    
    //reset=true, reset Control settings; no extension is needed in outfilename
    public boolean convertToLaTeX(ArrayList<String> lines,String outfilenamePrefix,boolean reset) throws InterruptedException{
    	loadENSDF(lines,reset);
    	
    	if(outfilenamePrefix.trim().length()==0)
    		setOutputFilename();
    	else if(outfilenamePrefix.toUpperCase().equals("DSID"))
    		setOutputFilenameByDSID();
    	else
    		setOutputFilename(outfilenamePrefix);
    	
    	return convertToLaTeX();//new PDF file is created

    }
    
    
    public void deleteFile(String fileName,String path){
    	File f;
    	f=new File(path+dirSeparator+fileName);
    				
        if(f.exists())
        	f.delete();
    }
    
    public void deleteFiles(Vector<String> fileNames,String path){
    	for(int i=0;i<fileNames.size();i++){
			deleteFile(fileNames.get(i),path);

    	}
    }
    
    public void deleteFigureFiles(){
		
    	if(tbl!=null && tbl.getFigureFiles().size()>0)
    		deleteFiles(tbl.getFigureFiles(),javands.main.Setup.outdir);
    }

    public void deleteLaTeXFiles(){
    	deleteLaTeXFiles(true);
    }
    
    public void deleteLaTeXFiles(boolean deleteTex){
    	deleteLaTeXFiles(LaTeXFilename,deleteTex);
    }
    
    public void deleteLaTeXFiles(String filename){
    	deleteLaTeXFiles(filename,true);
    }
    
    public void deleteAllLaTeXFiles(){
    	deleteLaTeXFiles();
    }
    
    public void deleteAllLaTeXFiles(String filename){
    	deleteLaTeXFiles(filename,true);
    }
    
    public void deleteLaTeXFiles(String filename,boolean deleteTex){
    	//delete LaTeX files
    	ArrayList<String> fileExtensions=new ArrayList<String>(Arrays.asList("aux","dvi","log","out","ps","toc"));
    	String name=Str.fileNamePrefix(filename);
    	Vector<String> filenames=new Vector<String>();
    	for(String s:fileExtensions)
    		filenames.add(name+"."+s);
    	
    	if(deleteTex)
    		filenames.add(name+".tex");
    	
    	deleteFiles(filenames,javands.main.Setup.outdir);
    }
    
    /* delete all generated files except for the PDF file*/
    public void cleanupFiles(){
    	deleteFigureFiles();
    	
    	deleteFile(scriptFilename,javands.main.Setup.outdir);
    	
    	deleteFile("control",javands.main.Setup.outdir);
    	
        deleteLaTeXFiles();//delete all LaTeX files excluding pdf and tex files
        
        deleteFile("dummy.txt",javands.main.Setup.outdir);
        
        cleanupDocumentFiles();
    }
    

    private void cleanupDocumentFiles(){
    	int n=documentFilenames.size();
    	if(n<=0)
    		return;
    	
    	for(int i=0;i<n;i++)
    		deleteLaTeXFiles(documentFilenames.get(i));
    }
    
    public void cleanupFilesWithMessage(long startTime){
		 try{
			 
			 //long startTime=System.currentTimeMillis();
			 
			 //first, check if those auxiliary (e.g., LaTex file) files exist
		     String pdfName=this.getExistingOutputFilename().trim(); //return the LaTeX filename without extension.
		     if(pdfName.length()<=0)//auxiliary files do not exist, no need to clean
		    	 return;

			 
		     boolean success=waitLaTeX(startTime);

		     printMessage("\nDeleting auxiliary files (figure, LaTeX, etc.)... ");
			 
		     if(success){
		    	//run.deleteFigureFiles();
				 
				cleanupFiles();//delete all generated files except for the pdf

				printMessage("***You will need to hit \"Create LaTeX\" again to generate");
				printMessage("     those auxiliary files in order to create PDF again.");
				printMessage("Done.\n"); 
		     }else{
		    	printMessage("***Auxiliary files can't be deleted or do not exist."); 
		    	//printMessage("** Try \"Create PDF with figure files\".");
		     }
			 
		 }catch(Exception e){
			 printMessage("***Error when clean up auxiliary files.\n");	
			 e.printStackTrace();
		 }
    }
    
    //wait until a new PDF file is created
    //Alternative solution is to use a second thread for creating the PDF and wait until
    //that thread terminates. But still the major issue is to handle the LaTeX errors and
    //to determine if a PDF has been successfully created.
    public void waitLaTeX_old(long startTime){
        String filename=Setup.outdir+dirSeparator+getExistingOutputFilename()+".pdf";
        File f=new File(filename);
        
        long oldPDFTime=0,newPDFTime=0;//for PDF file
        long oldDVITime=0,newDVITime=0;//for dvi file
        long latexLogTime=0;//for LaTex log file
        

        
        int totalTime=0;
        int interval=200;//200 ms
        boolean updating=false;
        
        try{
            while(Thread.currentThread().isAlive()){
            	
            	//debug
            	//System.out.println("@@@In Run.java line 1704: currentThread="+Thread.currentThread().getName()+" time="+totalTime/1000+" "+(oldTime>=startTime));
            	//System.out.println("       name="+filename+" start="+startTime+" old time="+oldPDFTime+" new time="+newPDFTime+" current time="+System.currentTimeMillis());
            	
            	Thread.sleep(interval);
            	totalTime+=interval;
            	
            	f=new File(filename);
            	if(f.exists()){
                	newPDFTime=f.lastModified();   
                	
                	//debug
                	//System.out.println("***In Run.java line 1713: currentThread="+Thread.currentThread().getName()+" time="+totalTime/1000);
                	//System.out.println("       name="+filename+" old time="+oldPDFTime+" new time="+newPDFTime+" "+(newPDFTime>oldPDFTime));
                	
                	
                	if(newPDFTime>oldPDFTime){
                	    if(newPDFTime>=startTime){
                	    	updating=true;
                	    }
                		
                		oldPDFTime=newPDFTime;
                		continue;
                	}else{
                    	if(updating){
                    		Thread.sleep(500);
                    		break;
                    	}else if(totalTime<2000){//wait for at most 2 second for a new pdf file to start being created
                    		continue;
                    	}
                    	
                	}
                	               	
            	}
            	
            	File log=new File(Setup.outdir+dirSeparator+getExistingOutputFilename()+".log");
            	if(log.exists()){
            		long t=log.lastModified();
            		
            		//debug
            		//System.out.println("In Run.java line 1774: latexlogtime="+latexLogTime+" new log time="+t);
            		
            		if(t>latexLogTime){//latex logfile is being updated
                		latexLogTime=t;
            			continue;
            		}
            		else{ //latex logfile is not updated. Something is wrong in latexing and it stops,
            			  //or the PDF file is created, but in this case the program will not reach here,
            			  //or the logfile is written completely but the PDF file is still in creation.
            			
            			if(t<startTime && totalTime<2000)//wait for at most 2 second for a new log file to start being created
            				continue;
            			
            			//reaching here, a pdf file is still not created but the log file stops updating, then check if a dvi file has been created
            			File dvi=new File(Setup.outdir+dirSeparator+getExistingOutputFilename()+".ps");
            			if(dvi.exists()){
                			newDVITime=dvi.lastModified();
                			if(newDVITime>oldDVITime){//dvi file is created, but pdf file is still in creation.
                				oldDVITime=newDVITime;
                				continue;
                			}else{
                            	if(oldDVITime>startTime){
                            		Thread.sleep(500);
                            		break;
                            	}else if(totalTime<2000){//wait for at most 2 second for a new DVI file to start being created
                            		continue;
                            	}
                			}
            			}

            	
            			//System.out.println(f.exists()+"  "+f.length()+" "+updating+" "+totalTime);
            			
            			//another time buffer, wait another 10 seconds
            			if((!f.exists() || f.length()==0 || !updating) && totalTime<10000)
            				continue;
            				
            			//when reaching there, something is wrong in running latex
            			printMessage("***Error when converting LaTeX to PDF.");
            			printMessage("   Please check the error by manually running the script.");
            			break;
            		}
            	}
            	//else
            	//	break;
            	
            	//totalTime+=interval;
            	//if(totalTime/1000>1000)//should not happen unless a new PDF is not created
            	//	break;
            	
            	
            
            }
        }catch(Exception e){
        	
        }

        
    }
    
    /*check if a new PDF file is created. If not, something is wrong in converting LaTeX to PDF*/
    public void checkPDF(){
    	
    }
    
    
    /////////////////
    public boolean waitLaTeX(long startTime){
        String filename=Setup.outdir+dirSeparator+getExistingOutputFilename();
        
        File pdf,log,dvi,dum;
        
        long oldPDFTime=0,PDFIdleTime=0;//for PDF file
        long oldDVITime=0,DVIIdleTime=0;//for dvi file
        long oldLOGTime=0,LOGIdleTime=0;//for LaTex log file
        long oldDUMTime=0,newDUMTime=0;//for a dummy log file, used as a check point to check if PDF file fully created.

        int totalTime=0;
        int interval=200;//200 ms
        boolean updating=false;
        boolean isLOGUpdated=false,isDVIUpdated=false,isPDFUpdated=false;
        boolean fail=false;
        
        try{
        	
        	
        	//debug
        	System.out.println("Waiting for PDF to be created...");
        	
        	log=new File(filename+".log");
        	dvi=new File(filename+".dvi");
        	pdf=new File(filename+".pdf");
        	dum=new File(Setup.outdir+dirSeparator()+"dummy.txt");
        	
        	
        	if(log.exists()) oldLOGTime=log.lastModified();
        	if(dvi.exists()) oldDVITime=dvi.lastModified();
        	if(pdf.exists()) oldPDFTime=pdf.lastModified();
        	if(dum.exists()) oldDUMTime=dum.lastModified();
        	
        	
            while(Thread.currentThread().isAlive()){
            	
            	//debug
            	//System.out.println("@@@In Run.java line 1704: currentThread="+Thread.currentThread().getName()+" time="+totalTime/1000);
            	//System.out.println("       name="+filename+" start="+startTime+" old time="+oldPDFTime+" current time="+System.currentTimeMillis());
            	
            	
            	Thread.sleep(interval);
            	totalTime+=interval;
            	
            	updating=false;
            	
            	log=new File(filename+".log");
            	dvi=new File(filename+".dvi");
            	pdf=new File(filename+".pdf");
            	dum=new File(Setup.outdir+dirSeparator()+"dummy.txt");
            	
            	
            	if(log.exists() && isFileUpdated(log,oldLOGTime)){
            		oldLOGTime=log.lastModified();
            		updating=true;
            		isLOGUpdated=true;
            		LOGIdleTime=0;
            	}else if(dvi.exists() && isFileUpdated(dvi,oldDVITime)){
            		oldDVITime=dvi.lastModified();
            		updating=true;
            		isDVIUpdated=true;
            		DVIIdleTime=0;
            	}else if(pdf.exists() && isFileUpdated(pdf,oldPDFTime)){
            		oldPDFTime=pdf.lastModified();
            		updating=true;
            		isPDFUpdated=true;
            		PDFIdleTime=0;
            	}else{
            	}            		
            	
        		if(oldLOGTime>=startTime && log.length()>0)
        			isLOGUpdated=true;
        		if(oldDVITime>=startTime && dvi.length()>0)
        			isDVIUpdated=true;
        		if(oldPDFTime>=startTime && pdf.length()>0)
        			isPDFUpdated=true;
        		
            	//debug
            	//System.out.println("\nIn Run line 1915: total time="+totalTime+" updating="+updating);
            	//System.out.println(" exist    :  pdf="+pdf.exists()+" dvi="+dvi.exists()+" log="+log.exists()+" dummy="+dum.exists());
            	//System.out.println(" isUpdated:  pdf="+isPDFUpdated+" dvi="+isDVIUpdated+" log="+isLOGUpdated);
            	//System.out.println(" idle time:  pdf="+PDFIdleTime/1000+" dvi="+DVIIdleTime/1000+" log="+LOGIdleTime/1000);            	
            	//System.out.println(" old time :  pdf="+oldPDFTime+" dvi="+oldDVITime+" log="+oldLOGTime+" dummmy="+dum.lastModified()+" start time="+startTime);  
            	//System.out.println(" last time:  pdf="+pdf.lastModified()+" dvi="+dvi.lastModified()+" log="+log.lastModified());  
            	//System.out.println(" time diff:  pdf="+(oldPDFTime-startTime)+" dvi="+(oldDVITime-startTime)+" log="+(oldLOGTime-startTime)+" dummy="+(dum.lastModified()-startTime));
            	//System.out.println(" size     :  pdf="+pdf.length()+" dvi="+dvi.length()+" log="+log.length()); 
            	
            	if(updating){	
            		//debug
            		//System.out.println("***case 1: totalTime="+totalTime/1000);
            		
        			if(totalTime/1000>1000){//should never be reached; set to avoid infinite loop in case 
        				fail=true;
        				break;
        			}
            		continue;
            	}else if(!dum.exists() || dum.lastModified()<startTime){
            		if(isPDFUpdated){//in the process of ps to pdf, when a pdf is eventually fully created, a dummy file will be created
            			PDFIdleTime+=interval;

            			//debug
                		//System.out.println("***case 2: PDFIdleTime="+PDFIdleTime/1000);

            			if(PDFIdleTime/1000>200){//should never be reached; set to avoid infinite loop in case 
            				fail=true;
            				break;
            			}
            			
            			continue;
            		}else if(isDVIUpdated){//in the process of latex to dvi
            			DVIIdleTime+=interval;
            			
                		//debug
                		//System.out.println("***case 3: DVIIdleTime="+DVIIdleTime/1000);

            			if(DVIIdleTime/1000>100){//should never be reached; set to avoid infinite loop in case 
            				fail=true;
            				break;
            			}
            			
            			continue;
            		}else if(isLOGUpdated){
            			LOGIdleTime+=interval;
            			
                		//debug
                		//System.out.println("***case 4: LOGIdleTime="+LOGIdleTime/1000);

            			if(LOGIdleTime/1000>2){//if reaching here, something is wrong 
            				fail=true;
            				break;
            			}
            			
            			continue;
            		}else{
                		//debug
                		//System.out.println("***case 5: totalTime="+totalTime/1000);

            			//There are situations that the created times for new PDF,LOG files are before startTime!
            			//The differences are seen so far in the range of ~100-~500 ms.
            			//Haven't figured out why. Note that these are for small files that are processed very quickly.
            			//For these cases, the PDF is created successfully.
            			if(oldPDFTime==oldLOGTime && oldLOGTime<startTime && oldLOGTime>startTime-2000)
            				break;

                		//System.out.println("***case 5.1: totalTime="+totalTime/1000);

            			//nothing has been created yet
            			//wait at most for 10 seconds for log file to start to be created
            			if(totalTime/1000<10) 
            				continue;
            			else if(totalTime/1000<50 && data.nLines()<2000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<100 && data.nLines()<4000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<150 && data.nLines()<6000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<200 && data.nLines()<8000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<250 && data.nLines()<10000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<300 && data.nLines()<12000)//still nothing, wait a little bit longer
            				continue;
            			else if(totalTime/1000<500 && data.nLines()<30000)//still nothing, wait a little bit longer
            				continue;
            			
                		//System.out.println("***case 5.2: totalTime="+totalTime/1000);

            			//when reaching there, something is wrong in running latex
                        fail=true;
            			break;
            		}
            		
            	}else if(!isPDFUpdated || !isDVIUpdated){
            	    fail=true;
            	    break;
            	}else{//when reaching here, a PDF file is fully created and now it is safe to delete all figure files
            		fail=false;
            		break;
            	}
            	           	            
            }//end while
            
            if(fail){
            	//System.out.println(" idle time:  pdf="+PDFIdleTime/1000+" dvi="+DVIIdleTime/1000+" log="+LOGIdleTime/1000);  
            	
    			//when reaching there, something is wrong in running latex
    			printMessage("Something possibly wrong when converting LaTeX to PDF.");
    			printMessage("Please check it by manually running the script if PDF not created.");
            }else{
            	printMessage("PDF file is successfully created!");
            }
        }catch(Exception e){
        	fail=true;
        	printMessage("Failed to convert LaTeX to PDF.");
        }

        return !fail;
    }
    
    boolean isFileUpdated(String filepath,long oldTime){
    	try{
    		File f;
    		if(filepath.contains("/") || filepath.contains("\\"))
    			f=new File(filepath);
    		else
    			f=new File(Setup.outdir+dirSeparator+filepath);
    		
    		return isFileUpdated(f,oldTime);
    		
    	}catch(Exception e){
    		return false;
    	}
    }
    
    boolean isFileUpdated(File f,long oldTime){
    	try{
    		if(f.lastModified()>oldTime && f.length()>0)
    			return true;
    		
    		return false;
    	}catch(Exception e){
    		return false;
    	}
    }
    
	public Vector<String> infileNamesV(){return infileNamesV;}
	
    public int getDataNLines(){
    	if(data!=null)
    		return data.nLines();
    	
    	return 0;
    }
    /*return a vector of all lines in the "log.txt" file*/
    public Vector<String> getLogs(){
    	Vector<String> logs=new Vector<String>();
    	String logPath=Setup.outdir+dirSeparator+"log.txt";

    	File f=new File(logPath);
    	if(f.exists()){
    		try {
				logs=Str.readFile(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return logs;
    }
    
    /* return a vector of all lines in the "log.txt" file 
     * if there is any error message in the file, otherwise
     * it returns an empty vector.
     */
    public Vector<String> getErrorLogs(){
        Vector<String> logs=getLogs();
        String s="";
        for(int j=0;j<logs.size();j++){
        	s+=logs.get(j)+"\n";            	          	            		            	
        }
        
        if(s.toUpperCase().indexOf("PROBLEM")>=0 || s.toUpperCase().indexOf("ERROR")>=0)  
        	return logs;
        else
        	return new Vector<String>();
    }
    
    /*return a string containing all lines in the "log.txt" file concatenated by "\n"*/
    public String printLogs(){
        Vector<String> logs=getLogs();
        String s="";
        for(int j=0;j<logs.size();j++){
        	s+=logs.get(j)+"\n";            	          	            		            	
        }
        
        return s;
    }
    /*return a string containing all lines in the "log.txt" file concatenated by "\n",
     *only when there is any error message in the log file, otherwise return an empty string. 
     */
    public String printErrorLogs(){
        Vector<String> logs=getErrorLogs();
        String s="";
        for(int j=0;j<logs.size();j++){
        	s+=logs.get(j)+"\n";            	          	            		            	
        }

        return s;
    }
   
    public void checkPDFViewer(String pdfViewer) throws Exception{
		String command="which";
		if(isWindows())
			command="where";
		
		Process proc=Runtime.getRuntime().exec(command+" "+pdfViewer);
		try {
			proc.waitFor();
    		if(proc.exitValue()!=0){
    			printMessage("\n\nWarning: The default PDF viewer \""+pdfViewer+"\" doesn't exist!\nPlease try opening the PDF file manually using a different viewer.\n");
    		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		proc.destroy();
    }
        
    public void debug(String text){
    	Str.debug(text);
    }
    
    public String usage(){
    	String s="";
    	s+="------------------------------------------------------------------------------------    \n";
    	s+="To use JAVA_NDS (for executable=JAVA_NDS.jar) in a command line:                        \n";
    	s+="    java -jar JAVA_NDS.jar PATH_TO_ENSDF_FILE OUTFILE_PATH [-OPTION1 -OPTION2 ...]      \n";
    	s+=" or java -jar JAVA_NDS.jar [-OPTION1 -OPTION2 ...] to open the program                  \n";
    	s+="                                                                                        \n";
    	s+="-OPTION1, -OPTION2,..., are following:                                                  \n";
    	s+="    -nodrawing    : to suppress all drawings in output                                  \n";
    	s+="                    (all drawings are generated by default)                             \n";
    	s+="    -reference    : to include a reference list at the end                              \n";
    	s+="    -nopdf        : to generate only LaTeX output                                       \n";
    	s+="                    (by default a script will be run to generate PDF)                   \n";
    	s+="    -namebydsid   : to name output file by DSID in input file                           \n";
    	s+="                    same can be achieved by using OUTFILE_PATH=DSID                     \n";
    	s+="    -noredirect   : to keep all running messages in terminal instead of redirecting     \n";
    	s+="                    (by default all messages are written into a log.txt file)           \n";
    	s+="    -nsrlogin=path: to access NSR database using login information in the file in path  \n";
    	s+="                    (by default or failed access, use simulating webpage search)        \n";
    	s+="    -JDBConly     : to obtain NSR information from database using JDBC only to skip     \n";
    	s+="                    simulating webpage search if JDBC fails                             \n";
    	s+="                    (works only if -nsrlogin=path is also given.)                       \n";
    	s+="    -silent       : to suppress messages when connecting to NSR database by JDBC        \n";
    	s+="                    (works only if -nsrlogin=path is also given.)                       \n";
    	s+="    -WORKDIR=path : to set the working folder (output path) to be the given path        \n";
    	s+="    -CURRDIR      : to set the working folder (output path) to be the current folder    \n";
    	s+="    -help         : to print usage                                                      \n";
    	s+="    -usage        : same as -help                                                       \n";
    	s+="------------------------------------------------------------------------------------    \n";
    	
    	return s;
    }
    
    public String version(){
        //Date date=new Date();
        //SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy");
    	//return "Version 1.5: last update on "+sdf.format(date);
    	
    	return JavaNDSControl.version;
    }

	public String title() {
		return JavaNDSControl.title;
	}

	@Override
	public void load(Vector<String> lines) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String printDeclaration(String indent) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return JavaNDSControl.name;
	}

	@Override
	public String outdir() {
		// TODO Auto-generated method stub
		return Setup.outdir;
	}

}

            