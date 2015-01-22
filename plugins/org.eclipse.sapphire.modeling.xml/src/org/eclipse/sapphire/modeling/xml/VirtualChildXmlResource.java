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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class VirtualChildXmlResource extends XmlResource
{
    private final XmlPath path;
    private Node lastDomNode;

    public VirtualChildXmlResource( final XmlResource parent,
                                    final XmlPath path )
    {
        super( parent );

        this.path = path;
    }
    
    @Override
    public XmlResource parent()
    {
        return (XmlResource) super.parent();
    }

    @Override
    public void init( final Element element )
    {
        super.init( element );
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                removeIfEmpty();
            }
        };
        
        for( Property property : element.properties() )
        {
            property.attach( listener );
        }
    }

    @Override
    public XmlElement getXmlElement( final boolean createIfNecessary )
    {
        final XmlElement parent = parent().getXmlElement( createIfNecessary );
        XmlElement element = null;
        
        if( parent != null )
        {
            element = (XmlElement) parent.getChildNode( this.path, createIfNecessary );
        }
        
        Node node = null;
        
        if( element != null )
        {
            node = element.getDomNode();
        }
        
        if( this.lastDomNode != node )
        {
            final XmlResourceStore store = adapt( RootXmlResource.class ).store();
            final Element modelElement = element();
            
            if( this.lastDomNode != null )
            {
                store.unregisterModelElement( this.lastDomNode, modelElement );
            }
            
            if( node != null )
            {
                store.registerModelElement( node, modelElement );
            }
            
            this.lastDomNode = node;
        }
        
        return element;
    }
    
    private void removeIfEmpty()
    {
        final XmlElement base = parent().getXmlElement( false );
        
        if( base != null )
        {
            final XmlElement element = (XmlElement) base.getChildNode( this.path, false );
            
            if( element != null && element != base && element.isEmpty() )
            {
                base.removeChildNode( this.path );
            }
        }
    }
    
}
