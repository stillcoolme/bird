package com.stillcoolme.framework.es.plugin;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

import java.util.Collection;

/**
 * @author: stillcoolme
 * @date: 2020/4/16 18:31
 * Function:
 */
public class FeatureComparePlugin extends Plugin implements ScriptPlugin {

    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {

        return new FeatureCompareEngine();
    }


}
