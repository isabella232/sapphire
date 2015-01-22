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

package org.eclipse.sapphire.sdk.extensibility.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.ReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ClassReferenceService extends ReferenceService<Class<?>>
{
    @Override
    protected Class<?> compute()
    {
        final String reference = context( Value.class ).text();
        
        Class<?> cl = null;

        if( reference != null )
        {
            try
            {
                cl = Element.class.getClassLoader().loadClass( reference );
            }
            catch( ClassNotFoundException e ) {}
        }
        
        return cl;
    }
    
}
