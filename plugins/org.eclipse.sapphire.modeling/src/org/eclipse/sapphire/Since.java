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

package org.eclipse.sapphire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a basic version constraint that determines whether a property is compatible with the context version.
 * 
 * <p>A basic version constraint is defined as being satisfied by a given version or any higher version, so 
 * <nobr>@Since( "1.2.3" )</nobr> is equivalent to <nobr>@VersionCompatibility( "[1.2.3" )</nobr>.</p>
 * 
 * <p>This annotation supports Sapphire Expression Language.</p>
 * 
 * <p><b>Applicability:</b> Properties</p>
 * <p><b>Service:</b> VersionCompatibilityService</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface Since
{
    String value();
}
