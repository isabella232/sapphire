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

import static org.eclipse.sapphire.modeling.localization.LocalizationUtil.transformCamelCaseToLabel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.LocalizationSystem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnumValueType

    extends ModelMetadataItem
    
{
    private Class<?> enumClass;
    private LocalizationService localizationService;
    
    public EnumValueType( final Class<?> enumClass )
    {
        this.enumClass = enumClass;
        this.localizationService = LocalizationSystem.service( enumClass );
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
                labelText = labelAnnotation.full().trim();
            }
            
            if( labelText == null || labelText.length() == 0 )
            {
                labelText = labelAnnotation.standard().trim();
            }
        }
        
        if( labelText == null || labelText.length() == 0 )
        {
            labelText = enumItem.name();
        }

        labelText = getLocalizationService().text( labelText, capitalizationType, includeMnemonic );
        
        return labelText;
    }
    
    @Override
    public LocalizationService getLocalizationService()
    {
        return this.localizationService;
    }

}
