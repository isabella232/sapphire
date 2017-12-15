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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlAttribute

    extends XmlNode
    
{
    private final Attr domAttr;
    
    public XmlAttribute( final Node domNode, final ModelStoreForXml modelStoreForXml )
    {
        super( domNode, modelStoreForXml );
        this.domAttr = (Attr) domNode;
    }
    
    @Override
    protected String getTextInternal()
    {
        final String text = this.domAttr.getValue();
        
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
        final String txt = ( text == null ? EMPTY_STRING : text );
        this.domAttr.setValue( txt );
    }
    
    @Override
    public void remove()
    {
        Element ownerElm = this.domAttr.getOwnerElement();
        String attrVal = this.domAttr.getName();
        ownerElm.removeAttribute( attrVal );
        
    }
    
}
