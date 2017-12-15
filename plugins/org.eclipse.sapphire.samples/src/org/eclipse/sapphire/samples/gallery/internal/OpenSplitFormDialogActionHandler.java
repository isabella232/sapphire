/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.swt.SapphireDialog;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class OpenSplitFormDialogActionHandler extends SapphireActionHandler
{
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = context.getPart().getLocalModelElement();
        
        final SapphireDialog dialog 
            = new SapphireDialog( context.getShell(), element, "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/gallery/GalleryEditor.sdef!SplitFormDialog" );
        
        dialog.open();
        
        return null;
    }
    
}
