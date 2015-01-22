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
 * Specifies how a reference value property should be resolved when the target of the reference is an
 * element in the same model as the reference. When more flexibility is necessary, {@link ElementReferenceService} or
 * {@link org.eclipse.sapphire.services.ReferenceService} can be implemented instead.
 * 
 * <p>A PossibleValuesService implementation is automatically provided when this annotation is used.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface ElementReference
{
    /**
     * Path through the model from the location of this annotation to the list containing elements being referenced.
     */
    
    String list();
    
    /**
     * Path through the model from an element in the list to the value property used by the reference. 
     */
    
    String key();
}
