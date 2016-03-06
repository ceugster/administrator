package ch.eugster.events.persistence.model;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

public class DeletedFilter implements DescriptorCustomizer
{

	@Override
	public void customize(ClassDescriptor descriptor) throws Exception
	{
		Expression deleted = new ExpressionBuilder().get("deleted").equal(false);
		descriptor.getQueryManager().setAdditionalJoinExpression(deleted);
	}

}
