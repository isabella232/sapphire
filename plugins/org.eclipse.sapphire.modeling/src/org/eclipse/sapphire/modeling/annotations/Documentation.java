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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.TYPE } )

public @interface Documentation
{
    /**
     * Returns the documentation content
     * [b] and [/b] to mark bold ranges
     * [br/] to mark a line break
     * [pbr/] to mark a paragraph break (a shorthand for [br/][br/])
     * [ol] to create an ordered list
     * [ul] to create an unordered list
     * [li] to denote a list item. Used in both ordered [ol] and unordered [il] list
     * 
     * @return the documentation content
     */
    String content() default "";
    
    Topic[] topics() default {};
    
    DocumentationMergeStrategy mergeStrategy() default DocumentationMergeStrategy.PREPEND;
    
    @interface Topic
    {
        /**
         * Returns the labels corresponding to related topics hrefs
         *
         * @return the label
         */
        String label();
        
        /**
         * Returns the URL (as a string) associated with related topics
         *         <p>
         *         Valid URL of a help resource is:
         *         <ul>
         *         <li>a <em>/pluginID/path/to/resource</em>, where
         *         <ul>
         *         <li><em>pluginID</em> is the unique identifier of the plugin
         *         containing the help resource,
         *         <li><em>path/to/document</em> is the help resource path,
         *         relative to the plugin directory.
         *         </ul>
         *         For example. <em>/myplugin/mytoc.xml</em> or
         *         <em>/myplugin/references/myclass.html</em> are vaild.
         *         <li>string representation of URI to an external document. In
         *         this case, all special characters have to be enoded such that the
         *         URI is appropriate to be opened with a web browser.
         *         <em>http://eclipse.org/documents/my%20file.html</em> and
         *         <em>jar:file:/c:/my%20sources/src.zip!/mypackage/MyClass.html</em>
         *         are examples of valid URIs.
         *         </ul>
         *         </p>
         * 
         * @return the URL (as a string) associated with the resource
         */
        String url();

    }
    
}
