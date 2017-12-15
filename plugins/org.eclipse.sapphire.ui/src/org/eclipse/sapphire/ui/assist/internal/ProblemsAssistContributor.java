/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProblemsAssistContributor extends PropertyEditorAssistContributor
{
    private final Status status;
    
    public ProblemsAssistContributor( final Status status )
    {
        setId( ID_PROBLEMS_CONTRIBUTOR );
        setPriority( PRIORITY_PROBLEMS_CONTRIBUTOR );
        this.status = status;
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        if( this.status.children().isEmpty() )
        {
            contribute( context, this.status );
        }
        else
        {
            for( Status child : this.status.children() )
            {
                contribute( context, child );
            }
        }
    }
    
    private static void contribute( final PropertyEditorAssistContext context,
                                    final Status status )
    {
        final Status.Severity valResultSeverity = status.severity();
        ImageData image = null;
        
        if( valResultSeverity == Status.Severity.ERROR )
        {
            image = ImageData.createFromClassLoader( SapphireImageCache.class, "Error.png" );
        }
        else if( valResultSeverity == Status.Severity.WARNING )
        {
            image = ImageData.createFromClassLoader( SapphireImageCache.class, "Warning.png" );
        }
        
        if( image != null )
        {
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            contribution.text( "<li style=\"image\" value=\"problem\">" + escapeForXml( status.message() ) + "</li>" );
            contribution.image( "problem", image );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_PROBLEMS );
            section.addContribution( contribution.create() );
        }
    }
    
}
