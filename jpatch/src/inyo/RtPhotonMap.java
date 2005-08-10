// Copyright (c) 2004 David Cuny
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
// associated documentation files (the "Software"), to deal in the Software without restriction, including 
// without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
// sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial 
// portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT 
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


/**
 * 
 * @author David Cuny
 *
 * This class is not implemented yet.
 */

package inyo;
import javax.vecmath.*;

class RtPhotonMap {
    int mapCount = 0;
    int mapSize = 0;
    RtPhoton photonMap[];

    // for finding closest matches
    int foundCount = 0;
    double foundFurthest = 0.0;
    int foundIndex[] = new int[50];
    double foundDistance[] = new double[50];
    
    // create a new photon map
    public RtPhotonMap( int size ) {
        this.mapSize = size;
        this.mapCount = 0;
        // this.photonMap = new rtPhotonMap[ size ];
    }
    
    // set the 
    public final void getRadiance( Point3d origin, int samples ) {
        // seed with an initial value        
        insertIntoFound( 0, origin.distance( photonMap[0].origin ) );
        
        
        // look through the photon map
        for ( int i = 1; i < mapCount; i++ ) {
            // get the photon
            RtPhoton photon = photonMap[i];
            
            // get the distance
            double distance = origin.distance( photonMap[i].origin );
            
            // smaller than prior values?
            if (distance < this.foundFurthest || foundCount < distance ) {
                // insert into the list
                insertIntoFound( i, distance );
            }
        }
    }
    
    // insert the index into the found list
    public final void insertIntoFound( int index, double distance ) {

        // where to insert it
        int insertAt = -1;
        
        for ( int i = 0; i < this.foundCount; i++ ) {
            if (this.foundDistance[i] < distance) {
                insertAt = i;
                break;
            }
        }

        // add to list?
        if (insertAt == -1) {
            // increase list size
            foundCount++;
            // insert at end
            insertAt = foundCount-1;
        }

        // scoot things over
        for ( int i = foundCount-1; i > insertAt; i-- ) {
            foundIndex[i] = foundIndex[i]-1;
            foundDistance[i] = foundDistance[i];
        }
        
        // insert item
        foundIndex[insertAt] = index;
        foundDistance[insertAt] = distance;
        
        // furthest item found?        
        this.foundFurthest = distance;
        }
    }