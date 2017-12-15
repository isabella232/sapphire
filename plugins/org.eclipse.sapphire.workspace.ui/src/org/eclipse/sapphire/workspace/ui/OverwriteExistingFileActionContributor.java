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

package org.eclipse.sapphire.workspace.ui;

import static org.eclipse.sapphire.workspace.CreateWorkspaceFileOp.PROBLEM_FILE_EXISTS;

import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class OverwriteExistingFileActionContributor extends PropertyEditorAssistContributor
{
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final CreateWorkspaceFileOp op = (CreateWorkspaceFileOp) context.getModelElement();
        
        if( op.getFileName().validation().contains( PROBLEM_FILE_EXISTS ) )
        {
            final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
            
            contribution.text( "<p><a href=\"action\" nowrap=\"true\">" + escapeForXml( Resources.action ) + "</a></p>" );
            
            contribution.link
            (
                "action",
                new Runnable()
                {
                    public void run()
                    {
                        op.setOverwriteExistingFile( true );
                    }
                }
            );
            
            final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
            section.addContribution( contribution.create() );
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String action;
        
        static
        {
            initializeMessages( OverwriteExistingFileActionContributor.class.getName(), Resources.class );
        }
    }
    
}
