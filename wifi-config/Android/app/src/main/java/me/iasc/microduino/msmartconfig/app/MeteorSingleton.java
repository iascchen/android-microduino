package me.iasc.microduino.msmartconfig.app;

import im.delight.android.ddp.Meteor;

/**
 * Copyright (C) 2015 Iasc CHEN
 * Created on 15/5/24.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MeteorSingleton {
    //public static String WS_SERVER_URL = "ws://192.168.199.240:3000/websocket";
    public static String WS_SERVER_URL = "ws://mcotton-01.chinacloudapp.cn/websocket";

    private static Meteor instance = null;

    public static Meteor getInstance(){
        if(instance == null){
            instance = new Meteor(WS_SERVER_URL);
        }
        return instance;
    }
}
