/*******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011h;

import org.eclipse.sapphire.ElementType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface TestModelElementB1 extends TestModelElementB
{
    ElementType TYPE = new ElementType( TestModelElementB1.class );

}
