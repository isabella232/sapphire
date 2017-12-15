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
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelMetadataItem 
{
    public ModelMetadataItem getBase()
    {
        return null;
    }
    
    public <A extends Annotation> List<A> getAnnotations( final Class<A> type )
    {
        final A annotation = getAnnotation( type, false );
        return ( annotation == null ? Collections.<A>emptyList() : Collections.singletonList( annotation ) );
    }
    
    public abstract <A extends Annotation> A getAnnotation( final Class<A> type,
                                                            final boolean localOnly );
    
    public final <A extends Annotation> A getAnnotation( final Class<A> type )
    {
        return getAnnotation( type, false );
    }
    
    public final boolean hasAnnotation( final Class<? extends Annotation> type,
                                        final boolean localOnly )
    {
        return ( getAnnotation( type, localOnly ) != null );
    }
    
    public final boolean hasAnnotation( final Class<? extends Annotation> type )
    {
        return ( getAnnotation( type ) != null );
    }
    
    public abstract String getResource( final String key );
    
    public String getLabel( final boolean longLabel,
                            final CapitalizationType capitalizationType,
                            final boolean includeMnemonic )
    {
        String labelText = null;

        final Label labelAnnotation = getAnnotation( Label.class, true );
        
        if( labelAnnotation != null )
        {
            final String labelResourceKeyBase = getLabelResourceKeyBase();
            
            if( longLabel )
            {
                final String labelResourceKey = labelResourceKeyBase + ".full";
                labelText = getResource( labelResourceKey );
            }
            
            if( labelText == null )
            {
                final String labelResourceKey = labelResourceKeyBase + ".standard";
                labelText = getResource( labelResourceKey );
            }
        }
        
        boolean transformNeeded = true;
        
        if( labelText == null )
        {
            final ModelMetadataItem base = getBase();
            
            if( base != null )
            {
                labelText = base.getLabel( longLabel, capitalizationType, includeMnemonic );
                transformNeeded = false;
            }
            else
            {
                labelText = getDefaultLabel();
            }
        }
        
        if( transformNeeded )
        {
            labelText = LabelTransformer.transform( labelText, capitalizationType, includeMnemonic );
        }
        
        return labelText;
    }
    
    protected abstract String getLabelResourceKeyBase();
    
    protected abstract String getDefaultLabel();
    
    protected final String transformCamelCaseToLabel( final String value )
    {
        final StringBuilder label = new StringBuilder();
        
        for( int i = 0, n = value.length(); i < n; i++ )
        {
            final char ch = value.charAt( i );
            
            if( Character.isUpperCase( ch ) )
            {
                if( label.length() > 0 )
                {
                    label.append( ' ' );
                }
                
                label.append( Character.toLowerCase( ch ) );
            }
            else
            {
                label.append( ch );
            }
        }
        
        return label.toString();
    }
    
}
