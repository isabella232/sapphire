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

package org.eclipse.sapphire.tests.services.t0004;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Service;

/**
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public interface TestElement extends Element {
    ElementType TYPE = new ElementType(TestElement.class);

    // *** Colors ***

    @PossibleValues(values = { "Red", "Green", "Blue", "Orange" })
    
    ValueProperty PROP_COLORS = new ValueProperty(TYPE, "Colors");

    Value<String> getColors();
    void setColors(String value);

    // *** Shapes ***

    @Service(impl = ShapePossibleValueService.class)
    
    ValueProperty PROP_SHAPES = new ValueProperty(TYPE, "Shapes");

    Value<String> getShapes();
    void setShapes(String value);

}
