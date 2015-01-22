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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ProblemsAssistContributor extends PropertyEditorAssistContributor
{
    private SapphirePart part;
    private Listener partValidationListener;
    
    public ProblemsAssistContributor()
    {
        setId( ID_PROBLEMS_CONTRIBUTOR );
        setPriority( PRIORITY_PROBLEMS_CONTRIBUTOR );
    }
    
    @Override
    public void init( final SapphirePart part )
    {
        this.part = part;
        
        if( this.part != null )
        {
	        this.partValidationListener = new FilteredListener<PartValidationEvent>()
	        {
	            @Override
	            protected void handleTypedEvent( final PartValidationEvent event )
	            {
	                broadcast();
	            }
	        };
	        
	        this.part.attach( this.partValidationListener );
        }
    }

    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final Status validation = this.part.validation();
        
        if( validation.children().isEmpty() )
        {
            contribute( context, validation );
        }
        else
        {
            for( Status child : validation.children() )
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
            image = ImageData.readFromClassLoader( SwtResourceCache.class, "Error.png" ).required();
        }
        else if( valResultSeverity == Status.Severity.WARNING )
        {
            image = ImageData.readFromClassLoader( SwtResourceCache.class, "Warning.png" ).required();
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

    @Override
    public void dispose()
    {
        if( this.partValidationListener != null )
        {
            this.part.detach( this.partValidationListener );
        }
    }
    
}
