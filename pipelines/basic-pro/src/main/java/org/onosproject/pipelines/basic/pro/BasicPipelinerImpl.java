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

import org.onosproject.net.DeviceId;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.behaviour.NextGroup;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.behaviour.PipelinerContext;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.routeservice.ResolvedRoute;
import org.onosproject.routeservice.RouteService;
import org.onosproject.net.flowobjective.FilteringObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.slf4j.Logger;

import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiMatchFieldId;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.onosproject.net.flow.instructions.Instructions.OutputInstruction;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Pipeliner implementation for basic.p4 that maps all forwarding objectives to
 * table0. All other types of objectives are not supported.
 */
public class BasicPipelinerImpl extends AbstractHandlerBehaviour implements Pipeliner {

    private final Logger log = getLogger(getClass());

    private FlowRuleService flowRuleService;
    private DeviceId deviceId;
    private static final String APP_NAME = "org.onosproject.p4basedrouting.fibinstaller";

    protected HostService hostService;
    protected RouteService routeService;

    // Table id definition
    private PiTableId fibTableId = BasicConstants.SWITCHINGRESS_UNITCAST_IPV4_FIB_FWD;
    private PiTableId ribTableId = BasicConstants.SWITCHINGRESS_UNITCAST_IPV4_FIB_RIB;
    private PiTableId nexthopTableId = BasicConstants.SWITCHINGRESS_NEXTHOP;
    private PiTableId rewriteTableId = BasicConstants.SWITCHINGRESS_REWRITE_SMAC_REWRITE_BY_PORTID;

    // Match field id definition
    private PiMatchFieldId matchDstIp = BasicConstants.DST_IP_ADDR;
    private PiMatchFieldId matchEgressPort = BasicConstants.EGRESS_PORT;
    private PiMatchFieldId matchNextHop = BasicConstants.SWITCHINGRESS_NEXTHOP_INDEX;

    // Action id definition
    private PiActionId rewriteSMacActionId = BasicConstants.SWITCHEGRESS_REWRITE_REWRITE_SMAC;
    private PiActionId fibfwdActionId = BasicConstants.SWITCHINGRESS_UNICAST_IPV4_FIB_FIB_FWD;
    private PiActionId ribRouteActionId = BasicConstants.SWITCHINGRESS_UNICAST_IPV4_FIB_RIB_ROUTE;
    private PiActionId ribMissActionId = BasicConstants.SWITCHINGRESS_UNICAST_IPV4_FIB_RIB_MISS;
    private PiActionId nextHopActionId = BasicConstants.SWITCHINGRESS_NEXTHOP_SET_NEXTHOP;

    // Action parameter id definition
    private PiActionParamId portParamId = BasicConstants.PORT_ID;
    private PiActionParamId smacParamId = BasicConstants.SMAC;
    private PiActionParamId nexthopParamId = BasicConstants.NEXTHOP_INDEX;
    private PiActionParamId dstMacParamId = BasicConstants.ETH_DST_ADDR;

    // Fake mac for rmac entry matching
    private MacAddress portFakeMac = MacAddress.valueOf("ab:cd:ef:ab:cd:ef");
    // Global counter for nexthop_index increment.
    private int nexthopIndexCounter = 1;

    @Override
    public void init(DeviceId deviceId, PipelinerContext context) {
        this.deviceId = deviceId;
        this.flowRuleService = context.directory().get(FlowRuleService.class);
        this.routeService = context.directory().get(RouteService.class);
        this.hostService = context.directory().get(HostService.class);
    }

    @Override
    public void filter(FilteringObjective obj) {
        obj.context().ifPresent(c -> c.onError(obj, ObjectiveError.UNSUPPORTED));
    }

