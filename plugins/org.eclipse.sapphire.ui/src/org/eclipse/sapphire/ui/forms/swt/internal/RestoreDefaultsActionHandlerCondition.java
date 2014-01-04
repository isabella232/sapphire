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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.internal.RestoreDefaultsActionHandler.collectProperties;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.forms.SectionPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RestoreDefaultsActionHandlerCondition extends SapphireCondition 
{
    public boolean evaluate()
    {
        final ISapphirePart part = getPart();
        
        if( part instanceof SectionPart )
        {
            final Set<Property> properties = new HashSet<Property>();
            collectProperties( (SectionPart) part, properties );        
            return ! properties.isEmpty();
        }
        
        return false;
    }
    
}

