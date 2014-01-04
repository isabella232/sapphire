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

package org.eclipse.sapphire.tests.modeling.misc.t0004;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface Misc0004TestElementReadOnly extends Misc0004TestElementWritable
{
    ElementType TYPE = new ElementType( Misc0004TestElementReadOnly.class );
    
    // *** Text ***

    @ReadOnly
    
    ValueProperty PROP_TEXT = new ValueProperty( TYPE, Misc0004TestElementWritable.PROP_TEXT );
    
    // *** Integer ***
    
    @ReadOnly

    ValueProperty PROP_INTEGER = new ValueProperty( TYPE, Misc0004TestElementWritable.PROP_INTEGER );

}
