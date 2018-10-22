package org.gwtproject.resources.client;


/**
 * This is an extension of ClientBundle that allows for name-based lookup of
 * resources. Note that the use of the methods defined within this interface
 * will prevent the compiler from pruning any of the resources declared in the
 * ClientBundle.
 */
public interface ClientBundleWithLookup {

    /**
     * Find a resource by the name of the function in which it is declared.
     *
     * @param name the name of the desired resource
     * @return the resource, or <code>null</code> if no such resource is defined.
     */
    ResourcePrototype getResource(String name);

    /**
     * A convenience method to iterate over all ResourcePrototypes contained in
     * the ClientBundle.
     */
    ResourcePrototype[] getResources();
}
