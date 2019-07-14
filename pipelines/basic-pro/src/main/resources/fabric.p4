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

#include "include/size.p4"
#include "include/control/port_counter.p4"
#include "include/control/acl.p4"
#include "include/control/packetio.p4"
#include "include/header.p4"
#include "include/checksum.p4"
#include "include/parser.p4"


control FabricIngress (inout parsed_headers_t hdr,
                       inout fabric_metadata_t fabric_metadata,
                       inout standard_metadata_t standard_metadata) {

    PacketIoIngress() pkt_io_ingress;
    Acl() acl;
    PortCountersControl() port_counters_control;

    apply {
        pkt_io_ingress.apply(hdr, fabric_metadata, standard_metadata);
        acl.apply(hdr, fabric_metadata, standard_metadata);
        port_counters_control.apply(hdr, fabric_metadata, standard_metadata);
    }
}

control FabricEgress (inout parsed_headers_t hdr,
                      inout fabric_metadata_t fabric_metadata,
                      inout standard_metadata_t standard_metadata) {

    PacketIoEgress() pkt_io_egress;

    apply {
        pkt_io_egress.apply(hdr, fabric_metadata, standard_metadata);
    }
}

@pkginfo(name="fwd.p4",version="1")
@pkginfo(organization="Edgecore")
@pkginfo(contact="jimmy_ou@edge-core.com")
@pkginfo(url="www.edge-core.com")
@brief("L2/L3 switch")
@description("L2/L3 switch.\ Built for test profile.")
@my_annotation1("Ttest_my_annotation1") // Not well-known, this will appear in PkgInfo annotations
V1Switch(
    FabricParser(),
    FabricVerifyChecksum(),
    FabricIngress(),
    FabricEgress(),
    FabricComputeChecksum(),
    FabricDeparser()
) main;
