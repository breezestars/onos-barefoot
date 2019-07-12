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

#include <core.p4>
#include <v1model.p4>

#include "../header.p4"

control Filtering (inout parsed_headers_t hdr,
                   inout fabric_metadata_t fabric_metadata,
                   inout standard_metadata_t standard_metadata) {

    /*
     * Ingress Port VLAN Table.
     *
     * Filter packets based on ingress port and VLAN tag.
     */
    direct_counter(CounterType.packets_and_bytes) ingress_port_vlan_counter;

    action deny() {
        // Packet from unconfigured port. Skip forwarding and next block.
        // Do ACL table in case we want to punt to cpu.
        fabric_metadata.skip_forwarding = _TRUE;
        fabric_metadata.skip_next = _TRUE;
        ingress_port_vlan_counter.count();
    }

    action permit() {
        // Allow packet as is.
        ingress_port_vlan_counter.count();
    }

    action permit_with_internal_vlan(vlan_id_t vlan_id) {
        fabric_metadata.vlan_id = vlan_id;
        permit();
    }

    // FIXME: remove the use of ternary match on valid inner VLAN.
    // Use multi-table approach to remove ternary matching
    table ingress_port_vlan {
        key = {
            standard_metadata.ingress_port : exact @name("ig_port");
            hdr.vlan_tag.isValid()         : exact @name("vlan_is_valid");
            hdr.vlan_tag.vlan_id           : ternary @name("vlan_id");
            hdr.inner_vlan_tag.vlan_id     : ternary @name("inner_vlan_id");
        }
        actions = {
            deny();
            permit();
            permit_with_internal_vlan();
        }
        const default_action = deny();
        counters = ingress_port_vlan_counter;
        size = PORT_VLAN_TABLE_SIZE;
    }

    /*
     * Forwarding Classifier.
     *
     * Set which type of forwarding behavior to execute in the next control block.
     * There are six types of tables in Forwarding control block:
     * - Bridging: default forwarding type
     * - MPLS: destination mac address is the router mac and ethernet type is
     *   MPLS(0x8847)
     * - IP Multicast: destination mac address is multicast address and ethernet
     *   type is IP(0x0800 or 0x86dd)
     * - IP Unicast: destination mac address is router mac and ethernet type is
     *   IP(0x0800 or 0x86dd)
     */
    direct_counter(CounterType.packets_and_bytes) fwd_classifier_counter;

    action set_forwarding_type(fwd_type_t fwd_type) {
        fabric_metadata.fwd_type = fwd_type;
        fwd_classifier_counter.count();
    }

    table fwd_classifier {
        key = {
            standard_metadata.ingress_port : exact @name("ig_port");
            hdr.ethernet.dst_addr          : ternary @name("eth_dst");
            fabric_metadata.is_ipv4        : exact @name("is_ipv4");
            fabric_metadata.is_ipv6        : exact @name("is_ipv6");
            fabric_metadata.is_mpls        : exact @name("is_mpls");
        }
        actions = {
            set_forwarding_type;
        }
        const default_action = set_forwarding_type(FWD_BRIDGING);
        counters = fwd_classifier_counter;
        size = FWD_CLASSIFIER_TABLE_SIZE;
    }

    apply {
        // Initialize lookup metadata. Packets without a VLAN header will be
        // treated as belonging to a default VLAN ID (see parser).
        if (hdr.vlan_tag.isValid()) {
            fabric_metadata.vlan_id = hdr.vlan_tag.vlan_id;
            fabric_metadata.vlan_pri = hdr.vlan_tag.pri;
            fabric_metadata.vlan_cfi = hdr.vlan_tag.cfi;
        }
        #ifdef WITH_DOUBLE_VLAN_TERMINATION
        if (hdr.inner_vlan_tag.isValid()) {
            fabric_metadata.inner_vlan_id = hdr.inner_vlan_tag.vlan_id;
            fabric_metadata.inner_vlan_pri = hdr.inner_vlan_tag.pri;
            fabric_metadata.inner_vlan_cfi = hdr.inner_vlan_tag.cfi;
        }
        #endif // WITH_DOUBLE_VLAN_TERMINATION
        if (!hdr.mpls.isValid()) {
            // Packets with a valid MPLS header will have
            // fabric_metadata.mpls_ttl set to the packet's MPLS ttl value (see
            // parser). In any case, if we are forwarding via MPLS, ttl will be
            // decremented in egress.
            fabric_metadata.mpls_ttl = DEFAULT_MPLS_TTL + 1;
        }

        // Set last_eth_type checking the validity of the L2.5 headers
        if (hdr.mpls.isValid()) {
            fabric_metadata.last_eth_type = ETHERTYPE_MPLS;
        } else {
            if (hdr.vlan_tag.isValid()) {
#if defined(WITH_XCONNECT) || defined(WITH_BNG) || defined(WITH_DOUBLE_VLAN_TERMINATION)
                if(hdr.inner_vlan_tag.isValid()) {
                    fabric_metadata.last_eth_type = hdr.inner_vlan_tag.eth_type;
                } else
#endif //  WITH_XCONNECT || WITH_BNG || WITH_DOUBLE_VLAN_TERMINATION
                    fabric_metadata.last_eth_type = hdr.vlan_tag.eth_type;
            } else {
                fabric_metadata.last_eth_type = hdr.ethernet.eth_type;
            }
        }

        ingress_port_vlan.apply();
        fwd_classifier.apply();
    }
}
