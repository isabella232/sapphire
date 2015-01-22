/******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespaces;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public class StandardXmlNamespaceResolver extends XmlNamespaceResolver
{
    private final ElementType type;
    
    public StandardXmlNamespaceResolver( final ElementType type )
    {
        this.type = type;
    }
    
    @Override
    public String resolve( final String prefix )
    {
        final XmlNamespaces xmlNamespacesAnnotation = this.type.getAnnotation( XmlNamespaces.class );

        if( xmlNamespacesAnnotation != null )
        {
            for( XmlNamespace xmlNamespaceAnnotation : xmlNamespacesAnnotation.value() )
            {
                if( xmlNamespaceAnnotation.prefix().equals( prefix ) )
                {
                    return xmlNamespaceAnnotation.uri();
                }
            }
        }
        
        final XmlNamespace xmlNamespaceAnnotation = this.type.getAnnotation( XmlNamespace.class );
        
        if( xmlNamespaceAnnotation != null )
        {
            if( xmlNamespaceAnnotation.prefix().equals( prefix ) )
            {
                return xmlNamespaceAnnotation.uri();
            }
        }

        return null;
    }
    
}
