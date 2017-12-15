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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.toImageDescriptor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireImageCache
{
    private final Map<ImageDescriptor,ImageHandle> imageDescToImageHandle = new HashMap<ImageDescriptor,ImageHandle>();
    
    private final Map<org.eclipse.sapphire.modeling.ImageData,ImageDescriptor> imageDataToImageDesc 
        = new HashMap<org.eclipse.sapphire.modeling.ImageData,ImageDescriptor>();
    
    public Image getImage( final ImageDescriptor imageDescriptor )
    {
        return getImage( imageDescriptor, Status.Severity.OK );
    }

    public Image getImage( final ImageDescriptor imageDescriptor,
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

    public ImageDescriptor getImageDescriptor( final ModelElementType type )
    {
        return toImageDescriptor( type.image() );
    }
    
    public Image getImage( final ModelElementType type )
    {
        return getImage( type.image() );
    }
    
    public Image getImage( final org.eclipse.sapphire.modeling.ImageData imageData )
    {
        return getImage( imageData, Status.Severity.OK );
    }
    
    public Image getImage( final org.eclipse.sapphire.modeling.ImageData imageData,
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
            
            return getImage( imageDescriptor, severity );
        }
        
        return null;
    }

    public void dispose()
    {
        for( ImageHandle imageHandle : this.imageDescToImageHandle.values() )
        {
            imageHandle.dispose();
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
