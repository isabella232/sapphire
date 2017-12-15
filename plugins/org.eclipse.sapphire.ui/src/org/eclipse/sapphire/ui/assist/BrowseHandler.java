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

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphireRenderingContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class BrowseHandler
{
    private IModelElement element;
    private ValueProperty property;
    
    public void init( final IModelElement element,
                      final ValueProperty property,
                      final Map<String,String> params )
    {
        this.element = element;
        this.property = property;
    }
    
    public final IModelElement getModelElement()
    {
        return this.element;
    }
    
    public final ValueProperty getProperty()
    {
        return this.property;
    }

    public String getLabel()
    {
        return Resources.defaultBrowseLabel;
    }
    
    public ImageDescriptor getImageDescriptor()
    {
        return SapphireImageCache.ACTION_BROWSE;
    }
    
    public abstract String browse( SapphireRenderingContext context );
    
    protected final String createBrowseDialogMessage( final String entity )
    {
        return NLS.bind( Resources.browseDialogMessage, entity );
    }
    
    private static final class Resources extends NLS 
    {
        public static String browseDialogMessage;
        public static String defaultBrowseLabel;

        static 
        {
            initializeMessages( BrowseHandler.class.getName(), Resources.class );
        }
    }
    
}