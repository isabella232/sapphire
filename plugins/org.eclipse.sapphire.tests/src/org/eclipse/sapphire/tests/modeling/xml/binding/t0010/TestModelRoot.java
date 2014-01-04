/*******************************************************************************
 * Copyright (c) 2014 Accenture and Oracle
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
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@XmlBinding( path = "root" )

public interface TestModelRoot extends Element
{
    ElementType TYPE = new ElementType(TestModelRoot.class);

    // *** Prop1 ***

    @Label(standard = "Prop1")
    @XmlBinding(path = "prop1")
    @InitialValue(text = "Default Prop1")
    ValueProperty PROP_PROP1 = new ValueProperty(TYPE, "Prop1");

    Value<String> getProp1();

    void setProp1(String value);

    // *** Prop2 ***

    @Label(standard = "Prop2")
    @XmlBinding(path = "prop2")
    @InitialValue(text = "Default Prop2")
    ValueProperty PROP_PROP2 = new ValueProperty(TYPE, "Prop2");

    Value<String> getProp2();

    void setProp2(String value);

    // *** Prop3 ***

    @Label(standard = "Prop3")
    @XmlBinding(path = "prop3")
    @InitialValue(text = "")
    @XmlValueBinding(path = "prop3", removeNodeOnSetIfNull = false)
    ValueProperty PROP_PROP3 = new ValueProperty(TYPE, "Prop3");

    Value<String> getProp3();

    void setProp3(String value);

    // *** AttributeProp1 ***

    @Label(standard = "AttributeProp1")
    @XmlBinding(path = "@attributeProp1")
    @InitialValue(text = "AttributeProp1")
    ValueProperty PROP_Attribute_Prop1 = new ValueProperty(TYPE,
            "AttributeProp1");

    Value<String> getAttributeProp1();

    void setAttributeProp1(String value);

    // *** Prop4 ***

    @Label(standard = "Prop4")
    @XmlBinding(path = "prop4")
    @InitialValue(text = "")
    @XmlValueBinding(path = "prop4", removeNodeOnSetIfNull = false)
    ValueProperty PROP_PROP4 = new ValueProperty(TYPE, "Prop4");

    Value<String> getProp4();

    void setProp4(String value);

    // // *** Prop5 ***

    @Type(base = TestChildElement.class)
    @Label(standard = "Prop5")
    @XmlBinding(path = "root/prop5")
    ImpliedElementProperty PROP_PROP5 = new ImpliedElementProperty(TYPE,
            "Prop5");

    TestChildElement getProp5();

}
