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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.SapphireWithDirectiveHelper.resolvePath;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.SapphireWithDirectiveHelper.ResolvePathResult;
import org.eclipse.sapphire.ui.def.IFormDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWithDirectiveImplied extends SapphirePartContainer
{
    private ModelPath path;
    private IModelElement element;
    private IFormDef formdef;
    
    @Override
    protected void init()
    {
        final ISapphireWithDirectiveDef def = (ISapphireWithDirectiveDef) this.definition;
        final ResolvePathResult resolvePathResult = resolvePath( getModelElement(), def, this.params );
        
        if( resolvePathResult.property != null )
        {
            throw new IllegalStateException();
        }
        
        this.path = resolvePathResult.path;
        this.element = resolvePathResult.element;
        
        if( def.getDefaultPage().getContent().size() > 0 )
        {
            this.formdef = def.getDefaultPage();
        }
        else
        {
            this.formdef = def.getPages().get( 0 );
        }
        
        super.init();
    }
    
    public IFormDef getFormDefinition()
    {
        return this.formdef;
    }
    
    public ModelPath getPath()
    {
        return this.path;
    }
    
    @Override
    public IModelElement getLocalModelElement()
    {
        return this.element;
    }

    @Override
    protected IModelElement getModelElementForChildParts()
    {
        return this.element;
    }

}
