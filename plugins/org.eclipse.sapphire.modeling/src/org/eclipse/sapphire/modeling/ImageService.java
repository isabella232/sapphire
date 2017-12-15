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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ImageService

    extends ModelElementService
    
{
    public abstract ImageData provide();
    
    /**
     * Event that should be triggered if the image changes after it was initially provided.
     */
    
    public static final class ImageChangedEvent extends Event
    {
        public ImageChangedEvent( final ImageService service )
        {
            super( service );
        }
        
        @Override
        public ImageService service()
        {
            return (ImageService) super.service();
        }
    }
    
}