    @Override
    public void forward(ForwardingObjective obj) {

        if (obj.appId().name().equals(APP_NAME)) {
            if (obj.treatment() == null) {
                obj.context().ifPresent(c -> c.onError(obj, ObjectiveError.UNSUPPORTED));
            }

            IPCriterion ipDst = (IPCriterion) obj.selector().getCriterion(Criterion.Type.IPV4_DST);
            Collection<ResolvedRoute> crr = routeService.getAllResolvedRoutes(ipDst.ip());
            Instruction instruction = obj.treatment().allInstructions().get(0);
            PortNumber port = ((OutputInstruction) instruction).port();

            PiCriterion dstIpMatchLpm = PiCriterion.builder()
                    .matchLpm(matchDstIp, ipDst.ip().address().toOctets(), ipDst.ip().prefixLength())
                    .build();

            // Exact match on egress_port
            PiCriterion egressPortMatch = PiCriterion.builder()
                    .matchExact(matchEgressPort, port.toLong())
                    .build();

            Set<FlowRule.Builder> sfb = new HashSet<>();
            for (ResolvedRoute rr:crr) {
                MacAddress mac = rr.nextHopMac();
                IpAddress ipa = rr.nextHop();
                IpPrefix ipx = rr.prefix();

                // Exact match on nexthop
                PiCriterion nexthopMatch = PiCriterion.builder()
                                                    .matchExact(matchNextHop, nexthopIndexCounter)
                                                    .build();

                PiActionParam portParam = new PiActionParam(portParamId, port.toLong());
                PiActionParam smacParam = new PiActionParam(smacParamId, portFakeMac.toBytes());
                PiActionParam nexthopParam = new PiActionParam(nexthopParamId, nexthopIndexCounter);
                PiActionParam dstMacParam = new PiActionParam(dstMacParamId, mac.toBytes());

                PiAction setNextHopAction = PiAction.builder()
                                                    .withId(ribRouteActionId)
                                                    .withParameter(nexthopParam)
                                                    .build();

                PiAction outputAndModAction = PiAction.builder()
                                                    .withId(nextHopActionId)
                                                    .withParameter(portParam)
                                                    .withParameter(dstMacParam)
                                                    .build();

                PiAction modSrcMacAction = PiAction.builder()
                                                    .withId(rewriteSMacActionId)
                                                    .withParameter(smacParam)
                                                    .build();
                Set<Host> hostSet = hostService.getHostsByIp(ipa);
                hostSet.forEach(host -> {
                    DeviceId deviceId = host.location().deviceId();
                    // RIB Entry
                    sfb.add(genFlowRule(deviceId, ribTableId, dstIpMatchLpm, setNextHopAction, obj.appId()));

                    // NextHop Entry
                    sfb.add(genFlowRule(deviceId, nexthopTableId, nexthopMatch, outputAndModAction, obj.appId()));

                    // Rewrite Entry
                    sfb.add(genFlowRule(deviceId, rewriteTableId, egressPortMatch, modSrcMacAction, obj.appId()));
                });
                addIndexCounter();
            }

            switch (obj.op()) {
                case ADD:
                    sfb.forEach(f -> flowRuleService.applyFlowRules(f.build()));
                    break;
                case REMOVE:
                    sfb.forEach(f -> flowRuleService.removeFlowRules(f.build()));
                    break;
                default:
                    log.warn("Unknown operation {}", obj.op());
            }
            obj.context().ifPresent(c -> c.onSuccess(obj));
        }
    }

    private void addIndexCounter() {
        nexthopIndexCounter++;
    }

    private FlowRule.Builder genFlowRule(DeviceId dvid,
                                        PiTableId ptb,
                                        PiCriterion pcn,
                                        PiAction pan,
                                        ApplicationId appid) {

        return DefaultFlowRule.builder()
            .forTable(ptb)
            .forDevice(dvid)
            .withSelector(DefaultTrafficSelector.builder().matchPi(pcn).build())
            .withTreatment(DefaultTrafficTreatment.builder().piTableAction(pan).build())
            .fromApp(appid)
            .withPriority(0)
            .makePermanent();
    }

    @Override
    public void next(NextObjective obj) {
        obj.context().ifPresent(c -> c.onError(obj, ObjectiveError.UNSUPPORTED));
    }

    @Override
    public List<String> getNextMappings(NextGroup nextGroup) {
        // We do not use nextObjectives or groups.
        return null;
    }
}