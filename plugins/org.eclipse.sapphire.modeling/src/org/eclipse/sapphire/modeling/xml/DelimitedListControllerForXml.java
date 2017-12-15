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

import org.eclipse.sapphire.modeling.DelimitedListController;
import org.eclipse.sapphire.modeling.IModelElement;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class DelimitedListControllerForXml<T extends IModelElement>

    extends DelimitedListController<T>

{
    private final XmlPath path;
    
    public DelimitedListControllerForXml( final XmlPath path,
                                          final char delimiter )
    {
        super( ',' );
        
        this.path = path;
    }

    @Override
    protected final String read()
    {
        final IModelElementForXml parent = (IModelElementForXml) getModelElement();
        final XmlElement parentXmlElement = parent.getXmlElement();
        
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
    protected final void write( String str )
    {
        final IModelElementForXml parent = (IModelElementForXml) getModelElement();
        final XmlElement parentXmlElement = parent.getXmlElement( true );
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
