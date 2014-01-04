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

package org.eclipse.sapphire.modeling.xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )

public @interface XmlValueBinding
{
    String path();
    
    /**
     * Instructs the XML binding system to treat the existence of the node 
     * specified by the path as the property value. This overrides the default behavior
     * of using the text content of the node at the end of the path for property
     * value. This setting expects a string of format "x;y" where the string in front
     * of the semicolon is used if the node exists and the string after the semicolon
     * is used if the node does not exist. The semicolon can be escaped by using "\;"
     * syntax.
     */
    
    String mapExistanceToValue() default "";
    
    /**
     * Controls whether the node specified by the path is removed if the property
     * is set to null. By default, the node will be removed and this is the appropriate
     * behavior under most circumstances.
     */
    
    boolean removeNodeOnSetIfNull() default true;
}
