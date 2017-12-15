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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.LayeredModelElementListController;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElementListControllerForXml<T extends IModelElementForXml>

    extends LayeredModelElementListController<T,XmlElement>

{
    private final Collection<String> xmlElementNames;
    
    public ModelElementListControllerForXml( final Collection<String> xmlElementNames )
    {
        this.xmlElementNames = xmlElementNames;
    }
    
    @Override
    public List<T> refresh( final List<T> content ) 
    {
        final XmlElement parent = getParentXmlElement( false );
        
        if( parent == null )
        {
            return Collections.emptyList();
        }
        else
        {
            final List<XmlElement> xmlElements = new ArrayList<XmlElement>();
            
            for( XmlElement element : parent.getChildElements() )
            {
                final String elementName = element.getDomNode().getLocalName();
                
                if( this.xmlElementNames.contains( elementName ) )
                {
                    xmlElements.add( element );
                }
            }
            
            return refresh( content, xmlElements );
        }
    }
    
    @Override
    public void swap( final T a, 
                      final T b )
    {
        validateEdit();
        a.getXmlElement().swap( b.getXmlElement() );
    }
    
    @Override
    protected XmlElement unwrap( final T obj )
    {
        return obj.getXmlElement();
    }

    protected abstract XmlElement getParentXmlElement( final boolean createIfNecessary );
    
    protected void validateEdit()
    {
        getModelElement().getModel().validateEdit();
    }
    
}
