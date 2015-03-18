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

package me.iasc.microduino.blueledpad.db;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class LedMatrixModel implements Parcelable {
    private int id;
    private String name;
    private String matrix;
    private Bitmap preview;

    public LedMatrixModel() {
        super();
    }

    public LedMatrixModel(Parcel in) {
        super();
        this.id = in.readInt();
        this.name = in.readString();
        this.matrix = in.readString();

        // TODO priview image
        // byte[] blob = in.read
        // this.preview = ImgUtility.getPhoto(blob);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getId());
        parcel.writeString(getName());
        parcel.writeString(getMatrix());
        // parcel.writeBlob(ImgUtility.getBytes( this.preview));
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", matrix="
                + matrix;
    }

    public static final Creator<LedMatrixModel> CREATOR = new Creator<LedMatrixModel>() {
        public LedMatrixModel createFromParcel(Parcel in) {
            return new LedMatrixModel(in);
        }

        public LedMatrixModel[] newArray(int size) {
            return new LedMatrixModel[size];
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

        LedMatrixModel other = (LedMatrixModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
