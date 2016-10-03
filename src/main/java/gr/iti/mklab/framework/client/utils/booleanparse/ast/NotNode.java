package gr.iti.mklab.framework.client.utils.booleanparse.ast;

import java.util.List;

import gr.iti.mklab.framework.client.utils.booleanparse.ast.visitor.BooleanQueryASTVisitor;

public class NotNode extends BooleanExpression {

	public NotNode(List<Node> operands) {
		super();
		setOperands(operands);
	}

	@Override
	public Object accept(BooleanQueryASTVisitor visitor) {
		return visitor.visitNot(this);
	}

	@Override
	public String toString() {
		return "not";
	}
}