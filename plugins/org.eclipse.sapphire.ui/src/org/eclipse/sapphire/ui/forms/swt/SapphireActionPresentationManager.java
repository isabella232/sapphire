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

package org.eclipse.sapphire.ui.forms.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireActionPresentationManager
{
    @Text( "actions" )
    private static LocalizableText defaultLabel;
    
    static
    {
        LocalizableText.init( SapphireActionPresentationManager.class );
    }

    private String label;
    private final Presentation context;
    private final SapphireActionGroup actions;
    private final List<SapphireActionPresentation> presentations;
    private final List<SapphireActionPresentation> presentationsReadOnly;
    
    public SapphireActionPresentationManager( final Presentation context,
                                              final SapphireActionGroup actions )
    {
        this.label = defaultLabel.text();
        this.context = context;
        this.actions = actions;
        this.presentations = new ArrayList<SapphireActionPresentation>();
        this.presentationsReadOnly = Collections.unmodifiableList( this.presentations );
    }
    
    public Presentation context()
    {
        return this.context;
    }
    
    public String getLabel()
    {
        return LabelTransformer.transform( this.label, CapitalizationType.TITLE_STYLE, false );
    }
    
    public void setLabel( final String label )
    {
        if( label == null )
        {
            this.label = defaultLabel.text();
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
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }
    
}
