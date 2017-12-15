/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ImpliedElementProperty extends ElementProperty
{
    public ImpliedElementProperty( final ElementType type,
                                   final String propertyName )
    {
        super( type, propertyName );
    }
        
    public ImpliedElementProperty( final ElementType type,
                                   final ImpliedElementProperty baseProperty )
    {
        super( type, baseProperty );
    }
    
}
