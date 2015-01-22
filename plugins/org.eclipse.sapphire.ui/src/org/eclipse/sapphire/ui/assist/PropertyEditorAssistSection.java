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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyEditorAssistSection
{
    private final String id;
    private String label;
    private final List<PropertyEditorAssistContribution> contributions;
    private final List<PropertyEditorAssistContribution> contributionsReadOnly;
    
    public PropertyEditorAssistSection( final String id )
    {
        this.id = id;
        this.label = null;
        this.contributions = new ArrayList<PropertyEditorAssistContribution>();
        this.contributionsReadOnly = Collections.unmodifiableList( this.contributions );
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    public void setLabel( final String label )
    {
        this.label = label;
    }
    
    public List<PropertyEditorAssistContribution> getContributions()
    {
        return this.contributionsReadOnly;
    }
    
    public void addContribution( final PropertyEditorAssistContribution contribution )
    {
        this.contributions.add( contribution );
    }
    
}
