package simplex;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Simplex s = new Simplex(new Vector[] {
				new Vector(0, 0),
				new Vector(1, 0),
				new Vector(0, 1)
		});
		Delaunay d = new Delaunay(s);
		d.add(new Vector(0.5, 0.0));
		d.add(new Vector(0.0, 0.5));
		d.add(new Vector(0.5, 0.5));
		
		Vector v = new Vector(0.3, 0.3);
		Simplex c = d.locate(v);
		System.out.println(v + " is in " + c);
		
	}

}
