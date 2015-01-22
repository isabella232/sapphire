/******************************************************************************
 * Copyright (c) 2015 Oracle, Liferay and agito
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Weise - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.java.jdt.ui.internal;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.java.JavaPackageName;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author <a href="mailto:andreas.weise@agito-it.de">Andreas Weise</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaPackageBrowseActionHandler extends BrowseActionHandler {

	@Text( "Select {0}" )
	private static LocalizableText dialogTitle;

    @Text( "Choose a package:" )
    private static LocalizableText dialogMessage;
	
	static {
		LocalizableText.init(JavaPackageBrowseActionHandler.class);
	}

	public static final String ID = "Sapphire.Browse.Java.Package";

	@Override
	public void init(final SapphireAction action, final ActionHandlerDef def) {
		super.init(action, def);

		setId(ID);
	}

	@Override
	public String browse(final Presentation context) {
		final Property property = property();

		final IJavaProject project = property.element().adapt(IJavaProject.class);

		try {
			final SelectionDialog dlg = JavaUI.createPackageDialog(((FormComponentPresentation) context).shell(),
					project, 0, null);

			dlg.setTitle( dialogTitle.format( property.definition().getLabel( true, CapitalizationType.TITLE_STYLE, false ) ) );
			dlg.setMessage( dialogMessage.text() );

			if (dlg.open() == SelectionDialog.OK) {
				Object results[] = dlg.getResult();
				assert results != null && results.length == 1;
				if (results[0] instanceof IPackageFragment) {
					return ((IPackageFragment) results[0]).getElementName();
				}
			}
		} catch (JavaModelException e) {
			Sapphire.service(LoggingService.class).log(e);
		}

		return null;
	}

	public static final class Condition extends PropertyEditorCondition {
		@Override
		protected boolean evaluate(final PropertyEditorPart part) {
			final Property property = part.property();

			return
            (
                property instanceof Value && 
                property.definition().isOfType( JavaPackageName.class ) &&
                property.element().adapt( IJavaProject.class ) != null
            );
		}
	}

}