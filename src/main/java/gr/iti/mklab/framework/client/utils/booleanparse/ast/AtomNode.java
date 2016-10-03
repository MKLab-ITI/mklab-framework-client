package gr.iti.mklab.framework.client.utils.booleanparse.ast;

import java.util.List;

import gr.iti.mklab.framework.client.utils.booleanparse.ast.visitor.BooleanQueryASTVisitor;

public class AtomNode extends BooleanExpression {

	public AtomNode(List<Node> operands){
		super();
		setOperands(operands);
	}

	@Override
	public Object accept(BooleanQueryASTVisitor visitor) {
		return visitor.visitAtom(this);
	}
	
	@Override
	public String toString() {
		return "atom";
	}
}