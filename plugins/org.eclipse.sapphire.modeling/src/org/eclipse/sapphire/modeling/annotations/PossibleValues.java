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

package org.eclipse.sapphire.modeling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.sapphire.modeling.Status;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface PossibleValues
{
    String[] values() default {};

    /**
     * Specifies that the set of possible values for a given value property comes from values
     * located at the specified path in the model. The path must resolve to a value property.
     * 
     * <p>Note that this annotation creates an implied DependsOn relationship to the specified 
     * path. It is not necessary to explicitly specify this relationship.</p>
     */
    
    String property() default "";
    String invalidValueMessage() default "";
    Status.Severity invalidValueSeverity() default Status.Severity.ERROR;
    boolean caseSensitive() default true;
    
    /**
     * Determines if the possible values are already ordered as intended. By default, the order
     * is not treated as significant and possible values are sorted alphabetically when presented.
     */
    
    boolean ordered() default false;
}
