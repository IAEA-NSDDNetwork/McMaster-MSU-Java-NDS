/*
 * Created 2007 Roy Zywina
 * Updated June 9 2011 Jeremie Choquette
 * Updated 2015 Jun Chen
 * 
 * 11/23/2018: write a new much faster method for downloading NSR information from NSR database for all
 *             keynumbers in the input mass-chain file at one time (hundreds times faster than the old method
 *             using the NSRLink manager which processes a single keynumber each time). See Reference.java
 *             downloadNSR() for more details.
 * 12/03/2018: add more options for usage in command line, including an option to load NSR-database login 
 *             information in a text file for direct access to NSR base. (username and password needed).
 *             See run.usage().
 * 03/19/2019: add code in findBreaks() to make a second try, when an attempt to split further scanned lines 
 *             that were splitable before further scanning, by removing last two lines, since for some cases,  
 *             it is the last line(s) that only has comment column that makes the whole table not splitable.
 */
package javands.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.*;

import ensdfparser.ensdf.ENSDF;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

/**
 * Program entry point.
 */
public class Main {
    
    /**main file, essentially just initializes things and then opens up the GUI */
    public static void main(String[] args)throws Exception{
        Setup.load();
        Translator.init();
        
        //testAPI();
        //countBlocks();
		//args=new String[] {"H:\\work\\evaluation\\ENSDF\\check\\check.ens","JAVA_NDS"};
		
    	if(args.length==0)
    		startUI();
    	else
    		runByCommand(args);
    }
    
    public static void startUI() throws IOException{
    	if(!Translator.hasInit())
    		Translator.init();
        
        //Translator.test();

        //launch master control panel
        try{
       	
        	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        	//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }catch(Exception e1){
            try{
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                	
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                    else{
                    	UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                    }
                }
                
            }catch(Exception e2){
            	
            }        	
        }
        

        javands.ui.MasterFrame frame=new javands.ui.MasterFrame();
        frame.setNameAndVersion(JavaNDSControl.name, JavaNDSControl.version);
        
        frame.setTitle("Nuclear Data Sheets Production Program");
    	
