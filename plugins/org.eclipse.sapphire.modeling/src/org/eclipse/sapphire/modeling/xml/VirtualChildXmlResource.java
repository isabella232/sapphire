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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class VirtualChildXmlResource

    extends XmlResource
    
{
    private final XmlPath path;

    public VirtualChildXmlResource( final XmlResource parent,
                                    final XmlPath path )
    {
        super( parent );

        this.path = path;
    }

    @Override
    public XmlElement getXmlElement( final boolean createIfNecessary )
    {
        final XmlElement parent = parent().getXmlElement( createIfNecessary );
        XmlElement element = null;
        
        if( parent != null )
        {
            element = (XmlElement) parent.getChildNode( this.path, createIfNecessary );
        }
        
        return element;
    }
    
}
