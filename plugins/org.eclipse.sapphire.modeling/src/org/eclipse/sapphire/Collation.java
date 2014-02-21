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
 * Specifies how property values should be sorted and matched. If more flexibility is needed,
 * {@link CollationService} can be implemented instead.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface Collation
{
    /**
     * Determines whether letter case differences should be respected or ignored.
     * 
     * <p>For example, if this attribute is set to true, values "Amelia" and "amelia" will be
     * treated as equivalent.</p>
     * 
     * <p>Supports Sapphire EL</p>
     */
    
    String ignoreCaseDifferences();
    
    /**
     * Determines whether collation is applied once globally to all property instances or individually
     * to each instance. Applying collation globally uses fewer system resources and makes the collation
     * specification usable by more parts of the framework, but does not allow the specification access
     * to the model. As a consequence, a global collation specification cannot be made to vary based on
     * model state.
     * 
     * <p>By default, collation will be applied at instance level. The framework may optimize the
     * specification to apply at global level if model access is determined to not be required.</p>
     */
    
    boolean global() default false;
}
