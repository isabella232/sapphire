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

package org.eclipse.sapphire.tests.conversion.enumeration;

import org.eclipse.sapphire.modeling.annotations.EnumSerialization;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public enum ThreeChoiceAnswerCustomized
{
    @EnumSerialization( primary = "yes", alternative = { "true", "1" } )
    
    YES,
    
    @EnumSerialization( primary = "maybe", alternative = "0", caseSensitive = false )
    
    MAYBE,
    
    @EnumSerialization( primary = "no", alternative = { "false", "-1" }, caseSensitive = true )
    
    NO
    
}
