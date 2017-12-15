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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorAssistContext
{
    private final SapphirePropertyEditor propertyEditor;
    private final SapphireRenderingContext context;
    private final LinkedHashMap<String,PropertyEditorAssistSection> sections; 
    private final Map<String,PropertyEditorAssistSection> sectionsReadOnly; 
    
    public PropertyEditorAssistContext( final SapphirePropertyEditor propertyEditor,
                                        final SapphireRenderingContext context )
    {
        this.propertyEditor = propertyEditor;
        this.context = context;
        this.sections = new LinkedHashMap<String,PropertyEditorAssistSection>();
        this.sectionsReadOnly = Collections.unmodifiableMap( this.sections );
    }
    
    public SapphirePropertyEditor getPropertyEditor()
    {
        return this.propertyEditor;
    }
    
    public IModelElement getModelElement()
    {
        return this.propertyEditor.getModelElement();
    }
    
    public ModelProperty getProperty()
    {
        return this.propertyEditor.getProperty().refine( getModelElement() );
    }
    
    public SapphireRenderingContext getUiContext()
    {
        return this.context;
    }
    
    public Shell getShell()
    {
        return this.context.getShell();
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
