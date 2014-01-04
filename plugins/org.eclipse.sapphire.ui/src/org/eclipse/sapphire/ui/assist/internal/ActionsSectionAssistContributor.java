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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ActionsSectionAssistContributor extends PropertyEditorAssistContributor
{
    @Text( "Actions" )
    private static LocalizableText sectionLabel;
    
    static
    {
        LocalizableText.init( ActionsSectionAssistContributor.class );
    }

    public ActionsSectionAssistContributor()
    {
        setId( ID_ACTIONS_SECTION_CONTRIBUTOR );
        setPriority( PRIORITY_ACTIONS_SECTION_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final PropertyEditorAssistSection section = context.getSection( SECTION_ID_ACTIONS );
        section.setLabel( sectionLabel.text() );
    }

}
