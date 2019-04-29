/* Copyright 2019 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.messaging.info.event;

import org.openkilda.messaging.info.InfoData;
import org.openkilda.model.SwitchId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IslOneWayLatency extends InfoData {
    private static final long serialVersionUID = 1L;

    @JsonProperty("latency_ns")
    private long latency;

    @JsonProperty("src_switch_id")
    private SwitchId srcSwitchId;

    @JsonProperty("src_port_no")
    private int srcPortNo;

    @JsonProperty("dst_switch_id")
    private SwitchId dstSwitchId;

    @JsonProperty("dst_port_no")
    private int dstPortNo;

    @JsonProperty("packet_id")
    private Long packetId;

    @JsonProperty("dst_switch_supports_round_trip_latency")
    private boolean dstSwitchSupportsRoundTripLatency;

    @JsonCreator
    public IslOneWayLatency(@JsonProperty("src_switch_id") SwitchId srcSwitchId,
                            @JsonProperty("src_port_no") int srcPortNo,
                            @JsonProperty("dst_switch_id") SwitchId dstSwitchId,
                            @JsonProperty("dst_port_no") int dstPortNo,
                            @JsonProperty("latency_ns") long latency,
                            @JsonProperty("packet_id") Long packetId,
                            @JsonProperty("dst_switch_supports_round_trip_latency")
                                        boolean dstSwitchSupportsRoundTripLatency) {
        this.srcSwitchId = srcSwitchId;
        this.srcPortNo = srcPortNo;
        this.dstSwitchId = dstSwitchId;
        this.dstPortNo = dstPortNo;
        this.latency = latency;
        this.packetId = packetId;
        this.dstSwitchSupportsRoundTripLatency = dstSwitchSupportsRoundTripLatency;
    }
}
