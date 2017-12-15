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

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.PLUGIN_ID;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.ImageProvider;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireImageCache
{
    public static final ImageDescriptor OVERLAY_ERROR
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/overlays/error.gif" );
    
    public static final ImageDescriptor OVERLAY_WARNING
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/overlays/warning.png" );
    
    public static final ImageDescriptor DECORATOR_ASSIST
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/decorators/assist.png" );
    
    public static final ImageDescriptor DECORATOR_ASSIST_FAINT
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/decorators/assist-faint.png" );
    
    public static final ImageDescriptor DECORATOR_BLANK
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/decorators/blank.png" );
    
    public static final ImageDescriptor ACTION_ADD 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/add.png" );
    
    public static final ImageDescriptor ACTION_EDIT 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/edit.png" );
    
    public static final ImageDescriptor ACTION_DELETE 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/delete.png" );
    
    public static final ImageDescriptor ACTION_MOVE_UP 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/move-up.png" );
    
    public static final ImageDescriptor ACTION_MOVE_DOWN 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/move-down.png" );
    
    public static final ImageDescriptor ACTION_MOVE_LEFT 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/move-left.png" );
    
    public static final ImageDescriptor ACTION_MOVE_RIGHT 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/move-right.png" );
    
    public static final ImageDescriptor ACTION_EXPAND_ALL 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/expand-all.png" );
    
    public static final ImageDescriptor ACTION_COLLAPSE_ALL 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/collapse-all.png" );
    
    public static final ImageDescriptor ACTION_RESTORE_DEFAULTS 
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/restore-defaults.png" );
    
    public static final ImageDescriptor ACTION_BROWSE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/browse.png" );
    
    public static final ImageDescriptor ACTION_BROWSE_MINI
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/browse-mini.png" );
    
    public static final ImageDescriptor ACTION_SELECT_ALL
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/select-all.png" );
    
    public static final ImageDescriptor ACTION_DESELECT_ALL
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/deselect-all.png" );
    
    public static final ImageDescriptor ACTION_SHOW_IN_SOURCE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/show-in-source.png" );
    
    public static final ImageDescriptor ACTION_DEFAULT
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/default.png" );
    
    public static final ImageDescriptor ACTION_HIDE_OUTLINE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/actions/hide-outline.png" );
    
    public static final ImageDescriptor OBJECT_FILE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/file.png" );
    
    public static final ImageDescriptor OBJECT_FOLDER
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/folder.png" );
    
    public static final ImageDescriptor OBJECT_PACKAGE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/package.png" );
    
    public static final ImageDescriptor OBJECT_CONTAINER_NODE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/container-node.png" );
    
    public static final ImageDescriptor OBJECT_LEAF_NODE
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/leaf-node.png" );
    
    public static final ImageDescriptor OBJECT_CHECK_ON
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/check-on.gif" );
    
    public static final ImageDescriptor OBJECT_CHECK_OFF
        = imageDescriptorFromPlugin( PLUGIN_ID, "images/objects/check-off.gif" );
    
    private final Map<ImageDescriptor,ImageHandle> imageDescToImageHandle = new HashMap<ImageDescriptor,ImageHandle>();
    
    private final Map<Class<? extends ImageProvider>,ImageProvider> imageProviderInstances 
        = new WeakHashMap<Class<? extends ImageProvider>,ImageProvider>();
    
    public Image getImage( final ImageDescriptor imageDescriptor )
    {
        return getImage( imageDescriptor, IStatus.OK );
    }

    public Image getImage( final ImageDescriptor imageDescriptor,
                           final int problemSeverity )
    {
        ImageHandle imageHandle = this.imageDescToImageHandle.get( imageDescriptor );
        
        if( imageHandle == null )
        {
            imageHandle = new ImageHandle( imageDescriptor );
            this.imageDescToImageHandle.put( imageDescriptor, imageHandle );
        }
        
        return imageHandle.getImage( problemSeverity );
    }

    public Image getImage( final String imagePath )
    {
        return getImage( imagePath, IStatus.OK );
    }

    public Image getImage( final String imagePath,
                           final int problemSeverity )
    {
        final ImageDescriptor imageDescriptor = getImageDescriptor( imagePath );
        
        if( imageDescriptor != null )
        {
            return getImage( imageDescriptor, problemSeverity );
        }
        
        return null;
    }

    public Image getImage( final ModelElementType type )
    {
        return getImage( type, IStatus.OK );
    }
    
    public Image getImage( final ModelElementType type,
                           final int problemSeverity )
    {
        return getImage( type, null, problemSeverity );
    }
    
    public Image getImage( final IModelElement element )
    {
        return getImage( element, IStatus.OK );
    }
    
    public Image getImage( final IModelElement element,
                           final int problemSeverity )
    {
        return getImage( element.getModelElementType(), element, problemSeverity );
    }
    
    private Image getImage( final ModelElementType type,
                            final IModelElement element,
                            final int problemSeverity )
    {
        final ImageDescriptor imageDescriptor = getImageDescriptor( type, element );
        
        if( imageDescriptor != null )
        {
            return getImage( imageDescriptor, problemSeverity );
        }
        
        return null;
    }
    
    public ImageDescriptor getImageDescriptor( final String imagePath )
    {
        final int slash = imagePath.indexOf( '/' );
        final String pluginId = imagePath.substring( 0, slash );
        final String relPath = imagePath.substring( slash + 1 );
        
        final ImageDescriptor imageDescriptor = imageDescriptorFromPlugin( pluginId, relPath );
        
        if( imageDescriptor == null )
        {
            final String msg = NLS.bind( Resources.couldNotLoadImage, imagePath );
            SapphireUiFrameworkPlugin.logError( msg, null );
        }
        
        return imageDescriptor;
    }

    public ImageDescriptor getImageDescriptor( final ModelElementType type )
    {
        return getImageDescriptor( type, null );
    }
    
    public ImageDescriptor getImageDescriptor( final IModelElement element )
    {
        return getImageDescriptor( element.getModelElementType(), element );
    }
    
    private ImageDescriptor getImageDescriptor( final ModelElementType type,
                                                final IModelElement element )
    {
        final org.eclipse.sapphire.modeling.annotations.Image imageAnnotation
            = type.getAnnotation( org.eclipse.sapphire.modeling.annotations.Image.class );
        
        if( imageAnnotation != null )
        {
            String imagePath = null;
            
            if( imageAnnotation.provider() != ImageProvider.class )
            {
                final Class<? extends ImageProvider> imageProviderClass = imageAnnotation.provider();
                ImageProvider imageProvider = this.imageProviderInstances.get( imageProviderClass );
                
                if( imageProvider == null )
                {
                    try
                    {
                        imageProvider = imageProviderClass.newInstance();
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
    
                    if( imageProvider != null )
                    {
                        this.imageProviderInstances.put( imageProviderClass, imageProvider );
                    }
                }
                
                if( imageProvider != null )
                {
                    if( element != null )
                    {
                        imagePath = imageProvider.getSmallImagePath( element );
                    }
                    else
                    {
                        imagePath = imageProvider.getSmallImagePath( type );
                    }
                }
            }
            else if( imageAnnotation.small().length() > 0 )
            {
                imagePath = imageAnnotation.small();
            }
            
            return getImageDescriptor( imagePath );
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
        
        public Image getImage( final int severity )
        {
            if( this.baseImageDescriptor == null )
            {
                return null;
            }
            
            if( severity == IStatus.ERROR )
            {
                if( this.errorImage == null )
                {
                    final ImageDescriptor desc = new ProblemOverlayImageDescriptor( this.baseImageDescriptor, IStatus.ERROR );
                    this.errorImage = desc.createImage();
                }
                
                return this.errorImage;
            }
            else if( severity == IStatus.WARNING )
            {
                if( this.warningImage == null )
                {
                    final ImageDescriptor desc = new ProblemOverlayImageDescriptor( this.baseImageDescriptor, IStatus.WARNING );
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
    
    private static final class Resources
    
        extends NLS
        
    {
        public static String couldNotLoadImage;
        
        static
        {
            initializeMessages( SapphireImageCache.class.getName(), Resources.class );
        }
    }

}
