package org.jboss.windup.rules.apps.javadecompiler;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class DecompileArchivesRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                                Query.find(ArchiveModel.class).as("allUnzippedArchives")
                    ).perform(
                                Iteration.over("allUnzippedArchives").as(ArchiveModel.class, "archive")
                                            .perform(new ProcyonDecompilerOperation("archive"))
                                            .endIteration()
                    );

    }

}