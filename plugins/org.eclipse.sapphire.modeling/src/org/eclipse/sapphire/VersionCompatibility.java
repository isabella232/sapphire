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
 * Specifies a version constraint that determines whether a property is compatible with the context version.
 * 
 * <p>A version constraint is a boolean expression that can check versions for applicability. In string 
 * format, it is represented as a comma-separated list of specific versions, closed 
 * ranges (expressed using "[1.2.3-4.5)" syntax and open ranges (expressed using "[1.2.3" or "4.5)" 
 * syntax). The square brackets indicate that the range includes the specified version. The parenthesis 
 * indicate that the range goes up to, but does not actually include the specified version.</p>
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

public @interface VersionCompatibility
{
    String value();
}
