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

import org.eclipse.sapphire.modeling.Status;

/**
 * Specifies the possible values for a property either statically or via a reference into the model. If more
 * flexibility is needed, {@link PossibleValuesService} can be implemented instead.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface PossibleValues
{
    /**
     * The static set of possible values. Either this attribute or the "property" attribute should be specified,
     * but not both. 
     */
    
    String[] values() default {};

    /**
     * The set of possible values to be drawn from a path into the model. The path end point must be a value
     * property. Either this attribute or the "values" attribute should be specified, but not both. 
     */
    
    String property() default "";
    
    /**
     * The template for the invalid value message using Sapphire EL syntax. A default template is
     * provided if an explicit one is not specified.
     */
    
    String invalidValueMessage() default "";
    
    /**
     * The severity of an invalid value. By default, the severity of error is used.
     */
    
    Status.Severity invalidValueSeverity() default Status.Severity.ERROR;
    
    /**
     * Specifies if the possible values are already ordered as intended. By default, the order
     * is not treated as significant and the possible values are sorted alphabetically when presented.
     */
    
    boolean ordered() default false;
}
