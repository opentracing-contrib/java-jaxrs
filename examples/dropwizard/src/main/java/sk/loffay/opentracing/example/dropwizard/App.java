/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.loffay.opentracing.example.dropwizard;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hawkular.apm.client.opentracing.APMTracer;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.opentracing.Tracer;
import sk.loffay.opentracing.jax.rs.SpanExtractRequestFilter;
import sk.loffay.opentracing.jax.rs.SpanFinishResponseFilter;

/**
 * @author Pavol Loffay
 */
public class App extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "dropwizard";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()));
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {
        Tracer tracer = new APMTracer();

        environment.jersey().register(new SpanExtractRequestFilter(tracer));
        environment.jersey().register(new SpanFinishResponseFilter());
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
//                TODO remove
                bind(APMTracer.class).to(Tracer.class);
            }
        });

        // Register resources
        environment.jersey().register(new HelloHandler(tracer));
    }
}
