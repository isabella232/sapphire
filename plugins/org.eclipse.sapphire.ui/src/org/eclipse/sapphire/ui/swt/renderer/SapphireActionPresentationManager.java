/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireActionPresentationManager
{
    private String label;
    private final SapphireRenderingContext context;
    private final SapphireActionGroup actions;
    private final List<SapphireActionPresentation> presentations;
    private final List<SapphireActionPresentation> presentationsReadOnly;
    
    public SapphireActionPresentationManager( final SapphireRenderingContext context,
                                              final SapphireActionGroup actions )
    {
        this.label = Resources.defaultLabel;
        this.context = context;
        this.actions = actions;
        this.presentations = new ArrayList<SapphireActionPresentation>();
        this.presentationsReadOnly = Collections.unmodifiableList( this.presentations );
    }
    
    public String getLabel()
    {
        return LabelTransformer.transform( this.label, CapitalizationType.TITLE_STYLE, false );
    }
    
    public void setLabel( final String label )
    {
        if( label == null )
        {
            this.label = Resources.defaultLabel;
        }
        else
        {
            this.label = label;
        }
    }
    
    public List<SapphireActionPresentation> getPresentations()
    {
        return this.presentationsReadOnly;
    }
    
    void addPresentation( final SapphireActionPresentation presentation )
    {
        this.presentations.add( presentation );
    }
    
    public SapphireRenderingContext getContext()
    {
        return this.context;
    }
    
    public SapphireActionGroup getActionGroup()
    {
        return this.actions;
    }
    
    public List<SapphireAction> getActions()
    {
        return this.actions.getActions();
    }
    
    public void dispose()
    {
        for( SapphireActionPresentation presentation : this.presentations )
        {
            try
            {
                presentation.dispose();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String defaultLabel;
    
        static
        {
            initializeMessages( SapphireActionPresentationManager.class.getName(), Resources.class );
        }
    }
    
}
