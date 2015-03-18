/*
 * Copyright (C) 2015 Iasc CHEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.iasc.microduino.blueledpad.ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String SOFT_SERIAL_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String MD_RX_TX = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String ETOH_RX_TX = "0000fff1-0000-1000-8000-00805f9b34fb";


    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        // Sample Services.
        attributes.put(SOFT_SERIAL_SERVICE, "Microduino BLE Serial");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");

        // Sample Characteristics.
        attributes.put(MD_RX_TX, "RX/TX data");
        attributes.put(ETOH_RX_TX, "RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
