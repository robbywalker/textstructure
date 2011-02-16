package main;

public class Pair<U,V> {

	private final U u;
	private final V v;

	public Pair(U u, V v) {
		this.u = u;
		this.v = v;
	}

	public U getFirst() {
		return u;
	}

	public V getSecond() {
		return v;
	}

	@Override
	public String toString() {

		return getFirst().toString() + " " + getSecond().toString();
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (this.toString().equals(o.toString())) return true;
		
		return false;
	}
}
