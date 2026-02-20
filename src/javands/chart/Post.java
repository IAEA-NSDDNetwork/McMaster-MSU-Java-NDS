/*
 * Tex.java
 *
 * Created on May 20, 2007, 11:39 AM
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
 * Utility class with various postscript. latex and metapost
 * related functions.
 *
 * @author Roy Zywina
 */
public class Post {
    // postscrpt (metapost) points per cm.
    protected final static float PT_PER_CM = 28.346456693f;
    // centimetres to PS points
    public static float cmToPoint(float x){
        return PT_PER_CM * x;
    }
    // PS points to centimeters
    public static float pointToCm(float x){
        return x / PT_PER_CM;
    }
}
