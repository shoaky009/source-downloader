package xyz.shoaky.sourcedownloader.maven;

import org.jetbrains.annotations.NotNull;
import xyz.shoaky.sourcedownloader.sdk.Plugin;
import xyz.shoaky.sourcedownloader.sdk.PluginContext;
import xyz.shoaky.sourcedownloader.sdk.PluginDescription;

public class JavaPlugin implements Plugin {
    @NotNull
    @Override
    public PluginDescription description() {
        return new PluginDescription("JavaPluginExample", "0.0.1");
    }

    @Override
    public void destroy(@NotNull PluginContext pluginContext) {

    }

    @Override
    public void init(@NotNull PluginContext pluginContext) {

    }
}
