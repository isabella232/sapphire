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

package org.eclipse.sapphire.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ProblemOverlayImageDescriptor extends CompositeImageDescriptor 
{
    private static final ImageData IMG_ERROR_OVERLAY
        = SwtUtil.createImageData( SwtResourceCache.class, "ErrorOverlay.gif" );
    
    private static final ImageData IMG_WARNING_OVERLAY
        = SwtUtil.createImageData( SwtResourceCache.class, "WarningOverlay.png" );

    private final ImageData base;
    private final ImageData overlay;
    private final Point size;
    
    public ProblemOverlayImageDescriptor( final ImageDescriptor base,
                                          final Status.Severity severity ) 
    {
        this.base = base.getImageData();
        this.overlay = getOverlay( severity );
        this.size = new Point( this.base.width, this.base.height );
    }
    
    protected void drawCompositeImage( final int width, 
                                       final int height ) 
    {
        drawImage( this.base, 0, 0 );
        drawImage( this.overlay, 0, height - this.overlay.height );
    }
    
    protected Point getSize()
    {
        return this.size;
    }
    
    private ImageData getOverlay( final Status.Severity severity )
    {
        if( severity == Status.Severity.ERROR )
        {
            return IMG_ERROR_OVERLAY;
        }
        else if( severity == Status.Severity.WARNING )
        {
            return IMG_WARNING_OVERLAY;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
