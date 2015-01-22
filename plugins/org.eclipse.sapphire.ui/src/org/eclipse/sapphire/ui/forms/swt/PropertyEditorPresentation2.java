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

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorPresentation2 extends FormComponentPresentation
{
    public PropertyEditorPresentation2( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    public abstract Rectangle bounds();
    
    public final Point getActionPopupPosition( final int width, final int height )
    {
        final Rectangle bounds = bounds();
        
        final int x = bounds.x - width + bounds.width;
        final int y;
        
        if( display().getBounds().height - ( bounds.y + bounds.height + 1 + height ) < 10 )
        {
            y = bounds.y - height - 1;
        }
        else
        {
            y = bounds.y + bounds.height + 1;
        }
        
        return new Point( x, y );
    }
    
}
