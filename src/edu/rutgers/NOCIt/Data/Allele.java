package edu.rutgers.NOCIt.Data;

public abstract class Allele {
	abstract public int hashCode();
	abstract public boolean equals(Object o);
	abstract public String toString();
	abstract public int getRepeats();
	abstract public int getFraction();
}
