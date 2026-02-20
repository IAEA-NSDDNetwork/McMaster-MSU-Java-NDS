/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javands.chart;

/**
 *
 * @author Jeremie
 */
public class LegendChart extends BaseChart {
    public void drawChart(java.io.Writer out,int n,boolean drawHead)throws java.io.IOException{

        if(drawHead){
        	writeFigureHead(out,n);
        	out.write("ahlength:=4;\nahangle:=22;\n");
        }
        
        out.write("label.urt(btex Diagram Legend etex,(110,315));\n");
        out.write("draw (0,300)--(300,300);\n");
        out.write("draw (0,285)--(300,285);\n");
        out.write("draw (150,300)--(150,180);\n");
        out.write("label.urt(btex "+otherLabelSize()+"Skeleton Scheme Legend etex,(0,288));\n");
        out.write("label.urt(btex "+otherLabelSize()+"Level Diagram Legend etex,(150,288));\n");
        out.write("drawarrow (10,275)--(50,275);\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay (I$_{\\gamma}$ $<$ 2\\%) etex,(55,270));\n");
        out.write("drawarrow (10,265)--(50,265) withcolor blue;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay (I$_{\\gamma}$ $<$ 10\\%) etex,(55,260));\n");
        out.write("drawarrow (10,255)--(50,255) withcolor red;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay (I$_{\\gamma}$ $>$ 10\\%) etex,(55,250));\n");
        out.write("drawarrow (10,245)--(50,245) dashed evenly;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay (Uncertain) etex,(55,240));\n");
        out.write("drawarrow (10,235)--(50,235) withpen pencircle scaled 2;\n");
        out.write("drawarrow (10,235)--(50,235) withpen pencircle scaled 0.5 withcolor white;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\alpha$ Decay etex,(55,230));\n");
        out.write("draw(30,225) withpen pencircle scaled 2;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Coincidence etex,(55,220));\n");
        out.write("draw(30,215) withpen pencircle scaled 2;\n");
        out.write("draw(30,215) withpen pencircle scaled 1 withcolor white;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Coincidence (Uncertain) etex,(55,210));\n");
        out.write("draw (10,205)--(50,205) withpen pencircle scaled 2;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Ground State Level etex,(55,200));\n");
        out.write("draw (10,195)--(50,195) dashed evenly;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Level (Uncertain) etex,(55,190));\n");



        out.write("drawarrow (160,275)--(200,275);\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\gamma$ Decay etex,(205,270));\n");
        out.write("drawarrow (160,265)--(200,265) withcolor blue;\n");
        out.write("label.urt(btex "+otherLabelSize()+"EC Decay etex,(205,260));\n");
        out.write("drawarrow (160,255)--(200,255) withcolor red;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\beta^{-}$ Decay etex,(205,250));\n");
        out.write("drawarrow (160,245)--(200,245) withpen pencircle scaled 2;\n");
        out.write("drawarrow (160,245)--(200,245) withpen pencircle scaled 0.5 withcolor white;\n");
        out.write("label.urt(btex "+otherLabelSize()+"$\\alpha$ Decay etex,(205,240));\n");
        out.write("draw (160,235)--(200,235) withpen pencircle scaled 2;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Ground State Level etex,(205,230));\n");
        out.write("draw (160,225)--(200,225) dashed evenly;\n");
        out.write("label.urt(btex "+otherLabelSize()+"Level (Uncertain) etex,(205,220));\n");
        if(drawHead)
        	writeFigureTail(out);

    }
    
    public void drawChart(java.io.Writer out,boolean drawHead)throws java.io.IOException{
        if(drawHead)
        	writeHead(out);
        
        drawChart(out,1,drawHead);
        
        if(drawHead)
        	writeTail(out);
    }
    
    public void drawChart(java.io.Writer out)throws java.io.IOException{
    	drawChart(out,true);
    }
    
}
