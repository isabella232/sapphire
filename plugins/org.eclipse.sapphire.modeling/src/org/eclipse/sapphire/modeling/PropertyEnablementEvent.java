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

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEnablementEvent extends PropertyEvent
{
    private final boolean before;
    private final boolean after;
    
    public PropertyEnablementEvent( final IModelElement element,
                                    final ModelProperty property,
                                    final boolean before,
                                    final boolean after )
    {
        super( element, property );
        
        this.before = before;
        this.after = after;
    }
    
    public boolean before()
    {
        return this.before;
    }
    
    public boolean after()
    {
        return this.after;
    }

}
