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

import static org.eclipse.sapphire.modeling.util.internal.MiscUtil.contains;
import static org.eclipse.sapphire.modeling.util.internal.MiscUtil.indexOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LayeredListBindingImpl;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class StandardXmlListBindingImpl

    extends LayeredListBindingImpl

{
    protected XmlPath path;
    protected String[] xmlElementNames;
    protected ModelElementType[] modelElementTypes;

    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        initBindingMetadata( element, property, params );
    }
    
    protected void initBindingMetadata( final IModelElement element,
                                        final ModelProperty property,
                                        final String[] params )
    {
        final XmlListBinding annotation = property.getAnnotation( XmlListBinding.class );
        
        if( annotation != null )
        {
            if( annotation.path().length() > 0 )
            {
                this.path = new XmlPath( annotation.path(), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
            }
            
            final XmlListBinding.Mapping[] mappings = annotation.mappings();
            
            this.xmlElementNames = new String[ mappings.length ];
            this.modelElementTypes = new ModelElementType[ mappings.length ];
            
            for( int i = 0; i < mappings.length; i++ )
            {
                final XmlListBinding.Mapping mapping = mappings[ i ];
                
                this.xmlElementNames[ i ] = mapping.element();
                this.modelElementTypes[ i ] = ModelElementType.getModelElementType( mapping.type() );
            }
        }
        else
        {
            this.path = new XmlPath( property.getName(), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
            
            final List<ModelElementType> types = property.getAllPossibleTypes();
            
            this.modelElementTypes = types.toArray( new ModelElementType[ types.size() ] );
            this.xmlElementNames = new String[ this.modelElementTypes.length ];
            
            for( int i = 0; i < this.modelElementTypes.length; i++ )
            {
                this.xmlElementNames[ i ] = this.modelElementTypes[ i ].getSimpleName().substring( 1 );
            }
        }
    }
    
    @Override
    public ModelElementType type( final Resource resource )
    {
        final XmlElement xmlElement = ( (XmlResource) resource ).getXmlElement();
        final String xmlElementName = xmlElement.getDomNode().getLocalName();
        
        for( int i = 0; i < this.xmlElementNames.length; i++ )
        {
            if( this.xmlElementNames[ i ].equals( xmlElementName ) )
            {
                return this.modelElementTypes[ i ];
            }
        }
        
        throw new IllegalStateException();
    }

    @Override
    protected List<?> readUnderlyingList()
    {
        final XmlElement parent = getXmlElement( false );
        
        if( parent == null )
        {
            return Collections.emptyList();
        }
        else
        {
            final List<XmlElement> list = new ArrayList<XmlElement>();
            
            for( XmlElement element : parent.getChildElements() )
            {
                final String xmlElementName = element.getDomNode().getLocalName();
                
                if( contains( this.xmlElementNames, xmlElementName ) )
                {
                    list.add( element );
                }
            }
            
            return list;
        }
    }

    @Override
    protected Object addUnderlyingObject( final ModelElementType type )
    {
        final String xmlElementName = this.xmlElementNames[ indexOf( this.modelElementTypes, type ) ];
        return getXmlElement( true ).addChildElement( xmlElementName );
    }

    @Override
    protected Resource createResource( final Object obj )
    {
        final XmlElement xmlElement = (XmlElement) obj;
        final XmlResource parentXmlResource = (XmlResource) element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
    }
    
    @Override
    public void remove( final Resource resource )
    {
        final XmlResource xmlResource = (XmlResource) resource;
        final XmlElement xmlElement = xmlResource.getXmlElement();
        
        xmlElement.remove();
        
        if( this.path != null )
        {
            final XmlElement base = getBaseXmlElement( false );
            
            if( base != null )
            {
                final XmlElement parent = (XmlElement) base.getChildNode( this.path, false );
                
                if( parent != null && parent.isEmpty() )
                {
                    base.removeChildNode( this.path );
                }
            }
        }
    }

    @Override
    public void swap( final Resource a, 
                      final Resource b )
    {
        final XmlElement x = ( (XmlResource) a ).getXmlElement();
        final XmlElement y = ( (XmlResource) b ).getXmlElement();
        
        x.swap( y );
    }

    protected XmlElement getXmlElement( final boolean createIfNecessary )
    {
        XmlElement parent = getBaseXmlElement( createIfNecessary );
        
        if( parent != null && this.path != null )
        {
            parent = (XmlElement) parent.getChildNode( this.path, createIfNecessary );
        }
        
        return parent;
    }
    
    protected XmlElement getBaseXmlElement( final boolean createIfNecessary )
    {
        final XmlResource resource = (XmlResource) element().resource();
        return resource.getXmlElement( createIfNecessary );
    }
    
}
