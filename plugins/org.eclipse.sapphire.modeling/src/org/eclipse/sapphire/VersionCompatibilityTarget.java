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

package org.eclipse.sapphire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the version compatibility target to be referenced by @Since and @VersionCompatibility annotations.
 * 
 * <p>When looking for the version compatibility target, the framework will first check the property, then the containing
 * element, then the parent property and the parent element, etc. The search continues until version compatibility target 
 * is found or the model root is reached.</p>
 *  
 * <p>This annotation supports Sapphire Expression Language.</p>
 * 
 * <p><b>Applicability:</b> Properties, Elements</p>
 * <p><b>Service:</b> ContextVersionService</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )

public @interface VersionCompatibilityTarget
{
    String version();
    String versioned();
}
