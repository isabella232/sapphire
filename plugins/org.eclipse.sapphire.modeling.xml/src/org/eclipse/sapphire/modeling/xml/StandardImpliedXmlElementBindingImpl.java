/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [334440] ImpliedElementProperty causes NPE if it doesn't have a XmlBinding
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.modeling.ElementBindingImpl;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardImpliedXmlElementBindingImpl

    extends ElementBindingImpl
    
{
    private XmlPath path;
    private Resource resource;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        if( element.service( property, PossibleTypesService.class ).types().size() > 1 )
        {
            throw new IllegalStateException();
        }
        
        String pathString = "";
        
        final XmlElementBinding xmlElementBindingAnnotation = property.getAnnotation( XmlElementBinding.class );
        
        if( xmlElementBindingAnnotation != null )
        {
            if( xmlElementBindingAnnotation.mappings().length > 0 )
            {
                throw new IllegalStateException();
            }
            
            pathString = xmlElementBindingAnnotation.path();
        }
        else
        {
            final XmlBinding xmlBindingAnnotation = property.getAnnotation( XmlBinding.class );
            
            if( xmlBindingAnnotation != null )
            {
                pathString = xmlBindingAnnotation.path();
            }
        }
        
        this.path = new XmlPath( pathString, ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
    }
    
    @Override
    public ModelElementType type( final Resource resource )
    {
        return property().getType();
    }

    @Override
    public Resource read()
    {
        if( this.resource == null )
        {
            final XmlResource parentXmlResource = (XmlResource) element().resource();
            this.resource = new VirtualChildXmlResource( parentXmlResource, this.path );
        }
        
        return this.resource;
    }
    
}
