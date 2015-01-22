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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0010;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public interface TestChildElement extends Element {

    ElementType TYPE = new ElementType(TestChildElement.class);

    // *** Prop51 ***

    @Label(standard = "Prop5-1")
    @XmlBinding(path = "prop5-1")
    @InitialValue(text = "prop5-1")
    ValueProperty PROP_PROP51 = new ValueProperty(TYPE, "Prop51");

    Value<String> getProp51();

    void setProp51(String value);

    // *** Prop52 ***

    @Label(standard = "PRop52")
    @XmlBinding(path = "prop5-2")
    @InitialValue(text = "prop5-2")
    ValueProperty PROP_PROP52 = new ValueProperty(TYPE, "Prop52");

    Value<String> getProp52();

    void setProp52(String value);

}
