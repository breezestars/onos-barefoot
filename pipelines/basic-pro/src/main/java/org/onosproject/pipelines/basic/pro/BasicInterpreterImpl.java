/*
 * Copyright 2017-present Open Networking Foundation
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
import com.google.common.collect.ImmutableMap;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiPipelineInterpreter;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.pi.runtime.PiPacketOperation;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.onosproject.net.PortNumber.CONTROLLER;
import static org.onosproject.net.flow.instructions.Instruction.Type.OUTPUT;
import static org.onosproject.net.flow.instructions.Instructions.OutputInstruction;
import static org.onosproject.pipelines.basic.pro.BasicConstants.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Interpreter implementation for basic.p4.
 */
public class BasicInterpreterImpl extends AbstractHandlerBehaviour
        implements PiPipelineInterpreter {

    private final Logger log = getLogger(getClass());

    private static final int PORT_BITWIDTH = 9;

    private static final Map<Integer, PiTableId> TABLE_MAP =
            new ImmutableMap.Builder<Integer, PiTableId>()
                    .put(0, SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_ETHERNET)
                    .put(1, SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_IPV4)
                    .put(2, SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_OTHER)
                    .put(3, SWITCHINGRESS_UNITCAST_IPV4_FIB_RIB)
                    .put(4, SWITCHINGRESS_UNITCAST_IPV4_FIB_FWD)
                    .put(5, SWITCHINGRESS_UNITCAST_RMAC)
                    .put(6, SWITCHINGRESS_INGRESS_PORT_MAPPING)
                    .put(7, SWITCHINGRESS_INGRESS_PORT_MAPPING_PORT_VLAN_TO_BD_MAPPING)
                    .put(8, SWITCHINGRESS_INGRESS_PORT_MAPPING_VLAN_TO_BD_MAPPING)
                    .put(9, SWITCHINGRESS_NEXTHOP)
                    .build();

    private static final Map<Criterion.Type, PiMatchFieldId> CRITERION_MAP =
            new ImmutableMap.Builder<Criterion.Type, PiMatchFieldId>()
                    .put(Criterion.Type.ETH_DST, HDR_ETHERNET_DST_ADDR)
                    .put(Criterion.Type.ETH_SRC, HDR_ETHERNET_SRC_ADDR)
                    .put(Criterion.Type.IPV4_SRC, HDR_IPV4_SRC_ADDR)
                    .put(Criterion.Type.IPV4_DST, HDR_IPV4_FIB_FIB_DST_ADDR)
                    .put(Criterion.Type.VLAN_VID, HDR_VLAN_TAG_VID)
                    .put(Criterion.Type.IN_PORT, IG_MD_PORT)
                    // TODO
                    .build();

    @Override
    public PiAction mapTreatment(TrafficTreatment treatment, PiTableId piTableId)
            throws PiInterpreterException {
        if (treatment.allInstructions().isEmpty()) {
            // No actions means drop.
            return PiAction.builder()
                            .withId(SWITCHINGRESS_NEXTHOP_SET_NEXTHOP_PROPERTIES_DROP)
                            .build();
        }

        Instruction instruction = treatment.allInstructions().get(0);
        switch (instruction.type()) {
            case OUTPUT:
                if (piTableId.equals(SWITCHINGRESS_UNITCAST_IPV4_FIB_RIB)) {
                    return outputPiAction((OutputInstruction) instruction,
                    SWITCHINGRESS_UNICAST_IPV4_FIB_RIB_ROUTE);
                } else  {
                    throw new PiInterpreterException(
                            "Output instruction not supported in table " + piTableId);
                }
            case NOACTION:
                return PiAction.builder().withId(ACT_NOACTION).build();
            default:
                throw new PiInterpreterException(format(
                        "Instruction type '%s' not supported", instruction.type()));
        }
    }

    private PiAction outputPiAction(OutputInstruction outInstruction, PiActionId piActionId)
            throws PiInterpreterException {
        PortNumber port = outInstruction.port();
        if (!port.isLogical()) {
            return PiAction.builder()
                    .withId(piActionId)
                    .withParameter(new PiActionParam(PORT_ID, port.toLong()))
                    .build();
        } else if (port.equals(CONTROLLER)) {
            return PiAction.builder()
            .withId(SWITCHINGRESS_INGRESS_PORT_MAPPING_SET_CPU_PORT_PROPERTIES)
            .build();
        } else {
            throw new PiInterpreterException(format(
                    "Egress on logical port '%s' not supported", port));
        }
    }

    @Override
    public Collection<PiPacketOperation> mapOutboundPacket(OutboundPacket packet)
            throws PiInterpreterException {
        TrafficTreatment treatment = packet.treatment();

        // basic.p4 supports only OUTPUT instructions.
        /*List<OutputInstruction> outInstructions = treatment
                .allInstructions()
                .stream()
                .filter(i -> i.type().equals(OUTPUT))
                .map(i -> (OutputInstruction) i)
                .collect(toList());

        if (treatment.allInstructions().size() != outInstructions.size()) {
            // There are other instructions that are not of type OUTPUT.
            throw new PiInterpreterException("Treatment not supported: " + treatment);
        }

        ImmutableList.Builder<PiPacketOperation> builder = ImmutableList.builder();
        for (OutputInstruction outInst : outInstructions) {
            if (outInst.port().isLogical() && !outInst.port().equals(FLOOD)) {
                throw new PiInterpreterException(format(
                        "Output on logical port '%s' not supported", outInst.port()));
            } else if (outInst.port().equals(FLOOD)) {
                // Since basic.p4 does not support flooding, we create a packet
                // operation for each switch port.
                final DeviceService deviceService = handler().get(DeviceService.class);
                for (Port port : deviceService.getPorts(packet.sendThrough())) {
                    builder.add(createPiPacketOperation(packet.data(), port.number().toLong()));
                }
            } else {
                builder.add(createPiPacketOperation(packet.data(), outInst.port().toLong()));
            }
        }*/
        ImmutableList.Builder<PiPacketOperation> builder = ImmutableList.builder();
        return builder.build();
    }

    @Override
    public InboundPacket mapInboundPacket(PiPacketOperation packetIn, DeviceId deviceId)
            throws PiInterpreterException {
        // Assuming that the packet is ethernet, which is fine since basic.p4
        // can deparse only ethernet packets.
        /*Ethernet ethPkt;
        try {
            ethPkt = Ethernet.deserializer().deserialize(packetIn.data().asArray(), 0,
                    packetIn.data().size());
        } catch (DeserializationException dex) {
            throw new PiInterpreterException(dex.getMessage());
        }

        // Returns the ingress port packet metadata.
        Optional<PiPacketMetadata> packetMetadata = packetIn.metadatas()
                .stream().filter(m -> m.id().equals(INGRESS_PORT))
                .findFirst();

        if (packetMetadata.isPresent()) {
            ImmutableByteSequence portByteSequence = packetMetadata.get().value();
            short s = portByteSequence.asReadOnlyBuffer().getShort();
            ConnectPoint receivedFrom = new ConnectPoint(deviceId, PortNumber.portNumber(s));
            ByteBuffer rawData = ByteBuffer.wrap(packetIn.data().asArray());
            return new DefaultInboundPacket(receivedFrom, ethPkt, rawData);
        } else {
            throw new PiInterpreterException(format(
                    "Missing metadata '%s' in packet-in received from '%s': %s",
                    INGRESS_PORT, deviceId, packetIn));
        }*/
        InboundPacket inpkt = null;
        return inpkt;
    }
/*
    private PiPacketOperation createPiPacketOperation(ByteBuffer data, long portNumber)
            throws PiInterpreterException {
        PiPacketMetadata metadata = createPacketMetadata(portNumber);
        return PiPacketOperation.builder()
                .withType(PACKET_OUT)
                .withData(copyFrom(data))
                .withMetadatas(ImmutableList.of(metadata))
                .build();
    }

    private PiPacketMetadata createPacketMetadata(long portNumber) throws PiInterpreterException {
        try {
            return PiPacketMetadata.builder()
                    .withId(EGRESS_PORT)
                    .withValue(copyFrom(portNumber).fit(PORT_BITWIDTH))
                    .build();
        } catch (ImmutableByteSequence.ByteSequenceTrimException e) {
            throw new PiInterpreterException(format(
                    "Port number %d too big, %s", portNumber, e.getMessage()));
        }
    }*/

    @Override
    public Optional<PiMatchFieldId> mapCriterionType(Criterion.Type type) {
        return Optional.ofNullable(CRITERION_MAP.get(type));
    }

    @Override
    public Optional<PiTableId> mapFlowRuleTableId(int flowRuleTableId) {
        return Optional.ofNullable(TABLE_MAP.get(flowRuleTableId));
    }
}
