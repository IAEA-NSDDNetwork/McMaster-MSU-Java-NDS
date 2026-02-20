package javands.main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Level;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

@SuppressWarnings("unused")
public class Test {

	public static void main(String[] args) throws Exception {
        //test1();
	    //loadENSDFTest();
	    //processTest();
	    //System.out.println(Str.isEqualValue("0.999999","1.000000",1E-5));
	    //test1();
	    
	    //Comment c=new Comment();
	    //System.out.println(c.isValidHeaderStr("ADAPTED"));
		
		String s1="target was irradiated with 1.4|*10{+17} {+48}Ca ions in 14 h. No events were observed for {+33}F, whereas estimated counts for {+33}F";
		String s2="target was irradiated with 1.4*10{+17} {+48}Ca ions in 14 h. No events were observed for {+33}F, whereas estimated counts for {+33}F";
		System.out.println(NDSConfig.findWidth(s1)+"  "+NDSConfig.findWidth(s2));
	}
	
	public static void test1() throws Exception{
        //Setup.load();
        Translator.init();
        
        //String out=Translator.procGenCom("1G11/2",0,false);
        //String out=Translator.printNumber("+057","+|@-33");
        
        //System.out.println(Str.capFirstLetters("A NUCLEAR DATA SHEETS FOR A=48 OF a DECAY".toLowerCase()));
        
        
        Comment c=new Comment();
        String s="";
        //s="J$atomic-beam method; EPR, optical spectroscopy (1962Sp03). Others: 1958Hu17, 1954Mu15, 1952Bo21. Parity from L(p,d)=L({+3}He,|a)=L(|a,{+3}He)=3";
        
        //s="L(p,d)=L({+3}He,|a)=L(|a,{+3}He)=3";
        //s="Jpi: atomic-beam method; EPR, optical spectroscopy (1962Sp03). Others: 1958Hu17, 1954Mu15, 1952Bo21. Parity from L(p,d)=L({+3}He,|a)=L(|a,{+3}He)=3.";
        s=        "172YB cL BAND(A)$K|p=0+ g.s. band.\n"
        		+ "172YB2cL Variations in g factors are deduced from |g(|q,H) data in Coul. ex.\n"
        		+ "172YB3cL for levels of J|p=2+ to 10+ (1979Wa15). Deviation from rotational\n"
        		+ "172YB4cL behavior is expressed in terms of g(J)=g(0)(1+|aJ{+2}) expression,\n"
        		+ "172YB5cL where 1979Wa15 deduce |a=+0.0010 {I15} from |g(|q,H) data.\n"
        		+ "172YB6cL Population in (d,t) involves pickup of 5/2[512|^] neutron from\n"
        		+ "172YB7cL {+173}Yb g.s., and in (t,d) involves stripping a neutron into the\n"
        		+ "172YB8cL 1/2[521|_] orbital of {+171}Yb g.s., based on comparisons of\n"
        		+ "172YB9cL experimental and theoretical strengths (1999Bu25)";
        String[] temp=s.split("\n");
        Vector<String> linesV=new Vector<String>();
        for(int i=0;i<temp.length;i++)
        	linesV.add(temp[i]);
        c.setValues(linesV);
        
        if(c.headAt(0).equals("BAND"))
    		s="Band";
    	else
    		s="Seq.";
    	
    	s+="(";
    	if(c.isBigC())
    			s+="^";//uppercase comment, not translate the band label, eg, A to alpha, G to gamma in latex
    	s+=c.flagAt(0, 0)+"): ";

    	
        //c.setBody(s,true);
        //String s=EnsdfUtil.printContRecord("%B=0.14 LE");
        
        Comment c1=new Comment();
        
        //c1.setBigC(c.isBigC());
        
        c1.setBody(s+c.body());
        
        //System.out.println(c1.isBigC());
        
        //System.out.println(c1.body());
        
        //System.out.println(c.getRegTranslated());
        
        //s=Translator.translate(s,113,s.length());
        //System.out.println(s);
	}

