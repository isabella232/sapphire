/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "dialog" )
@Image( path = "DialogDef.png" )
@XmlBinding( path = "dialog" )

public interface DialogDef extends CompositeDef
{
    ElementType TYPE = new ElementType( DialogDef.class );
    
    // *** Label ***
    
    @Label( standard = "label" )
    @Required
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** InitialFocus ***
    
    @Label( standard = "initial focus" )
    @XmlBinding( path = "initial-focus" )
    
    ValueProperty PROP_INITIAL_FOCUS = new ValueProperty( TYPE, "InitialFocus" );
    
    Value<String> getInitialFocus();
    void setInitialFocus( String value );
    
}
