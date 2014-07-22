package org.jboss.windup.reporting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.BlackListModel;
import org.jboss.windup.graph.model.ClassificationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@RunWith(Arquillian.class)
public class BlackListQueryTest extends AbstractTestCase
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(AbstractTestCase.class)
                    .addAsResource(new File("src/test/resources/reports"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testQuerying() throws Exception
    {
        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");
        FileModel f3 = context.getFramed().addVertex(null, FileModel.class);
        f3.setFilePath("/f3");
        FileModel f4 = context.getFramed().addVertex(null, FileModel.class);
        f4.setFilePath("/f4");
        FileModel f5 = context.getFramed().addVertex(null, FileModel.class);
        f5.setFilePath("/f5");
        FileModel f6 = context.getFramed().addVertex(null, FileModel.class);
        f6.setFilePath("/f6");
        FileModel f7 = context.getFramed().addVertex(null, FileModel.class);
        f7.setFilePath("/f7");

        BlackListModel b1 = context.getFramed().addVertex(null, BlackListModel.class);
        BlackListModel b1b = context.getFramed().addVertex(null, BlackListModel.class);
        b1.setFileModel(f1);
        b1b.setFileModel(f1);

        BlackListModel b2 = context.getFramed().addVertex(null, BlackListModel.class);
        b2.setFileModel(f2);

        ClassificationModel c1 = context.getFramed().addVertex(null, ClassificationModel.class);
        ClassificationModel c1b = context.getFramed().addVertex(null, ClassificationModel.class);
        c1.setFileModel(f1);
        c1b.setFileModel(f1);

        ClassificationModel c2 = context.getFramed().addVertex(null, ClassificationModel.class);
        c2.setFileModel(f3);

        List<Vertex> vertexList = new ArrayList<>();
        for (Vertex v : context.getFramed().query()
                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource").vertices())
        {
            vertexList.add(v);
        }

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(context.getFramed().query()
                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource").vertices());

        GremlinPipeline<Vertex, Vertex> blacklistPipeline = new GremlinPipeline<Vertex, Vertex>(context
                    .getFramed().query()
                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource").vertices());
        blacklistPipeline.as("fileModel1").in(BlackListModel.FILE_MODEL).back("fileModel1");

        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(context
                    .getFramed().query()
                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource").vertices());
        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL).back("fileModel2");
        pipeline.or(blacklistPipeline, classificationPipeline);

        // pipeline.V();

        System.out.println("--------------------------------------------------");
        for (Vertex v : pipeline)
        {
            FileModel fm = context.getFramed().frame(v, FileModel.class);
            System.out.println("FM: " + fm.getFilePath());
        }
        System.out.println("--------------------------------------------------");
    }
}