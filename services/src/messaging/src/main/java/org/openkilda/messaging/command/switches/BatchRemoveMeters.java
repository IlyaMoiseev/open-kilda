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

package org.openkilda.messaging.command.switches;

import org.openkilda.messaging.command.CommandData;
import org.openkilda.model.SwitchId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class BatchRemoveMeters extends CommandData {

    @JsonProperty("switch_id")
    SwitchId switchId;

    @JsonProperty("meters_id")
    List<Long> metersId;

    @JsonCreator
    public BatchRemoveMeters(@JsonProperty("switch_id") SwitchId switchId,
                             @JsonProperty("meters_id") List<Long> metersId) {
        this.switchId = switchId;
        this.metersId = metersId;
    }
}