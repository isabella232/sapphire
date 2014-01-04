/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlNamespaceResolver
{
    @Text( "Could not resolve namespace for {0} node name." )
    private static LocalizableText couldNotResolveNamespace; 
    
    static
    {
        LocalizableText.init( XmlNamespaceResolver.class );
    }

    public abstract String resolve( final String prefix );
    
    public final QName createQualifiedName( final String name )
    {
        String prefix = "";
        String localName = null;
        final int colon = name.indexOf( ':' );
        
        if( colon == -1 )
        {
            localName = name;
        }
        else
        {
            prefix = name.substring( 0, colon );
            localName = name.substring( colon + 1 );
        }
        
        String namespace = resolve( prefix );
            
        if( prefix.length() != 0 && ( namespace == null || namespace.length() == 0 ) )
        {
            final String msg = couldNotResolveNamespace.format( name );
            throw new IllegalArgumentException( msg );
        }
        
        return new QName( namespace, localName, prefix );
    }
    
}
