package javands.chart;

import ensdfparser.ensdf.*;

/**
 * The fan layout object calculates where the labels should be placed
 * given a set of levels that are already in fixed locations.
 */
public class LevelLabelLayout {
    protected class Fan{
        // level position, not to be changed
        public float levpos;
        // label position, must stay within limits
        public float labpos;
        // level object, for easy searching
        public Level level;
    }
    // don't go over height of chart
    float max=0;
    // height of font
    float fontsize=6.0f;
    // allow compaction when theres no room
    boolean compact = false;
    /**
     * Set whether or not level labels ill be compacted when they don't fit.
     * Default is false.
     */
    public void setCompact(boolean c){
        compact = c;
    }
    java.util.Vector<Fan> fan;
    /** Creates a new instance of FanLayout */
    public LevelLabelLayout() {
        fan = new java.util.Vector<Fan>();
    }
    /// Add a level given its vertical position
    public void addLevel(Level l,float levpos){
        Fan f = new Fan();
        f.levpos = levpos;
        f.labpos = levpos;
        f.level  = l;
        fan.add(f);
    }
    /// Perform a position adjutment step.
    /// several dozen steps will generally be required.
    protected boolean adjust(Fan f[],float height,float font){
        // an adjustment step is 0.5 mm
        final float step = 0.05f;
        // has there ben a change?
        boolean changed = false;
        
        for (int x=1; x<f.length; x++){
            float glo,ghi,plo=-font-0.01f,phi=height+font+0.01f,pos;
            pos = f[x].labpos;
            if (x>0) plo = f[x-1].labpos;
            if (x<f.length-1)
                phi = f[x+1].labpos;
            glo = pos - plo;
            ghi = phi - pos;
            
            if (glo<font && ghi<font){
                // squeezed but nowhere to go
            }
            else if (glo>font && ghi>font){
                // lots of room
            }
            else if (glo<font){
                // neet to push up
                f[x].labpos+=step;
                changed = true;
            }
            else{
                // need to push down
                f[x].labpos-=step;
                changed = true;
            }
            /*
            // move up a bit
            if (glo < font && ghi>=(font)){// && x!=0){
                f[x].labpos += step;
                changed = true;
            }
            // move down a bit
            else if (ghi < font && glo>=(font)){// && x<f.length-1){
                f[x].labpos -= step;
                changed = true;
            }
             **/
        }
        return changed;
    }
    /// Calculate fanning given chart height and font size
    public void calc(float height,float font){
        //max = height+font;
        int x,y;
        Fan f[] = new Fan[fan.size()];
        fan.copyInto(f);
        // sort array by level position
        for (x=f.length-1; x>0; x--){
            for (y=0; y<x; y++){
                if (f[y].levpos>f[y+1].levpos){
                    Fan tmp=f[y];
                    f[y]=f[y+1];
                    f[y+1]=tmp;
                }
            }
        }
        // perform fitting optimization until everything fits or 100 
        // iterations have passed.
        for (x=0; x<100; x++){
            if (!adjust(f,height,font)) break;
            //System.out.printf("P1 %f, ",f[0].labpos);
        }
    }
    /// Get a given levels label position.
    public float getPosition(Level l){
        for (int x=0; x<fan.size(); x++){
            if (((Fan)fan.get(x)).level.ES().equals(l.ES())){
                return ((Fan)fan.get(x)).labpos;
            }
        }
        return -1;
    }
    
    /// print out debug information
    public void debug(){
        //
        for (int x=0; x<fan.size(); x++){
            Fan f = (Fan)fan.get(x);
            System.out.printf("Level %s, Level pos %f, Fan pos %f\n",
                f.level.ES(),f.levpos,f.labpos);
        }
    }
}
