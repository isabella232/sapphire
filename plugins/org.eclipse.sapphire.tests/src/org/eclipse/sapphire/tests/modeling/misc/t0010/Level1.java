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

package org.eclipse.sapphire.tests.modeling.misc.t0010;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface Level1 extends Element
{
    ElementType TYPE = new ElementType( Level1.class );

    public interface Level2 extends Level1
    {
        ElementType TYPE = new ElementType( Level2.class );

        public interface Level3 extends Level2
        {
            ElementType TYPE = new ElementType( Level3.class );
        }
    }

}