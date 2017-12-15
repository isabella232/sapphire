/******************************************************************************
 * Copyright (c) 2012 Oracle
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

public final class SapphireActionLocationHintAfter

    extends SapphireActionLocationHint
    
{
    public SapphireActionLocationHintAfter( final String entity )
    {
        super( entity );
    }
    
    public String toString()
    {
        return "after:" + getReferenceEntityId();
    }
    
}
