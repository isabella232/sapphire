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

package org.eclipse.sapphire.services;

import static org.eclipse.sapphire.modeling.util.MiscUtil.list;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.modeling.ModelPath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DependenciesServiceData
{
    private final Set<ModelPath> dependencies;
    
    public DependenciesServiceData( final Collection<ModelPath> dependencies )
    {
        final Set<ModelPath> clean = new HashSet<ModelPath>();
        
        for( ModelPath dependency : dependencies )
        {
            if( dependency != null )
            {
                clean.add( dependency );
            }
        }
        
        this.dependencies = Collections.unmodifiableSet( clean );
    }
    
    public DependenciesServiceData( final ModelPath... dependencies )
    {
        this( list( dependencies ) );
    }
    
    public Set<ModelPath> dependencies()
    {
        return this.dependencies;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof DependenciesServiceData )
        {
            final DependenciesServiceData data = (DependenciesServiceData) obj;
            return this.dependencies.equals( data.dependencies );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.dependencies.hashCode();
    }
    
}
