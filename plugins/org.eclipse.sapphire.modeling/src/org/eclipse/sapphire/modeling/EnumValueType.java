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
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnumValueType

    extends ModelMetadataItem
    
{
    private Class<?> enumClass;
    private Map<String,String> resources;
    
    public EnumValueType( final Class<?> enumClass )
    {
        this.enumClass = enumClass;
        this.resources = null;
    }
    
    @Override
    public <A extends Annotation> A getAnnotation( final Class<A> type,
                                                   final boolean localOnly )
    {
        return this.enumClass.getAnnotation( type );
    }

    public <A extends Annotation> A getAnnotation( final Enum<?> enumItem,
                                                   final Class<A> type )
    {
        final Field enumItemField;
        
        try
        {
            enumItemField = this.enumClass.getField( enumItem.name() );
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( e );
        }
        
        return enumItemField.getAnnotation( type );
    }
    
    public Enum<?>[] getItems()
    {
        try
        {
            final Method valuesMethod = this.enumClass.getMethod( "values" ); //$NON-NLS-1$
            return (Enum<?>[]) valuesMethod.invoke( null );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    @Override
    protected String getLabelResourceKeyBase()
    {
        return "$type$";
    }
    
    @Override
    protected String getDefaultLabel()
    {
        String className = this.enumClass.getName();
        final int lastDot = className.lastIndexOf( '.' );
        
        if( lastDot != -1 )
        {
            className = className.substring( lastDot + 1 );
        }
        
        return transformCamelCaseToLabel( className );
    }

    public String getLabel( final Enum<?> enumItem,
                            final boolean longLabel,
                            final CapitalizationType capitalizationType,
                            final boolean includeMnemonic )
    {
        String labelText = null;

        final Label labelAnnotation = getAnnotation( enumItem, Label.class );
        
        if( labelAnnotation != null )
        {
            if( longLabel )
            {
                final String labelResourceKey = enumItem.name() + ".full";
                labelText = getResource( labelResourceKey );
            }
            
            if( labelText == null )
            {
                final String labelResourceKey = enumItem.name() + ".standard";
                labelText = getResource( labelResourceKey );
            }
        }
        
        if( labelText != null )
        {
            labelText = LabelTransformer.transform( labelText, capitalizationType, includeMnemonic );
        }
        else
        {
            labelText = enumItem.name();
        }
        
        return labelText;
    }
    
    @Override
    public synchronized String getResource( final String key )
    {
        if( this.resources == null )
        {
            final ResourceBundle resourceBundle 
                = ResourceBundle.getBundle( this.enumClass.getName(), Locale.getDefault(), 
                                            this.enumClass.getClassLoader() );
            
            this.resources = new HashMap<String,String>();
            
            for( Enumeration<String> keys = resourceBundle.getKeys(); keys.hasMoreElements(); )
            {
                final String k = keys.nextElement();
                this.resources.put( k, resourceBundle.getString( k ) );
            }
        }
        
        return this.resources.get( key );
    }
    
}
