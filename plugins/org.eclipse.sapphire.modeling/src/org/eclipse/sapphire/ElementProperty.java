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

package org.eclipse.sapphire;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ElementProperty extends PropertyDef
{
    public ElementProperty( final ElementType type,
                            final String propertyName )
    {
        super( type, propertyName, null );
    }
        
    public ElementProperty( final ElementType type,
                            final ElementProperty baseProperty )
    {
        super( type, baseProperty.name(), baseProperty );
    }
    
}
