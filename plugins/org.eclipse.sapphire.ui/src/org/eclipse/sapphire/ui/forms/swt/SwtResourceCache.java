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

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.toImageDescriptor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.ProblemOverlayImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SwtResourceCache
{
    private final Map<ImageDescriptor,ImageHandle> imageDescToImageHandle = new HashMap<ImageDescriptor,ImageHandle>();
    
    private final Map<org.eclipse.sapphire.ImageData,ImageDescriptor> imageDataToImageDesc 
        = new HashMap<org.eclipse.sapphire.ImageData,ImageDescriptor>();
    
    private final Map<org.eclipse.sapphire.Color,Color> colors = new HashMap<org.eclipse.sapphire.Color,Color>();
    
    public Image image( final ImageDescriptor imageDescriptor )
    {
        return image( imageDescriptor, Status.Severity.OK );
    }

    public Image image( final ImageDescriptor imageDescriptor,
                        final Status.Severity problemSeverity )
    {
        ImageHandle imageHandle = this.imageDescToImageHandle.get( imageDescriptor );
        
        if( imageHandle == null )
        {
            imageHandle = new ImageHandle( imageDescriptor );
            this.imageDescToImageHandle.put( imageDescriptor, imageHandle );
        }
        
        return imageHandle.getImage( problemSeverity );
    }

    public Image image( final ElementType type )
    {
        return image( type.image() );
    }
    
    public Image image( final org.eclipse.sapphire.ImageData imageData )
    {
        return image( imageData, Status.Severity.OK );
    }
    
    public Image image( final org.eclipse.sapphire.ImageData imageData,
                        final Status.Severity severity )
    {
        if( imageData != null )
        {
            ImageDescriptor imageDescriptor = this.imageDataToImageDesc.get( imageData );
            
            if( imageDescriptor == null )
            {
                imageDescriptor = toImageDescriptor( imageData );
                this.imageDataToImageDesc.put( imageData, imageDescriptor );
            }
            
            return image( imageDescriptor, severity );
        }
        
        return null;
    }
    
    public Color color( final org.eclipse.sapphire.Color color )
    {
        return color( color, null );
    }

    public Color color( final org.eclipse.sapphire.Color color,
                        final org.eclipse.sapphire.Color def )
    {
        final org.eclipse.sapphire.Color sapphireColor = ( color == null ? def : color );
        
        if( sapphireColor != null )
        {
            Color swtColor = this.colors.get( sapphireColor );
            
            if( swtColor == null )
            {
                swtColor = new Color( Display.getCurrent(), sapphireColor.red(), sapphireColor.green(), sapphireColor.blue() );
                this.colors.put( sapphireColor, swtColor );
            }
            
            return swtColor;
        }
        
        return null;
    }
    
    public void dispose()
    {
        for( ImageHandle imageHandle : this.imageDescToImageHandle.values() )
        {
            imageHandle.dispose();
        }
        
        for( Color color : this.colors.values() )
        {
            color.dispose();
        }
    }
    
    private static final class ImageHandle
    {
        private final ImageDescriptor baseImageDescriptor;
        private Image baseImage;
        private Image warningImage;
        private Image errorImage;
        
        public ImageHandle( final ImageDescriptor baseImageDescriptor )
        {
            this.baseImageDescriptor = baseImageDescriptor;
        }
        
        public Image getImage( final Status.Severity severity )
        {
            if( this.baseImageDescriptor == null )
            {
                return null;
            }
            
            if( severity == Status.Severity.ERROR )
            {
                if( this.errorImage == null )
                {
                    final ImageDescriptor desc = new ProblemOverlayImageDescriptor( this.baseImageDescriptor, Status.Severity.ERROR );
                    this.errorImage = desc.createImage();
                }
                
                return this.errorImage;
            }
            else if( severity == Status.Severity.WARNING )
            {
                if( this.warningImage == null )
                {
                    final ImageDescriptor desc = new ProblemOverlayImageDescriptor( this.baseImageDescriptor, Status.Severity.WARNING );
                    this.warningImage = desc.createImage();
                }
                
                return this.warningImage;
            }
            else
            {
                if( this.baseImage == null )
                {
                    this.baseImage = this.baseImageDescriptor.createImage();
                }
                
                return this.baseImage;
            }
        }
        
        public void dispose()
        {
            if( this.baseImage != null )
            {
                this.baseImage.dispose();
            }
            
            if( this.errorImage != null )
            {
                this.errorImage.dispose();
            }
            
            if( this.warningImage != null )
            {
                this.warningImage.dispose();
            }
        }
    }

}
