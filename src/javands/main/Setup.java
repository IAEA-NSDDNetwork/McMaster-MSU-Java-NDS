/*
 * Setup.java
 *
 * Created on March 17, 2007, 6:52 PM
 *
 * Copyright (c) 2007 Roy Zywina
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package javands.main;

import java.io.*;
import java.beans.*;
import java.util.*;

import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.util.Str;

/**
 * This object contains the application setup.  Paths to specific utilities 
 * needed by the program, etc.
 * 
 * Setup is saved/loaded from users home directory.
 *
 * @author Roy Zywina
 */
public class Setup{

	
	public static String configFileName="JAVA_NDS_conf.xml";
	
    // the defaults should be fine for most unix systems
    
	public static String dirSeparator=File.separator;

    /// directory used last
    public static String filedir = null;
    /// directory for the output files
    public static String outdir=System.getProperty("user.dir")+dirSeparator+"out";
    
    /// default folder to store the xml configure files for all codes
    public static String confdir=System.getProperty("user.home");
    
    //public static String lastOutdir=outdir;
    
    /// full path of latex executable
    public static String latex = "/usr/bin/latex";
    /// full path of metapost executable
    public static String metapost = "/usr/bin/mpost";
    
    //for policy comment in abstract: NDS issue month and policy URL 
    //public static String NDSPolicyURL="https://www.nndc.bnl.gov/nds/docs/NDSPolicies.pdf";
    //public static String NDSIssueMonthForPolicy="April";
    
    /// load configuration
    public static void load(){
        
    	File f=new File(confdir);
    	if(!f.exists())
    		confdir=System.getProperty("user.dir");
    	
        String fn = confdir + dirSeparator+".nds_conf.xml";
        HashMap<?, ?> hm;
        
        //System.out.println(" out A="+System.getProperty("user.dir"));
        try{

        	
            XMLDecoder d = new XMLDecoder(
                new BufferedInputStream(
                    new FileInputStream(fn)));
            hm = (HashMap<?, ?>)d.readObject();
            d.close();
            latex = (String)hm.get("latex");
            metapost = (String)hm.get("metapost");
            filedir  = (String)hm.get("filedir");
            outdir=(String)hm.get("outdir");

            if(outdir==null || outdir.isEmpty())
            	outdir=System.getProperty("user.dir");
            
            String month=(String)hm.get("issuemonth");
            String url=(String)hm.get("policyurl");
            if(month!=null && !month.trim().isEmpty()) {
            	NDSControl.NDSIssueMonthForPolicy=Str.capFirstLetters(month.trim().toLowerCase());
            }
          
            if(url!=null && !url.trim().isEmpty()) {
            	NDSControl.NDSPolicyURL=url.trim();
            }
            
            //System.out.println(" output dir="+Setup.outdir);
        }catch(Exception ex){

        } 
        
        
    	//File f=new File(outdir);
    	//if(!f.exists()){
    	//	f.mkdir();
    	//}

        
    }
    public static void load(String confdir1){
    	confdir=confdir1;
    	load();
    }
    /// save configuration
    public static void save(){
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("latex",latex);
        hm.put("metapost",metapost);
        hm.put("filedir",filedir);
        hm.put("outdir",outdir);
        
        hm.put("issuemonth", NDSControl.NDSIssueMonthForPolicy);
        hm.put("policyurl", NDSControl.NDSPolicyURL);
        
    	File f=new File(confdir);
    	if(!f.exists())
    		confdir=System.getProperty("user.dir");
    	
        String fn = confdir + dirSeparator+".nds_conf.xml";
        try{
            XMLEncoder e = new XMLEncoder(
                new BufferedOutputStream(
                    new FileOutputStream(new File(fn))));
            e.writeObject(hm);
            e.close();
        }catch(Exception ex){} // ignore error        
    }

    //set conf dir to be the user dir (where the code is run)
    public static void setUserDirs(String indir1,String outdir1) {
    	try {
    		filedir=indir1;
    		outdir=outdir1;
    		
    		//lastOutdir=outdir;
    	}catch(Exception e) {
    		
    	}
    }
    
}

