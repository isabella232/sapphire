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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireCondition;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorCondition extends SapphireCondition
{
    public final boolean evaluate()
    {
        final ISapphirePart part = getPart();
        
        if( part instanceof PropertyEditorPart )
        {
            return evaluate( (PropertyEditorPart) part );
        }
        
        return false;
    }
    
    protected abstract boolean evaluate( final PropertyEditorPart part );
    
}
