/******************************************************************************
 * Copyright (c) 2011 Oracle
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
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface ILevel1 extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ILevel1.class );

    @GenerateImpl

    public interface ILevel2 extends ILevel1
    {
        ModelElementType TYPE = new ModelElementType( ILevel2.class );

        @GenerateImpl

        @Label( standard = "abc")
        public interface ILevel3 extends ILevel2
        {
            ModelElementType TYPE = new ModelElementType( ILevel3.class );
        }
    }

}