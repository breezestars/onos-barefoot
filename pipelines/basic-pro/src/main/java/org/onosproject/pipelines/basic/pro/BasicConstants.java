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

import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;

/**
 * Constants for basic pipeline.
 */
public final class BasicConstants {

    // hide default constructor
    private BasicConstants() {
    }

    // Header field IDs . (Defined with star)
    public static final PiMatchFieldId HDR_ETHERNET_SRC_ADDR =
            PiMatchFieldId.of("hdr.ethernet.src_addr");
    public static final PiMatchFieldId HDR_ETHERNET_DST_ADDR =
            PiMatchFieldId.of("hdr.ethernet.dst_addr");
    public static final PiMatchFieldId HDR_VLAN_TAG_VALID =
            PiMatchFieldId.of("hdr.vlan_tag[0].$valid$");
    public static final PiMatchFieldId HDR_VLAN_TAG_VID =
            PiMatchFieldId.of("hdr.vlan_tag[0].vid");
    public static final PiMatchFieldId HDR_FLAGS_IPV4_CHECKSUM_ERR =
            PiMatchFieldId.of("flags.ipv4_checksum_err");
    public static final PiMatchFieldId HDR_IPV4_VERSION =
            PiMatchFieldId.of("hdr.ipv4.version");
    public static final PiMatchFieldId HDR_IPV4_IHL =
            PiMatchFieldId.of("hdr.ipv4.ihl");
    public static final PiMatchFieldId HDR_IPV4_FLAGS =
            PiMatchFieldId.of("hdr.ipv4.flags");
    public static final PiMatchFieldId HDR_IPV4_FLAG_OFFSET =
            PiMatchFieldId.of("hdr.ipv4.frag_offset");
    public static final PiMatchFieldId HDR_IPV4_TTL =
            PiMatchFieldId.of("hdr.ipv4.ttl");
    public static final PiMatchFieldId HDR_IPV4_SRC_ADDR =
            PiMatchFieldId.of("hdr.ipv4.src_addr");
    public static final PiMatchFieldId HDR_TCP_VALID =
            PiMatchFieldId.of("hdr.tcp.$valid$");
    public static final PiMatchFieldId HDR_UDP_VALID =
            PiMatchFieldId.of("hdr.udp.$valid$");
    public static final PiMatchFieldId HDR_IGMP_VALID =
            PiMatchFieldId.of("hdr.igmp.$valid$");
    public static final PiMatchFieldId HDR_ICMP_VALID =
            PiMatchFieldId.of("hdr.icmp.$valid$");
    public static final PiMatchFieldId HDR_ARP_VALID =
            PiMatchFieldId.of("hdr.arp.$valid$");
    public static final PiMatchFieldId HDR_IPV4_FIB_FIB_DST_ADDR =
            PiMatchFieldId.of("dst_addr");
    public static final PiMatchFieldId IPV4_FIB_FIB_LPM_VRF =
            PiMatchFieldId.of("vrf");
    public static final PiMatchFieldId IG_MD_RMAC_GROUP =
            PiMatchFieldId.of("ig_md.rmac_group");
    public static final PiMatchFieldId IG_MD_PORT =
            PiMatchFieldId.of("ig_md.port");
    public static final PiMatchFieldId IG_MD_PORT_LAG_INDEX =
            PiMatchFieldId.of("ig_md.port_lag_index");
    public static final PiMatchFieldId IG_MD_NEXTHOP =
            PiMatchFieldId.of("ig_md.nexthop");
    public static final PiMatchFieldId LKP_MAC_DST_ADDR =
            PiMatchFieldId.of("lkp.mac_dst_addr");
    public static final PiMatchFieldId HDR_PACKET_OUT_VALID =
            PiMatchFieldId.of("hdr.packet_out.$valid$");
    // Table IDs .(Defined.)
    public static final PiTableId SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_ETHERNET =
            PiTableId.of("SwitchIngress.pkt_validation.validate_ethernet");
    public static final PiTableId SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_IPV4 =
            PiTableId.of("SwitchIngress.pkt_validation.validate_ipv4");
    public static final PiTableId SWITCHINGRESS_PACKET_VALIDATION_VALIDATE_OTHER =
            PiTableId.of("SwitchIngress.pkt_validation.validate_other");
    public static final PiTableId SWITCHINGRESS_UNITCAST_IPV4_FIB_RIB =
            PiTableId.of("SwitchIngress.unicast.ipv4_fib.rib");
    public static final PiTableId SWITCHINGRESS_UNITCAST_IPV4_FIB_FIB_LPM =
            PiTableId.of("SwitchIngress.unicast.ipv4_fib.fib_lpm");
    public static final PiTableId SWITCHINGRESS_UNITCAST_RMAC =
            PiTableId.of("SwitchIngress.unicast.rmac");
    public static final PiTableId SWITCHINGRESS_INGRESS_PORT_MAPPING =
            PiTableId.of("SwitchIngress.ingress_port_mapping.port_mapping");
    public static final PiTableId SWITCHINGRESS_INGRESS_PORT_MAPPING_PORT_VLAN_TO_BD_MAPPING =
            PiTableId.of("SwitchIngress.ingress_port_mapping.port_vlan_to_bd_mapping");
    public static final PiTableId SWITCHINGRESS_INGRESS_PORT_MAPPING_VLAN_TO_BD_MAPPING =
            PiTableId.of("SwitchIngress.ingress_port_mapping.vlan_to_bd_mapping");
    public static final PiTableId SWITCHINGRESS_NEXTHOP =
            PiTableId.of("SwitchIngress.nexthop.nexthop");
    // Action IDs
    public static final PiActionId ACT_NOACTION =
            PiActionId.of("NoAction");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_INIT_L4_LKP_PORTS =
            PiActionId.of("SwitchIngress.pkt_validation.init_l4_lkp_ports");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_MALFORMED_NON_IP_PKT =
            PiActionId.of("SwitchIngress.pkt_validation.malformed_non_ip_pkt");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_UNICAST_PKT_UNTAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_unicast_pkt_untagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_MULTICAST_PKT_UNTAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_multicast_pkt_untagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_BROADCAST_PKT_UNTAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_broadcast_pkt_untagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_UNICAST_PKT_TAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_unicast_pkt_tagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_MULTICAST_PKT_TAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_multicast_pkt_tagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_BROADCAST_PKT_TAGGED =
            PiActionId.of("SwitchIngress.pkt_validation.valid_broadcast_pkt_tagged");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_MALFORMED_IPV4_PKT =
            PiActionId.of("SwitchIngress.pkt_validation.malformed_ipv4_pkt");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_VALID_IPV4_PKT =
            PiActionId.of("SwitchIngress.pkt_validation.valid_ipv4_pkt");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_SET_TCP_PORTS =
            PiActionId.of("SwitchIngress.pkt_validation.set_tcp_ports");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_SET_UDP_PORTS =
            PiActionId.of("SwitchIngress.pkt_validation.set_udp_ports");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_SET_ICMP_TYPE =
            PiActionId.of("SwitchIngress.pkt_validation.set_icmp_type");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_SET_IGMP_TYPE =
            PiActionId.of("SwitchIngress.pkt_validation.set_igmp_type");
    public static final PiActionId SWITCHINGRESS_PKT_VALIDATION_SET_ARP_OPCODE =
            PiActionId.of("SwitchIngress.pkt_validation.set_arp_opcode");
    public static final PiActionId SWITCHINGRESS_UNICAST_IPV4_FIB_RIB_ROUTE =
            PiActionId.of("SwitchIngress.unicast.ipv4_fib.rib_route");
    public static final PiActionId SWITCHINGRESS_UNICAST_IPV4_FIB_FIB_FWD =
            PiActionId.of("SwitchIngress.unicast.ipv4_fib.fib_fwd");
    public static final PiActionId SWITCHINGRESS_UNICAST_IPV4_FIB_FIB_MISS =
            PiActionId.of("SwitchIngress.unicast.ipv4_fib.fib_miss");
    public static final PiActionId SWITCHINGRESS_UNICAST_IPV4_FIB_FIB_MYIP =
            PiActionId.of("SwitchIngress.unicast.ipv4_fib.fib_myip");
    public static final PiActionId SWITCHINGRESS_UNICAST_RMAC_HIT =
            PiActionId.of("SwitchIngress.unicast.rmac_hit");
    public static final PiActionId SWITCHINGRESS_UNICAST_RMAC_MISS =
            PiActionId.of("SwitchIngress.unicast.rmac_miss");
    public static final PiActionId SWITCHINGRESS_INGRESS_PORT_MAPPING_SET_CPU_PORT_PROPERTIES =
            PiActionId.of("SwitchIngress.ingress_port_mapping.set_cpu_port_properties");
    public static final PiActionId SWITCHINGRESS_INGRESS_PORT_MAPPING_SET_PORT_PROPERTIES =
            PiActionId.of("SwitchIngress.ingress_port_mapping.set_port_properties");
    public static final PiActionId SWITCHINGRESS_PORT_VLAN_MISS =
            PiActionId.of("port_vlan_miss");
    public static final PiActionId SWITCHINGRESS_INGRESS_PORT_MAPPING_SET_BD_PROPERTIES =
            PiActionId.of("SwitchIngress.ingress_port_mapping.set_bd_properties");
    public static final PiActionId SWITCHINGRESS_NEXTHOP_SET_NEXTHOP_PROPERTIES =
            PiActionId.of("SwitchIngress.nexthop.set_nexthop_properties");
    public static final PiActionId SWITCHINGRESS_NEXTHOP_SET_NEXTHOP_PROPERTIES_POST_ROUTED_FLOOD =
            PiActionId.of("SwitchIngress.nexthop.set_nexthop_properties_post_routed_flood");
    public static final PiActionId SWITCHINGRESS_NEXTHOP_SET_NEXTHOP_PROPERTIES_GLEAN =
            PiActionId.of("SwitchIngress.nexthop.set_nexthop_properties_glean");
    public static final PiActionId SWITCHINGRESS_NEXTHOP_SET_NEXTHOP_PROPERTIES_DROP =
            PiActionId.of("SwitchIngress.nexthop.set_nexthop_properties_drop");
    public static final PiActionId SWITCHINGRESS_NEXTHOP_SET_TUNNEL_PROPERTIES =
            PiActionId.of("SwitchIngress.nexthop.set_tunnel_properties");

