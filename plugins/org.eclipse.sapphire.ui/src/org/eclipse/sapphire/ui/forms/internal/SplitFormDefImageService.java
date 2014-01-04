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

package org.eclipse.sapphire.ui.forms.internal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.SplitFormDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SplitFormDefImageService extends ImageService
{
    private static final ImageData IMG_HORIZONTAL_2 = ImageData.readFromClassLoader( SplitFormDef.class, "SplitFormDef-Horizontal-2.png" ).required();
    private static final ImageData IMG_HORIZONTAL_3 = ImageData.readFromClassLoader( SplitFormDef.class, "SplitFormDef-Horizontal-3.png" ).required();
    private static final ImageData IMG_VERTICAL_2 = ImageData.readFromClassLoader( SplitFormDef.class, "SplitFormDef-Vertical-2.png" ).required();
    private static final ImageData IMG_VERTICAL_3 = ImageData.readFromClassLoader( SplitFormDef.class, "SplitFormDef-Vertical-3.png" ).required();
    
    private Listener listener;
    
    @Override
    protected void initImageService()
    {
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
            }
        };
        
        final SplitFormDef def = context( SplitFormDef.class );
        
        def.getSections().attach( this.listener );
        def.getOrientation().attach( this.listener );
    }

    @Override
    protected ImageData compute()
    {
        final SplitFormDef def = context( SplitFormDef.class );
        
        if( def.getOrientation().content() == Orientation.HORIZONTAL )
        {
            if( def.getSections().size() > 2 )
            {
                return IMG_HORIZONTAL_3;
            }
            else
            {
                return IMG_HORIZONTAL_2;
            }
        }
        else
        {
            if( def.getSections().size() > 2 )
            {
                return IMG_VERTICAL_3;
            }
            else
            {
                return IMG_VERTICAL_2;
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            final SplitFormDef def = context( SplitFormDef.class );
            
            def.getSections().detach( this.listener );
            def.getOrientation().detach( this.listener );
            
            this.listener = null;
        }
    }
    
}
