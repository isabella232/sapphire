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

import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlMetaComment

    extends XmlComment
    
{
    public XmlMetaComment( final XmlElement parent,
                           final Node domNode )
    {
        super( parent, domNode );
    }
    
    public String getName()
    {
        final String text = super.getText();
        final int colon = text.indexOf( ':' );
        
        if( colon == -1 )
        {
            return text;
        }
        else
        {
            return text.substring( 0, colon );
        }
    }
    
    public void setName( final String name )
    {
        validateEdit();
        
        final String n = ( name == null ? EMPTY_STRING : name.trim() );
        super.setText( n + ":" + getText() );
    }

    @Override
    public String getText()
    {
        final String text = super.getText();
        final int colon = text.indexOf( ':' );
        
        if( colon == -1 || colon + 1 == text.length() )
        {
            return EMPTY_STRING;
        }
        else
        {
            return text.substring( colon + 1 );
        }
    }
    
    @Override
    public void setText( final String text )
    {
        validateEdit();
        
        final String txt = ( text == null ? EMPTY_STRING : text.trim() );
        super.setText( getName() + ":" + txt );
    }
    
}
