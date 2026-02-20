
package javands.chart;

import java.util.*;

import ensdfparser.ensdf.Band;
import ensdfparser.nds.config.NDSConfig;

/**
 * The band layout object determines the optimal width of each band.
 * This is done based on the number of gamma columns needed.  It also
 * tells you if you need to prune gamma columns to get things to fit 
 * properly.
 *
 * @author Roy Zywina
 */
public class BandLayout {
    float W; // width of chart (cm)
    float bSep; // band separation (cm)
    float mGap;
    int indexStart;
    class BandWraps{
        public int ncol=0;   // number of gamma columns
        public float left=0; // left position
        public float right=0;// right position
        public float gap=0;  // optimal gap between gamma columns
        public int vcol=0;   // visible columns
        public Band band=null;
        
        
    }
    Vector<BandWraps> bandWraps;
    
    /** Creates a new instance of BandLayout */
    public BandLayout(float width,float bandsep,int off) {
        indexStart=off;
        W=width;
        mGap=0.15f;
        bSep = bandsep;
        bandWraps = new Vector<BandWraps>();
    }
    /**
     * Add a band to the list.
     */
    public void addBand(Band b,int ncol){
        BandWraps bw = new BandWraps();
        if (ncol<=0) ncol=1; // make strange input work
        bw.ncol = ncol;
        bw.band=b;
        bandWraps.add(bw);
    }
    /**
     * Perform calculations.
     */
    public void calc(){
        if (bandWraps.size()==0) return;
        //int N = band.size();
        // effective width after gaps are removed
        @SuppressWarnings("unused")
		int tot = 0; // total columns
        BandWraps[] bnd = new BandWraps[bandWraps.size()];
        bandWraps.copyInto(bnd);
        int x;
        for (x=0; x<bnd.length; x++){
            tot += bnd[x].ncol;
        }
        float offset=0;
        float leftmar=0,rightmar=0;
        for (x=0; x<bnd.length; x++){
            float w=NDSConfig.bandWidths[x+indexStart];
            float eW=w-2.0f;
            
            float[] margins=BaseChart.recalculateMargins(bnd[x].band.levels(), BaseChart.LEFT_PAD, BaseChart.RIGHT_PAD);
            leftmar=margins[0];
            rightmar=margins[1];
            eW=w-leftmar-rightmar-0.2f;
            
            // calculate positions
            bnd[x].left = offset;
            bnd[x].right= offset + w;
            
            // check gaps
            if (bnd[x].ncol>1)
                bnd[x].gap = eW / (bnd[x].ncol-1);
            else
                bnd[x].gap = mGap;

            bnd[x].vcol = bnd[x].ncol;
            if (bnd[x].gap < mGap){
                // need to prune
                bnd[x].vcol = (int)(eW / mGap);
                if (bnd[x].vcol<=0)
                    bnd[x].vcol = 1;
            }
            offset += w+bSep;
        }
        
    }
    /**
     * Get number of bands held.
     */
    public int sizeOfBands(){
        return bandWraps.size();
    }
    /**
     * Get left position of band.
     */
    public float getLeft(int index){
        return ((BandWraps)bandWraps.get(index)).left;
    }
    /**
     * Get right position of band.
     */
    public float getRight(int index){
        return ((BandWraps)bandWraps.get(index)).right;
    }
    /**
     * Get optimal gap between gamma columns.
     */
    public float getGap(int index){
        return ((BandWraps)bandWraps.get(index)).gap;
    }
    /**
     * Get number of visable columns.
     */
    public int getVisable(int index){
        return ((BandWraps)bandWraps.get(index)).vcol;
    }
}
