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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.SapphireWithDirectiveHelper.resolvePath;

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.SapphireWithDirectiveHelper.ResolvePathResult;
import org.eclipse.sapphire.ui.def.FormDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWithDirectiveImplied extends FormPart
{
    private ModelPath path;
    private IModelElement element;
    private FormDef formdef;
    
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

    @Override
    protected List<SapphirePart> initChildParts()
    {
        final IModelElement element = getLocalModelElement();
        final ListFactory<SapphirePart> partsListFactory = ListFactory.start();
        
        for( PartDef childPartDef : this.formdef.getContent() )
        {
            partsListFactory.add( create( this, element, childPartDef, this.params ) );
        }
        
        return partsListFactory.result();
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

}
