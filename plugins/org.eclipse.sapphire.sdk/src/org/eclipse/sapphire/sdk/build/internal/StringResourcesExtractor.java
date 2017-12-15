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

package org.eclipse.sapphire.sdk.build.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.PropertyAccessFunction;
import org.eclipse.sapphire.modeling.el.RootPropertyAccessFunction;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.localization.LocalizationUtil;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.sdk.ISapphireExtensionDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StringResourcesExtractor
{
    public static boolean check( final File file )
    {
        final String fileName = file.getName();
        
        if( fileName.endsWith( ".sdef" ) )
        {
            return true;
        }
        else if( fileName.equals( "sapphire-extension.xml" ) && file.getParentFile().getName().equals( "META-INF" ) )
        {
            return true;
        }
        
        return false;
    }
    
    public static String extract( final File file )
    
        throws Exception
        
    {
        // Gather string resources from the input..
        
        final Set<String> strings = new HashSet<String>();
        final ModelElementType type;
        
        if( file.getName().endsWith( ".sdef" ) )
        {
            type = ISapphireUiDef.TYPE;
        }
        else
        {
            type = ISapphireExtensionDef.TYPE;
            
        }
        
        final IModelElement root = type.instantiate( new RootXmlResource( new XmlResourceStore( file ) ) );
        
        try
        {
            gather( root, strings );
        }
        finally
        {
            root.dispose();
        }
        
        if( strings.isEmpty() )
        {
            return null;
        }
        
        // Build a lookup table with synthesized resource keys.
        
        final Properties resources = new Properties();
        
        for( String string : strings )
        {
            final String digest = LocalizationUtil.createStringDigest( string );
            resources.put( digest, string );
        }
        
        // Serialize the resources file content and return it to caller.
        
        final ByteArrayOutputStream resourcesFileContentBytes = new ByteArrayOutputStream();
        resources.store( resourcesFileContentBytes, null );

        final String resourcesFileContent = new String( resourcesFileContentBytes.toByteArray() );

        return resourcesFileContent;
    }

    private static void gather( final IModelElement element,
                                final Set<String> strings )
    {
        for( ModelProperty property : element.getModelElementType().getProperties() )
        {
            if( property instanceof ValueProperty )
            {
                if( property.hasAnnotation( Localizable.class ) )
                {
                    if( property.getTypeClass() == Function.class )
                    {
                        final Function function = (Function) element.read( (ValueProperty) property ).getContent( false );
                        
                        if( function != null )
                        {
                            gather( function, strings );
                        }
                    }
                    else
                    {
                        final String value = element.read( (ValueProperty) property ).getText( false );
                        
                        if( value != null )
                        {
                            strings.add( value );
                        }
                        
                        final DefaultValue defaultValueAnnotation = property.getAnnotation( DefaultValue.class );
                        
                        if( defaultValueAnnotation != null )
                        {
                            final String defaultValue = defaultValueAnnotation.text();
                            
                            if( defaultValue.length() > 0 )
                            {
                                strings.add( defaultValue );
                            }
                        }
                    }
                }
            }
            else if( property instanceof ListProperty )
            {
                for( IModelElement child : element.read( (ListProperty) property ) )
                {
                    gather( child, strings );
                }
            }
            else if( property instanceof ImpliedElementProperty )
            {
                gather( element.read( (ImpliedElementProperty) property ), strings );
            }
            else if( property instanceof ElementProperty )
            {
                final IModelElement child = element.read( (ElementProperty) property ).element();
                
                if( child != null )
                {
                    gather( child, strings );
                }
            }
        }
    }
    
    private static void gather( final Function function,
                                final Set<String> strings )
    {
        if( function instanceof Literal )
        {
            Object value = ( (Literal) function ).value();
            
            if( value instanceof String )
            {
                strings.add( (String) value );
            }
        }
        else if( function instanceof RootPropertyAccessFunction || function instanceof PropertyAccessFunction )
        {
            return;
        }
        else
        {
            for( Function operand : function.operands() )
            {
                gather( operand, strings );
            }
        }
    }
    
}
