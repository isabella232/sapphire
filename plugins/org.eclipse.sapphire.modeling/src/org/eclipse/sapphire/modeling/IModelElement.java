/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface IModelElement

    extends IModelParticle
    
{
    ModelElementType getModelElementType();
    ModelProperty getParentProperty();
    
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
     * Retrieves the specified service for this model element. Not all services will 
     * be available for all model elements. This method will return <code>null</code>
     * if the service is not available.
     * 
     * @param <S> the type of the service
     * @param serviceType the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    <S extends ModelElementService> S service( Class<S> serviceType );
    
    /**
     * Retrieves the standard service for this model element. This service is provided
     * by the Sapphire framework and is available for all model elements. This method
     * will never return <code>null</code>.
     * 
     * @return the standard service
     */
    
    StandardModelElementService service();
    
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
