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
 * Specifies a validation rule for a property or an element using Sapphire EL. Multiple rules can be specified
 * by using @{@link Validations} container.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface Validation
{
    /**
     * The rule (expressed using Sapphire EL) that will trigger the validation problem when it 
     * evaluates to false. 
     */
    
    String rule();
    
    /**
     * The message for the validation problem that is generated when the rule evaluates to false.
     */
    
    String message();
    
    /**
     * The severity of the validation problem that is generated when the rule evaluates to false.
     * Can be either error or warning. Defaults to error.
     */
    
    Status.Severity severity() default Status.Severity.ERROR;
}