        frame.setVisible(true);
        frame.setResizable(false);
    }
    
    static void testAPI() throws Exception{
        Run run=new Run();
        run.initWebDisplay();
             

        String baseOutDir="./out";
        //String basePDFUrl="http://www.nndc.bnl.gov/ensnds";
        String basePDFUrl="/home/junchen/work/evaluation/mytools/JAVA_NDS_2015_NIMBUS/out";

        
        String outDir=baseOutDir;
        String PDFUrl=basePDFUrl;
        
        String dsid,A,en,Nuc;
        ArrayList<String> lines=new ArrayList<String>();       
        Vector<String> currentXREFs=new Vector<String>();
        
        //run.loadENSDFDict("./test/local_dic2.dat");
        
        PrintWriter runLog=new PrintWriter(new FileOutputStream(new File(baseOutDir+"/run.log")),true); //gloabl log file
        
        PrintWriter missingFiles=new PrintWriter(new FileOutputStream(new File(baseOutDir+"/missingFiles.lst")),true);
        
    	long startTime,endTime,tempStart,tempEnd;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	tempStart=startTime;
    	
    	boolean useTestFile=true;
    	
    	if(useTestFile){
            File f=new File("./test/test.ens");    
            run.loadENSDF(f);
            lines.addAll(ensdfparser.nds.util.Str.readFile(f));
            runLog.println("Run log for processing file:"+f.getAbsolutePath()+"\n");
    	}else{
            String filePath;
            
            ////////////////
            int start=92;
            int end=92;
            ////////////////
            
            runLog.println("Run log for processing files: mass="+start+" to "+end+"\n");      
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("E MM/dd/yyyy 'at' hh:mm:ss a zzz");
            runLog.println("Generated at: "+sdf.format(date));
            
            
            for(int mass=start;mass<=end;mass++){
            	
            	filePath="./test/ensdf_170501";
            	
            	if(mass<10)
            		filePath+="/ensdf.00"+mass;
            	else if(mass<100)
            		filePath+="/ensdf.0"+mass;
            	else 
            		filePath+="/ensdf."+mass;
            	
            	File f=new File(filePath);
            	if(!f.exists()){
            		runLog.println("Warning: file for mass="+String.format("%3s", mass)+" does not exist! Skip it.");
            		continue;
            	}
            	
            	lines.addAll(ensdfparser.nds.util.Str.readFile(f));
            	lines.add("");
            }
    	}  
        
        Vector<Vector<String>> blocks=Str.splitStringBlock(lines);
             
       
        
        //nds.ui.EnsdfWrap[] ews=run.getEnsdfWraps();
        int ngood=0,nbad=0;
        int ngoodTotal=0,nbadTotal=0;
        String prevA="",prevNuc="";
        Vector<String> currentDSIDs=new Vector<String>();
        
        for(int i=0;i<blocks.size();i++){
        	        	
        	ArrayList<String> list=new ArrayList<String>();
        	list.addAll(blocks.get(i));
        	//nds.ui.EnsdfWrap ew=ews[i];
        	//String dsid=ew.getENSDF().DSId();
        	
        	dsid=list.get(0).substring(9,39).trim();
        	A=list.get(0).substring(0,3).trim();
        	en=list.get(0).substring(3,5).trim().toUpperCase();
        	Nuc=list.get(0).substring(0,5);
        	
        	if(en.length()>1)
        		en=en.substring(0, 1)+en.toLowerCase().charAt(1);
        	
        	if(A.length()>0){
        		outDir=baseOutDir+"/"+A;
        		PDFUrl=basePDFUrl+"/"+A;
        		if(en.length()>0){
            		outDir=outDir+"/"+en;
            		PDFUrl=PDFUrl+"/"+en;
        		}      	    
        	}
        	
        			
            run.setOutputDir(outDir);
            run.setPDFLinkDir(PDFUrl);
            
            File dir=new File(outDir);
            if(!dir.exists()){
            	dir.mkdirs();
            	if(!dir.exists()){
            		runLog.println("Something is wrong! Can't create directory:"+outDir);
            		runLog.close();
            		missingFiles.close();
            		return;
            	}
            }
            
            if(!A.equals(prevA)){
            	
                if(prevA.length()>0){
                	tempEnd=System.currentTimeMillis();
                	runLog.println("*** End processing mass="+prevA);
                	runLog.println("*** Total "+(ngood+nbad)+" blocks, "+ngood+" success, "+nbad+" fail");
                	runLog.println("*** Processing time: "+String.format("%.3f", (float)(tempEnd-tempStart)/1000)+" seconds");
                }
            	
            	runLog.println("\n*** Processing mass="+A);
            	tempStart=System.currentTimeMillis();
            	ngood=0;
            	nbad=0;
            }
            	
            

            /////////////////
            //There could be mismatch between DSID in XREF list of an Adopted dataset and DSID in individual dataset.
            //That will result in the XREF link in the pdf file of Adopted dataset failing to work. To solve this,
            //assuming the Adopted dataset always processed first, check the XREFs from the Adopted dataset when 
            //processing each individual dataset, if there is a mismatch, use the DSID in the XREF list to name the
            //pdf file.
            
            ensdfparser.ensdf.ENSDF ens = new ENSDF();
            if(!en.isEmpty()){
                ens.setValues((Vector<String>)blocks.get(i));   
            }else{
            	ens.setValuesHeaderOnly((Vector<String>)blocks.get(i));  
            }
            dsid=ens.DSId0();
            
            //System.out.println(" EN="+en+" DSID="+dsid);
            //System.out.println("****"+run.DSID2Filename(dsid));
            
            
            //check if the dataset of each XREF exists
            if(!Nuc.equals(prevNuc) || i==blocks.size()-1){
            	for(int x=0;x<currentXREFs.size();x++){
            		String xref=currentXREFs.get(x);
            		if(!currentDSIDs.contains(xref))
            			missingFiles.println(prevNuc+": "+xref);
            	}
            }
            
            if(!Nuc.equals(prevNuc)){
            	currentXREFs.clear();
            	currentDSIDs.clear();
            }
            
            if(dsid.contains("ADOPTED LEVELS") && ens.nXRefs()>0){
            	for(int x=0;x<ens.nXRefs();x++)
            		currentXREFs.add(ens.xRefAt(x).DSId());               
            }           


            if(dsid.length()>0)
            	currentDSIDs.add(dsid);
            
            String altDSID="";
            if(currentXREFs.size()>0 && !currentXREFs.contains(dsid)){
            	for(int x=0;x<currentXREFs.size();x++){
            		String xref=currentXREFs.get(x);
            		if(dsid.indexOf(xref)==0 && xref.length()>altDSID.length()){//usually it is the XREF DSID that is incomplete
            			altDSID=xref;
            			//System.out.println("XREF="+xref);
            			//System.out.println("dsid="+dsid);
            			//System.out.println("  altDSID="+altDSID);
            		}

            	}
            	
            	//usually the mismatch happens because of the use of continuation DSID line 
            	//in the dataset, while only the first line DSID is used as XREF
            	if(altDSID.length()>28)
            		dsid=altDSID;
            	else if(!dsid.contains("ADOPTED"))
            		runLog.println("Warning: no matched DSID in XREF for dataset DSID="+dsid);
            }
            

            //System.out.println("PDF name="+dsid);
            
            ////////////////
            
            boolean success=false;
            success=run.convert(list, run.DSID2Filename(dsid));
            
            
            if(!success){
            	runLog.println("PDF is not created for: "+A+en+":"+dsid);
            	nbad++;
            	nbadTotal++;
            }else{
            	runLog.println("PDF is created for: "+A+en+":"+dsid);
            	ngood++;
            	ngoodTotal++;
            }


            
            String s=run.printErrorLogs();
            if(s.length()>0)
            	runLog.println(s);
            
            if(i==blocks.size()-1){
            	tempEnd=System.currentTimeMillis();
            	runLog.println("*** End processing mass="+A);
            	runLog.println("*** Total "+(ngood+nbad)+" blocks, "+ngood+" success, "+nbad+" fail");
            	runLog.println("*** Processing time: "+String.format("%.3f", (float)(tempEnd-tempStart)/1000)+" seconds");
            }
            	
            prevA=A;
            prevNuc=Nuc;
        }

        
        runLog.println("\nTotal "+blocks.size()+" blocks, "+ngoodTotal+" success, "+nbadTotal+" fail");
        
        endTime=System.currentTimeMillis();
        timeElapsed=(float)(endTime-startTime)/1000;
        runLog.println("Time elapsed: "+String.format("%.3f", timeElapsed)+" seconds");
        
        runLog.close();
        missingFiles.close();
    
        //run.loadENSDF(list);
        //long startTime=System.currentTimeMillis();      
        //run.convert();    
        //run.waitLaTeX(startTime);
        //run.cleanupFiles();
    }
        
    
    @SuppressWarnings("unused")
	public static void runByCommand(String[] args) throws Exception{

        //Control settings are reset in Run() constructor
    	//Setup.load();
    	
        Run run=new Run();             
        run.redirectOutputToFile(false);

        System.out.println("---------------------------------------------------------------------------");
        System.out.println(run.version());
        
    	System.out.println("Start convertENSDF(args): for usage, run with -USAGE argument");
        if(args.length==0){
        	System.out.println("Error: no ENSDF file path is given!");
        	System.out.println(run.usage());
        	return;
        }
        
        String s=args[0].toUpperCase();
        if(args.length==1 && (s.contains("-HELP")||s.contains("-USAGE"))){
       		System.out.println(run.usage());       	
        	return;
        }
        
        boolean openGUI=false;
        String outputDir="",inputDir="";  
        String inputfilePath="";     
        String outfilename="";
        int count=0;
        
        File f=null;
        if(args[0].charAt(0)=='-'){
            System.out.println("No input ENSDF file is specified! The GUI will open\n");
            openGUI=true;
        }else {
            f=new File(args[0]);
            if(!f.exists()){
            	System.out.println("Error: ENSDF file does not exist or wrong file path:\n");
            	System.out.println("  "+f.getAbsolutePath());
            	return;
            }
  
            File parentFile=f.getAbsoluteFile().getParentFile();
            if(parentFile==null){
                inputDir=".";
            }else{
                inputDir=parentFile.getAbsolutePath();
            }           
            outputDir=inputDir;
            
            //outdir+=run.dirSeparator()+"NDS_out";//default outdir

   
            if(args.length>1 && ((String)args[1]).trim().charAt(0)!='-'){
            	String pathArg=(String)args[1];
            	//this usage is only used for Java_RULER and Java_NDS not for other codes
            	if(pathArg.contains(File.separator) || pathArg.equals(".")) {
            		//pathArg is used as out dir, output file name will be set later
                    outputDir=args[1];//output path
                    
                    File fout=new File(outputDir);
                    if(!fout.exists()) {
                        System.out.println("Warning: output path does not exist and will be created.:");
                        System.out.println("  "+f.getAbsolutePath());
                        System.out.flush();
                        fout.mkdir();
                    }
                    
                    //outfilename="JAVA_NDS";
            	}else {
            		//pathArg is used as output file name (no extension)
                	outfilename=args[1];//output file name (no extension)
                	
                	File fout=new File(outfilename);
                	File parentDir=fout.getAbsoluteFile().getParentFile();
                	if(parentDir!=null){
                		outputDir=parentDir.getAbsolutePath();
                		outfilename=fout.getName();
                	}
                	
            	}

            	count++;
            	
            }
        }
        

        run.initDict();
        
        boolean latexOnly=false;//convert to LaTeX only without running the script to generate PDF output
        boolean printUsage=false;
        boolean redirectToFile=true;//redirect all System.out.println() messages to a file
        
        //other options, all options begin with '-'
        //skip the option otherwise
        for(int i=count;i<args.length;i++){
        	s=((String)args[i]).trim();
        	if(s.charAt(0)!='-')
        		continue;
        	
        	while(s.length()>0 && s.charAt(0)=='-')
        		s=s.substring(1);
        	
        	s=s.trim();
        	if(s.length()<=0)
        		continue;
        	
        	  
        	String s0=s;
        	s=s.toUpperCase();
        	
        	if(s.equals("NODRAWING")){//include drawings by default
        		NDSControl.includeAllDrawings=false;
        		NDSControl.nodrawing=true;
        	}else if(s.startsWith("SHOWSUP")) {
        		NDSControl.showSuppressed=true;
        	}else if(s.indexOf("REF")==0){//no reference by default
        		NDSControl.hasReference=true;
        	}else if(s.equals("NAMEBYDSID")){
            	outfilename="DSID";
        	}else if(s.equals("NOPDF")){
        		latexOnly=true;
        	}else if(s.equals("HELP")){
        		printUsage=true;
        	}else if(s.equals("JDBCONLY")){
        		NDSControl.isForcedNSRWebQuery=false;
        	}else if(s.contains("NSRLOGIN")){
        		String[] params=new String[3];
        		String NSRLoginFileName="";
        		File loginFile=null;
	        	
        		params=args[i].split("=");

        		//System.out.println(s+"  params="+params.length);

        		
        		if(params.length>=2){
        			NSRLoginFileName=params[1];
        			loginFile=new File(params[1]);
        	        
        			//System.out.println(params[1]+" "+loginFile.getAbsolutePath()+"  "+loginFile.exists());
        			
        	        if(!loginFile.exists()){
        	        	System.out.println("Warning: login file for NSR database doesn't exist!");
        	        	System.out.println("         "+loginFile.getAbsolutePath());
        	        	System.out.println("         NSR information will obtained by simulating web search.");
        	        	NSRLoginFileName="";
        	        }else
        	        	NSRLoginFileName=loginFile.getAbsolutePath();
        		}else{
    	        	System.out.println("Warning: login file for NSR database is not given!");
    	        	System.out.println("         NSR information will obtained by simulating web search.");
        		}
        		
        		NDSControl.NSRDataBaseLoginFileName=NSRLoginFileName;
        	
        	}else if(s.equals("NOREDIRECT")){
        		redirectToFile=false;
        		run.redirectOutputToMessenger(false);
        		run.setRedirectOutputToFile(redirectToFile);
        	}else if(s.contains("SILENT")){
        		NDSControl.isSilentJDBC=true;
        	}else if(s.startsWith("ERRORLIMIT=")){
            	//skip do nothing
            }else if(s.startsWith("WORKDIR=") || s.startsWith("CURR")) {
            	String dir="";
            	if(s.startsWith("WORKDIR")) {
                	int n=s0.indexOf("=");
                	dir=s0.substring(n+1).trim();
            	}
            		
            	File f1=null;
            	if(dir.isEmpty()) {
            		f1=new File(System.getProperty("user.dir"));
            	}else {
            		f1=new File(dir);
            	}
            	
            	dir=f1.getAbsolutePath();
            	if(!f1.exists()) {
            		System.out.println("Error: dir="+dir+" does not exist!");
            		return;
            	}
            	
            	
            	NDSControl.workdir=dir;
            	outputDir=dir;
            	
            	//System.out.println(dir);
            	//System.out.println(System.getProperty("user.dir"));
            }else if((s.contains("HELP")||s.contains("USAGE"))) {
                printUsage=true;
            }else {
        		System.out.println("Error: invalid argument: "+s0);
        		return;
            }
        }
        
        File dir=new File(outputDir);
        if(!dir.exists() && f!=null){
        	System.out.println("Warning: output path does not exist and will be created.:");
        	System.out.println("  "+f.getAbsolutePath());
        	System.out.flush();
        	dir.mkdir();
        }
        
        if(!outputDir.isEmpty()) {
     	    Setup.filedir=inputDir;
        	Setup.outdir=outputDir;   
            Setup.save();
        }
        
        //run.redirectOutputToFile(true);
        run.setOutputDir(outputDir);
        
        
        if(openGUI) {
        	startUI();
        }else {
            if(NDSControl.isSilentJDBC){
            	System.out.println("input : "+f.getName());
            	System.out.println("output: "+outfilename);
            }else{
            	System.out.println("input : "+f.getAbsolutePath());
            	System.out.println("output: "+outputDir+run.dirSeparator()+outfilename);	
            }

        	
        	if(outfilename.equals("DSID")){
        		System.out.println("        DSID record in ENSDF file will be used to name output");
        	}
        	System.out.println("        output name applies to all relevant output files including .tex, .pdf, etc");
        	
        	if(printUsage)
        		System.out.println(run.usage());
        	
            ArrayList<String> list=new ArrayList<String>();
            list.addAll(ensdfparser.nds.util.Str.readFile(f));
                    
            
            //if outfilename='', default name will be used.
            //Control settings are reset in this call by default, unless reset=false is specified.
            //If not specified, the control settings from input arguments will be override.
            outfilename=Str.fileNamePrefix(outfilename);
            
            if(latexOnly){
            	run.convertToLaTeX(list,outfilename,false);
            }else
            	run.convert(list,outfilename,false);
        
            //run.loadENSDF(list);
            //long startTime=System.currentTimeMillis();      
            //run.convert();    
            //run.waitLaTeX(startTime);
            //run.cleanupFiles();
        }

    }
    
    
}
