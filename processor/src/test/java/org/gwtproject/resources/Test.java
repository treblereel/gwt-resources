package org.gwtproject.resources;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/5/18
 */
public class Test {
    static final String OBFUSCATION_PATTERN = "[a-zA-Z][a-zA-Z0-9]*-[a-zA-Z][a-zA-Z0-9]*";

    public static void main(String[] args){
        System.out.println("NTSU22D-o-a".matches(OBFUSCATION_PATTERN ));
    }
}
