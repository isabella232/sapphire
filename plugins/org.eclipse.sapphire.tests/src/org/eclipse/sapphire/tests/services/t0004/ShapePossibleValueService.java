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

import java.util.Set;

import org.eclipse.sapphire.PossibleValuesService;

/**
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public final class ShapePossibleValueService extends PossibleValuesService {

    @Override
    protected void compute(Set<String> values) {
        values.add("Circle");
        values.add("Circle1");
        values.add("Circle2");
        values.add("Rectangle");
        values.add("Rectangle1");
        values.add("Rectangle2");
        values.add("Triangle");
        values.add("Square");
        values.add("Quadrilateral");
        values.add("Polygon");
    }

}
