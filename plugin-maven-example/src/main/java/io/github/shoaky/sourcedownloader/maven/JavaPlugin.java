package io.github.shoaky.sourcedownloader.maven;

import org.jetbrains.annotations.NotNull;
import io.github.shoaky.sourcedownloader.sdk.Plugin;
import io.github.shoaky.sourcedownloader.sdk.PluginContext;
import io.github.shoaky.sourcedownloader.sdk.PluginDescription;

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
