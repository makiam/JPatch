
package javaview_test;

import jv.vecmath.PdVector;
import jv.vecmath.PiVector;
import jv.geom.PgElementSet;

public class Triangulierung {

	private
		int numVertices_, numTriangles_;
	    PdVector[] vertices_;
	    PiVector[] triangles_;
	    // double[][] vertices_;
	    // int[][] triangles_;
	    
    public
    	Triangulierung(int anzX, int anzY)
        {
    	     numVertices_ = (anzX)*(anzY);
    	     numTriangles_= 2*(anzX-1)*(anzY-1);
    	  
    	     vertices_ = new PdVector[numVertices_]; // new double[numVertices_][2];
    	     triangles_ = new PiVector[numTriangles_];
    	     
    	     double dx = 1.0/(anzX-1.0);
    	     double dy = 1.0/(anzY-1.0);
    	     
    	     int idx=0;
    	     for (int sp=0; sp < anzX; ++sp)
    	     {
    	    	 double x=sp*dx;
    	    	 
    	     	 for (int ze=0; ze < anzY; ++ze)
    	    	 {
    	             double y=ze*dy;
    	             vertices_[idx] = new PdVector(3); // 3 koordinaten
    	             vertices_[idx].setEntry(0, y);
    	             vertices_[idx].setEntry(1, x);
    	             vertices_[idx].setEntry(2, 0.0);
    	             idx++;
    	         }
    	     }
    	     idx = 0;
    	     for (int sp=0; sp<anzX-1; ++sp)
    	     {
    	    	 for(int ze=0; ze<anzY-1; ++ze)
    	    	 {
    	    		 int i1= ze+sp*anzY;
    	    		 int i2= i1+anzY;
    	    		 int i3= i1+1;
    	    		 int i4= i2+1;
    	    		 
    	    		 triangles_[idx] = new PiVector(3); // 3 punkte
    	    		 triangles_[idx].setEntry(0, i1);
    	    		 triangles_[idx].setEntry(1, i2);
    	    		 triangles_[idx].setEntry(2, i3);
    	    		 idx++;
    	    		 triangles_[idx] = new PiVector(3); // 3 punkte
      	    		 triangles_[idx].setEntry(0, i2);
    	    		 triangles_[idx].setEntry(1, i3);
    	    		 triangles_[idx].setEntry(2, i4);
    	    		 
    	    		 idx++;
    	    		 
    	    	 }
    	     }
        } // konstruktor fertig !
    
    double getX(int vertix) { return vertices_[vertix].getEntry(0); }
    double getY(int vertix) { return vertices_[vertix].getEntry(1); }
    double getZ(int vertix) { return vertices_[vertix].getEntry(2); }
    
    int getNumOfVertices() { return numVertices_; }
    int getNumOfTriangles() { return numTriangles_; }
    
    void   setZ(int vertix, double val) { vertices_[vertix].setEntry(2,val); }
    
    PgElementSet makeGeom()
    {
    	PgElementSet geom = new PgElementSet(3);
    	geom.setNumVertices(numVertices_);
    	geom.setDimOfVertices(3);
    	geom.setVertices(vertices_);
    	
    	geom.setNumElements(numTriangles_);
    	geom.setDimOfElements(3);
    	geom.setElements(triangles_);
    	
    	return geom;
    	
    }
    
    
		
}
