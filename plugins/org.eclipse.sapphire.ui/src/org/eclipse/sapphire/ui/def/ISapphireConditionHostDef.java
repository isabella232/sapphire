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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.java.JavaTypeConstraints;
import org.eclipse.sapphire.modeling.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphireConditionHostDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireConditionHostDef.class );
    
    // *** ConditionClass ***
    
    @Reference( target = Class.class )
    @Label( standard = "condition class" )
    @JavaTypeConstraints( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.ui.SapphireCondition" )
    @MustExist
    @XmlBinding( path = "condition" )
    
    ValueProperty PROP_CONDITION_CLASS = new ValueProperty( TYPE, "ConditionClass" );
    
    ReferenceValue<Class<?>> getConditionClass();
    void setConditionClass( String value );
    
}
