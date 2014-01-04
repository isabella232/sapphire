/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [350340] Eliminate DocumentationProvider annotation in favor of service approach
 ******************************************************************************/

package org.eclipse.sapphire.modeling.annotations;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public enum DocumentationMergeStrategy
{
    /**
     * Indicates that documentation content should be prepended to parent property's documentation content. 
     */
    
    PREPEND,

    /**
     * Indicates that documentation content should be appended to parent property's documentation content.
     */
    
    APPEND,
    
    /**
     * Indicates that documentation content should replace parent property's documentation content.
     */
    
    REPLACE
}
