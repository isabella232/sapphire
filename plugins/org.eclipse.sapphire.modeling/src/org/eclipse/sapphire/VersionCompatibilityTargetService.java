/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.DataService;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * Produces the version compatibility target to be referenced by VersionCompatibilityService. Most frequently specified
 * via an @VersionCompatibilityTarget annotation.
 * 
 * <p>When looking for the version compatibility target, the framework will first check the property, then the containing
 * element, then the parent property and the parent element, etc. The search continues until version compatibility target 
 * is found or the model root is reached.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class VersionCompatibilityTargetService extends DataService<VersionCompatibilityTargetService.Data>
{
    @Override
    protected final void initDataService()
    {
        initContextVersionService();
    }

    protected void initContextVersionService()
    {
    }
    
    public final Version version()
    {
        final Data data = data();
        return ( data == null ? null : data.version() );
    }
    
    public final String versioned()
    {
        final Data data = data();
        return ( data == null ? null : data.versioned() );
    }
    
    public static VersionCompatibilityTargetService find( final Element element,
                                                          final PropertyDef property )
    {
        VersionCompatibilityTargetService service = element.property( property ).service( VersionCompatibilityTargetService.class );
        
        if( service == null )
        {
            service = element.service( VersionCompatibilityTargetService.class );
            
            if( service == null )
            {
                final Property parent = element.parent();
                
                if( parent != null )
                {
                    service = find( parent.element(), parent.definition() );
                }
            }
        }
        
        return service;
    }
    
    public static final class Data
    {
        private final Version version;
        private final String versioned;
        
        public Data( final Version version,
                     final String versioned )
        {
            this.version = version;
            this.versioned = versioned;
        }
        
        public Version version()
        {
            return this.version;
        }
        
        public String versioned()
        {
            return this.versioned;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof Data )
            {
                final Data data = (Data) obj;
                return EqualsFactory.start().add( this.version, data.version ).add( this.versioned, data.versioned ).result();
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return HashCodeFactory.start().add( this.version ).add( this.versioned ).result();
        }
    }
    
}
