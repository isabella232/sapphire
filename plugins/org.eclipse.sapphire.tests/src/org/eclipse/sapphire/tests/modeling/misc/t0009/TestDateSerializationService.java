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

package org.eclipse.sapphire.tests.modeling.misc.t0009;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.serialization.DateSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestDateSerializationService extends DateSerializationService
{
    private final static List<DateFormat> TEST_FORMATS;
    
    static 
    {
        final List<DateFormat> formats = new ArrayList<DateFormat>();
        formats.add( new SimpleDateFormat( "dd.MM.yyyy" ) );
        formats.add( new SimpleDateFormat( "yyyy/MM/dd" ) );
        
        TEST_FORMATS = Collections.unmodifiableList(formats);
    };

    @Override
    public List<? extends DateFormat> formats()
    {
        return TEST_FORMATS;
    }
    
}
