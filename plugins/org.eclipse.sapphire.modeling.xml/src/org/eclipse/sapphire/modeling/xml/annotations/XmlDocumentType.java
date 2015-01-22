/*******************************************************************************
 * Copyright (c) 2015 Accenture Services Pvt Ltd. and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kamesh Sampath - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/
package org.eclipse.sapphire.modeling.xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will add the &lt;!DOCTYPE rootElelemtName PUBLIC publicId
 * systemId &gt; to the XML documents that uses the DTD schema for validation
 * 
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlDocumentType {

    /**
     * The public Id that will be used in &lt;!DOCTYPE rootElelemtName PUBLIC publicId systemId &gt;
     */
    String publicId() default "";

    /**
     * The system Id that will be used in &lt;!DOCTYPE rootElelemtName SYSTEM systemId &gt; or 
     * &lt;!DOCTYPE rootElelemtName PUBLIC publicId systemId &gt;
     */
    String systemId();
}
