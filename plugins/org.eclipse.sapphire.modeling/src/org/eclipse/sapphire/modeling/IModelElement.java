/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.services.Service;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface IModelElement extends IModelParticle
{
    ModelElementType getModelElementType();
    ModelProperty getParentProperty();
    
    Object read( ModelProperty property );
    
    <T> Value<T> read( ValueProperty property );
    
    <T extends IModelElement> ModelElementHandle<T> read( ElementProperty property );

    <T extends IModelElement> T read( ImpliedElementProperty property );
    
    <T extends IModelElement> ModelElementList<T> read( ListProperty property );

    <T> Transient<T> read( TransientProperty property );
    
    SortedSet<String> read( ModelPath path );

    void read( ModelPath path,
               Collection<String> result );
    
    void write( ValueProperty property,
                Object value );
    
    void write( TransientProperty property,
                Object value );
    
    /**
     * Updates the cached value of the specified model property if the underlying
     * storage has changed. If the value is updated, a property change event will
     * trigger. This method variant will not force caching of properties that have
     * not been accessed.
     * 
     * @param property the property to refresh
     */
    
    void refresh( ModelProperty property );
    
    /**
     * Updates the cached value of the specified model property if the underlying
     * storage has changed. If the value is updated, a property change event will
     * trigger. In the case of element or list property, nested properties will
     * not be refreshed. 
     * 
     * @param property the property to refresh
     * @param force whether to force caching of property even if it has not been
     *   accessed
     */
    
    void refresh( ModelProperty property,
                  boolean force );

    /**
     * Updates the cached value of the specified model property if the underlying
     * storage has changed. If the value is updated, a property change event will
     * trigger. 
     * 
     * @param property the property to refresh
     * @param force whether to force caching of property even if it has not been
     *   accessed
     * @param deep whether to refresh nested properties for element and list
     *   properties; has no effect for value properties
     */
    
    void refresh( ModelProperty property,
                  boolean force,
                  boolean deep );
    
    /**
     * Updates the cached values of all properties of this model element. This method
     * variant will not force caching of properties that have not been accessed.
     */
    
    void refresh();
    
    /**
     * Updates the cached values of all properties of this model element. In the case 
     * of element or list property, nested properties will not be refreshed.
     * 
     * @param force whether to force caching of properties that have not been
     *   accessed
     */
    
    void refresh( boolean force );

    /**
     * Updates the cached values of all properties of this model element. 
     * 
     * @param force whether to force caching of properties that have not been
     *   accessed
     * @param deep whether to refresh nested properties for element and list
     *   properties; has no effect for value properties
     */
    
    void refresh( boolean force,
                  boolean deep );
    
    /**
     * Copies the values of the provided element's properties to this element. This
     * operation overwrites any previous values held by this element. The type of the
     * provided element must be identical to the type of this element. 
     * 
     * @param element the element to copy
     * @throws IllegalArgumentException if the type of the provided element does not
     *   match the type of this element
     */
    
    void copy( IModelElement element );

    /**
     * Retrieves the specified service for this model element. Not all services will 
     * be available for all model elements. This method will return <code>null</code>
     * if the service is not available.
     * 
     * @param <S> the type of the service
     * @param serviceType the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    <S extends Service> S service( Class<S> serviceType );
    <S extends Service> List<S> services( Class<S> serviceType );
    
    <S extends Service> S service( ModelProperty property, Class<S> serviceType );
    <S extends Service> List<S> services( ModelProperty property, Class<S> serviceType );
    
    boolean isPropertyEnabled( ModelProperty property );
    
    void addListener( ModelElementListener listener );
    void addListener( ModelPropertyListener listener, String path );
    void addListener( ModelPropertyListener listener, ModelPath path );
    void removeListener( ModelElementListener listener );
    void removeListener( ModelPropertyListener listener, String path );
    void removeListener( ModelPropertyListener listener, ModelPath path );
    void notifyPropertyChangeListeners( ModelProperty property );
    
    void dispose();
}
