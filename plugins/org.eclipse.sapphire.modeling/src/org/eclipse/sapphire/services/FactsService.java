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

package org.eclipse.sapphire.services;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.sapphire.util.SortedSetFactory;

/**
 * Produces a list of short statements about a property meant to convey property's semantics. This service is
 * most commonly used as a way to translate machine friendly model annotations into user friendly verbal
 * statements. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class FactsService extends Service
{
    public final SortedSet<String> facts()
    {
        final SortedSet<String> facts = new TreeSet<String>();
        
        facts( facts );
        
        final SortedSetFactory<String> clean = SortedSetFactory.start();
        
        for( String fact : facts )
        {
            if( fact != null )
            {
                fact = fact.trim();
                
                if( fact.length() > 0 )
                {
                    clean.add( fact );
                }
            }
        }
        
        return clean.result();
    }
    
    protected abstract void facts( final SortedSet<String> facts );

}
