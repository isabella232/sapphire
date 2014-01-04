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
import org.eclipse.sapphire.modeling.DelimitedListBindingImpl;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlDelimitedListBindingImpl extends DelimitedListBindingImpl
{
    private final XmlPath path;
    
    public XmlDelimitedListBindingImpl( final XmlPath path )
    {
        this.path = path;
    }

    @Override
    protected final String readListString()
    {
        final Element parent = property().element();
        final XmlElement parentXmlElement = ( (XmlResource) parent.resource() ).getXmlElement();
        
        if( parentXmlElement == null )
        {
            return null;
        }
        
        final XmlNode listXmlNode = parentXmlElement.getChildNode( this.path, false );
        
        if( listXmlNode == null )
        {
            return null;
        }
        
        return listXmlNode.getText();
    }

    @Override
    protected final void writeListString( String str )
    {
        final Element parent = property().element();
        final XmlElement parentXmlElement = ( (XmlResource) parent.resource() ).getXmlElement( true );
        final XmlNode listXmlNode = parentXmlElement.getChildNode( this.path, false );
        
        if( str == null )
        {
            if( listXmlNode != null )
            {
                listXmlNode.remove();
            }
        }
        else
        {
            parentXmlElement.setChildNodeText( this.path, str, false );
        }
    }
    
}
