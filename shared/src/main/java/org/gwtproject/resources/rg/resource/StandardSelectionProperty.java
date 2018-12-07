package org.gwtproject.resources.rg.resource;

import org.gwtproject.resources.ext.SelectionProperty;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.SortedSet;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/5/18
 */
public class StandardSelectionProperty implements SelectionProperty {

    private final String name;
    private SortedSet<String> values;
    private final String activeValue;


    public StandardSelectionProperty(String name, String activeValue){
        this.activeValue = activeValue;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCurrentValue() {
        return activeValue;
    }

    @Override
    public SortedSet<String> getPossibleValues() {
        throw new NotImplementedException();
    }
}
