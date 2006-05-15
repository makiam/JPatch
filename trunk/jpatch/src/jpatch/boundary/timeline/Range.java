package jpatch.boundary.timeline;

public class Range {
	public int firstTrack = Integer.MAX_VALUE;
	public int lastTrack = Integer.MIN_VALUE;
	public int firstFrame = Integer.MAX_VALUE;
	public int lastFrame = Integer.MIN_VALUE;
	
	public Range() { }
	
	public Range(Range range) {
		this.firstTrack = range.firstTrack;
		this.lastTrack = range.lastTrack;
		this.firstFrame = range.firstFrame;
		this.lastFrame = range.lastFrame;
	}
	
	public String toString() {
		return ("Range tracks:" + firstTrack + ".." + lastTrack + " frames:" + firstFrame + ".." + lastFrame);
	}
}
