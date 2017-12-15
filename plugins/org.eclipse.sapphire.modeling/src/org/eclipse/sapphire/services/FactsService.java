/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Produces a list of short statements about a property meant to convey property's semantics. This service is
 * most commonly used as a way to translate machine friendly model annotations into user friendly verbal
 * statements. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class FactsService extends Service
{
    public final List<String> facts()
    {
        final List<String> facts = new ArrayList<String>();
        
        facts( facts );
        
        final List<String> clean = new ArrayList<String>();
        
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
        
        return Collections.unmodifiableList( clean );
    }
    
    protected abstract void facts( final List<String> facts );

}
