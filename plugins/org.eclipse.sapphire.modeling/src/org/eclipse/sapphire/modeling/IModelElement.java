/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [374530] Expose disposed() API on IModelElement 
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.services.Service;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface IModelElement extends IModelParticle
{
    ModelElementType type();
    ModelProperty getParentProperty();
    
    <T extends IModelElement> T initialize();
    
    <T extends ModelProperty> T property( String name );
    List<ModelProperty> properties();
    
    Object read( ModelProperty property );
    Object read( String property );
    
    <T> Value<T> read( ValueProperty property );
    
    <T extends IModelElement> ModelElementHandle<T> read( ElementProperty property );

    <T extends IModelElement> ModelElementList<T> read( ListProperty property );

    <T> Transient<T> read( TransientProperty property );
    
    SortedSet<String> read( ModelPath path );

    void read( ModelPath path,
               Collection<String> result );
    
    void write( ModelProperty property,
                Object value );

    void write( String property,
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
     * trigger. This method variant will not force caching of properties that have
     * not been accessed.
     * 
     * @param property the name of the property to refresh
     */
    
    void refresh( String property );
    
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
     * trigger. In the case of element or list property, nested properties will
     * not be refreshed. 
     * 
     * @param property the name of the property to refresh
     * @param force whether to force caching of property even if it has not been
     *   accessed
     */
    
    void refresh( String property,
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
     * Updates the cached value of the specified model property if the underlying
     * storage has changed. If the value is updated, a property change event will
     * trigger. 
     * 
     * @param property the name of the property to refresh
     * @param force whether to force caching of property even if it has not been
     *   accessed
     * @param deep whether to refresh nested properties for element and list
     *   properties; has no effect for value properties
     */
    
    void refresh( String property,
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
    <S extends Service> S service( String property, Class<S> serviceType );
    <S extends Service> List<S> services( ModelProperty property, Class<S> serviceType );
    <S extends Service> List<S> services( String property, Class<S> serviceType );
    
    boolean enabled( ModelProperty property );
    boolean enabled( String property );
    
    /**
     * Determines if the specified property is empty. The empty state is defined as follows:
     * 
     * <ul>
     *   <li><b>Value Property</b> - has null value or has default value</li>
     *   <li><b>Element Property</b> - element does not exist</li>
     *   <li><b>Implied Element Property</b> - none of the child element's properties are non-empty</li>
     *   <li><b>List Property</b> - list size is zero</li>
     *   <li><b>Transient Property</b> - has null content</li>
     * </ul>
     * 
     * @param property the property to check
     * @return true if the specified property is empty, false otherwise
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    boolean empty( ModelProperty property );

    /**
     * Determines if the specified property is empty. The empty state is defined as follows:
     * 
     * <ul>
     *   <li><b>Value Property</b> - has null value or has default value</li>
     *   <li><b>Element Property</b> - element does not exist</li>
     *   <li><b>Implied Element Property</b> - none of the child element's properties are non-empty</li>
     *   <li><b>List Property</b> - list size is zero</li>
     *   <li><b>Transient Property</b> - has null content</li>
     * </ul>
     * 
     * @param property the name of the property to check
     * @return true if the specified property is empty, false otherwise
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    boolean empty( String property );
    
    Status validation( ModelProperty property );
    Status validation( String property );
    
    boolean attach( Listener listener );
    void attach( Listener listener, String path );
    void attach( Listener listener, ModelPath path );
    void attach( Listener listener, ModelProperty property );
    boolean detach( Listener listener );
    void detach( Listener listener, String path );
    void detach( Listener listener, ModelPath path );
    void detach( Listener listener, ModelProperty property );
    
    void dispose();
    boolean disposed();
}
