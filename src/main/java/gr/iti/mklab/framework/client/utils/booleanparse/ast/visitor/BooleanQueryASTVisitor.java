package gr.iti.mklab.framework.client.utils.booleanparse.ast.visitor;

import gr.iti.mklab.framework.client.utils.booleanparse.ast.AndNode;
import gr.iti.mklab.framework.client.utils.booleanparse.ast.AtomNode;
import gr.iti.mklab.framework.client.utils.booleanparse.ast.NotNode;
import gr.iti.mklab.framework.client.utils.booleanparse.ast.OrNode;

public interface BooleanQueryASTVisitor {

	public Number visitAnd(AndNode andNode);
	public Number visitOr(OrNode orNode);
	public Number visitNot(NotNode notNode);
	public Number visitAtom(AtomNode atomNode);

}