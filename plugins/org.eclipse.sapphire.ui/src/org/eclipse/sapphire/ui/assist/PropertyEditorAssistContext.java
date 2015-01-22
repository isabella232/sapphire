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

package org.eclipse.sapphire.ui.assist;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyEditorAssistContext
{
    private final SapphirePart part;
    private final Shell shell;
    private final LinkedHashMap<String,PropertyEditorAssistSection> sections; 
    private final Map<String,PropertyEditorAssistSection> sectionsReadOnly; 
    
    public PropertyEditorAssistContext( final SapphirePart part,
                                        final Shell shell )
    {
        this.part = part;
        this.shell = shell;
        this.sections = new LinkedHashMap<String,PropertyEditorAssistSection>();
        this.sectionsReadOnly = Collections.unmodifiableMap( this.sections );
    }
    
    public SapphirePart getPart()
    {
        return this.part;
    }
        
    public Shell getShell()
    {
        return this.shell;
    }
    
    public boolean isEmpty()
    {
        for( PropertyEditorAssistSection section : this.sections.values() )
        {
            if( ! section.getContributions().isEmpty() )
            {
                return false;
            }
        }
        
        return true;
    }
    
    public Map<String,PropertyEditorAssistSection> getSections()
    {
        return this.sectionsReadOnly;
    }
    
    public PropertyEditorAssistSection getSection( final String id )
    {
        PropertyEditorAssistSection section = this.sections.get( id );
        
        if( section == null )
        {
            section = new PropertyEditorAssistSection( id );
            this.sections.put( id, section );
        }
        
        return section;
    }
    
}
