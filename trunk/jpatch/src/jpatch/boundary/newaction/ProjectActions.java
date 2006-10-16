package jpatch.boundary.newaction;

import java.awt.event.ActionEvent;

import javax.swing.*;

import jpatch.boundary.*;
import jpatch.entity.*;

public class ProjectActions {
	private enum Type { NEW, OPEN, CLOSE, DELETE }
	
	public static Action createNewProjectAction() {
		return new ProjectAction(Type.NEW);
	}
	
	public static Action createOpenProjectAction() {
		return new ProjectAction(Type.OPEN);
	}
	
	public static Action createCloseProjectAction() {
		return new ProjectAction(Type.CLOSE);
	}
	
	public static Action createDeleteProjectAction() {
		return new ProjectAction(Type.DELETE);
	}
	
	private static class ProjectAction extends JPatchAction{
		private Type type;
		
		private ProjectAction(Type type) {
			this.type = type;
		}
		
		public void actionPerformed(ActionEvent e) {
			JPatchObject object = (Project) Main.getInstance().getSelectedTreeUserObject();
			if (object == null || !(object instanceof Project)) {
				throw new IllegalStateException("no project selected: " + object);
			}
			Project project = (Project) object;
			switch (type) {
			case OPEN:
				if (project.isOpen()) {
					throw new IllegalStateException("project is already open: " + project);
				}
				project.setOpen(true);
				Main.getInstance().repaintTree();
				break;
			case CLOSE:
				if (!project.isOpen()) {
					throw new IllegalStateException("project is already closed: " + project);
				}
				project.setOpen(false);
				Main.getInstance().repaintTree();
				break;
			}
		}
	}
}
