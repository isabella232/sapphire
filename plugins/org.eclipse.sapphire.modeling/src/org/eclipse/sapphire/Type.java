/******************************************************************************
 * Copyright (c) 2016 Oracle
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
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface Type
{
    Class<?> base();
    Class<?>[] possible() default {};

    /**
     * Specifies if the possible types are already ordered as intended. By default, the order
     * is not treated as significant and the possible types are sorted alphabetically by the type
     * name when presented.
     */
    
    boolean ordered() default false;
    
}
