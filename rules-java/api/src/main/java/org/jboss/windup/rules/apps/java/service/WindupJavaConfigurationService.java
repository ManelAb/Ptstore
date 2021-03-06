package org.jboss.windup.rules.apps.java.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.PackageModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;

/**
 * Provides methods for loading and working with {@link WindupJavaConfigurationModel} objects.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class WindupJavaConfigurationService extends GraphService<WindupJavaConfigurationModel>
{

    private List<String> ignoredRegexes;

    public WindupJavaConfigurationService(GraphContext context)
    {
        super(context, WindupJavaConfigurationModel.class);
    }

    /**
     * Loads the single {@link WindupJavaConfigurationModel} from the graph.
     */
    public static synchronized WindupJavaConfigurationModel getJavaConfigurationModel(GraphContext context)
    {
        WindupJavaConfigurationService service = new WindupJavaConfigurationService(context);
        WindupJavaConfigurationModel config = service.getUnique();
        if (config == null)
            config = service.create();
        return config;
    }

    public List<String> getIgnoredFileRegexes()
    {
        if (ignoredRegexes == null)
        {
            ignoredRegexes = new ArrayList<>();

            WindupJavaConfigurationModel cfg = getJavaConfigurationModel(getGraphContext());
            for (IgnoredFileRegexModel ignored : cfg.getIgnoredFileRegexes())
            {
                //TODO: Consider having isCompilable() in case there is no message but is not compilable
            	if(ignored.getCompilationError() == null) {
            		ignoredRegexes.add(ignored.getRegex());
            	}
            }
        }
        return ignoredRegexes;
    }

    /**
     * This is similar to {@link WindupJavaConfigurationService#shouldScanPackage(String)}, except that it expects to be given a file path (for
     * example, "/path/to/file.class"). This will use a string.contains approach, as we cannot know for sure what type of path prefixes may exist
     * before the package name part of the path.
     *
     * Also, this can only work reliably for class files, though it will generally work with java files if they are in package appropriate folders.
     */
    public boolean shouldScanFile(String path)
    {
        WindupJavaConfigurationModel configuration = getJavaConfigurationModel(getGraphContext());
        path = FilenameUtils.separatorsToUnix(path);
        for (PackageModel excludePackage : configuration.getExcludeJavaPackages())
        {
            String packageAsPath = excludePackage.getPackageName().replace(".", "/");
            if (path.contains(packageAsPath))
                return false;
        }

        boolean shouldScan = true;
        for (PackageModel includePackage : configuration.getScanJavaPackages())
        {
            String packageAsPath = includePackage.getPackageName().replace(".", "/");
            if (path.contains(packageAsPath))
            {
                shouldScan = true;
                break;
            }
            else
            {
                shouldScan = false;
            }
        }
        return shouldScan;
    }

    /**
     * Indicates whether the provided package should be scanned (based upon the inclusion/exclusion lists).
     */
    public boolean shouldScanPackage(String pkg)
    {
        // assume an empty string if it wasn't specified
        if (pkg == null)
        {
            pkg = "";
        }
        WindupJavaConfigurationModel configuration = getJavaConfigurationModel(getGraphContext());
        for (PackageModel pkgModel : configuration.getExcludeJavaPackages())
        {
            String excludePkg = pkgModel.getPackageName();
            if (pkg.startsWith(excludePkg))
            {
                return false;
            }
        }

        // if the list is empty, assume it is intended to just accept all packages
        if (!configuration.getScanJavaPackages().iterator().hasNext())
        {
            return true;
        }

        for (PackageModel pkgModel : configuration.getScanJavaPackages())
        {
            String includePkg = pkgModel.getPackageName();
            if (pkg.startsWith(includePkg))
            {
                return true;
            }
        }

        return false;
    }
}
