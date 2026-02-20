/*
 * TagLayout.java
 *
 * Created on May 31, 2007, 6:55 PM
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

package javands.chart;

/**
 * The tag layout object helps determin whether or not
 * a tag representing the end band of a gamma ray can be shown.
 *
 * @author Roy Zywina
 */
public class TagLayout {
    protected class Tag{
        public float pos;
        public boolean fixed;
        public boolean shown;
        public String code;
    }
    
    java.util.Vector<Tag> tags;
    int numshown;
    float fontsize;
    /** Creates a new instance of TagLayout */
    public TagLayout(float size) {
        tags = new java.util.Vector<Tag>();
        numshown=0;
        fontsize=size;
    }
    
    /// fixed tags are assumed to have already been displayed
    public void addFixedTag(float pos){
        Tag t = new Tag();
        t.pos = pos;
        t.fixed=true;
        t.shown=true;
        t.code="";
        tags.add(t);
    }
    /// normal tags are added in the hope that they can be printed
    /// but they will be cut if there are collisions
    public void addTag(float pos,String code){
        for (int x=0; x<tags.size(); x++){
            Tag t = (Tag)tags.get(x);
            if (t.pos==pos) return;
        }
        Tag t = new Tag();
        t.pos = pos;
        t.fixed=false;
        t.shown=true;
        t.code=code;
        tags.add(t);
    }
    /// calculate what stays and goes.
    /// size is the height of the tags
    public void calc(){
        numshown=0;
        for (int x=0; x<tags.size(); x++){
            Tag t1 = (Tag)tags.get(x);
            if (t1.fixed) continue;
            for (int y=0; y<tags.size(); y++){
                if (x==y) continue;
                Tag t2 = (Tag)tags.get(y);
                if (!t2.shown) continue;
                
                float delta = Math.abs(t1.pos - t2.pos);
                if (delta<fontsize)
                    t1.shown=false;
            }
            if (t1.shown)
                numshown++;
        }
    }
    /// return number of tags shown
    public int sizeOfTags(){
        return numshown;
    }
    /// get the nth shown tag's code
    public String getTagAt(int n){
        int count=0;
        for (int x=0; x<tags.size(); x++){
            Tag t = (Tag)tags.get(x);
            if (t.fixed) continue;
            if (!t.shown) continue;
            if (count==n)
                return t.code;
            count++;
        }
        return null;
    }
}
