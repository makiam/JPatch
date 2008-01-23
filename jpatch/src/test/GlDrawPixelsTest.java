package test;

/**
 * This program demonstrates drawing pixels and shows the effect of
 * glDrawPixels(), glCopyPixels(), and glPixelZoom(). Interaction: moving the
 * mouse while pressing the mouse button will copy the image in the lower-left
 * corner of the window to the mouse position, using the current pixel zoom
 * factors. There is no attempt to prevent you from drawing over the original
 * image. If you press the 'r' key, the original image and zoom factors are
 * reset. If you press the 'z' or 'Z' keys, you change the zoom factors.
 * 
 * @author Kiet Le (java port)
 */

import java.awt.Point;
import java.awt.event.*;
import java.nio.ByteBuffer;

import javax.swing.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;

/**
 * This program demonstrates drawing pixels and shows the effect of
 * glDrawPixels(), glCopyPixels(), and glPixelZoom(). Interaction: moving the
 * mouse while pressing the mouse button will copy the image in the lower-left
 * corner of the window to the mouse position, using the current pixel zoom
 * factors. There is no attempt to prevent you from drawing over the original
 * image. If you press the 'r' key, the original image and zoom factors are
 * reset. If you press the 'z' or 'Z' keys, you change the zoom factors.
 * 
 * @author Kiet Le (Java port)
 */
public class GlDrawPixelsTest
  extends JFrame
    implements GLEventListener, KeyListener, MouseMotionListener
{
  private GLCapabilities caps;
  private GLCanvas canvas;
  private GLU glu;
  private GLUT glut;
  //
  private static final int checkImageWidth = 64;
  private static final int checkImageHeight = 64;
  private static final int rgb = 3;
  private byte checkImage[][][];
  private ByteBuffer checkImageBuf = 
  BufferUtil.newByteBuffer(checkImageHeight * checkImageWidth * rgb);

  private static float zoomFactor = 1.0f;
  private static int height;
  private Point mousePoint;// = new Point(); 

  //
  public GlDrawPixelsTest()
  {
    super("image");// name of file as title in window bar
    /*
     * display mode (single buffer and RGBA)
     */
    caps = new GLCapabilities();
    caps.setDoubleBuffered(false);
    System.out.println(caps.toString());
    canvas = new GLCanvas(caps);
    canvas.addGLEventListener(this);
    canvas.addKeyListener(this);
    canvas.addMouseMotionListener(this);
    //
    getContentPane().add(canvas);
  }

  /*
   * Declare initial window size, position, and set frame's close behavior. Open
   * window with "hello" in its title bar. Call initialization routines.
   * Register callback function to display graphics. Enter main loop and process
   * events.
   */
  public void run()
  {
    setSize(250, 250);
    setLocationRelativeTo(null); // center
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
    canvas.requestFocusInWindow();
  }

  public static void main(String[] args)
  {
    new GlDrawPixelsTest().run();
  }

  public void init(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    glu = new GLU();
    glut = new GLUT();
    //
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    gl.glShadeModel(GL.GL_FLAT);
    makeCheckImage();
    gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
  }

  public void display(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    if (mousePoint != null)
    {
      int screeny = height - (int)mousePoint.getY();
      gl.glRasterPos2i(mousePoint.x, screeny);
      gl.glPixelZoom(zoomFactor, zoomFactor);
      gl.glCopyPixels(0, 0, checkImageWidth, checkImageHeight, GL.GL_COLOR);
      //gl.glPixelZoom(1.0f, 1.0f);
      //mousePoint = null;
    }
    else gl.glRasterPos2i(0, 0);

    gl.glDrawPixels(checkImageWidth, checkImageHeight, GL.GL_RGB,
        GL.GL_UNSIGNED_BYTE, checkImageBuf);

    gl.glFlush();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
  {
    GL gl = drawable.getGL();
    
    gl.glViewport(0, 0, w, h);
    height = h;
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0.0, (double) w, 0.0, (double) h);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
      boolean deviceChanged)
  {
  }

  /*
   * 3D array won't be used. I left it here for you to see.
   */
  private void makeCheckImage()
  {
    byte c = (byte) 0xFF;

    for (int i = 0; i < checkImageWidth; i++)
    {
      for (int j = 0; j < checkImageHeight; j++)
      {
        // c = ((((i & 0x8) == 0) ^ ((j & 0x8)) == 0)) * 255;
        c = (byte)( ( ((byte)((i & 0x8)==0?0x00:0xff)//
            ^(byte)((j & 0x8)==0?0x00:0xff))));
        // checkImage[i][j][0] = (byte) c;
        // checkImage[i][j][1] = (byte) c;
        // checkImage[i][j][2] = (byte) c;
        checkImageBuf.put((byte) c);
        checkImageBuf.put((byte) c);
        checkImageBuf.put((byte) c);
      }
    }
    checkImageBuf.rewind();
  }//

  public void keyTyped(KeyEvent e)
  {
  }

  public void keyPressed(KeyEvent key)
  {
    switch (key.getKeyChar()) {
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
      case 'r':
      case 'R':
        zoomFactor = 1.0f;
        System.out.println("zoomFactor reset to 1.0\n");
        break;
      case 'z':
        zoomFactor += 0.5;
        if (zoomFactor >= 3.0) zoomFactor = 3.0f;
        System.out.println("zoomFactor is now " + zoomFactor);
        break;
      case 'Z':
        zoomFactor -= 0.5;
        if (zoomFactor <= 0.5) zoomFactor = 0.5f;
        System.out.println("zoomFactor is now " + zoomFactor);
        break;

      default:
        break;
    }
    canvas.display();
  }

  public void keyReleased(KeyEvent e)
  {
  }

  public void mouseDragged(MouseEvent e)
  {

  }

  public void mouseMoved(MouseEvent mouse)
  {
    mousePoint = mouse.getPoint();
    //screeny = height - (int) mouse.getY();
    canvas.display();
  }

}