	public static void loadENSDFTest() throws Exception  {
        Setup.load();
        Translator.init();
        
        String fileDir="/Users/chenj/work/evaluation/mytools/ENSDFSearch/test/ensdf_170501";
        String filePath="";
        Vector<String> filePaths=new Vector<String>();
        
        int istart=178;
        int iend=178;
        
        fileDir=fileDir+File.separator;
        
        Run run=new Run();
        run.redirectOutputToFile("./test_log.txt");
        
        for(int A=istart;A<=iend;A++){
            
            if(A<10)
                filePath=fileDir+"ensdf.00"+A;
            else if(A<100)
                filePath=fileDir+"ensdf.0"+A;
            else 
                filePath=fileDir+"ensdf."+A;
            

            try {
                
                File f=new File(filePath);
                if(!f.exists()) {
                    System.out.println("Warning: file "+filePath+" does not exist! Skip it.");
                    continue;
                }
                System.out.println("##### load "+filePath);
                MassChain data=new MassChain();
                data.load(f);  
                
            }catch(Exception e) {
                e.printStackTrace();
            }
    

        } 	    
	}


	   
	public static void processTest() throws Exception {

        String script="NDS.bat";
        String path=javands.main.Setup.outdir+"\\"+script;
        String command="";
        String os=System.getProperty("os.name").toLowerCase();
        String outdir="";  
    
        
        if(os.contains("linux")||os.contains("mac")){
            script="NDS.sh";
            path=javands.main.Setup.outdir+"/"+script;
        }     
        
	    try {            

	        Process proc=null;
	        
	        if(os.contains("linux")||os.contains("mac")){

	            
	            String shell="/bin/bash";
	                                 
	            String outpath="/Users/chenj/work/evaluation/ENSDF/check";
	            File wd = new File(outpath);
	            proc = null;
	           
	            System.out.println("dir="+outpath);
	              
	            System.out.println("system path="+System.getenv("PATH"));
	               
	            int option=1;
	            
	            //method 1: 
	            if(option==1) {
	            //proc = Runtime.getRuntime().exec(shell+" -c gnome-terminal", null, wd);
	            proc = Runtime.getRuntime().exec(shell, null, wd);
	            //proc = Runtime.getRuntime().exec(new String[]{"/bin/sh",outpath+"/NDS.sh"});
	            //proc = Runtime.getRuntime().exec(shell,new String[] {"PATH="+System.getenv("PATH")}, wd);
	            }
	            
	            //method 2 (modern): using ProcessBuilder
	            if(option==2) {
	            ProcessBuilder processBuilder = new ProcessBuilder(shell);
	            processBuilder.directory(wd);
	            
	            Map<String, String> envs = processBuilder.environment();

	            envs.put("PATH", System.getenv("PATH"));	            
	            //envs.put("PATH", "/Library/Tex/texbin:/usr/local/bin");
	            
	            processBuilder.redirectErrorStream();
	   	            
	            //processBuilder.command(shell);
	            proc=processBuilder.start();
	            }
	            
	            if (proc != null) {
	                
	               BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	               PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);

	               System.out.println(" shell="+shell+" script="+script);
	               
	               out.println("where mpost");
	               //out.println("where ps2pdf");
	               
	               //out.println("source "+script);
	               
                   //out.println("man ps2pdf");

	               
	               out.println("pwd");
	               out.println("exit");
	               
	               System.out.println(" test0");
	               

	               
	               String line="";

	               System.out.println(" test0.5");
	               //System.out.println(in.lines().count());
	               
	               boolean isLatexError=false;
	               while ((line=in.readLine()) != null) {                  
	                 //printMessage(line);
	                 System.out.println(line);
	                 if(line.trim().indexOf('?')==0){//try to catch latex error
	                    //System.out.println("######################test");
	                     isLatexError=true;
	                    break;
	                 }
	               }
	               
	               System.out.println(" test1");
	               
	               //if(isLatexError) {
	               //    throw new Exception("Error when converting LaTeX to PDF. Probably format issue.");
	               //}
	               
	                 
	                       
	               //proc.waitFor();
	               System.out.println(" test2");
	               
	               in.close();
	               out.close();
	               proc.destroy();
	               
	               System.out.println(" test3");
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

	            
	            command="cmd /c start "+script;       //start your process in it's separate console (that's what start does)
	            proc=rt.exec(command,         //path to executable
	                    null,                         // env vars, null means pass parent env
	                    new File(javands.main.Setup.outdir));  // working directory 
	                
	        }
	    } catch(Exception e) {
       
	        //System.out.println(e.toString());
        
	        e.printStackTrace();
    
	    }
	    
	    System.out.println("end");
    
	}
}
