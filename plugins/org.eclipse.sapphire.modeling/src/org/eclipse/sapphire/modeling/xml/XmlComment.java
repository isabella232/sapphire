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

import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class XmlComment

    extends XmlNode
    
{
    private final Comment domComment;
    
    public XmlComment( final Node domNode, final ModelStoreForXml modelStoreForXml )
    {
        super( domNode, modelStoreForXml );
        this.domComment = (Comment) domNode;
    }
    
    public Comment getDomComment()
    {
        return this.domComment;
    }
    
    @Override
    protected String getTextInternal()
    {
        final String text = this.domComment.getData();
        
        if( text == null )
        {
            return EMPTY_STRING;
        }
        else
        {
            return text.trim();
        }
    }
    
    @Override
    public void setText( final String text )
    {
        final String txt = ( text == null ? EMPTY_STRING : text.trim() );
        this.domComment.setData( txt );
    }
    
    @Override
    public void remove()
    {
        final Node parent = this.domComment.getParentNode();
        final Node previousSibling = this.domComment.getPreviousSibling();
         
        parent.removeChild( this.domComment );
         
        if( previousSibling.getNodeType() == Node.TEXT_NODE &&
            previousSibling.getNodeValue().trim().length() == 0 )
        {
            parent.removeChild( previousSibling );
        }
    }
    
    
}
