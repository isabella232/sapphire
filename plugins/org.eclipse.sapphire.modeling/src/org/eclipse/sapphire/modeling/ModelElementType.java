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

package org.eclipse.sapphire.modeling;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementType 
    
    extends ModelMetadataItem
    
{
    private final Class<?> modelElementClass;
    private final List<ModelProperty> properties;
    private Map<String,String> resources;
    
    public ModelElementType( final Class<?> modelElementClass )
    {
        this.modelElementClass = modelElementClass;
        this.properties = new ArrayList<ModelProperty>();
        this.resources = null;
    }
    
    public static ModelElementType getModelElementType( final Class<?> modelElementClass )
    {
        return getModelElementType( modelElementClass, true );
    }
    
    public static ModelElementType getModelElementType( final Class<?> modelElementClass,
                                                        final boolean throwExceptionIfNotFound )
    {
        for( Field field : modelElementClass.getFields() )
        {
            if( field.getName().equals( "TYPE" ) ) //$NON-NLS-1$
            {
                try
                {
                    final Object fieldValue = field.get( null );
                    
                    if( fieldValue instanceof ModelElementType )
                    {
                        return (ModelElementType) fieldValue;
                    }
                    else
                    {
                        break;
                    }
                }
                catch( IllegalAccessException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
        
        if( throwExceptionIfNotFound )
        {
            throw new IllegalArgumentException( "Did not find TYPE field on " + modelElementClass.getName() ); //$NON-NLS-1$
        }
        else
        {
            return null;
        }
    }
    
    public Class<?> getModelElementClass()
    {
        return this.modelElementClass;
    }
    
    public String getSimpleName()
    {
        return this.modelElementClass.getSimpleName();
    }
    
    public String getQualifiedName()
    {
        return this.modelElementClass.getName();
    }
    
    public List<ModelProperty> getProperties()
    {
        final TreeMap<String,ModelProperty> properties = new TreeMap<String,ModelProperty>();
        
        for( Class<?> cl : this.modelElementClass.getInterfaces() )
        {
            final ModelElementType t = getModelElementType( cl, false );
            
            if( t != null )
            {
                for( ModelProperty property : t.getProperties() )
                {
                    properties.put( property.getName(), property );
                }
            }
        }
        
        for( ModelProperty property : this.properties )
        {
            properties.put( property.getName(), property );
        }
        
        return new ArrayList<ModelProperty>( properties.values() );
    }
    
    public ModelProperty getProperty( final String propertyName )
    {
        for( ModelProperty property : getProperties() )
        {
            if( property.getName().equalsIgnoreCase( propertyName ) )
            {
                return property;
            }
        }
        
        return null;
    }
    
    void addProperty( final ModelProperty property )
    {
        this.properties.add( property );
    }
    
    @Override
    public <A extends Annotation> A getAnnotation( final Class<A> type,
                                                   final boolean localOnly )
    {
        return this.modelElementClass.getAnnotation( type );
    }

    @Override
    public synchronized String getResource( final String key )
    {
        if( this.resources == null )
        {
            this.resources = new HashMap<String,String>();

            try
            {
                final ResourceBundle resourceBundle 
                    = ResourceBundle.getBundle( this.modelElementClass.getName(), Locale.getDefault(), 
                                                this.modelElementClass.getClassLoader() );
                
                for( Enumeration<String> keys = resourceBundle.getKeys(); keys.hasMoreElements(); )
                {
                    final String k = keys.nextElement();
                    this.resources.put( k, resourceBundle.getString( k ) );
                }
            }
            catch( MissingResourceException e )
            {
                // Intentionally ignoring. It is acceptable for types to not have any resources.
            }
        }
        
        return this.resources.get( key );
    }
    
    @Override
    protected String getLabelResourceKeyBase()
    {
        return "$type$";
    }
    
    @Override
    protected String getDefaultLabel()
    {
        String className = this.modelElementClass.getName();
        final int lastDot = className.lastIndexOf( '.' );
        
        if( lastDot != -1 )
        {
            className = className.substring( lastDot + 1 );
        }
        
        return transformCamelCaseToLabel( className );
    }

    protected static abstract class ModelPropertyInitListener
    {
        public abstract void propertyInitialized( final ModelProperty property );
    }

}
