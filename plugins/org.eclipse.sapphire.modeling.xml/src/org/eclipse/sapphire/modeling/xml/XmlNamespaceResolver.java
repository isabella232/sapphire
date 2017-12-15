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

package org.eclipse.sapphire.modeling.xml;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class XmlNamespaceResolver
{
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
        
        String namespace = null;
        
        if( prefix.length() > 0 )
        {
            namespace = resolve( prefix );
            
            if( namespace == null || namespace.length() == 0 )
            {
                final String msg = NLS.bind( Resources.couldNotResolveNamespace, name );
                throw new IllegalArgumentException( msg );
            }
        }
        
        return new QName( namespace, localName, prefix );
    }
    
    private static final class Resources extends NLS
    {
        public static String couldNotResolveNamespace; 
        
        static
        {
            initializeMessages( XmlNamespaceResolver.class.getName(), Resources.class );
        }
    }
    
}
