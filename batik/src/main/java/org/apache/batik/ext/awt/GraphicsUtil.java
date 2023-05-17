/*

   Copyright 2001-2004  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.ext.awt;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;


/**
 * Set of utility methods for Graphics.
 * These generally bypass broken methods in Java2D or provide tweaked
 * implementations.
 *
 * @author <a href="mailto:Thomas.DeWeeese@Kodak.com">Thomas DeWeese</a>
 * @version $Id: GraphicsUtil.java,v 1.36 2005/03/27 08:58:32 cam Exp $
 */
public class GraphicsUtil {

    public static AffineTransform IDENTITY = new AffineTransform();

    public final static boolean WARN_DESTINATION = true;

    /**
     * Standard prebuilt Linear_sRGB color model with no alpha */
    public final static ColorModel Linear_sRGB =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_LINEAR_RGB), 24,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0x0, false,
                             DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with premultiplied alpha.
     */
    public final static ColorModel Linear_sRGB_Pre =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_LINEAR_RGB), 32,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0xFF000000, true,
                             DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with unpremultiplied alpha.
     */
    public final static ColorModel Linear_sRGB_Unpre =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_LINEAR_RGB), 32,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0xFF000000, false,
                             DataBuffer.TYPE_INT);

    /**
     * Standard prebuilt sRGB color model with no alpha.
     */
    public final static ColorModel sRGB =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_sRGB), 24,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0x0, false,
                             DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with premultiplied alpha.
     */
    public final static ColorModel sRGB_Pre =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_sRGB), 32,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0xFF000000, true,
                             DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with unpremultiplied alpha.
     */
    public final static ColorModel sRGB_Unpre =
        new DirectColorModel(ColorSpace.getInstance
                             (ColorSpace.CS_sRGB), 32,
                             0x00FF0000, 0x0000FF00,
                             0x000000FF, 0xFF000000, false,
                             DataBuffer.TYPE_INT);

    /**
     * An internal optimized version of copyData designed to work on
     * Integer packed data with a SinglePixelPackedSampleModel.  Only
     * the region of overlap between src and dst is copied.
     *
     * Calls to this should be preflighted with is_INT_PACK_Data
     * on both src and dest (requireAlpha can be false).
     *
     * @param src The source of the data
     * @param dst The destination for the data.
     */
    public static void copyData_INT_PACK(Raster src, WritableRaster dst) {
        // System.out.println("Fast copyData");
        int x0 = dst.getMinX();
        if (x0 < src.getMinX()) x0 = src.getMinX();

        int y0 = dst.getMinY();
        if (y0 < src.getMinY()) y0 = src.getMinY();

        int x1 = dst.getMinX()+dst.getWidth()-1;
        if (x1 > src.getMinX()+src.getWidth()-1)
            x1 = src.getMinX()+src.getWidth()-1;

        int y1 = dst.getMinY()+dst.getHeight()-1;
        if (y1 > src.getMinY()+src.getHeight()-1)
            y1 = src.getMinY()+src.getHeight()-1;

        int width  = x1-x0+1;
        int height = y1-y0+1;

        SinglePixelPackedSampleModel srcSPPSM;
        srcSPPSM = (SinglePixelPackedSampleModel)src.getSampleModel();

        final int     srcScanStride = srcSPPSM.getScanlineStride();
        DataBufferInt srcDB         = (DataBufferInt)src.getDataBuffer();
        final int []  srcPixels     = srcDB.getBankData()[0];
        final int     srcBase =
            (srcDB.getOffset() +
             srcSPPSM.getOffset(x0-src.getSampleModelTranslateX(),
                                y0-src.getSampleModelTranslateY()));


        SinglePixelPackedSampleModel dstSPPSM;
        dstSPPSM = (SinglePixelPackedSampleModel)dst.getSampleModel();

        final int     dstScanStride = dstSPPSM.getScanlineStride();
        DataBufferInt dstDB         = (DataBufferInt)dst.getDataBuffer();
        final int []  dstPixels     = dstDB.getBankData()[0];
        final int     dstBase =
            (dstDB.getOffset() +
             dstSPPSM.getOffset(x0-dst.getSampleModelTranslateX(),
                                y0-dst.getSampleModelTranslateY()));

        if ((srcScanStride == dstScanStride) &&
            (srcScanStride == width)) {
            // System.out.println("VERY Fast copyData");

            System.arraycopy(srcPixels, srcBase, dstPixels, dstBase,
                             width*height);
        } else if (width > 128) {
            int srcSP = srcBase;
            int dstSP = dstBase;
            for (int y=0; y<height; y++) {
                System.arraycopy(srcPixels, srcSP, dstPixels, dstSP, width);
                srcSP += srcScanStride;
                dstSP += dstScanStride;
            }
        } else {
            for (int y=0; y<height; y++) {
                int srcSP = srcBase+y*srcScanStride;
                int dstSP = dstBase+y*dstScanStride;
                for (int x=0; x<width; x++)
                    dstPixels[dstSP++] = srcPixels[srcSP++];
            }
        }
    }

    public static void copyData_FALLBACK(Raster src, WritableRaster dst) {
        // System.out.println("Fallback copyData");

        int x0 = dst.getMinX();
        if (x0 < src.getMinX()) x0 = src.getMinX();

        int y0 = dst.getMinY();
        if (y0 < src.getMinY()) y0 = src.getMinY();

        int x1 = dst.getMinX()+dst.getWidth()-1;
        if (x1 > src.getMinX()+src.getWidth()-1)
            x1 = src.getMinX()+src.getWidth()-1;

        int y1 = dst.getMinY()+dst.getHeight()-1;
        if (y1 > src.getMinY()+src.getHeight()-1)
            y1 = src.getMinY()+src.getHeight()-1;

        int width  = x1-x0+1;
        int [] data = null;

        for (int y = y0; y <= y1 ; y++)  {
            data = src.getPixels(x0,y,width,1,data);
            dst.setPixels       (x0,y,width,1,data);
        }
    }

    /**
     * Copies data from one raster to another. Only the region of
     * overlap between src and dst is copied.  <tt>Src</tt> and
     * <tt>Dst</tt> must have compatible SampleModels.
     *
     * @param src The source of the data
     * @param dst The destination for the data.
     */
    public static void copyData(Raster src, WritableRaster dst) {
        if (is_INT_PACK_Data(src.getSampleModel(), false) &&
            is_INT_PACK_Data(dst.getSampleModel(), false)) {
            copyData_INT_PACK(src, dst);
            return;
        }

        copyData_FALLBACK(src, dst);
    }

    /**
     * Create a new ColorModel with it's alpha premultiplied state matching
     * newAlphaPreMult.
     * @param cm The ColorModel to change the alpha premult state of.
     * @param newAlphaPreMult The new state of alpha premult.
     * @return   A new colorModel that has isAlphaPremultiplied()
     *           equal to newAlphaPreMult.
     */
    public static ColorModel
        coerceColorModel(ColorModel cm, boolean newAlphaPreMult) {
        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            return cm;

        // Easiest way to build proper colormodel for new Alpha state...
        // Eventually this should switch on known ColorModel types and
        // only fall back on this hack when the CM type is unknown.
        WritableRaster wr = cm.createCompatibleWritableRaster(1,1);
        return cm.coerceData(wr, newAlphaPreMult);
    }

    /**
     * Coerces data within a bufferedImage to match newAlphaPreMult,
     * Note that this can not change the colormodel of bi so you
     *
     * @param wr The raster to change the state of.
     * @param cm The colormodel currently associated with data in wr.
     * @param newAlphaPreMult The desired state of alpha Premult for raster.
     * @return A new colormodel that matches newAlphaPreMult.
     */
    public static ColorModel
        coerceData(WritableRaster wr, ColorModel cm, boolean newAlphaPreMult) {

        // System.out.println("CoerceData: " + cm.isAlphaPremultiplied() +
        //                    " Out: " + newAlphaPreMult);
        if (cm.hasAlpha()== false)
            // Nothing to do no alpha channel
            return cm;

        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            // nothing to do alpha state matches...
            return cm;

        // System.out.println("CoerceData: " + wr.getSampleModel());

        if (newAlphaPreMult) {
            multiplyAlpha(wr);
        } else {
            divideAlpha(wr);
        }

        return coerceColorModel(cm, newAlphaPreMult);
    }

    public static void multiplyAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            mult_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            mult_INT_PACK_Data(wr);
        else {
            int [] pixel = null;
            int    bands = wr.getNumBands();
            float  norm = 1f/255f;
            int x0, x1, y0, y1, a, b;
            float alpha;
            x0 = wr.getMinX();
            x1 = x0+wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0+wr.getHeight();
            for (int y=y0; y<y1; y++)
                for (int x=x0; x<x1; x++) {
                    pixel = wr.getPixel(x,y,pixel);
                    a = pixel[bands-1];
                    if ((a >= 0) && (a < 255)) {
                        alpha = a*norm;
                        for (b=0; b<bands-1; b++)
                            pixel[b] = (int)(pixel[b]*alpha+0.5f);
                        wr.setPixel(x,y,pixel);
                    }
                }
        }
    }
    
    public static void divideAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            divide_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            divide_INT_PACK_Data(wr);
        else {
            int x0, x1, y0, y1, a, b;
            float ialpha;
            int    bands = wr.getNumBands();
            int [] pixel = null;
        
            x0 = wr.getMinX();
            x1 = x0+wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0+wr.getHeight();
            for (int y=y0; y<y1; y++)
                for (int x=x0; x<x1; x++) {
                    pixel = wr.getPixel(x,y,pixel);
                    a = pixel[bands-1];
                    if ((a > 0) && (a < 255)) {
                        ialpha = 255/(float)a;
                        for (b=0; b<bands-1; b++)
                            pixel[b] = (int)(pixel[b]*ialpha+0.5f);
                        wr.setPixel(x,y,pixel);
                    }
                }
        }
    }

    public static boolean is_INT_PACK_Data(SampleModel sm,
                                           boolean requireAlpha) {
        // Check ColorModel is of type DirectColorModel
        if(!(sm instanceof SinglePixelPackedSampleModel)) return false;

        // Check transfer type
        if(sm.getDataType() != DataBuffer.TYPE_INT)       return false;

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)sm;

        int [] masks = sppsm.getBitMasks();
        if (masks.length == 3) {
            if (requireAlpha) return false;
        } else if (masks.length != 4)
            return false;

        if(masks[0] != 0x00ff0000) return false;
        if(masks[1] != 0x0000ff00) return false;
        if(masks[2] != 0x000000ff) return false;
        if ((masks.length == 4) &&
            (masks[3] != 0xff000000)) return false;

        return true;
    }

        public static boolean is_BYTE_COMP_Data(SampleModel sm) {
            // Check ColorModel is of type DirectColorModel
            if(!(sm instanceof ComponentSampleModel))    return false;

            // Check transfer type
            if(sm.getDataType() != DataBuffer.TYPE_BYTE) return false;

            return true;
        }

    protected static void divide_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Divide Int");

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int base
            = (db.getOffset() +
               sppsm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                               wr.getMinY()-wr.getSampleModelTranslateY()));
        int pixel, a, aFP;
        // Access the pixel data array
        final int pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width;
            while (sp < end) {
                pixel = pixels[sp];
                a = pixel>>>24;
                if (a<=0) {
                    pixels[sp] = 0x00FFFFFF;
                }
                else if (a<255) {
                    aFP = (0x00FF0000/a);
                    pixels[sp] =
                        ((a << 24) |
                         (((((pixel&0xFF0000)>>16)*aFP)&0xFF0000)    ) |
                         (((((pixel&0x00FF00)>>8) *aFP)&0xFF0000)>>8 ) |
                         (((((pixel&0x0000FF))    *aFP)&0xFF0000)>>16));
                }
                sp++;
            }
        }
    }

    protected static void mult_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int base
            = (db.getOffset() +
               sppsm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                               wr.getMinY()-wr.getSampleModelTranslateY()));
        // Access the pixel data array
        final int pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width;
            while (sp < end) {
                int pixel = pixels[sp];
                int a = pixel>>>24;
                if ((a>=0) && (a<255)) {
                    pixels[sp] = ((a << 24) |
                                  ((((pixel&0xFF0000)*a)>>8)&0xFF0000) |
                                  ((((pixel&0x00FF00)*a)>>8)&0x00FF00) |
                                  ((((pixel&0x0000FF)*a)>>8)&0x0000FF));
                }
                sp++;
            }
        }
    }


    protected static void divide_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride  = csm.getPixelStride();
        final int [] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
        final int base
            = (db.getOffset() +
               csm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                             wr.getMinY()-wr.getSampleModelTranslateY()));


        int a=0;
        int aOff = bandOff[bandOff.length-1];
        int bands = bandOff.length-1;
        int b, i;
        // Access the pixel data array
        final byte pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width*pixStride;
            while (sp < end) {
              a = pixels[sp+aOff]&0xFF;
              if (a==0) {
                for (b=0; b<bands; b++)
                  pixels[sp+bandOff[b]] = (byte)0xFF;
              } else if (a<255) {
                int aFP = (0x00FF0000/a);
                for (b=0; b<bands; b++) {
                  i = sp+bandOff[b];
                  pixels[i] = (byte)(((pixels[i]&0xFF)*aFP)>>>16);
                }
              }
              sp+=pixStride;
            }
        }
    }

    protected static void mult_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride  = csm.getPixelStride();
        final int [] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
        final int base
            = (db.getOffset() +
               csm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                             wr.getMinY()-wr.getSampleModelTranslateY()));


        int a=0;
        int aOff = bandOff[bandOff.length-1];
        int bands = bandOff.length-1;
        int b, i;

        // Access the pixel data array
        final byte pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width*pixStride;
            while (sp < end) {
              a = pixels[sp+aOff]&0xFF;
              if (a!=0xFF)
                for (b=0; b<bands; b++) {
                  i = sp+bandOff[b];
                  pixels[i] = (byte)(((pixels[i]&0xFF)*a)>>8);
                }
              sp+=pixStride;
            }
        }
    }

