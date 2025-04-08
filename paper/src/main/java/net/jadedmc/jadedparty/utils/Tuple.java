package net.jadedmc.jadedparty.utils;

/**
 * Represents two objects stored together.
 * @param <X> First object, aka "Left Object".
 * @param <Y> Second object, aka "Right Object".
 */
public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    /**
     * Creates the Tuple.
     * @param x Left Object.
     * @param y Right Object.
     */
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the first object in the Tuple.
     * @return First object.
     */
    public X getLeft() {
        return this.x;
    }

    /**
     * Gets the second object in the Tuple.
     * @return Second object,
     */
    public Y getRight() {
        return this.y;
    }

    /**
     * Converts the Tuple to a String.
     * @return String representation of the Tuple.
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    /**
     * Compare the Tuple to another object.
     * @param other Object the Tuple is being compared to.
     * @return true if they are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Tuple)){
            return false;
        }

        Tuple<X,Y> other_ = (Tuple<X,Y>) other;

        // this may cause NPE if nulls are valid values for x or y. The logic may be improved to handle nulls properly, if needed.
        return other_.x.equals(this.x) && other_.y.equals(this.y);
    }

    /**
     * Calculates a hash code for the Tuple.
     * @return Tuple's hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }
}