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

package org.eclipse.sapphire.modeling.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelPropertyPropertyTester

    extends PropertyTester
    
{
    private static final String PROP_TYPE = "Type"; //$NON-NLS-1$
    
    public boolean test( final Object receiver, 
                         final String property, 
                         final Object[] args, 
                         final Object value )
    {
        if( receiver instanceof ModelProperty )
        {
            final ModelProperty prop = (ModelProperty) receiver;
            
            if( property.equals( PROP_TYPE ) )
            {
                final Class<?> type = prop.getTypeClass();
                return checkClassName( type, (String) value );
            }
        }

        throw new IllegalStateException();
    }
    
    private static boolean checkClassName( final Class<?> cl,
                                           final String name )
    {
        return cl.getCanonicalName().equals( name );
    }

}
