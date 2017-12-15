/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.ui.def.SplitFormBlockDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SplitFormBlockPart extends FormPart
{
    @Override
    public SplitFormBlockDef definition()
    {
        return (SplitFormBlockDef) super.definition();
    }
    
    public int getWeight()
    {
        int weight = definition().getWeight().getContent();
        
        if( weight < 1 )
        {
            weight = 1;
        }
        
        return weight;
    }
    
}
