/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0010;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface Level1 extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( Level1.class );

    @GenerateImpl

    public interface Level2 extends Level1
    {
        ModelElementType TYPE = new ModelElementType( Level2.class );

        @GenerateImpl

        public interface Level3 extends Level2
        {
            ModelElementType TYPE = new ModelElementType( Level3.class );
        }
    }

}