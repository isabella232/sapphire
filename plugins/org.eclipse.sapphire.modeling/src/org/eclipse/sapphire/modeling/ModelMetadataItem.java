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

package org.eclipse.sapphire.modeling;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.MapFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ModelMetadataItem 
{
    private List<Annotation> annotations;
    private Map<Class<? extends Annotation>,Annotation> annotationByType;
    
    public ModelMetadataItem getBase()
    {
        return null;
    }
    
    private void initAnnotations()
    {
        synchronized( this )
        {
            if( this.annotations == null )
            {
                final ListFactory<Annotation> annotationsListFactory = ListFactory.start();
                initAnnotations( annotationsListFactory );
                this.annotations = annotationsListFactory.result();
                
                final MapFactory<Class<? extends Annotation>,Annotation> annotationByTypeMapFactory = MapFactory.start();
                
                for( Annotation annotation : this.annotations )
                {
                    annotationByTypeMapFactory.add( annotation.annotationType(), annotation );
                }
                
                this.annotationByType = annotationByTypeMapFactory.result();
            }
        }
    }
    
    protected abstract void initAnnotations( ListFactory<Annotation> annotations );
    
    public <A extends Annotation> List<A> getAnnotations( final Class<A> type )
    {
        final A annotation = getAnnotation( type );
        return ( annotation == null ? Collections.<A>emptyList() : Collections.singletonList( annotation ) );
    }
    
    public <A extends Annotation> A getAnnotation( final Class<A> type )
    {
        initAnnotations();
        return type.cast( this.annotationByType.get( type ) );
    }
    
    public final boolean hasAnnotation( final Class<? extends Annotation> type )
    {
        return ( getAnnotation( type ) != null );
    }
    
    public final String getLabel( final boolean longLabel,
                                  final CapitalizationType capitalizationType,
                                  final boolean includeMnemonic )
    {
        String labelText = null;

        final Label labelAnnotation = getAnnotation( Label.class );
        
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
            labelText = getLocalizationService().text( labelText, capitalizationType, includeMnemonic );
        }
        
        return labelText;
    }
    
    protected abstract String getDefaultLabel();
    
    public abstract LocalizationService getLocalizationService();
    
}
