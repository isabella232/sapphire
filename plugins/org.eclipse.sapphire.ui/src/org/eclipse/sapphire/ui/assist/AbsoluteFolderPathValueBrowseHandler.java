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

package org.eclipse.sapphire.ui.assist;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsoluteFolderPathValueBrowseHandler 

    extends BrowseHandler
    
{
    @Override
    public String getLabel()
    {
        return Resources.label;
    }

    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_OBJ_FOLDER );    
    }

    @Override
    public String browse( final SapphireRenderingContext context )
    {
        final ValueProperty property = getProperty();
        
        final DirectoryDialog dialog = new DirectoryDialog( context.getShell() );
        dialog.setText( property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
        dialog.setMessage( createBrowseDialogMessage( property.getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
        
        final Value<IPath> value = property.<Value<IPath>>invokeGetterMethod( getModelElement() );
        final IPath path = value.getContent();
        
        if( path != null )
        {
            dialog.setFilterPath( path.toOSString() );
        }
        
        return dialog.open();
    }
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( AbsoluteFolderPathValueBrowseHandler.class.getName(), Resources.class );
        }
    }

}