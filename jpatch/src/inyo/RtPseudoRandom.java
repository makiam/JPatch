package inyo;

/**
 * Provides pseudo-random number generator, for repeatable sequences
 */
public class RtPseudoRandom {

	double seed = 0;

	private static int ia = 16807;
	private static int ib15 = 32768;
	private static int ib16 = 65536;
	private static int IMAX = 2147483647;

	
	public RtPseudoRandom( double seed ) {
		this.seed = seed;
	}
	
	double next() {
	  int iprhi;
	  int ixhi;
	  int k;
	  int leftlo;
	  int loxa;
	  double temp;
	  
	  // seed should not be zero
	  if ( seed == 0 ) {
	    seed = IMAX;
	  }

	  // get the 15 high order bits of SEED.
	  ixhi = (int)(seed / ib16);

	  // get the 16 low bits of SEED and form the low product.
	  loxa = (int)(( seed - ixhi * ib16 ) * ia);

	  // get the 15 high order bits of the low product.
	  leftlo = loxa / ib16;
	  
	  // form the 31 highest bits of the full product.
	  iprhi = ixhi * ia + leftlo;

	  // get overflow past the 31st bit of full product.
	  k = iprhi / ib15;

	  // assemble all the parts and presubtract IMAX.  The parentheses are essential.
	  seed = ( ( ( loxa - leftlo * ib16 ) - IMAX ) + ( iprhi - k * ib15 ) * ib16 ) + k;

	  // Add IMAX back in if necessary.
	  if (seed < 0) {
	    seed += IMAX;
	  }

	  // multiply by 1 / (2**31-1).
	  return seed * 4.656612875e-10;
	}

}
