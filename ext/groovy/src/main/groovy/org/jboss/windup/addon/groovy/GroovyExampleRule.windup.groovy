import org.ocpsoft.rewrite.context.EvaluationContext;

import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.JavaClassModel;

buildWindupRule("ExampleBlacklistRule")
    .addRule()
    .when(
        GraphSearchConditionBuilder.create("javaClasses")
            .ofType(JavaClassModel.class)
    )
    .perform(
        Iteration.over("javaClasses").var("javaClass").perform(
            new GraphOperation  () {
                public void perform(GraphRewrite event, EvaluationContext context) {
                    System.out.println("Performing rewrite operation")
                }
            }
        )
    )
    