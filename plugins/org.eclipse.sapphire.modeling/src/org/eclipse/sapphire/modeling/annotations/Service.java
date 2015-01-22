/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attaches a custom service implementation to an element or a property.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE, ElementType.FIELD } )

public @interface Service
{
    /**
     * The service implementation class.
     */
    
    Class<? extends org.eclipse.sapphire.services.Service> impl();
    
    /**
     * The parameters to pass to the service after it is instantiated.
     */
    
    Param[] params() default {};
    
    /**
     * The ids of services that should not be active when this service is active.
     */
    
    String[] overrides() default {};
    
    /**
     * The context where the service should be activated. Can be either {@link Context.METAMODEL} or {@link Context.INSTANCE}. 
     * The default is {@link Context.INSTANCE}. 
     */
    
    Context context() default Context.INSTANCE;
    
    /**
     * Holds a single service parameter.
     */
    
    @interface Param
    {
        /**
         * Parameter name.
         */
        
        String name();
        
        /**
         * Parameter value.
         */
        
        String value();
    }
    
    /**
     * An enumeration of available service contexts.
     */
    
    enum Context
    {
        /**
         * Designates the definition of an element or a property. When a service is attached at metamodel context,
         * all instances share the same copy of the service and the service implementation is not able to access
         * the instances.  
         */
        
        METAMODEL,
        
        /**
         * Designates an instance of an element or a property. When a service is attached at instance context,
         * each instance has its own copy of the service and the service implementation is able to access
         * the instance via <nobr><code>context(Element.class)</code></nobr> or <code><nobr>context(Property.class)</nobr></code>
         * invocation.
         */
        
        INSTANCE
    }
    
}
