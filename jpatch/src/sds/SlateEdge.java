package sds;

public class SlateEdge {
	final Level2Vertex vertex;
	final SlateEdge pair;
	final HalfEdge parentEdge;
	final Face parentFace;
	final boolean primary;
	Slate2 slate;
	
	public SlateEdge(Level2Vertex firstVertex, Level2Vertex secondVertex, HalfEdge parentEdge0, HalfEdge parentEdge1) {
		this.pair = new SlateEdge(this, parentEdge1, secondVertex);
		this.parentEdge = parentEdge0;
		this.vertex = firstVertex;
		this.parentFace = null;
		this.primary = true;
	}
	
	public SlateEdge(Slate2 slate, Level2Vertex firstVertex, Level2Vertex secondVertex, Face parentFace) {
		this.slate = slate;
		this.pair = new SlateEdge(slate, this, parentFace, secondVertex);
		this.parentFace = parentFace;
		this.vertex = firstVertex;
		this.parentEdge = null;
		this.primary = true;
	}
	
	private SlateEdge(SlateEdge pair, HalfEdge parentEdge, Level2Vertex vertex) {
		this.pair = pair;
		this.parentEdge = parentEdge;
		this.vertex = vertex;
		this.parentFace = null;
		this.primary = false;
	}
	
	private SlateEdge(Slate2 slate, SlateEdge pair, Face parentFace, Level2Vertex vertex) {
		this.slate = slate;
		this.pair = pair;
		this.parentFace = parentFace;
		this.vertex = vertex;
		this.parentEdge = null;
		this.primary = false;
	}
	
	public SlateEdge getPrimary() {
		return primary ? this : pair;
	}
	
	@Override
	public String toString() {
		return super.toString() + ":" + slate;
	}
}