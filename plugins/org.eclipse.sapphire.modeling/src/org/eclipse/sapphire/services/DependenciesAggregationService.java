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

package org.eclipse.sapphire.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyService;

/**
 * Aggregates the data from all applicable dependencies services in order to produce a single set of dependencies.
 * 
 * <p>An implementation of this service is provided with Sapphire. This service is not intended to
 * be implemented by adopters.</p>
 * 
 * @since 0.3.1
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DependenciesAggregationService extends ModelPropertyService
{
    public final Set<ModelPath> dependencies()
    {
        final Set<ModelPath> dependencies = new HashSet<ModelPath>();
        
        for( DependenciesService ds : element().services( property(), DependenciesService.class ) )
        {
            dependencies.addAll( ds.dependencies() );
        }
        
        return Collections.unmodifiableSet( dependencies );
    }

}
