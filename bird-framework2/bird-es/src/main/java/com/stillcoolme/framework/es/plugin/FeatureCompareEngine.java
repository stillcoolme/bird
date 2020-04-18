package com.stillcoolme.framework.es.plugin;

import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

import java.io.IOException;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2020/4/16 18:39
 * Function:
 */
public class FeatureCompareEngine implements ScriptEngine {

    @Override
    public String getType() {
        return "feature_compare";
    }

    @Override
    public <FactoryType> FactoryType compile(String scriptName, String scriptSource,
                                             ScriptContext<FactoryType> scriptContext, Map<String, String> map) {

        if (scriptContext.equals(ScoreScript.CONTEXT) == false) {
            throw new IllegalArgumentException(getType() + " scripts cannot be used for context [" + scriptContext.name + "]");
        }
        // we use the script "source" as the script identifier
        if ("binary_vector_score".equals(scriptSource)) {


        }

        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
