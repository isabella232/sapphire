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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.SapphirePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertiesViewContributionManager
{
    private final SapphirePart part;
    private final PropertiesViewContributorDef def;
    private final Element element;
    private PropertiesViewContributionPart propertiesViewContribution;
    private boolean propertiesViewContributionInitialized;
    
    public PropertiesViewContributionManager( final SapphirePart part )
    {
        this( part, part.getModelElement() );
    }
    
    public PropertiesViewContributionManager( final SapphirePart part,
                                              final Element element )
    {
        this( part, element, (PropertiesViewContributorDef) part.definition() );
    }
    
    public PropertiesViewContributionManager( final SapphirePart part,
                                              final Element element,
                                              final PropertiesViewContributorDef def )
    {
        this.part = part;
        this.element = element;
        this.def = def;
    }
    
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( ! this.propertiesViewContributionInitialized )
        {
            final PropertiesViewContributionDef def = this.def.getPropertiesViewContribution();
            
            if( ! def.getPages().isEmpty() )
            {
                this.propertiesViewContribution = new PropertiesViewContributionPart();
                this.propertiesViewContribution.init( this.part, this.element, def, this.part.getParams() );
                this.propertiesViewContribution.initialize();
            }
            
            this.propertiesViewContributionInitialized = true;
        }
        
        return this.propertiesViewContribution;
    }
    
}
