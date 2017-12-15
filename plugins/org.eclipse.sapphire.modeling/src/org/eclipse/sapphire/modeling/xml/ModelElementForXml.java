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

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElementForXml

    extends ModelElement
    implements IModelElementForXml
    
{
    private XmlElement element;
    
    public ModelElementForXml( final ModelElementType type,
                               final IModelParticle parent,
                               final ModelProperty parentProperty,
                               final XmlElement element )
    {
        super( type, parent, parentProperty );

        this.element = element;
    }
    
    public XmlElement getXmlElement()
    {
        return getXmlElement( false );
    }
    
    public XmlElement getXmlElement( final boolean createIfNecessary )
    {
        return this.element;
    }
    
    public XmlNode getXmlNode( final ModelProperty property )
    {
        return null;
    }
    
}