    // Action Param IDs
    public static final PiActionParamId REASON = PiActionParamId.of("reason");
    public static final PiActionParamId IP_FRAG = PiActionParamId.of("ip_frag");
    public static final PiActionParamId NEXTHOP_INDEX = PiActionParamId.of("nexthop_index");
    public static final PiActionParamId PORT_LAG_INDEX = PiActionParamId.of("port_lag_index");
    public static final PiActionParamId PORT_LAG_LABEL = PiActionParamId.of("port_lag_label");
    public static final PiActionParamId EXCLUSION_ID = PiActionParamId.of("exclusion_id");
    public static final PiActionParamId TRUST_MODE = PiActionParamId.of("trust_mode");
    public static final PiActionParamId LEARNING_MODE = PiActionParamId.of("learning_mode");
    public static final PiActionParamId QOS_GROUP = PiActionParamId.of("qos_group");
    public static final PiActionParamId COLOR = PiActionParamId.of("color");
    public static final PiActionParamId TC = PiActionParamId.of("tc");
    public static final PiActionParamId MAC_PKT_CLASS = PiActionParamId.of("mac_pkt_class");
    public static final PiActionParamId BD = PiActionParamId.of("bd");
    public static final PiActionParamId VRF = PiActionParamId.of("vrf");
    public static final PiActionParamId BD_LABEL = PiActionParamId.of("bd_label");
    public static final PiActionParamId RID = PiActionParamId.of("rid");
    public static final PiActionParamId STP_GROUP = PiActionParamId.of("stp_group");
    public static final PiActionParamId IPV4_UNICAST_ENABLE = PiActionParamId.of("ipv4_unicast_enable");
    public static final PiActionParamId IPV4_MULTICAST_ENABLE = PiActionParamId.of("ipv4_multicast_enable");
    public static final PiActionParamId IGMP_SNOOPING_ENABLE = PiActionParamId.of("igmp_snooping_enable");
    public static final PiActionParamId IPV6_UNICAST_ENABLE = PiActionParamId.of("ipv6_unicast_enable");
    public static final PiActionParamId IPV6_MULTICAST_ENABLE = PiActionParamId.of("ipv6_multicast_enable");
    public static final PiActionParamId MLD_SNOOPING_ENABLE = PiActionParamId.of("mld_snooping_enable");
    public static final PiActionParamId MRPF_GROUP = PiActionParamId.of("mrpf_group");
    public static final PiActionParamId RMAC_GROUP = PiActionParamId.of("rmac_group");
    public static final PiActionParamId MGID = PiActionParamId.of("mgid");
    public static final PiActionParamId TUNNEL_INDEX = PiActionParamId.of("tunnel_index");
    public static final PiActionParamId PORT_ID = PiActionParamId.of("port_id");
}
