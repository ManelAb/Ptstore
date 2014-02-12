package org.jboss.windup.graph.dao;

import java.util.Iterator;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;

import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;


public class BaseDaoBean<T extends VertexFrame> {

	protected final Class<T> type;
	protected final String typeValue;
	
	@Inject
	GraphContext context;
	
	public GraphContext getContext() {
		return context;
	}
	
	public BaseDaoBean(Class<T> type) {
		this.type = type;
		
		TypeValue typeValue = type.getAnnotation(TypeValue.class);
		if(typeValue == null) {
			throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
		}
		this.typeValue = typeValue.value();
	}

	public void delete(T obj) {
		context.getFramed().removeVertex(obj.asVertex());
	}

	public Iterable<T> getByProperty(String key, Object value) {
		return (Iterable<T>)context.getFramed().getVertices(key, value, type);
	}

	public Iterable<T> findValueMatchingRegex(String key, String ... regex) {
		//build regex
		if(regex.length == 0) {
			return IterablesUtil.emptyIterable();
		}
		
		final String regexFinal;
		if(regex.length == 1) {
			regexFinal = regex[0];
		}
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("\\b(");
			int i=0;
			for(String value : regex) {
				if(i>0) {
					builder.append("|");
				}
				builder.append(value);
				i++;
			}
			builder.append(")\\b");
			regexFinal = builder.toString();
		}
		
		
		return context.getFramed().query().has("type", typeValue).has(key, Text.REGEX, regexFinal).vertices(type);
	}
	
	public T create(Object id) {
		return (T)context.getFramed().addVertex(id, type);
	}


	public T create() {
		return (T)context.getFramed().addVertex(null, type);
	}
	
	public Iterable<T> getAll() {
		return (Iterable<T>)context.getFramed().query().has("type", typeValue).vertices(type);
	}

	public TitanTransaction newTransaction() {
		return context.getGraph().newTransaction();
	}
	
	public long count(Iterable<?> obj) {
		GremlinPipeline pipe = new GremlinPipeline();
		return pipe.start(obj).count();
	}
	
	public T getById(Object id) {
		return context.getFramed().getVertex(id, type);
	}

	public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException {
		Iterable<T> results = getByProperty(property, value);
		
		if(!results.iterator().hasNext()) {
			return null;
		}
		
		Iterator<T> iter = results.iterator();
		T result = iter.next();
		
		if(iter.hasNext()) {
			throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
		}
		
		return result;
	}

	public T castToType(VertexFrame v) {
		Vertex vertex = v.asVertex();
		TypeValue value = type.getAnnotation(TypeValue.class);
		TypeField field = type.getAnnotation(TypeField.class);
		
		String property = "type";
		if(field != null) {
			property = field.value();
		}
		String typeValue = this.type.getName();
		if(value != null) {
			typeValue = value.value();
		}
		
		vertex.setProperty(property, typeValue);
		context.getGraph().commit();
		return context.getFramed().frame(vertex, type);
		
	}
	
	public T castToType(Vertex vertex) {
		return context.getFramed().frame(vertex, type);
	}
	

	public void commit() {
		this.context.getGraph().commit();
	}
}