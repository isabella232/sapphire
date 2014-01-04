/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.policies;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.swt.gef.commands.CreateConnectionCommand;
import org.eclipse.sapphire.ui.swt.gef.commands.ReconnectConnectionCommand;
import org.eclipse.sapphire.ui.swt.gef.figures.DiagramConnectionFigure;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.swt.SWT;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramNodeEditPolicy extends GraphicalNodeEditPolicy {

    private static final org.eclipse.sapphire.Color DUMMY_CONNECTION_FOREGROUND = new org.eclipse.sapphire.Color(0xFF, 0x99, 0x33);

	Rectangle rec;

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		CreateConnectionCommand cmd = (CreateConnectionCommand) request.getStartCommand();
		cmd.setTarget((DiagramNodeModel) getHost().getModel());
		return cmd;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		DiagramNodeModel source = (DiagramNodeModel) getHost().getModel();
		Object def = request.getNewObjectType();
		CreateConnectionCommand cmd = new CreateConnectionCommand(source, (IDiagramConnectionDef)def);
		request.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		DiagramNodeModel newTarget = (DiagramNodeModel) getHost().getModel();
		DiagramConnectionEditPart editPart = (DiagramConnectionEditPart)request.getConnectionEditPart();
		ReconnectConnectionCommand cmd = new ReconnectConnectionCommand(editPart.getCastedModel());
		cmd.setNewTarget(newTarget);
		return cmd;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		DiagramNodeModel newSource = (DiagramNodeModel) getHost().getModel();
		DiagramConnectionEditPart editPart = (DiagramConnectionEditPart)request.getConnectionEditPart();
		ReconnectConnectionCommand cmd = new ReconnectConnectionCommand(editPart.getCastedModel());
		cmd.setNewSource(newSource);
		return cmd;
	}

	private void identifySourceFigure(Request req) {
		if (req instanceof CreateConnectionRequest) {
			CreateConnectionRequest r = (CreateConnectionRequest) req;
			if (r.getSourceEditPart() instanceof AbstractGraphicalEditPart) {
				AbstractGraphicalEditPart ep = (AbstractGraphicalEditPart) r.getSourceEditPart();
				rec = ep.getFigure().getBounds();
			}
		} else {
			rec = null;
		}
	}

	@Override
	protected Connection createDummyConnection(Request req) {
		identifySourceFigure(req);
		IFigure hostFigure = getHostFigure();
		PolylineConnection connection = new DummyPolylineConnection(hostFigure);
		connection.setLineWidth(2);
		connection.setLineStyle(SWT.LINE_DASH);
		if (getHost() instanceof DiagramNodeEditPart) {
			DiagramResourceCache cache = ((DiagramNodeEditPart)getHost()).getCastedModel().getDiagramModel().getResourceCache();
			connection.setForegroundColor(cache.getColor(DUMMY_CONNECTION_FOREGROUND));
		}
		return connection;
	}

	private final class DummyPolylineConnection extends PolylineConnection {
		private IFigure hostFigure;

		DummyPolylineConnection(IFigure hostFigure) {
			super();
			setHostFigure(hostFigure);
		}

		@Override
		public void paint(Graphics g) {
			// We do not draw unless the target position of the
			// dummy connection lies outside of the source figure's bounds.
			// But we have to draw in polyline connections if a connection is
			// created starting from an existing one.
			if (rec != null && (!(getHostFigure() instanceof DiagramConnectionFigure)) && rec.contains(getPoints().getLastPoint())) {
				return;
			}

			g.setAntialias(SWT.ON);
			super.paint(g);
		}

		/**
		 * @return the hostFigure
		 */
		private IFigure getHostFigure() {
			return hostFigure;
		}

		/**
		 * @param hostFigure
		 *            the hostFigure to set
		 */
		private void setHostFigure(IFigure hostFigure) {
			this.hostFigure = hostFigure;
		}
	}
}
