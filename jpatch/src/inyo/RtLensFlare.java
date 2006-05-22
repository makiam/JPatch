package inyo;

/*
 * Created on Mar 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author David Cuny
 *
 */
public class RtLensFlare { 

	static int clip( int min, int max, int value ) {
		if (value < min) return 0;
		if (value > max) return max;
		return value;
	}
	
	static void drawFlare(RtCanvas canvas, int x, int y, int radius) {
		
		// point off canvas?
		if (x < -canvas.halfWide || x > canvas.halfWide || y < -canvas.halfHigh || y > canvas.halfHigh) {
			// don't render it
			return;
		}
		
		drawSunburst( canvas, x, y, radius, 12 );
		// first halo
		drawHalo( canvas, (int)(x/2), (int)(y/2), (int)(radius/2), 1, 100, 100, 100);
		// small burst
		drawHalo( canvas, (int)(x/3), (int)(y/3), (int)(radius/4), 0, 100, 100, 100);
		// next halo
		drawHalo( canvas, (int)(x/8), (int)(y/8), radius, 1, 100, 100, 100);
		// next burst
		drawHalo( canvas, (int)(-x/2), (int)(-y/2), (int)(radius/2), 0, 100, 100, 100);
		// next halo
		drawHalo( canvas, (int)(-x/4), (int)(-y/4), (int)(radius/4), 1, 100, 100, 100);
		// next burst
		drawHalo( canvas, (int)(-x/5.5), (int)(-y/5.5), (int)(radius/4), 0, 100, 100, 100);
		
		
		// draw main flare
		//drawSunburst( canvas, x, y, (int)(radius*1.2), 20 );
		//drawHalo( canvas, x, y, (int)((double)radius), 0, 220, 220, 120);
		//drawHalo( canvas, x, y, (int)((double)radius*.7f), 1, 180, 80, 80);
		
		// secondary flare
		//drawHalo( canvas, -x, -y, (int)((double)radius*.4f), 2, 80, 80, 180);

		// secondary flare
		//drawHalo( canvas, -x/2, -y/2, (int)((double)radius*.3f), 0, 80, 180, 80);

		// secondary flare
		//drawHalo( canvas, (int)(-x/1.2), (int)(-y/1.2), (int)((double)radius*.3f), 0, 180, 80, 180);

		// secondary flare
		//drawHalo( canvas, (int)(x/3.2), (int)(y/3.2), (int)((double)radius*.7f), 0, 80, 180, 180);
	}
	
	static void drawParticle(RtCanvas canvas, int fx, int fy, int radius) {
		// draw a soft sphere centered at (fx, fy)
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
	            float r2 = x*x + y*y;
	            float c = 1 - r2/(radius*radius);
	            c = c*c;
	            c = c*c;
	            int xx = x+fx;
	            int yy = y+fy;
	            
	            // scale color
	            int intensity = (int)(c*10);
	            
	            // add to intensity
	            canvas.addPixel(RtCanvas.CARTESIAN, xx, yy, intensity, intensity, intensity);	            	
			}
		}
	}
	
	static void drawSunburst( RtCanvas canvas, int x, int y, int radius, int rays ) {

		double angle = 0;
		double deltaAngle = (2 * Math.PI) / rays;
		
		// create a flare for each particle
		for (int i = 0; i < rays; i++) {
			// calculate the dx/dy from the angle
		    double dx = Math.cos(angle);
		    double dy = Math.sin(angle);

		    // start from center
		    float fx = x;
		    float fy = y;
		    
		    // move length of radius
		    for (int j = 0; j < radius; j++) {
		    	// draw the particle
		        drawParticle(canvas, (int)fx, (int)fy, 6);
		        
		        // move ahead
		        fx += dx;
		        fy += dy;
		    }
		    
			// move ahead
			angle += deltaAngle;

		}		
	}
	
	static void drawHalo( RtCanvas canvas, int x, int y, int radius, int style, int red, int green, int blue ) {
		
		float intensity = 0, r;
		
		// iterate over the height of the flare
		for (int dy = -radius; dy <= radius; dy++) {
			// actual position in image
			int imageY = dy+y;
			// iterate over the width of the flare
			for (int dx = -radius; dx <= radius; dx++) {
				// calculate actual position in image
				int imageX = dx+x;
				
				// outside of image?
				if (imageX < -canvas.halfWide || imageY < -canvas.halfHigh || imageX >= canvas.halfWide || imageY >= canvas.halfHigh) {
					break;
				}

				// calculate distance from center
			    r = (float)(Math.sqrt( dx*dx + dy*dy )/((double)radius));

			    // which algorithm are we using?
			    switch (style) {
			    	// blob: (1-r)^2 
			    	case 0:
			    		intensity = 1-r;
			    		intensity = intensity*intensity;			    		
			    		break;
			    		
			    	// ring with quick fade: r^6
			    	case 1:
			    		intensity = r*r;
			    		intensity = intensity*intensity;
			    		intensity = intensity*intensity*intensity;
			    		break;
			    	
			    	case 2:
			    		// ring fading towards center: r
			    		intensity = r;			    		
			    		break;
			    		
		    		// ring of width .2 and radius R
		    		case 3:
			    		intensity = 1-Math.abs(r-.9f)/.1f;
			    		if (intensity < 0) intensity = 0;
			    		intensity = intensity*intensity;
			    		intensity = intensity*intensity;
			    		break;			    			
			    }			    

		    	// smooth dropoff
		    	intensity *= 1-RtShader.smoothstep((float)(1-.01), (float)(1+.01), r);			    	

		    	// add to buffer
			    canvas.addPixel(RtCanvas.CARTESIAN, imageX, imageY, (int)(red*intensity), (int)(green*intensity), (int)(blue*intensity));
			}
			
		}
	}
		
}
