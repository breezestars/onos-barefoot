/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.pipelines.basic.pro;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.pi.model.DefaultPiPipeconf;
import org.onosproject.net.pi.model.PiPipeconf;
import org.onosproject.net.pi.model.PiPipeconfId;
import org.onosproject.net.pi.model.PiPipelineInterpreter;
import org.onosproject.net.pi.model.PiPipelineModel;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.p4runtime.model.P4InfoParser;
import org.onosproject.p4runtime.model.P4InfoParserException;

import java.net.URL;
import java.util.Collection;

import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.P4_INFO_TEXT;
import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.TOFINO_BIN;
import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.TOFINO_CONTEXT_JSON;

/**
 * Pipeline config loader for basic pipeline.
 */
@Component(immediate = true)
public class HwPipeconfLoader {

    private static final String MAVERICKS = "mavericks";
    private static final String TNA_ROUTER = "tna_router";

    private static final String PLAIN_BASIC = "";

    private static final Collection<String> APPENDICES = ImmutableList
            .of(PLAIN_BASIC);

    private static final Collection<String> PLATFORMS = ImmutableList
            .of(TNA_ROUTER);

    private static final String BASE_PATH = "/p4c-out/fwd/tofino/tna_router/";
    private static final String BASE_TOFINO_BIN_PATH = "/p4c-out/fwd/tofino/tna_router/pipe/tofino.bin";
    private static final String BASE_CONTEXT_JSON_PATH = "/p4c-out/fwd/tofino/tna_router/pipe/context.json";
    private static final String BASE_P4INFO_PATH = "/p4c-out/fwd/tofino/tna_router/p4info.txt";

    private static final Collection<PiPipeconf> ALL_PIPECONFS = buildAll();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService piPipeconfService;

    @Activate
    public void activate() {
        // Registers all pipeconf at component activation.
        ALL_PIPECONFS.forEach(piPipeconfService::register);
    }

    @Deactivate
    public void deactivate() {
        ALL_PIPECONFS.stream().map(PiPipeconf::id).forEach(piPipeconfService::unregister);
    }

    private static PiPipeconf buildTofinoPipeconf(String platform, String appendix) {
        final PiPipeconfId pipeconfId = new PiPipeconfId(
                "org.onosproject.pipelines.basic.pro." + platform);
        final URL tofinoBinUrl = HwPipeconfLoader.class
                .getResource(BASE_TOFINO_BIN_PATH);
        final URL contextJsonUrl = HwPipeconfLoader.class
                .getResource(BASE_CONTEXT_JSON_PATH);
        final URL p4InfoUrl = HwPipeconfLoader.class
                .getResource(BASE_P4INFO_PATH);

        return DefaultPiPipeconf.builder()
                .withId(pipeconfId)
                .withPipelineModel(parseP4Info(p4InfoUrl))
                .addBehaviour(PiPipelineInterpreter.class, BasicInterpreterImpl.class)
                .addBehaviour(Pipeliner.class, BasicPipelinerImpl.class)
                .addExtension(P4_INFO_TEXT, p4InfoUrl)
                .addExtension(TOFINO_BIN, tofinoBinUrl)
                .addExtension(TOFINO_CONTEXT_JSON, contextJsonUrl)
                .build();
    }

    private static Collection<PiPipeconf> buildAll() {
        ImmutableList.Builder<PiPipeconf> builder = ImmutableList.builder();
        for (String platform : PLATFORMS) {
            for (String appendix : APPENDICES) {
                builder.add(buildTofinoPipeconf(platform, appendix));
            }
        }
        return builder.build();
    }

    private static PiPipelineModel parseP4Info(URL p4InfoUrl) {
        try {
            return P4InfoParser.parse(p4InfoUrl);
        } catch (P4InfoParserException e) {
            throw new IllegalStateException(e);
        }
    }
}
