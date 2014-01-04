/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "rawtypes" ) // ILabelProvider is parameterized since Eclipse 4.4

public final class ValueLabelProvider implements ILabelProvider
{
    private final PropertyEditorPart part;
    private final ValueProperty property;
    private final LocalizationService localizationService;
    private final ValueLabelService valueLabelService;
    private final ValueImageService valueImageService;
    
    public ValueLabelProvider( final PropertyEditorPart part,
                               final ValueProperty property )
    {
        if( part == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.part = part;
        this.property = property;
        
        this.localizationService = this.part.definition().adapt( LocalizationService.class );
        this.valueLabelService = this.property.service( ValueLabelService.class );
        this.valueImageService = this.property.service( ValueImageService.class );
        
    }
    public String getText( final Object element )
    {
        final String value = (String) element;
        String label = null;
        
        try
        {
            label = this.valueLabelService.provide( value );
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        if( label == null )
        {
            label = value;
        }
        else if( ! label.equals( value ) )
        {
            label = this.localizationService.transform( label, CapitalizationType.FIRST_WORD_ONLY, false );
        }
        
        return label;
    }

    public Image getImage( final Object element )
    {
        final ImageData imageData = getImageData( element );
        Image image = null;
        
        if( imageData != null )
        {
            image = this.part.getSwtResourceCache().image( imageData );
        }
        
        return image;
    }
    
    public ImageData getImageData( final Object element )
    {
        ImageData imageData = null;
        
        try
        {
            imageData = this.valueImageService.provide( (String) element );
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return imageData;
    }

    public boolean isLabelProperty( final Object element,
                                    final String property )
    {
        return true;
    }

    public void addListener( final ILabelProviderListener listener )
    {
    }

    public void removeListener( final ILabelProviderListener listener )
    {
    }
    
    public void dispose()
    {
    }
    
}
