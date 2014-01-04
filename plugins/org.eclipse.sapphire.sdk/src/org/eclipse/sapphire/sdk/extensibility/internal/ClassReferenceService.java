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

package org.eclipse.sapphire.sdk.extensibility.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.services.ReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ClassReferenceService

    extends ReferenceService

{
    @Override
    public Class<?> resolve( final String className )
    {
        Class<?> cl = null;

        if( className != null )
        {
            try
            {
                cl = Element.class.getClassLoader().loadClass( className );
            }
            catch( ClassNotFoundException e ) {}
        }
        
        return cl;
    }
    
}
