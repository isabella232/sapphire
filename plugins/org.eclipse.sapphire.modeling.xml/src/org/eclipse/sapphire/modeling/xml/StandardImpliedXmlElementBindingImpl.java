/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.ElementPropertyBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardImpliedXmlElementBindingImpl extends ElementPropertyBinding
{
    private XmlPath path;
    private Resource resource;
    
    @Override
    public void init( final Property property )
    {
        super.init( property );
        
        if( property.service( PossibleTypesService.class ).types().size() > 1 )
        {
            throw new IllegalStateException();
        }
        
        String pathString = "";
        
        final XmlElementBinding xmlElementBindingAnnotation = property.definition().getAnnotation( XmlElementBinding.class );
        
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
            final XmlBinding xmlBindingAnnotation = property.definition().getAnnotation( XmlBinding.class );
            
            if( xmlBindingAnnotation != null )
            {
                pathString = xmlBindingAnnotation.path();
            }
            else
            {
                pathString = property.name();
            }
        }
        
        this.path = new XmlPath( pathString, ( (XmlResource) property.element().resource() ).getXmlNamespaceResolver() );
    }
    
    @Override
    public ElementType type( final Resource resource )
    {
        return property().definition().getType();
    }

    @Override
    public Resource read()
    {
        if( this.resource == null )
        {
            final XmlResource parentXmlResource = (XmlResource) property().element().resource();
            this.resource = new VirtualChildXmlResource( parentXmlResource, this.path );
        }
        
        return this.resource;
    }
    
}
