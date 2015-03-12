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

package me.iasc.microduino.bluevoice.db;

import android.os.Parcel;
import android.os.Parcelable;

public class VoiceCmdModel implements Parcelable {
    private static String SEPARATOR = "|x0";

    private int id;

    private String code;
    private String voice;
    private String name;


    public VoiceCmdModel() {
        super();
    }

    public VoiceCmdModel(Parcel in) {
        super();
        this.id = in.readInt();
        this.code = in.readString();
        this.voice = in.readString();
        this.name = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code.toLowerCase();
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{a0");
        sb.append(getVoice()).append(SEPARATOR)
                .append(getCode()).append("}");
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getId());
        parcel.writeString(getCode());
        parcel.writeString(getVoice());
        parcel.writeString(getName());
    }

    public static final Creator<VoiceCmdModel> CREATOR = new Creator<VoiceCmdModel>() {
        public VoiceCmdModel createFromParcel(Parcel in) {
            return new VoiceCmdModel(in);
        }

        public VoiceCmdModel[] newArray(int size) {
            return new VoiceCmdModel[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        VoiceCmdModel other = (VoiceCmdModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
