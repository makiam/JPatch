package simplex;

import java.util.*;

public class Delaunay {
	private Map<Simplex, Set<Simplex>> neighbors;
	private Simplex prevSimplex;
	
	public boolean contains(Simplex simplex) {
		return neighbors.containsKey(simplex);
	}
	
	public Simplex locate(Vector point) {
		Simplex simplex = prevSimplex;
		if (!contains(simplex)) {
			simplex = null;
		}
		
		Set<Simplex> visitedSimplices = new HashSet<Simplex>();
		while (simplex != null) {
			if (visitedSimplices.contains(simplex)) {
				throw new RuntimeException(); // should not happen
			}
			visitedSimplices.add(simplex);
			Vector corner = simplex.isOutside(point);
			if (corner == null) {
				return simplex;
			}
			simplex = neighborOpposite(corner, simplex);
		}
		
		System.out.println("Cecking all simplices");
		for (Simplex s : neighbors.keySet()) {
			if (s.isOutside(point) == null) {
				return s;
			}
		}
		
		System.out.println("No simplex holds " + point);
		return null;
	}
	
	public void add(Vector site) {
		Set<Simplex> newSimplices = new HashSet<Simplex>();
		Set<Simplex> oldSimplices = new HashSet<Simplex>();
		Set<Simplex> doneSimplices = new HashSet<Simplex>();
		Queue<Simplex> queue = new LinkedList<Simplex>();
		
		Simplex simplex = locate(site);
		
		if (simplex == null || simplex.contains(site)) {
			return;
		}
		
		queue.add(simplex);
		while (!queue.isEmpty()) {
			simplex = queue.remove();
			if (simplex.vsCircumcircle(site) == 1) {
				continue;
			}
			oldSimplices.add(simplex);
			for (Simplex s : neighbors.get(simplex)) {
				if (doneSimplices.contains(simplex)) {
					continue;
				}
				doneSimplices.add(simplex);
				queue.add(simplex);
			}
		}
		
		for (Vector[] facet : Simplex.getBoundary(oldSimplices)) {
			Vector[] newSimplex = new Vector[facet.length + 1];
			System.arraycopy(facet, 0, newSimplex, 0, facet.length);
			newSimplex[facet.length] = site;
			newSimplices.add(new Simplex(newSimplex));
		}
		
		update(oldSimplices, newSimplices);
	}
	
	public Simplex neighborOpposite(Vector vertex, Simplex simplex) {
		if (!simplex.contains(vertex)) {
			throw new IllegalArgumentException(vertex + " not in " + simplex);
		}
		simplexLoop:
		for (Simplex neighbor : neighbors.get(simplex)) {
			for (Vector v : simplex.getVertices()) {
				if (v == vertex) {
					continue;
				}
				if (!neighbor.contains(v)) {
					continue simplexLoop;
				}
			}
			return neighbor;
		}
		return null;
	}
}
