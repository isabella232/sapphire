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

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class InfoSectionAssistContributor extends PropertyEditorAssistContributor
{
    @Text( "Information" )
    private static LocalizableText sectionLabel;
    
    static
    {
        LocalizableText.init( InfoSectionAssistContributor.class );
    }

    public InfoSectionAssistContributor()
    {
        setId( ID_INFO_SECTION_CONTRIBUTOR );
        setPriority( PRIORITY_INFO_SECTION_CONTRIBUTOR );
    }
    
    @Override
    public void contribute( final PropertyEditorAssistContext context )
    {
        final PropertyEditorAssistSection section = context.getSection( SECTION_ID_INFO );
        section.setLabel( sectionLabel.text() );
    }
    
}
