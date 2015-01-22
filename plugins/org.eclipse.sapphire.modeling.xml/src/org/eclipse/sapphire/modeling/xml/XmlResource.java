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
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlResource extends Resource
{
    private XmlNamespaceResolver xmlNamespaceResolver;

    public XmlResource( final Resource parent )
    {
        super( parent );
    }
    
    @Override
    public void init( final Element modelElement )
    {
        super.init( modelElement );
        
        this.xmlNamespaceResolver = new StandardXmlNamespaceResolver( modelElement.type() );
    }

    public final XmlNamespaceResolver getXmlNamespaceResolver()
    {
        return this.xmlNamespaceResolver;
    }
    
    public abstract XmlElement getXmlElement( boolean createIfNecessary );
    
    public final XmlElement getXmlElement()
    {
        return getXmlElement( false );
    }
    
    @Override
    protected PropertyBinding createBinding( final Property property )
    {
        PropertyBinding binding = null;
        
        if( property instanceof Value )
        {
            final CustomXmlValueBinding customBindingAnnotation = property.definition().getAnnotation( CustomXmlValueBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    binding = null;
                }
            }
            else
            {
                binding = new StandardXmlValueBindingImpl();
            }
        }
        else if( property instanceof ElementHandle )
        {
            final CustomXmlElementBinding customBindingAnnotation = property.definition().getAnnotation( CustomXmlElementBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    binding = null;
                }
            }
            else if( property.definition() instanceof ImpliedElementProperty )
            {
                binding = new StandardImpliedXmlElementBindingImpl();
            }
            else
            {
                binding = new StandardXmlElementBindingImpl();
            }
        }
        else if( property instanceof ElementList )
        {
            final CustomXmlListBinding customBindingAnnotation = property.definition().getAnnotation( CustomXmlListBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    binding = null;
                }
            }
            else
            {
                binding = new StandardXmlListBindingImpl();
            }
        }
        
        return binding;
    }
    
}
