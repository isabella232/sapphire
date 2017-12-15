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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProblemsAssistContributor

    extends PropertyEditorAssistContributor
    
{
    private final IStatus status;
    
    public ProblemsAssistContributor( final IStatus status )
    {
        setId( ID_PROBLEMS_CONTRIBUTOR );
        setPriority( PRIORITY_PROBLEMS_CONTRIBUTOR );
        this.status = status;
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        if( this.status.isMultiStatus() )
        {
            for( IStatus child : this.status.getChildren() )
            {
                contribute( context, child );
            }
        }
        else
        {
            contribute( context, this.status );
        }
    }
    
    private static void contribute( final PropertyEditorAssistContext context,
                                    final IStatus status )
    {
        final int valResultSeverity = status.getSeverity();
        String imageKey = null;
        Image image = null;
        
        if( valResultSeverity == Status.ERROR )
        {
            imageKey = "error";
            image = PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK );
        }
        else if( valResultSeverity == Status.WARNING )
        {
            imageKey = "error";
            image = PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK );
        }
        
        if( imageKey != null )
        {
            final PropertyEditorAssistContribution contribution = new PropertyEditorAssistContribution();
            contribution.setText( "<li style=\"image\" value=\"" + imageKey + "\">" + escapeForXml( status.getMessage() ) + "</li>" );
            contribution.setImage( imageKey, image );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_PROBLEMS );
            section.addContribution( contribution );
        }
    }
    
}
