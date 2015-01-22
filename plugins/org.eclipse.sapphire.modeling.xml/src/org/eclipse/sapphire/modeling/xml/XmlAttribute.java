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

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.xml.XmlUtil.EMPTY_STRING;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlAttribute

    extends XmlNode
    
{
    public XmlAttribute( final XmlElement parent,
                         final Node domNode )
    {
        super( parent.getResourceStore(), parent, domNode );
    }
    
    @Override
    public Attr getDomNode()
    {
        return (Attr) super.getDomNode();
    }
    
    public String getLocalName()
    {
        final Attr attr = getDomNode();
        
        String name = attr.getLocalName();
        
        if( name == null || name.length() == 0 )
        {
            name = attr.getName();
        }
        
        return name;
    }
    
    @Override
    public String getText()
    {
        final String text = getDomNode().getValue();
        
        if( text == null )
        {
            return EMPTY_STRING;
        }
        else
        {
            return text;
        }
    }
    
    @Override
    public void setText( final String text )
    {
        validateEdit();
        
        final String txt = ( text == null ? EMPTY_STRING : text );
        getDomNode().setValue( txt );
    }
    
    @Override
    public void remove()
    {
        validateEdit();
        
        final Attr attr = getDomNode();
        final Element ownerElm = attr.getOwnerElement();
        final String attrVal = attr.getName();
        ownerElm.removeAttribute( attrVal );
    }
    
}
