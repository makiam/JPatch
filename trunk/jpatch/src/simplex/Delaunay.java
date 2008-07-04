package simplex;

import java.util.*;

public class Delaunay {
	private Map<Simplex, Set<Simplex>> neighbors = new HashMap<Simplex, Set<Simplex>>();
	private Simplex prevSimplex;
	
	/**
     * Constructor.
     * @param simplex the initial Simplex.
     */
    public Delaunay (Simplex simplex) {
        neighbors.put(simplex, new HashSet<Simplex>());
    }
    
	public boolean contains(Simplex simplex) {
		return neighbors.containsKey(simplex);
	}
	
	public Collection<Simplex> getSimplices() {
		return neighbors.keySet();
	}
	
	public Simplex locate(Vector point) {
		System.out.println("locating " + point);
//		Simplex simplex = prevSimplex;
//		if (!contains(simplex)) {
//			simplex = null;
//		}
//		
//		Set<Simplex> visitedSimplices = new HashSet<Simplex>();
//		while (simplex != null) {
//			if (visitedSimplices.contains(simplex)) {
//				throw new RuntimeException(); // should not happen
//			}
//			visitedSimplices.add(simplex);
//			Vector corner = simplex.isOutside(point);
//			if (corner == null) {
//				return simplex;
//			}
//			simplex = neighborOpposite(corner, simplex);
//		}
//		
//		System.out.println("Cecking all simplices");
		for (Simplex s : neighbors.keySet()) {
			if (s.isOutside(point) == null) {
				System.out.println(point + " is in " + s);
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
	
	/**
     * Update by replacing one set of Simplices with another.
     * Both sets of simplices must fill the same "hole" in the
     * Triangulation.
     * @param oldSet set of Simplices to be replaced
     * @param newSet set of replacement Simplices
     */
    public void update (Set<Simplex> oldSet, 
                        Set<Simplex> newSet) {
        // Collect all simplices neighboring the oldSet
        Set<Simplex> allNeighbors = new HashSet<Simplex>();
        for (Simplex simplex: oldSet)
            allNeighbors.addAll(neighbors.get(simplex));
        // Delete the oldSet
        for (Simplex simplex: oldSet) {
            for (Simplex n: neighbors.get(simplex))
                neighbors.get(n).remove(simplex);
            neighbors.remove(simplex);
            allNeighbors.remove(simplex);
        }
        // Include the newSet simplices as possible neighbors
        allNeighbors.addAll(newSet);
        // Create entries for the simplices in the newSet
        for (Simplex s: newSet)
            neighbors.put(s, new HashSet<Simplex>());
        // Update all the neighbors info
        for (Simplex s1: newSet)
        for (Simplex s2: allNeighbors) {
            if (!s1.isNeighbor(s2)) continue;
            neighbors.get(s1).add(s2);
            neighbors.get(s2).add(s1);
        }
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
