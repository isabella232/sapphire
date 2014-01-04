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

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Resource;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ChildXmlResource extends XmlResource
{
    private final XmlElement xmlElement;

    public ChildXmlResource( final Resource parent,
                             final XmlElement xmlElement )
    {
        super( parent );
        
        this.xmlElement = xmlElement;
    }
    
    @Override
    public void init( final Element modelElement )
    {
        super.init( modelElement );
        
        adapt( RootXmlResource.class ).store().registerModelElement( this.xmlElement.getDomNode(), modelElement );
    }

    @Override
    public XmlElement getXmlElement( final boolean createIfNecessary )
    {
        return this.xmlElement;
    }
    
}
