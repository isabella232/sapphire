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

import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class XmlResource

    extends Resource
    
{
    private XmlNamespaceResolver xmlNamespaceResolver;

    public XmlResource( final XmlResource parent )
    {
        super( parent );
    }
    
    @Override
    public void init( final IModelElement modelElement )
    {
        super.init( modelElement );
        
        this.xmlNamespaceResolver = new StandardXmlNamespaceResolver( modelElement.getModelElementType() );
    }

    @Override
    public XmlResource parent()
    {
        return (XmlResource) super.parent();
    }

    @Override
    public RootXmlResource root()
    {
        return (RootXmlResource) super.root();
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
    protected BindingImpl createBinding( final ModelProperty property )
    {
        BindingImpl binding = null;
        String[] params = null;
        
        if( property instanceof ValueProperty )
        {
            final CustomXmlValueBinding customBindingAnnotation = property.getAnnotation( CustomXmlValueBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                    params = customBindingAnnotation.params();
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    binding = null;
                }
            }
            else
            {
                binding = new StandardXmlValueBindingImpl();
            }
        }
        else if( property instanceof ElementProperty )
        {
            final CustomXmlElementBinding customBindingAnnotation = property.getAnnotation( CustomXmlElementBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                    params = customBindingAnnotation.params();
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    binding = null;
                }
            }
            else if( property instanceof ImpliedElementProperty )
            {
                binding = new StandardImpliedXmlElementBindingImpl();
            }
            else
            {
                binding = new StandardXmlElementBindingImpl();
            }
        }
        else if( property instanceof ListProperty )
        {
            final CustomXmlListBinding customBindingAnnotation = property.getAnnotation( CustomXmlListBinding.class );
            
            if( customBindingAnnotation != null )
            {
                try
                {
                    binding = customBindingAnnotation.impl().newInstance();
                    params = customBindingAnnotation.params();
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    binding = null;
                }
            }
            else
            {
                binding = new StandardXmlListBindingImpl();
            }
        }
        
        if( binding != null )
        {
            binding.init( element(), property, params );
        }
        
        return binding;
    }
    
}
