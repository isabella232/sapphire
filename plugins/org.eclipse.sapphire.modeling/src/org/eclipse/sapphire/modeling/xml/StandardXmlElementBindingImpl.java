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

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LayeredElementBindingImpl;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardXmlElementBindingImpl

    extends LayeredElementBindingImpl
    
{
    private XmlPath path;
    private String[] xmlElementNames;
    private ModelElementType[] modelElementTypes;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        final XmlElementBinding xmlElementBindingAnnotation = property.getAnnotation( XmlElementBinding.class );
        
        if( xmlElementBindingAnnotation != null )
        {
            if( xmlElementBindingAnnotation.path().length() > 0 )
            {
                this.path = new XmlPath( xmlElementBindingAnnotation.path(), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
            }
            
            final XmlElementBinding.Mapping[] mappings = xmlElementBindingAnnotation.mappings();
            
            this.xmlElementNames = new String[ mappings.length ];
            this.modelElementTypes = new ModelElementType[ mappings.length ];
            
            for( int i = 0; i < mappings.length; i++ )
            {
                final XmlElementBinding.Mapping mapping = mappings[ i ];
                
                this.xmlElementNames[ i ] = mapping.element();
                this.modelElementTypes[ i ] = ModelElementType.getModelElementType( mapping.type() );
            }
        }
        else
        {
            final XmlBinding xmlBindingAnnotation = property.getAnnotation( XmlBinding.class );
            
            if( xmlBindingAnnotation != null && property.getAllPossibleTypes().size() == 1 )
            {
                final String path = xmlBindingAnnotation.path();
                final int slashIndex = path.lastIndexOf( '/' );
                
                if( slashIndex == -1 )
                {
                    this.xmlElementNames = new String[] { path };
                    this.modelElementTypes = new ModelElementType[] { property.getType() };
                }
                else if( slashIndex > 0 && slashIndex < path.length() - 1 )
                {
                    this.path = new XmlPath( path.substring( 0, slashIndex ), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
                    this.xmlElementNames = new String[] { path.substring( slashIndex + 1 ) };
                    this.modelElementTypes = new ModelElementType[] { property.getType() };
                }
            }
            
            if( this.xmlElementNames == null )
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
    protected Object readUnderlyingObject()
    {
        XmlElement parent = ( (XmlResource) element().resource() ).getXmlElement( false );
        
        if( parent != null )
        {
            if( this.path != null )
            {
                parent = (XmlElement) parent.getChildNode( this.path, false );
            }
            
            if( parent != null )
            {
                for( XmlElement element : parent.getChildElements() )
                {
                    final String xmlElementName = element.getDomNode().getLocalName();
                    
                    if( contains( this.xmlElementNames, xmlElementName ) )
                    {
                        return element;
                    }
                }
            }
        }
        
        return null;
    }

    @Override
    protected Object createUnderlyingObject( final ModelElementType type )
    {
        final String xmlElementName = this.xmlElementNames[ indexOf( this.modelElementTypes, type ) ];
        
        XmlElement parent = ( (XmlResource) element().resource() ).getXmlElement( true );
        
        if( this.path != null )
        {
            parent = (XmlElement) parent.getChildNode( this.path, true );
        }
        
        return parent.getChildElement( xmlElementName, true );
    }

    @Override
    protected Resource createResource( final Object obj )
    {
        final XmlElement xmlElement = (XmlElement) obj;
        final XmlResource parentXmlResource = (XmlResource) element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
    }
    
    @Override
    public void remove()
    {
        XmlElement parent = ( (XmlResource) element().resource() ).getXmlElement( false );
        
        if( parent != null )
        {
            if( this.path != null )
            {
                parent = (XmlElement) parent.getChildNode( this.path, false );
            }
            
            if( parent != null )
            {
                for( XmlElement element : parent.getChildElements() )
                {
                    final String xmlElementName = element.getDomNode().getLocalName();
                    
                    if( contains( this.xmlElementNames, xmlElementName ) )
                    {
                        element.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean removable()
    {
        return true;
    }
    
}
