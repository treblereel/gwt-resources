package org.gwtproject.resources.ext;

import java.util.SortedSet;

/**
 * A named deferred binding (property, value) pair for use in generators.
 *
 */
public interface SelectionProperty {

    /**
     * The name of the property.
     *
     * @return the property name as a String.
     * */
    String getName();

    /**
     * The value for the permutation currently being considered.
     *
     * @return the property value as a String.
     */
    String getCurrentValue();

    /**
     * Returns the possible values for the property in sorted order.
     *
     * @return a SortedSet of Strings containing the possible property values.
     */
    SortedSet<String> getPossibleValues();
}
