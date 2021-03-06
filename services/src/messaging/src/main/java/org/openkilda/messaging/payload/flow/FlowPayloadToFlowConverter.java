/* Copyright 2017 Telstra Open Source
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

package org.openkilda.messaging.payload.flow;

import org.openkilda.messaging.model.FlowDto;

/**
 * Northbound utility methods.
 */
public final class FlowPayloadToFlowConverter {
    /**
     * Builds {@link FlowDto} instance by {@link FlowPayload} instance.
     *
     * @param flowPayload {@link FlowPayload} instance
     * @return {@link FlowDto} instance
     */
    public static FlowDto buildFlowByFlowPayload(FlowPayload flowPayload) {
        return new FlowDto(
                flowPayload.getId(),
                flowPayload.getMaximumBandwidth(),
                flowPayload.isIgnoreBandwidth(),
                flowPayload.getDescription(),
                flowPayload.getSource().getSwitchDpId(),
                flowPayload.getSource().getPortId(),
                flowPayload.getSource().getVlanId(),
                flowPayload.getDestination().getSwitchDpId(),
                flowPayload.getDestination().getPortId(),
                flowPayload.getDestination().getVlanId(),
                flowPayload.isPinned());
    }

    private FlowPayloadToFlowConverter() {
    }
}
