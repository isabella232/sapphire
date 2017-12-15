/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.util.Set;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.util.ReadOnlySetFactory;

/**
 * Aggregates the data from all applicable dependencies services in order to produce a single set of dependencies.
 * 
 * <p>An implementation of this service is provided with Sapphire. This service is not intended to
 * be implemented by adopters.</p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DependenciesAggregationService extends DataService<DependenciesServiceData>
{
    @Override
    protected DependenciesServiceData compute()
    {
        final ReadOnlySetFactory<ModelPath> dependencies = ReadOnlySetFactory.create();
        
        for( DependenciesService ds : context( IModelElement.class ).services( context( ModelProperty.class ), DependenciesService.class ) )
        {
            dependencies.add( ds.dependencies() );
        }
        
        return new DependenciesServiceData( dependencies.export() );
    }
    
    public final Set<ModelPath> dependencies()
    {
        return data().dependencies();
    }

}
