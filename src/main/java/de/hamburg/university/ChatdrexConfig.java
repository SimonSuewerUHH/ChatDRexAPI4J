package de.hamburg.university;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@StaticInitSafe
@ConfigMapping(prefix = "chatdrex")
public interface ChatdrexConfig {

    GuardTrailsConfig guardtrails();
    ToolsConfig tools();


    interface GuardTrailsConfig {
        PromptInjectionConfig promptInjection();
        GroundingConfig grounding();
    }

    interface GroundingConfig {
        ScoreConfig score();

        @WithDefault("true")
        boolean enabled();
    }

    interface PromptInjectionConfig {
        ScoreConfig score();

        @WithDefault("true")
        boolean enabled();

        AgentConfig agent();
    }

    interface ScoreConfig {
        @WithDefault("0.7")
        double threshold();
    }

    interface AgentConfig {
        @WithDefault("true")
        boolean enabled();
    }

    interface ToolsConfig {
        KgQueryConfig kgQuery();
    }

    interface KgQueryConfig {
        @WithDefault("3")
        int retries();

        @WithDefault("0.3")
        double minGeneDisorderScore();

        @WithDefault("3000")
        int maxResultLength();

        @WithDefault("0.7")
        double minNodeScore();

        @WithDefault("5")
        int queryTopNode();
    }
}
