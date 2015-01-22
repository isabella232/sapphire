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

import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class XmlComment

    extends XmlNode
    
{
    public XmlComment( final XmlElement parent,
                       final Node domNode )
    {
        super( parent.getResourceStore(), parent, domNode );
    }
    
    @Override
    public Comment getDomNode()
    {
        return (Comment) super.getDomNode();
    }
    
    @Override
    public String getText()
    {
        final String text = getDomNode().getData();
        
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
        validateEdit();
        
        final String txt = ( text == null ? EMPTY_STRING : text.trim() );
        getDomNode().setData( txt );
    }
    
    @Override
    public void remove()
    {
        validateEdit();
        
        final Comment comment = getDomNode();
        final Node parent = comment.getParentNode();
        final Node previousSibling = comment.getPreviousSibling();
         
        parent.removeChild( comment );
         
        if( previousSibling.getNodeType() == Node.TEXT_NODE &&
            previousSibling.getNodeValue().trim().length() == 0 )
        {
            parent.removeChild( previousSibling );
        }
    }
    
}
