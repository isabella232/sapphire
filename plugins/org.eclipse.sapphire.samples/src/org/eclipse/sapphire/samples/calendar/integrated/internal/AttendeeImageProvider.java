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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.ImageProvider;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.internal.SapphireSamplesPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AttendeeImageProvider

    extends ImageProvider
    
{
    private static final String IMG_PERSON = SapphireSamplesPlugin.PLUGIN_ID + "/images/person.png";
    private static final String IMG_PERSON_FADED = SapphireSamplesPlugin.PLUGIN_ID + "/images/person-faded.png";

    @Override
    public String getSmallImagePath( final IModelElement element )
    {
        if( ( (IAttendee) element ).isInContactsDatabase().getContent() )
        {
            return IMG_PERSON;
        }
        else
        {
            return IMG_PERSON_FADED;
        }
    }

    @Override
    public String getSmallImagePath( final ModelElementType type )
    {
        return IMG_PERSON;
    }
    
}
