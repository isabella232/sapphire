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

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.DataService;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * Determines whether a property is compatible with the version compatibility target. Most frequently specified
 * via an @Since or an @VersionCompatibility annotation.
 * 
 * <p>When looking for the version compatibility target, the framework will first check the property, then the containing
 * element, then the parent property and the parent element, etc. The search continues until version compatibility target 
 * is found or the model root is reached.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class VersionCompatibilityService extends DataService<VersionCompatibilityService.Data>
{
    @Override
    protected final void initDataService()
    {
        initVersionCompatibilityService();
    }

    protected void initVersionCompatibilityService()
    {
    }
    
    /**
     * Returns the name of the version compatibility target that was used for the last compatibility
     * computation.
     */
    
    public final String versioned()
    {
        final Data data = data();
        return ( data == null ? null : data.versioned() );
    }
    
    /**
     * Returns the version of the version compatibility target that was used for the last compatibility
     * computation. 
     */
    
    public final Version version()
    {
        final Data data = data();
        return ( data == null ? null : data.version() );
    }
    
    /**
     * Returns true if the property is currently compatibility with the version compatibility target,
     * otherwise returns false.
     */
    
    public final boolean compatible()
    {
        final Data data = data();
        return ( data == null ? true : data.compatible() );
    }
    
    public static class Data
    {
        private final boolean compatible;
        private final Version version;
        private final String versioned;
        
        public Data( final boolean compatible,
                     final Version version,
                     final String versioned )
        {
            this.compatible = compatible;
            this.version = version;
            this.versioned = versioned;
        }
        
        public boolean compatible()
        {
            return this.compatible;
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
                return EqualsFactory.start().add( this.compatible, data.compatible ).add( this.version, data.version ).add( this.versioned, data.versioned ).result();
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return HashCodeFactory.start().add( this.compatible ).add( this.version ).add( this.versioned ).result();
        }
    }
    
}