/*
  This is skanky debugging code that might be useful in the future:

            if (count == 33) {
                String label = "sub [" + x + ", " + y + "]: ";
                org.ImageDisplay.showImage
                    (label, subBI);
                org.ImageDisplay.printImage
                    (label, subBI,
                     new Rectangle(75-iR.x, 90-iR.y, 32, 32));
                
            }


            // if ((count++ % 50) == 10)
            //     org.ImageDisplay.showImage("foo: ", subBI);


            Graphics2D realG2D = g2d;
            while (realG2D instanceof sun.java2d.ProxyGraphics2D) {
                realG2D = ((sun.java2d.ProxyGraphics2D)realG2D).getDelegate();
            }
            if (realG2D instanceof sun.awt.image.BufferedImageGraphics2D) {
                count++;
                if (count == 34) {
                    RenderedImage ri;
                    ri = ((sun.awt.image.BufferedImageGraphics2D)realG2D).bufImg;
                    // g2d.setComposite(SVGComposite.OVER);
                    // org.ImageDisplay.showImage("Bar: " + count, cr);
                    org.ImageDisplay.printImage("Bar: " + count, cr,
                                                new Rectangle(75, 90, 32, 32));

                    org.ImageDisplay.showImage ("Foo: " + count, ri);
                    org.ImageDisplay.printImage("Foo: " + count, ri,
                                                new Rectangle(75, 90, 32, 32));

                    System.out.println("BI: "   + ri);
                    System.out.println("BISM: " + ri.getSampleModel());
                    System.out.println("BICM: " + ri.getColorModel());
                    System.out.println("BICM class: " + ri.getColorModel().getClass());
                    System.out.println("BICS: " + ri.getColorModel().getColorSpace());
                    System.out.println
                        ("sRGB CS: " + 
                         ColorSpace.getInstance(ColorSpace.CS_sRGB));
                    System.out.println("G2D info");
                    System.out.println("\tComposite: " + g2d.getComposite());
                    System.out.println("\tTransform" + g2d.getTransform());
                    java.awt.RenderingHints rh = g2d.getRenderingHints();
                    java.util.Set keys = rh.keySet();
                    java.util.Iterator iter = keys.iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();

                        System.out.println("\t" + o.toString() + " -> " +
                                           rh.get(o).toString());
                    }

                    ri = cr;
                    System.out.println("RI: "   + ri);
                    System.out.println("RISM: " + ri.getSampleModel());
                    System.out.println("RICM: " + ri.getColorModel());
                    System.out.println("RICM class: " + ri.getColorModel().getClass());
                    System.out.println("RICS: " + ri.getColorModel().getColorSpace());
                }
            }
*/

}
