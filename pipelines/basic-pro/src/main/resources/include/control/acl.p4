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

#include "../define.p4"
#include "../header.p4"

control Acl (inout parsed_headers_t hdr,
             inout fabric_metadata_t fabric_metadata,
             inout standard_metadata_t standard_metadata) {

    /*
     * ACL Table.
     */

    direct_counter(CounterType.packets_and_bytes) acl_counter;

    counter(132, CounterType.packets) acl_port_counter;

    action punt_to_cpu() {
        standard_metadata.egress_spec = CPU_PORT;
        acl_counter.count();
    }

    action output(port_num_t port_num) {
        standard_metadata.egress_spec = port_num;
        acl_counter.count();
    }

    action broadcast(){
        standard_metadata.mcast_grp = 1;
    }

    action drop() {
        mark_to_drop();
        acl_counter.count();
    }

    @brief("Brief ACL Table")
    @description("Description ACL Table")
    table acl {
        key = {
            standard_metadata.ingress_port : ternary;
            hdr.ethernet.src_addr          : ternary;
            hdr.ethernet.dst_addr          : ternary;
            hdr.ethernet.eth_type          : ternary;
            hdr.ipv4.src_addr              : ternary;
            hdr.ipv4.dst_addr              : ternary;
            hdr.ipv4.protocol              : ternary;
        }

        actions = {
            output;
            punt_to_cpu;
            drop;
        }

        const default_action = drop();
        size = ACL_TABLE_SIZE;
        counters = acl_counter;
    }

    apply {
        acl_port_counter.count((bit<32>) standard_metadata.ingress_port);
        acl.apply();
    }
}
