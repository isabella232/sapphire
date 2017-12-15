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

package org.eclipse.sapphire.ui;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePropertyEditorCondition

    extends SapphireCondition
    
{
    public final boolean evaluate()
    {
        final ISapphirePart part = getPart();
        
        if( part instanceof SapphirePropertyEditor )
        {
            return evaluate( (SapphirePropertyEditor) part );
        }
        
        return false;
    }
    
    protected abstract boolean evaluate( final SapphirePropertyEditor part );
    
}
