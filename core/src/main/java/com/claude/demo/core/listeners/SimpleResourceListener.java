/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.claude.demo.core.listeners;

import java.util.List;

import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

/**
 * A service to demonstrate how changes in the resource tree
 * can be listened for. 
 * Please note, that apart from EventHandler services,
 * the immediate flag should not be set on a service.
 */
@Component(service = ResourceChangeListener.class,
           immediate = true
)
@Slf4j
@ServiceDescription("Demo to listen on changes in the resource tree")
public class SimpleResourceListener implements ResourceChangeListener {

    @Override
    public void onChange(List<ResourceChange> changes) {
        changes.forEach(change -> {
            log.debug("Resource event: {} at: {} isExternal", change.getType(), change.getPath(), change.isExternal());
        });
        
    }
}

