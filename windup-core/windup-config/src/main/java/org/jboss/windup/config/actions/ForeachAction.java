package org.jboss.windup.config.actions;

import java.util.LinkedList;
import java.util.List;

import org.jboss.windup.config.base.Action;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

public class ForeachAction<T extends Resource> implements Action<T> {
	
	protected final List<Action> actions;
	
	public ForeachAction() {
		this.actions = new LinkedList<Action>();
	}
	
	@Override
	public void execute(GraphContext graphContext, T obj, LocalContext context) {
		for(Action action : actions) {
			//create scoped context.
			LocalContext localContext = new LocalContext(context);
			action.execute(graphContext, obj, localContext);
		}
	}

	@Override
	public String toString() {
		return "ForeachAction [actions=" + actions + "]";
	}

	public List<Action> getActions() {
		return actions;
	}
	
}
