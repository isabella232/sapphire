/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [374530] Expose disposed() API on IModelElement 
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface Element extends Observable
{
    ElementType TYPE = new ElementType( Element.class );
    
    Resource resource();
    Element root();
    Property parent();
    <T> T nearest( Class<T> type );    
    Status validation();
    <A> A adapt( Class<A> adapterType );
    
    ElementType type();
    
    <T extends Element> T initialize();
    
    /**
     * Returns all properties of this element.
     * 
     * @return all properties of this element
     */
    
    SortedSet<Property> properties();
    
    Set<Property> properties( String path );
    Set<Property> properties( ModelPath path );
    
    /**
     * Returns the property specified by the given path. Only property name path segments are supported.
     * Using other segments, such as a parent navigation or a type filter, will result in an exception.
     * 
     * @param path the path specifying the property
     * @return the property or null if not found
     * @throws IllegalArgumentException if path is null or if path uses unsupported path segments
     */
    
    Property property( String path );
    
    /**
     * Returns the property specified by the given path. Only property name path segments are supported.
     * Using other segments, such as a parent navigation or a type filter, will result in an exception.
     * 
     * @param path the path specifying the property
     * @return the property or null if not found
     * @throws IllegalArgumentException if path is null or if path uses unsupported path segments
     */
    
    Property property( ModelPath path );
    
    /**
     * Returns the property instance for the given property.
     * 
     * @param property the property
     * @return the property instance
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    Property property( PropertyDef property );
    
    /**
     * Returns the property instance for the given property.
     * 
     * @param property the property
     * @return the property instance
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    <T> Value<T> property( ValueProperty property );
    
    /**
     * Returns the property instance for the given property.
     * 
     * @param property the property
     * @return the property instance
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    <T> Transient<T> property( TransientProperty property );
    
    /**
     * Returns the property instance for the given property.
     * 
     * @param property the property
     * @return the property instance
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    <T extends Element> ElementHandle<T> property( ElementProperty property );

    /**
     * Returns the property instance for the given property.
     * 
     * @param property the property
     * @return the property instance
     * @throws IllegalArgumentException if property is null or does not belong to this element's type
     */
    
    <T extends Element> ElementList<T> property( ListProperty property );
    
    /**
     * Returns all non-empty properties of this element.
     * 
     * @return all non-empty properties of this element
     */
    
    SortedSet<Property> content();
    
    boolean visit( String path, PropertyVisitor visitor );
    boolean visit( ModelPath path, PropertyVisitor visitor );

    void refresh();
    
    /**
     * Determines if this element is empty. An element is empty when all of its properties are empty.
     * 
     * @return true if this element is empty and false otherwise
     */
    
    boolean empty();
    
    /**
     * Clears all properties of this element.
     */
    
    void clear();
    
    /**
     * Copies all properties of the provided source element to this element. The source element does not
     * have to be of the same type as target. Only properties that match on name and type will be copied.
     * 
     * @param source the element to copy from
     * @throws IllegalArgumentException if source is null
     * @throws IllegalStateException if this element or the source element is already disposed
     */
    
    void copy( Element source );
    
    /**
     * Returns the service of the specified type from the element instance service context.
     * 
     * <p>Service Context: <b>Sapphire.Element.Instance</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    <S extends Service> S service( Class<S> type );
    
    /**
     * Returns services of the specified type from the element instance service context.
     * 
     * <p>Service Context: <b>Sapphire.Element.Instance</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the list of services or an empty list if none are available
     */
    
    <S extends Service> List<S> services( Class<S> type );
    
    void attach( Listener listener, String path );
    void attach( Listener listener, ModelPath path );
    void detach( Listener listener, String path );
    void detach( Listener listener, ModelPath path );
    
    void dispose();
    boolean disposed();
}
