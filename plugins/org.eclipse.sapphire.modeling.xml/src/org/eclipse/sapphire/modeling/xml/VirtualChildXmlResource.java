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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class VirtualChildXmlResource

    extends XmlResource
    
{
    private final XmlPath path;

    public VirtualChildXmlResource( final XmlResource parent,
                                    final XmlPath path )
    {
        super( parent );

        this.path = path;
    }
    
    @Override
    public void init( final IModelElement modelElement )
    {
        super.init( modelElement );
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                removeIfEmpty();
            }
        };
        
        for( ModelProperty property : modelElement.getModelElementType().getProperties() )
        {
            modelElement.addListener( listener, property.getName() );
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
