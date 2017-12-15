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

public final class ElementValidationEvent extends ElementEvent
{
    private final Status before;
    private final Status after;
    
    public ElementValidationEvent( final IModelElement element,
                                   final Status before,
                                   final Status after )
    {
        super( element );
        
        this.before = before;
        this.after = after;
    }
    
    public Status before()
    {
        return this.before;
    }
    
    public Status after()
    {
        return this.after;
    }

}
